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
package org.gft.processors.productiontracking;

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


public class ProductionTracking extends StreamPipesDataProcessor {

    public static final String VALUE = "value";
    public static final String OUT_VALUE = "out-value";
    private static final String BOOLEAN_MAPPING = "boolean-mapping";
    public static final String PRODUCTION = "production";
    private static final String FIRST_TIMESTAMP = "timestamp-first" ;
    private static final String SECOND_TIMESTAMP = "timestamp-second" ;
    private static final String THRESHOLD = "threshold";
    private static final String TASK = "task";
    private String mappingField;
    private String first_timestamp;
    private String second_timestamp;
    private Double threshold;
    private String value;


    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("org.gft.processors.productiontracking")
                .category(DataProcessorType.FILTER, DataProcessorType.BOOLEAN_OPERATOR)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .withLocales(Locales.EN)
                .requiredStream(StreamRequirementsBuilder.create()
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(VALUE), PropertyScope.MEASUREMENT_PROPERTY)
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(FIRST_TIMESTAMP), PropertyScope.NONE)
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(SECOND_TIMESTAMP), PropertyScope.NONE)
                        .requiredPropertyWithUnaryMapping(EpRequirements.booleanReq(),
                                Labels.withId(BOOLEAN_MAPPING), PropertyScope.NONE)
                        .build())
                .requiredFloatParameter(Labels.withId(THRESHOLD))
                .outputStrategy(OutputStrategies.fixed(EpProperties.timestampProperty("timestamp"),
                        EpProperties.booleanEp(Labels.withId(OUT_VALUE), "value", SO.Number),
                        EpProperties.booleanEp(Labels.withId(PRODUCTION), "production", SO.Boolean),
                        EpProperties.booleanEp(Labels.withId(TASK), "task", SO.Boolean)))
                .build();
    }

    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
        this.value = processorParams.extractor().mappingPropertyValue(VALUE);
        this.first_timestamp = processorParams.extractor().mappingPropertyValue(FIRST_TIMESTAMP);
        this.second_timestamp  = processorParams.extractor().mappingPropertyValue(SECOND_TIMESTAMP);
        this.mappingField = processorParams.extractor().mappingPropertyValue(BOOLEAN_MAPPING);
        this.threshold = processorParams.extractor().singleValueParameter(THRESHOLD ,Double.class);
    }

    @Override
    public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {
        Double value = event.getFieldBySelector(this.value).getAsPrimitive().getAsDouble();
        Long first_timestamp = event.getFieldBySelector(this.first_timestamp).getAsPrimitive().getAsLong();
        Long second_timestamp = event.getFieldBySelector(this.second_timestamp).getAsPrimitive().getAsLong();
        Boolean bool = event.getFieldBySelector(this.mappingField).getAsPrimitive().getAsBoolean();

        event.addField("timestamp", first_timestamp);
        event.addField("value", value);
        if (first_timestamp >= second_timestamp && bool) {

            event.addField("task", true);
            if(Math.abs(this.threshold)  <= value){
                event.addField("production", true);
            }else{
                event.addField("production", false);
            }

        }else{
            event.addField("task", false);
            event.addField("production", false);
        }

        spOutputCollector.collect(event);
    }

    @Override
    public void onDetach() throws SpRuntimeException {
    }
}
