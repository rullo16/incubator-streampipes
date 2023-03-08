/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gft.adapters.backend;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.streampipes.connect.adapter.guess.SchemaGuesser;
import org.apache.streampipes.connect.adapter.model.generic.Protocol;
import org.apache.streampipes.connect.api.IFormat;
import org.apache.streampipes.connect.api.IParser;
import org.apache.streampipes.connect.api.exception.ParseException;
import org.apache.streampipes.model.AdapterType;
import org.apache.streampipes.model.connect.grounding.ProtocolDescription;
import org.apache.streampipes.model.connect.guess.GuessSchema;
import org.apache.streampipes.model.schema.EventSchema;
import org.apache.streampipes.sdk.builder.adapter.ProtocolDescriptionBuilder;
import org.apache.streampipes.sdk.extractor.StaticPropertyExtractor;
import org.apache.streampipes.sdk.helpers.AdapterSourceType;
import org.apache.streampipes.sdk.helpers.Locales;
import org.apache.streampipes.sdk.utils.Assets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackendHttpStreamProtocol extends BackendPullProtocol {

  private static final long interval = 300; //TODO set this in HttpConfig
  Logger logger = LoggerFactory.getLogger(Protocol.class);
  public static final String ID = "org.gft.adapters.backend";
  BackendHttpConfig config;
  public BackendHttpStreamProtocol() {
  }

  public BackendHttpStreamProtocol(IParser parser, IFormat format, BackendHttpConfig config) {
    super(parser, format, interval);
    this.config = config;
  }

  @Override
  public ProtocolDescription declareModel() {
    return ProtocolDescriptionBuilder.create(ID)
            .withAssets(Assets.DOCUMENTATION, Assets.ICON)
            .withLocales(Locales.EN)
            .sourceType(AdapterSourceType.STREAM)
            .category(AdapterType.Generic)
            .requiredTextParameter(BackendHttpUtils.getUsernameLabel())
            .requiredSecret(BackendHttpUtils.getPasswordLabel())
            .requiredTextParameter(BackendHttpUtils.getSignalLabel())
            .requiredTextParameter(BackendHttpUtils.getLowestLabel())
            .requiredTextParameter(BackendHttpUtils.getHighestLabel(), "CurrentDateTime")
            .build();
  }

  @Override
  public Protocol getInstance(ProtocolDescription protocolDescription, IParser parser, IFormat format) {
    StaticPropertyExtractor extractor = StaticPropertyExtractor.from(protocolDescription.getConfig(),  new ArrayList<>());
    BackendHttpConfig config = BackendHttpUtils.getConfig(extractor);
    return new BackendHttpStreamProtocol(parser, format, config);
  }

  @Override
  public GuessSchema getGuessSchema() throws ParseException {
    int n = 2;

    InputStream dataInputStream;
    dataInputStream = getDataFromEndpoint();

    List<byte[]> dataByte = parser.parseNEvents(dataInputStream, n);
    if (dataByte.size() < n) {
      logger.error("Error in BackendHttpStreamProtocol! Required: " + n + " elements but the resource just had: " +
              dataByte.size());

      dataByte.addAll(dataByte);
    }
    EventSchema eventSchema= parser.getEventSchema(dataByte);

    return SchemaGuesser.guessSchema(eventSchema);
  }

  @Override
  public List<Map<String, Object>> getNElements(int n) throws ParseException {
    List<Map<String, Object>> result = new ArrayList<>();

    InputStream dataInputStream;
    dataInputStream = getDataFromEndpoint();

    List<byte[]> dataByte = parser.parseNEvents(dataInputStream, n);

    // Check that result size is n. Currently just an error is logged. Maybe change to an exception
    if (dataByte.size() < n) {
      logger.error("Error in BackendHttpStreamProtocol! User required: " + n + " elements but the resource just had: " +
              dataByte.size());
    }

    for (byte[] b : dataByte) {
      result.add(format.parse(b));
    }

    return result;
  }

  public InputStream getDataFromEndpoint() throws ParseException{
    InputStream result = null;
    String accessToken = login();
    String urlString = getUrl();

    if (!config.getHighestDate().equals("CurrentDateTime") && config.getLowestDate().compareToIgnoreCase(config.getHighestDate()) >= 0) {
      logger.warn("Adapter Stopped: there is not anymore data to retrieve in the time interval!!!");
      logger.warn("Stop Adapter on the User Interface!!!");
      stop();
    }

    try {
      // Set the URL of the API endpoint
      URL url = new URL(urlString);
      // Open a connection to the API endpoint
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Accept", "application/json");
      // Set the token in the HTTP header of the request
      connection.setRequestProperty("Authorization", "Bearer " + accessToken);
      connection.setRequestProperty("Transfer-Encoding", "chunked");
      connection.setRequestProperty("Connection", "keep-alive");
      connection.setDoOutput(true);
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(30000);
      // Send the GET request to the API endpoint
      connection.connect();

      result = connection.getInputStream();
    } catch (Exception e) {
      // Handle any exceptions that occur
      e.printStackTrace();
    }
    return result;
  }


  private String getUrl(){
    String urlString, first_date, second_date, current_time = config.CurrentDateTime();

    if((config.getSecondDate().compareToIgnoreCase(current_time)>0) && config.getHighestDate().equals("CurrentDateTime")){
      first_date = config.precedentCurrentTime(current_time);
      second_date = current_time;
    }else{
      first_date = config.firstDateTime();
      second_date = config.secondDateTime();
    }

    urlString = config.getBaseUrl()+"?page="+config.getPage()+"&length="+config.getLength()+"&filter="+config.getFilter(first_date,second_date)+"&sort="+config.getSort();

    //replace spaces by "%20" to avoid 400 Bad Request
    if(urlString.contains(" "))
      urlString = urlString.replace(" ", "%20");

    return urlString;
  }

  @Override
  public String getId() {
    return ID;
  }

  private String login() throws org.apache.http.ParseException {
    String response, token;

    try {
      Request request = Request.Post(config.getLoginUrl())
              .connectTimeout(1000)
              .socketTimeout(10000)
              .setHeader("Content-Type", "application/x-www-form-urlencoded")
              .setHeader("Accept","application/json")
              .bodyForm(Form.form().add("grant_type", config.getGrantType()).add("client_id", config.getClientId())
                      .add("client_secret", config.getClientSecret()).add("username",config.getUsername())
                      .add("password", config.getPassword()).add("scope",config.getScope()).build())
              .setHeader("connection","keep-alive");

      response = request
              .execute().returnContent().toString();

      if (response == null)
        throw new org.apache.http.ParseException("Could not receive Data from file: " + config.getLoginUrl());

      JsonObject json_object = new Gson().fromJson(response, JsonObject.class);
      // Access the data in the JSON object
      token = json_object.get("access_token").getAsString();

    } catch (Exception e) {
      throw new org.apache.http.ParseException("Error while fetching data from URL: " + config.getLoginUrl());
    }
    return token;
  }

}