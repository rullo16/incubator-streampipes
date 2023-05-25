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

package org.gft.processors.signalmonitoring;

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

public class SignalMonitoringProcessor extends StreamPipesDataProcessor {

  private static final String VALUE = "number-mapping";
  private static final String THRESHOLD = "threshold";
  private static final String OPERATION = "operation";
  private static final String INCREASE_DECREASE = "increase-decrease";
  private static final String TIMESTAMP = "timestamp-in";
  private static final String DURATION = "window";
  private double threshold;
  private String value;
  private Double precedent_value = 0.0;
  private String operations;
  private Integer variation;
  private String timestamp;

  private Long time_window;
  private Long dynamic_time = 0L;
  private Long time_true = 0L;


    //Define abstract stream requirements such as event properties that must be present in any input stream
    // that is later connected to the element using the StreamPipes UI
    @Override
  public DataProcessorDescription declareModel() {
    return ProcessingElementBuilder.create("org.gft.processors.signalmonitoring")
            .category(DataProcessorType.AGGREGATE)
            .withAssets(Assets.DOCUMENTATION, Assets.ICON)
            .withLocales(Locales.EN)
            .requiredStream(StreamRequirementsBuilder
                    .create()
                    .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                            Labels.withId(TIMESTAMP), PropertyScope.NONE)
                    .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                            Labels.withId(VALUE),
                            PropertyScope.NONE).build())
            .requiredSingleValueSelection(Labels.withId(OPERATION), Options.from("< / >", "<= / >="))
            .requiredFloatParameter(Labels.withId(THRESHOLD))
            .requiredIntegerParameter(Labels.withId(INCREASE_DECREASE), 0, 100, 1)
            .requiredIntegerParameter(Labels.withId(DURATION), 0, 100, 1)
           .outputStrategy(OutputStrategies.fixed(EpProperties.timestampProperty("timestamp"),
                    EpProperties.doubleEp(Labels.empty(), "value", SO.NUMBER),
                    EpProperties.booleanEp(Labels.empty(), "monitored_signal", SO.BOOLEAN)))
            .build();
  }

    //Triggered once a pipeline is started. Allow to identify the actual stream that are connected to the pipeline element and the runtime names
    // to build the different selectors ("stream"::"runtime name") to use in onEvent method to retrieve the exact data.
    @Override
  public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
    this.timestamp = processorParams.extractor().mappingPropertyValue(TIMESTAMP);
    this.threshold = processorParams.extractor().singleValueParameter(THRESHOLD, Double.class);
    this.variation = processorParams.extractor().singleValueParameter(INCREASE_DECREASE, Integer.class);
    this.operations = processorParams.extractor().selectedSingleValue(OPERATION, String.class);
    this.value = processorParams.extractor().mappingPropertyValue(VALUE);
    Integer window = processorParams.extractor().singleValueParameter(DURATION, Integer.class);
    this.time_window = (long) (window * 60 * 1000);
  }

    // Get fields from an incoming event by providing the corresponding selector (casting to their corresponding target data types).
    // Then use the if conditional statements to define the output value
  @Override
  public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {
    Boolean satisfies_monitoring = null;

    Long timestamp = event.getFieldBySelector(this.timestamp).getAsPrimitive().getAsLong();
    Double current_value = event.getFieldBySelector(this.value).getAsPrimitive().getAsDouble();

    // filter the input value and select which are useful based on a given threshold.
    if(this.operations.equals("< / >") && (current_value < -Math.abs(this.threshold) || current_value > Math.abs(this.threshold))){
      satisfies_monitoring = isSatisfiesMonitoring(current_value, timestamp);
    }

    if(this.operations.equals("<= / >=") && (current_value <= -Math.abs(this.threshold) || current_value >= Math.abs(this.threshold))){
      satisfies_monitoring = isSatisfiesMonitoring(current_value, timestamp);
    }

    if(this.operations.equals("< / >") && Math.abs(current_value) <= Math.abs(this.threshold)){
        satisfies_monitoring = checkOutOfRange(timestamp);
    }

    if(this.operations.equals("<= / >=") && Math.abs(current_value) < Math.abs(this.threshold)){
       satisfies_monitoring = checkOutOfRange(timestamp);
    }


    // update precedent value to the current value for the next monitoring
    this.precedent_value = current_value;

    event.addField("timestamp", timestamp);
    event.addField("value", current_value);
    event.addField("monitored_signal", satisfies_monitoring);
    spOutputCollector.collect(event);
  }

    private Boolean checkOutOfRange(Long timestamp) {
        if (this.dynamic_time.equals(this.time_true) && (this.dynamic_time != 0L)) {
            this.dynamic_time = timestamp;
            return true;
        }

        return Math.abs(this.dynamic_time - timestamp) < this.time_window;
    }

    //  monitor and handle the numerical signal (negative and positive) to create the Boolean-like signal
   // Detects the increase/decrease of a numerical field over two consecutive value and return true if the variation is satisfied.
  private boolean isSatisfiesMonitoring(Double current_value, Long timestamp) {
      boolean satisfies_monitoring = false;

      if(this.precedent_value < 0.0 && current_value < 0.0){

          if (current_value <= this.precedent_value){
            satisfies_monitoring = current_value <= (this.precedent_value * (1 + (this.variation / 100.0)));
          }else {
            satisfies_monitoring = current_value >= (this.precedent_value * (1 - (this.variation / 100.0)));
          }
      }

      if(this.precedent_value >= 0.0 && current_value >= 0.0){

          if (current_value >= this.precedent_value){
            satisfies_monitoring = current_value >= (this.precedent_value * (1 + (this.variation / 100.0)));
          }else {
            satisfies_monitoring = current_value <= (this.precedent_value * (1 - (this.variation / 100.0)));
          }
      }

      if((this.precedent_value <= 0.0 && current_value >= 0.0 )|| (this.precedent_value >= 0.0 && current_value <= 0.0)){
          satisfies_monitoring = true;
      }

      if (satisfies_monitoring) {
          this.dynamic_time = timestamp;
          this.time_true = timestamp;
          return true;
      }

      if (this.dynamic_time.equals(this.time_true) && (this.dynamic_time != 0L)) {
          this.dynamic_time = timestamp;
          return true;
      }

      return Math.abs(this.dynamic_time - timestamp) < this.time_window;

  }

  @Override
  public void onDetach() throws SpRuntimeException {

  }


      /*if(this.dynamic_time >= -1 && this.dynamic_time < this.time_window){
          this.dynamic_time++;
          return true;
      }*/

}
