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

package org.gft.processors.boologicoperator;

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
import org.gft.processors.boologicoperator.enums.BoolOperatorType;
import org.gft.processors.boologicoperator.operations.IBoolOperation;
import org.gft.processors.boologicoperator.operations.factory.BoolOperationFactory;

import static org.gft.processors.boologicoperator.enums.BoolOperatorType.NOT;


public class BoolOperatorProcessor extends StreamPipesDataProcessor {
    private static final String BOOLEAN_OPERATOR_TYPE = "operator-field";
    private static final String FIRST_PROPERTIES = "first-property";
    private static final String SECOND_PROPERTIES = "second-property";
    private BoolOperatorType operator_type;
    private String first_property;
    private String second_property;

    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("org.gft.processors.boologicoperator")
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .withLocales(Locales.EN)
                .category(DataProcessorType.AGGREGATE, DataProcessorType.BOOLEAN_OPERATOR)
                .requiredStream(StreamRequirementsBuilder.create()
                        .requiredPropertyWithUnaryMapping(EpRequirements.booleanReq(),
                                Labels.withId(FIRST_PROPERTIES), PropertyScope.NONE)
                        .requiredPropertyWithUnaryMapping(EpRequirements.booleanReq(),
                                Labels.withId(SECOND_PROPERTIES), PropertyScope.NONE)
                        .build())
                .requiredSingleValueSelection(Labels.withId(BOOLEAN_OPERATOR_TYPE), Options.from(
                        BoolOperatorType.AND.operator(),
                        BoolOperatorType.OR.operator(),
                        BoolOperatorType.NOT.operator(),
                        BoolOperatorType.XOR.operator(),
                        BoolOperatorType.X_NOR.operator(),
                        BoolOperatorType.NOR.operator()))
                .outputStrategy(OutputStrategies.append(
                        EpProperties.booleanEp(Labels.empty(), "boolean-operations-result", SO.Boolean)))
                .build();
    }

    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
        this.first_property = processorParams.extractor().mappingPropertyValue(FIRST_PROPERTIES);
        this.second_property = processorParams.extractor().mappingPropertyValue(SECOND_PROPERTIES);
        String operator = processorParams.extractor().selectedSingleValue(BOOLEAN_OPERATOR_TYPE, String.class);
        this.operator_type = BoolOperatorType.getBooleanOperatorType(operator);
    }

    @Override
    public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {
        Boolean first_property = event.getFieldBySelector(this.first_property).getAsPrimitive().getAsBoolean();
        Boolean second_property = event.getFieldBySelector(this.second_property).getAsPrimitive().getAsBoolean();
        IBoolOperation<Boolean> boolOperation = BoolOperationFactory.getBoolOperation(this.operator_type);
        Boolean result;

        result = boolOperation.evaluate(first_property, second_property);
        event.addField("boolean-operations-result", result);
        spOutputCollector.collect(event);
    }

    @Override
    public void onDetach() throws SpRuntimeException {
    }

}
