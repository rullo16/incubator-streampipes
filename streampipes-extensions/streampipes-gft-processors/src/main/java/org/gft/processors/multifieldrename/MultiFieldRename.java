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

package org.gft.processors.multifieldrename;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.DataProcessorType;
import org.apache.streampipes.model.graph.DataProcessorDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.runtime.field.AbstractField;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.ProcessingElementBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.helpers.*;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;

public class MultiFieldRename extends StreamPipesDataProcessor {

    private static final String FIRST_CONVERT_PROPERTY = "first-convert-property";
    private static final String SECOND_CONVERT_PROPERTY = "second-convert-property";
    private static final String FIRST_FIELD_NAME = "first-field-name";
    private static final String SECOND_FIELD_NAME = "second-field-name";
    private String first_oldPropertyName;
    private String second_oldPropertyName;
    private String first_newPropertyName;
    private String second_newPropertyName;

    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("org.gft.processors.multifieldrename")
                .category(DataProcessorType.TRANSFORM)
                .withLocales(Locales.EN)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .requiredStream(StreamRequirementsBuilder
                        .create()
                        .requiredPropertyWithUnaryMapping(EpRequirements.anyProperty(), Labels.withId
                                (FIRST_CONVERT_PROPERTY), PropertyScope.NONE)
                        .requiredPropertyWithUnaryMapping(EpRequirements.anyProperty(), Labels.withId
                                (SECOND_CONVERT_PROPERTY), PropertyScope.NONE)
                        .build())
                .requiredTextParameter(Labels.withId(FIRST_FIELD_NAME))
                .requiredTextParameter(Labels.withId(SECOND_FIELD_NAME))
                .outputStrategy(OutputStrategies.transform(
                        TransformOperations.dynamicRuntimeNameTransformation(FIRST_CONVERT_PROPERTY, FIRST_FIELD_NAME),
                        TransformOperations.dynamicRuntimeNameTransformation(SECOND_CONVERT_PROPERTY, SECOND_FIELD_NAME)))
                .build();
    }

    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {

        this.first_oldPropertyName = processorParams.extractor().mappingPropertyValue(FIRST_CONVERT_PROPERTY);
        this.first_newPropertyName = processorParams.extractor().singleValueParameter(FIRST_FIELD_NAME, String.class);
        this.second_oldPropertyName = processorParams.extractor().mappingPropertyValue(SECOND_CONVERT_PROPERTY);
        this.second_newPropertyName = processorParams.extractor().singleValueParameter(SECOND_FIELD_NAME, String.class);
    }

    @Override
    public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {

        AbstractField<?> first_propertyValue = event.getFieldBySelector(first_oldPropertyName);
        AbstractField<?> second_propertyValue = event.getFieldBySelector(second_oldPropertyName);
        event.removeFieldBySelector(first_oldPropertyName);
        event.addField(first_newPropertyName, first_propertyValue);
        event.removeFieldBySelector(second_oldPropertyName);
        event.addField(second_newPropertyName, second_propertyValue);
        spOutputCollector.collect(event);
    }

    @Override
    public void onDetach() throws SpRuntimeException {

    }
}
