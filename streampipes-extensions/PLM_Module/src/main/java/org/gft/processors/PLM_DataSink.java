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

package org.gft.processors;

import org.json.*;
import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.dataformat.SpDataFormatDefinition;
import org.apache.streampipes.dataformat.json.JsonDataFormatDefinition;
import org.apache.streampipes.model.DataSinkType;
import org.apache.streampipes.model.graph.DataSinkDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.DataSinkBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.helpers.EpRequirements;
import org.apache.streampipes.sdk.helpers.Labels;
import org.apache.streampipes.sdk.helpers.Locales;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.wrapper.context.EventSinkRuntimeContext;
import org.apache.streampipes.wrapper.standalone.SinkParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataSink;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class PLM_DataSink extends StreamPipesDataSink {

private String exampleText;

private static final String EXAMPLE_KEY = "example-key";

  private String USERNAME = "username";
  private String PASSWORD = "password";
  private static final String REST_ENDOPOINT_URL = "https://kyklos.jotne.com/EDMtruePLM/api" ;
  private SpDataFormatDefinition dataFormatDefinition;

  @Override
  public DataSinkDescription declareModel(){
          return DataSinkBuilder.create("org.gft.processors.sink")
          .withAssets(Assets.DOCUMENTATION,Assets.ICON)
          .withLocales(Locales.EN)
          .category(DataSinkType.UNCATEGORIZED)
          .requiredTextParameter(Labels.withId(USERNAME))
                  .requiredTextParameter(Labels.withId(PASSWORD))
          .build();
  }

  @Override
  public void onInvocation(SinkParams sinkParams,
                           EventSinkRuntimeContext ctx) throws SpRuntimeException{
    this.dataFormatDefinition = new JsonDataFormatDefinition();
  }

  @Override
  public void onEvent(Event event) {

  }

  @Override
  public void onDetach(){

  }

  private static JSONArray csvToJson(JSONObject csv_data) {
    JSONArray csv_values = csv_data.getJSONArray("values");
    JSONArray json_values = new JSONArray();
    for (int i = 0; i < csv_values.length(); i++){
      String[] val_parts = csv_values.getString(i).split(",");
      String json_string = "{\"timestamp\": " + Double.parseDouble(val_parts[0])+ "," + "\"value\": " +
              Integer.parseInt(val_parts[1]) + "}";
      JSONObject json_object = new JSONObject(json_string);
      json_values.put(json_object);
    }        return json_values;
  }

  private static StringBuilder connectToPLMBackend(String urlString, String token) {
    URL url;
    String line;
    BufferedReader reader;
    StringBuilder response = new StringBuilder();
    HttpURLConnection connection;

    //replace spaces by "%20" to avoid 400 Bad Request
    if(urlString.contains(" "))
      urlString = urlString.replace(" ", "%20");

    try {
      // Set the URL of the API endpoint
      url = new URL(urlString);
      // Open a connection to the API endpoint
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "application/json");
      // Set the token in the HTTP header of the request
      connection.setRequestProperty("Authorization", "Bearer" + token);
      connection.setRequestProperty("transfer-encoding", "chunked");
      connection.setRequestProperty("connection", "keep-alive");
      // Send the GET request to the API endpoint
      connection.connect();
      // Read the response from the API endpoint
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    } catch (Exception e) {
      // Handle any exceptions that occur
      e.printStackTrace();
    }
    return response;
  }


  private static JSONObject dataRequest(JSONObject prop, String token) {
    JSONObject data;
    String urn, urlString, json_object;
    StringBuilder response;

    urn = prop.getJSONArray("props").getJSONObject(0).getString("urn");

    urlString = base_url + "bkd/aggr/" + repository + "/" + model  + "/" + prop.get("id")+ "/" + urn + "/" + token;
    response = connectToPLMBackend(urlString, token);

    // Get a String representation of the StringBuilder
    json_object = response.toString();
    // Convert the String to a JSONArray
    data = new JSONObject(json_object);

    return data;
  }



  private static JSONArray sensorsList(String token) {
    String urlString, json_array;
    StringBuilder response;
    JSONArray sensors;

    // Set the URL of the API endpoint
    urlString = base_url + "bkd/q_search/" + repository + "/" + model + "/" + token + "?case_sens=false&domains=PROPERTY&pattern=*&folder_only=false";
    response = connectToPLMBackend(urlString, token);

    // Get a String representation of the StringBuilder
    json_array = response.toString();
    // Convert the String to a JSONArray
    sensors = new JSONArray(json_array);

    return sensors;
  }


  private static String login() {
    String token = null;
    try {
      // Set the URL of the API endpoint
      URL url = new URL("https://kyklos.jotne.com/EDMtruePLM/api/admin/token?group=sdai-group&pass=!Gen8ric&user=gkyklos");

      // Open a connection to the API endpoint
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      // Send the POST request to the API endpoint
      connection.connect();

      // Read the response from the API endpoint
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }

      System.out.println(response);
      String json_string = String.valueOf(response);
      // Parse the JSON string as a JSON object
      JSONObject json_object = new JSONObject(json_string);
      // Access the data in the JSON object
      token = json_object.getString("token");

    } catch (Exception e) {
      // Handle any exceptions that occur
      e.printStackTrace();
    }
    return token;
  }

}
