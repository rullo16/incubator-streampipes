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

package org.gft.processors.hoursmonitoring;

import org.apache.streampipes.logging.api.Logger;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.runtime.EventProcessor;

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

public class BoolTimer implements EventProcessor<BoolTimerParameters> {
  private static Logger LOG;
  private String fieldName;
  private boolean measureTrue;
  private Long timestamp;
  private int day_precedent = -1, month_precedent = -1;
  private double outputDivisor;
  private final DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  List<Double> measuredTimeListDaily = new ArrayList<>();
  List<Double> measuredTimeListWeekly = new ArrayList<>();
  List<Double> measuredTimeListMonthly = new ArrayList<>();
  private Double hours_daily = 0.0, hours_weekly = 0.0, hours_monthly = 0.0;
  private double result = 0.0;


  @Override
  public void onInvocation(BoolTimerParameters booleanInverterParameters,
                           SpOutputCollector spOutputCollector,
                           EventProcessorRuntimeContext runtimeContext) {
    LOG = booleanInverterParameters.getGraph().getLogger(BoolTimer.class);
    this.fieldName = booleanInverterParameters.getFieldName();
    this.measureTrue = booleanInverterParameters.isMeasureTrue();
    this.timestamp = Long.MIN_VALUE;
    this.outputDivisor = booleanInverterParameters.getOutputDivisor();
  }

  @Override
  public void onEvent(Event inputEvent, SpOutputCollector out) {

    boolean field = inputEvent.getFieldBySelector(this.fieldName).getAsPrimitive().getAsBoolean();

    if (this.measureTrue == field) {
      if (this.timestamp == Long.MIN_VALUE) {
        this.timestamp = System.currentTimeMillis();
      }
    } else {
      if (this.timestamp != Long.MIN_VALUE) {
        Long current_time = System.currentTimeMillis();
        long difference = current_time - this.timestamp;

        this.result = difference / this.outputDivisor;

        //recovery date value
        String date = getTheDate(current_time);

        // Day and Month extraction
        String[] ymd_hms = date.split(" ");
        String[] ymd = ymd_hms[0].split("-");
        int day_current = Integer.parseInt(ymd[2]);
        int month_current = Integer.parseInt(ymd[1]);
        String day = getCurrentDay(date);

        this.measuredTimeListDaily.add(this.result);

        if(day_current != this.day_precedent && this.day_precedent != -1){
            // reset day for computations
            this.day_precedent = day_current;

            //perform operations to obtain hours per day
            this.hours_daily = calculateHours(this.measuredTimeListDaily);
            this.measuredTimeListDaily.clear();

            if(day.equals("Mon")){
              this.hours_weekly = calculateHours(this.measuredTimeListWeekly);
              this.measuredTimeListWeekly.clear();
            }

            if(month_current != this.month_precedent){
              this.month_precedent = month_current;
              this.hours_monthly = calculateHours(this.measuredTimeListMonthly);
              this.measuredTimeListMonthly.clear();
            }
        }
        this.timestamp = Long.MIN_VALUE;
      }
    }

    inputEvent.addField("measured_time", this.result);
    inputEvent.addField("hours_daily", this.hours_daily);
    inputEvent.addField("hours_weekly", this.hours_weekly);
    inputEvent.addField("hours_monthly", this.hours_monthly);
    out.collect(inputEvent);
  }


  @Override
  public void onDetach() {
  }

  public static void main(String... args) {

    double result = (60000L / 631.1);

    System.out.println(result);

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

  private Double calculateHours(List<Double> measuredTimeList) {
    double sum = 0.0;
    DecimalFormat df = new DecimalFormat("#.#####");
    df.setRoundingMode(RoundingMode.CEILING);
    for (Double value : measuredTimeList) sum = sum + value;
    return Double.parseDouble(df.format(sum));
  }

}
