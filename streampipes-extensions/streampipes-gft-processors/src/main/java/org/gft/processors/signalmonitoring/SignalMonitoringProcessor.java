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
    private static final String VALUE_OUT = "value";
  private static final String THRESHOLD = "threshold";
  private static final String OPERATION = "operation";
  private static final String INCREASE_DECREASE = "increase-decrease";
  private static final String MONITORED_SIGNAL = "monitored-signal";
  private static final String TIMESTAMP = "timestamp-in";
  private double threshold;
  private String value;
  private Double precedent_value = 0.0;
  private String operations;
  private Integer variation;
  private String timestamp;

  /* TODO make it fixed output
            delete signal field
   */

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
            .requiredSingleValueSelection(Labels.withId(OPERATION), Options.from("< and/or >", "<= and/or >="))
            .requiredFloatParameter(Labels.withId(THRESHOLD))
            .requiredIntegerParameter(Labels.withId(INCREASE_DECREASE), 0, 100, 1)
            .outputStrategy(OutputStrategies.fixed(EpProperties.timestampProperty("timestamp"),
                    EpProperties.booleanEp(Labels.withId(VALUE_OUT), "value", SO.Number),
                    EpProperties.booleanEp(Labels.withId(MONITORED_SIGNAL), "monitored_signal", SO.Boolean)))
            .build();
  }

  @Override
  public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
    this.timestamp = processorParams.extractor().mappingPropertyValue(TIMESTAMP);
    this.threshold = processorParams.extractor().singleValueParameter(THRESHOLD,Double.class);
    this.variation = processorParams.extractor().singleValueParameter(INCREASE_DECREASE, Integer.class);
    this.operations = processorParams.extractor().selectedSingleValue(OPERATION,String.class);
    this.value = processorParams.extractor().mappingPropertyValue(VALUE);
  }

  @Override
  public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {
    Boolean satisfies_monitoring = false;

    Long timestamp = event.getFieldBySelector(this.timestamp).getAsPrimitive().getAsLong();
    Double current_value = event.getFieldBySelector(this.value).getAsPrimitive().getAsDouble();

    if(this.operations.equals("< and/or >") && (current_value < -Math.abs(this.threshold) || current_value > Math.abs(this.threshold))){
      satisfies_monitoring = isSatisfiesMonitoring(current_value);
    }

    if(this.operations.equals("<= and/or >=") && (current_value <= -Math.abs(this.threshold) || current_value >= Math.abs(this.threshold))){
      satisfies_monitoring = isSatisfiesMonitoring(current_value);
    }

    this.precedent_value = current_value;

    event.addField("timestamp", timestamp);
    event.addField("value", current_value);
    event.addField("monitored_signal", satisfies_monitoring);
    spOutputCollector.collect(event);
  }

  private boolean isSatisfiesMonitoring(Double current_value) {
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

      return satisfies_monitoring;
  }

  @Override
  public void onDetach() throws SpRuntimeException {

  }

}
