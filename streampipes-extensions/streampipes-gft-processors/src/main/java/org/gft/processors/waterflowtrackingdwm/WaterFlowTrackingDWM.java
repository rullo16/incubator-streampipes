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

package org.gft.processors.waterflowtrackingdwm;

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


public class WaterFlowTrackingDWM extends StreamPipesDataProcessor {

  private String input_WaterFlow_value;
  private String input_timestamp_value;
  private String input_choice;
  private int day_precedent = -1, month_precedent = -1;
  private double daily_consumption = 0.0;
  private double monthly_consumption = 0.0;
  private double weekly_consumption = 0.0;
  private static final String ID = "org.gft.processors.waterflowtrackingdwm";
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


  //Define abstract stream requirements such as event properties that must be present
  // in any input stream that is later connected to the element using the StreamPipes UI
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
            .outputStrategy(OutputStrategies.append(EpProperties.doubleEp(Labels.withId(MONTHLY_CONSUMPTION), "monthlyConsumption", SO.NUMBER),
                    EpProperties.doubleEp(Labels.withId(DAILY_CONSUMPTION), "dailyConsumption", SO.NUMBER),
                    EpProperties.doubleEp(Labels.withId(WEEKLY_CONSUMPTION), "weeklyConsumption", SO.NUMBER)))
            .build();
  }

  //Triggered once a pipeline is started. Allow to identify the actual stream that are connected to the pipeline element and the runtime names
  // to build the different selectors ("stream"::"runtime name") to use in onEvent method to retrieve the exact data.
  @Override
  public void onInvocation(ProcessorParams parameters, SpOutputCollector out, EventProcessorRuntimeContext ctx) throws SpRuntimeException  {
    this.input_WaterFlow_value = parameters.extractor().mappingPropertyValue(INPUT_VALUE);
    this.input_timestamp_value = parameters.extractor().mappingPropertyValue(TIMESTAMP_VALUE);
    this.input_choice  = parameters.extractor().selectedSingleValue(CHOICE, String.class);
  }

  // Get fields from an incoming event by providing the corresponding selector (casting to their corresponding target data types).
  // Then use the if conditional statements to define the output value
  @Override
  public void onEvent(Event event,SpOutputCollector out){
    //recovery water flow value
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

    String day = getCurrentDay(date);

    //if true compute first the consumption of the day that has just passed
    if(day_current != this.day_precedent && this.day_precedent != -1){

        // reset day for computations
        this.day_precedent = day_current;
        // Add current events for the next computation
        this.waterFlowList.add(water_flow );
        this.timestampsList.add(timestamp);
        //perform operations to obtain hourly consumption from instantaneous water flows
        if(this.input_choice.equals("Yes")){
          this.daily_consumption = this.waterFlowList.get(this.waterFlowList.size()-1) - this.waterFlowList.get(0);
        }else{
          this.daily_consumption = instantToDailyConsumption(this.waterFlowList, this.timestampsList);
        }

        this.dailyConsumptionListForWeek.add(this.daily_consumption);
        this.dailyConsumptionListForMonth.add(this.daily_consumption);
        // Remove all elements from the Lists
        this.waterFlowList.clear();
        this.timestampsList.clear();
        // Add current events for the next computation
        this.waterFlowList.add(water_flow );
        this.timestampsList.add(timestamp);

        //if the day coincide with monday compute the consumption of the week that has just passed
        if(day.equals("Mon")){
          this.weekly_consumption = dailyConsumptionsToWeeklyOrMonthlyConsumption(dailyConsumptionListForWeek);
          this.dailyConsumptionListForWeek.clear();
        }

        //if true compute consumption of the month that has just passed
        if(month_current != this.month_precedent){
          this.month_precedent = month_current;
          this.monthly_consumption = dailyConsumptionsToWeeklyOrMonthlyConsumption(dailyConsumptionListForMonth);
          this.dailyConsumptionListForMonth.clear();
        }

    }else {
        // set the start time for computations
        if (this.day_precedent == -1){
          this.month_precedent = month_current;
          this.day_precedent = day_current;
        }
        // add water flow to the lists
        this.waterFlowList.add(water_flow);
        this.timestampsList.add(timestamp);
    }

    event.addField("dailyConsumption", this.daily_consumption);
    event.addField("weeklyConsumption", this.weekly_consumption);
    event.addField("monthlyConsumption", this.monthly_consumption);

    out.collect(event);
  }

  //from timestamp to date format
  private String getTheDate(Long timestamp) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(timestamp);
    return  date_format.format(cal.getTime());
  }

  // return the day of the current date
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

  // return the sum of daily consumption as weekly or monthly consumption
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

    // perform Riemann approximations by trapezoids which is an approximation of the area
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