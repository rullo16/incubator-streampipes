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

package org.gft.processors.waterflowtracking;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.DataProcessorType;
import org.apache.streampipes.model.graph.DataProcessorDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.ProcessingElementBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.helpers.*;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.vocabulary.SO;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class WaterFlowTracking extends StreamPipesDataProcessor {

  private String input_WaterFlow_value;
  private String input_timestamp_value;
  private String input_choice;
  private static int day_precedent = -1, month_precedent = -1;
  private static double daily_consumption = 0.0;
  private static double monthly_consumption = 0.0;
  private static double weekly_consumption = 0.0;
  private static final String ID = "org.gft.processors.waterflowtracking";
  private static final String INPUT_VALUE = "value";
  private static final String TIMESTAMP_VALUE = "timestamp_value";
  private static final String CHOICE = "choice";
  private static final String DAILY_CONSUMPTION = "daily_consumption";
  private static final String WEEKLY_CONSUMPTION = "weekly_consumption";
  private static final String MONTHLY_CONSUMPTION = "monthly_consumption";

  private final DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  List<Double> waterFlowList = new ArrayList<>();
  List<Long> timestampsList = new ArrayList<>();
  List<Double> dailyConsumptionListForMonth = new ArrayList<>();
  List<Double> dailyConsumptionListForWeek = new ArrayList<>();


  @Override
  public DataProcessorDescription declareModel() {
    return ProcessingElementBuilder.create(ID)
            .withAssets(Assets.DOCUMENTATION, Assets.ICON)
            .withLocales(Locales.EN)
            .category(DataProcessorType.AGGREGATE)
            .requiredStream(StreamRequirementsBuilder.create()
                    .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                            Labels.withId(INPUT_VALUE), PropertyScope.NONE)
                    .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                            Labels.withId(TIMESTAMP_VALUE), PropertyScope.NONE)
                    .build())
            .requiredSingleValueSelection(Labels.withId(CHOICE),
                Options.from("No", "Yes"))
            .outputStrategy(OutputStrategies.append(EpProperties.doubleEp(Labels.withId(MONTHLY_CONSUMPTION), "monthly consumption", SO.Number),
                    EpProperties.doubleEp(Labels.withId(DAILY_CONSUMPTION), "daily consumption", SO.Number),
                    EpProperties.doubleEp(Labels.withId(WEEKLY_CONSUMPTION), "weekly consumption", SO.Number)))
            .build();
  }

  @Override
  public void onInvocation(ProcessorParams parameters, SpOutputCollector out, EventProcessorRuntimeContext ctx) throws SpRuntimeException  {
    this.input_WaterFlow_value = parameters.extractor().mappingPropertyValue(INPUT_VALUE);
    this.input_timestamp_value = parameters.extractor().mappingPropertyValue(TIMESTAMP_VALUE);
    this.input_choice  = parameters.extractor().selectedSingleValue(CHOICE, String.class);
  }

  @Override
  public void onEvent(Event event,SpOutputCollector out){
    //recovery power value
    Double water_flow = event.getFieldBySelector(this.input_WaterFlow_value).getAsPrimitive().getAsDouble();
    //recovery timestamp value
    Long timestamp = event.getFieldBySelector(this.input_timestamp_value).getAsPrimitive().getAsLong();
    //recovery date value
    String date = getTheDate(timestamp);

    // Day and Month extraction
    String[] ymd_hms = date.split(" ");
    String[] ymd = ymd_hms[0].split("-");
    int day_current = Integer.parseInt(ymd[2]);
    int month_current = Integer.parseInt(ymd[1]);

    if((day_current != day_precedent || month_current != month_precedent) && day_precedent != -1){

      if(day_current != day_precedent){
        // reset day for computations
        day_precedent = day_current;
        // Add current events for the next computation
        waterFlowList.add(water_flow );
        timestampsList.add(timestamp);
        //perform operations to obtain hourly power from instantaneous powers
        if(this.input_choice.equals("Yes")){
          daily_consumption = waterFlowList.get(waterFlowList.size()-1) - waterFlowList.get(0);
        }else{
          daily_consumption = instantToDailyConsumption(waterFlowList, timestampsList);
        }
        dailyConsumptionListForWeek.add(daily_consumption);
        dailyConsumptionListForMonth.add(daily_consumption);
        // Remove all elements from the Lists
        waterFlowList.clear();
        timestampsList.clear();
        // Add current events for the next computation
        waterFlowList.add(water_flow );
        timestampsList.add(timestamp);
      }

    }else {
      // set the start time for computations
      if (day_precedent == -1){
        month_precedent = month_current;
        day_precedent = day_current;
      }
      // add power to the lists
      waterFlowList.add(water_flow);
      timestampsList.add(timestamp);
    }

    if(getCurrentDay(date).equals("Mon")){
      weekly_consumption = dailyConsumptionsToWeeklyOrMonthlyConsumption(dailyConsumptionListForWeek);
      dailyConsumptionListForWeek.clear();
    }

    if(month_current != month_precedent){
      month_precedent = month_current;
      monthly_consumption = dailyConsumptionsToWeeklyOrMonthlyConsumption(dailyConsumptionListForMonth);
      dailyConsumptionListForMonth.clear();
    }

    event.addField("daily consumption", daily_consumption);
    event.addField("weekly consumption", weekly_consumption);
    event.addField("monthly consumption", monthly_consumption);

    out.collect(event);
  }

  private String getTheDate(Long timestamp) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(timestamp);
    return  date_format.format(cal.getTime());
  }

  private String getCurrentDay(String date){
    String day = null;
    try{
      Date myDate = date_format.parse(date);
      LocalDateTime localDateTime = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      // convert LocalDateTime to date
      Date date_plus = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
      String[] s = date_plus.toString().split(" ");
      day = s[0];
    }catch (ParseException e){
      e.printStackTrace();
    }
    return day;
  }

  private double dailyConsumptionsToWeeklyOrMonthlyConsumption(List<Double> dailyConsumptionList) {
    double sum = 0.0;
    DecimalFormat df = new DecimalFormat("#.#####");
    df.setRoundingMode(RoundingMode.CEILING);
    for (Double value : dailyConsumptionList) sum = sum + value;
    return Double.parseDouble(df.format(sum));
  }

  public double instantToDailyConsumption(List<Double> water_flows, List<Long> timestamps) {
    double sum = 0.0;
    double first_base;
    double second_base;
    long height;
    DecimalFormat df = new DecimalFormat("#.#####");
    df.setRoundingMode(RoundingMode.CEILING);
    //perform Riemann approximations by trapezoids which is an approximation of the area
    // under the curve (which corresponds to the water consumption) formed by the points
    // with coordinate water flows(ordinate) e timestamps(abscissa)
    for(int i = 0; i<water_flows.size()-1; i++){
      first_base = water_flows.get(i);
      second_base = water_flows.get(i+1);
      height = (timestamps.get(i+1) - timestamps.get(i))/1000;
      sum += ((first_base + second_base) / (2*3600)) * height ;
    }
    return Double.parseDouble(df.format(sum));
  }

  @Override
  public void onDetach(){
  }

}