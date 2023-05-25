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

package org.gft.processors.settostream;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.DataProcessorType;
import org.apache.streampipes.model.graph.DataProcessorDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.ProcessingElementBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.helpers.EpRequirements;
import org.apache.streampipes.sdk.helpers.Labels;
import org.apache.streampipes.sdk.helpers.Locales;
import org.apache.streampipes.sdk.helpers.OutputStrategies;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;

public class SetToStream extends StreamPipesDataProcessor {
    private static final String TIMESTAMP = "timestamp";
    private static final String SLEEP = "sleep";
    private String timestamp;
    private Integer delay;

    //Define abstract stream requirements such as event properties that must be present
    // in any input stream that is later connected to the element using the StreamPipes UI
    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("org.gft.processors.settostream")
                .category(DataProcessorType.AGGREGATE)
                .withLocales(Locales.EN)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .requiredStream(StreamRequirementsBuilder.create()
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(TIMESTAMP), PropertyScope.NONE)
                        .build())
                .requiredIntegerParameter(Labels.withId(SLEEP), 10, 100, 1)
                .outputStrategy(OutputStrategies.keep())
                .build();
    }

    //Triggered once a pipeline is started. Allow to identify the actual stream that are connected to the pipeline element and the runtime names
    // to build the different selectors ("stream"::"runtime name") to use in onEvent method to retrieve the exact data.
    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
        this.timestamp = processorParams.extractor().mappingPropertyValue(TIMESTAMP);
        this.delay = processorParams.extractor().singleValueParameter(SLEEP, Integer.class);
    }

    // Get fields from an incoming event by providing the corresponding selector (casting to their corresponding target data types).
    // Then use the if conditional statements to define the output value
    @Override
    public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {
        Long current_time =  event.getFieldBySelector(this.timestamp).getAsPrimitive().getAsLong();
        spOutputCollector.collect(event);
        try {
            // Adding a 0.0this.delay-second delay (this.delay milliseconds)
            long delay = (long) this.delay;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() throws SpRuntimeException {

    }
}
