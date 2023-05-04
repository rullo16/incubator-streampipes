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

package org.gft.processors.powertracking;

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class PowerTrackingProcessor extends StreamPipesDataProcessor {
    private String input_power_value;
    private String input_timestamp_value;
    private Double waiting_time;
    private Double waitingtime_start = 0.0;
    private Double hourlytime_start = 0.0;
    private double hourly_consumption = 0.0;
    private double waitingtime_consumption = 0.0;
    private static final String ID = "org.gft.processors.powertracking";
    private static final String INPUT_VALUE = "value";
    private static final String TIMESTAMP_VALUE = "timestamp_value";
    private static final String WAITING_TIME = "time_range";
    private static final String HOURLY_CONSUMPTION = "hourly_consumption";
    private static final String WAITINGTIME_CONSUMPTION = "waitingtime_consumption";

    List<Double> powersListForHourlyBasedComputation = new ArrayList<>();
    List<Double> timestampsListForHourlyBasedComputation = new ArrayList<>();
    List<Double> powersListForWaitingTimeBasedComputation = new ArrayList<>();
    List<Double> timestampsListForWaitingTimeBasedComputation = new ArrayList<>();


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
                .requiredIntegerParameter(Labels.withId(WAITING_TIME))

                .outputStrategy(OutputStrategies.append(EpProperties.doubleEp(Labels.withId(WAITINGTIME_CONSUMPTION), "waitingtimeConsumption", SO.Number),
                        EpProperties.doubleEp(Labels.withId(HOURLY_CONSUMPTION), "hourlyConsumption", SO.Number)))
                .build();
    }

    @Override
    public void onInvocation(ProcessorParams parameters, SpOutputCollector out, EventProcessorRuntimeContext ctx) throws SpRuntimeException  {
        this.input_power_value = parameters.extractor().mappingPropertyValue(INPUT_VALUE);
        this.input_timestamp_value = parameters.extractor().mappingPropertyValue(TIMESTAMP_VALUE);
        this.waiting_time = parameters.extractor().singleValueParameter(WAITING_TIME, Double.class);
    }

    @Override
    public void onEvent(Event event,SpOutputCollector out){
        double waiting_time = this.waiting_time*60*1000;

        Double power = event.getFieldBySelector(this.input_power_value).getAsPrimitive().getAsDouble();

        Double timestamp = event.getFieldBySelector(this.input_timestamp_value).getAsPrimitive().getAsDouble();

       if(((timestamp - this.waitingtime_start >= waiting_time) || (timestamp - this.hourlytime_start >= 3600000)) && this.waitingtime_start != 0.0){

            if(timestamp - this.waitingtime_start >= waiting_time){
                this.waitingtime_start = timestamp;
                this.powersListForWaitingTimeBasedComputation.add(power);
                this.timestampsListForWaitingTimeBasedComputation.add(timestamp);
                this.waitingtime_consumption = powerToEnergy(this.powersListForWaitingTimeBasedComputation, this.timestampsListForWaitingTimeBasedComputation);
                this.powersListForWaitingTimeBasedComputation.clear();
                this.timestampsListForWaitingTimeBasedComputation.clear();
                this.powersListForWaitingTimeBasedComputation.add(power);
                this.timestampsListForWaitingTimeBasedComputation.add(timestamp);
            }

            if (timestamp - this.hourlytime_start >= 3600000) {
                this.hourlytime_start  = timestamp;
                this.powersListForHourlyBasedComputation.add(power);
                this.timestampsListForHourlyBasedComputation.add(timestamp);
                this.hourly_consumption = powerToEnergy(this.powersListForHourlyBasedComputation, this.timestampsListForHourlyBasedComputation);
                this.powersListForHourlyBasedComputation.clear();
                this.timestampsListForHourlyBasedComputation.clear();
                this.powersListForHourlyBasedComputation.add(power);
                this.timestampsListForHourlyBasedComputation.add(timestamp);
            }

        }else {
           if (this.waitingtime_start == 0.0){
               this.hourlytime_start = timestamp;
               this.waitingtime_start = timestamp;
           }
           this.powersListForWaitingTimeBasedComputation.add(power);
           this.timestampsListForWaitingTimeBasedComputation.add(timestamp);
           this.powersListForHourlyBasedComputation.add(power);
           this.timestampsListForHourlyBasedComputation.add(timestamp);

        }

        event.addField("waitingtimeConsumption", this.waitingtime_consumption);
        event.addField("hourlyConsumption", this.hourly_consumption);

        out.collect(event);

    }

    public double powerToEnergy(List<Double> powers, List<Double> timestamps) {
        double sum = 0.0;
        double first_base;
        double second_base;
        double height;
        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.CEILING);
        for(int i = 0; i<powers.size()-1; i++){
            first_base = powers.get(i);
            second_base = powers.get(i+1);
            height = (timestamps.get(i+1) - timestamps.get(i))/1000;
            sum += ((first_base + second_base) / 2) * height ;
        }
        return Double.parseDouble(df.format(sum/3600));
    }

    @Override
    public void onDetach(){
    }

}