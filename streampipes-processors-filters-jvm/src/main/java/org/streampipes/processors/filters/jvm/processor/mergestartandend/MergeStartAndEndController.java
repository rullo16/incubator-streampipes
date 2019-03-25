/*
Copyright 2019 FZI Forschungszentrum Informatik

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.streampipes.processors.filters.jvm.processor.mergestartandend;

import org.streampipes.model.DataProcessorType;
import org.streampipes.model.graph.DataProcessorDescription;
import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.processors.filters.jvm.config.FiltersJvmConfig;
import org.streampipes.sdk.builder.ProcessingElementBuilder;
import org.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.streampipes.sdk.extractor.ProcessingElementParameterExtractor;
import org.streampipes.sdk.helpers.EpRequirements;
import org.streampipes.sdk.helpers.OutputStrategies;
import org.streampipes.sdk.helpers.SupportedFormats;
import org.streampipes.sdk.helpers.SupportedProtocols;
import org.streampipes.wrapper.standalone.ConfiguredEventProcessor;
import org.streampipes.wrapper.standalone.declarer.StandaloneEventProcessingDeclarer;

import java.util.List;

public class MergeStartAndEndController extends StandaloneEventProcessingDeclarer<MergeStartAndEndParameters> {

  @Override
  public DataProcessorDescription declareModel() {
    //TODO: Add output strategy (check dashboard for how-to)?
    return ProcessingElementBuilder.create("org.streampipes.processors.filters.jvm.mergerorg.streampipes.processors.filters.jvm.processor.mergestartandend",
            "MergeStartAndEnd", "Merges two event streams if there is a start and an end")
            .category(DataProcessorType.TRANSFORM)
            .iconUrl(FiltersJvmConfig.getIconUrl("projection"))
            .requiredStream(StreamRequirementsBuilder
                    .create()
                    .requiredProperty(EpRequirements.anyProperty())
                    .build())
            .requiredStream(StreamRequirementsBuilder
                    .create()
                    .requiredProperty(EpRequirements.anyProperty())
                .build())
            .outputStrategy(OutputStrategies.custom(true))
            .supportedFormats(SupportedFormats.jsonFormat())
            .supportedProtocols(SupportedProtocols.jms(), SupportedProtocols.kafka())
            .build();
  }

  @Override
  public ConfiguredEventProcessor<MergeStartAndEndParameters>
  onInvocation(DataProcessorInvocation graph, ProcessingElementParameterExtractor extractor) {

    List<String> outputKeySelectors = extractor.outputKeySelectors();



    MergeStartAndEndParameters staticParam = new MergeStartAndEndParameters(
            graph, outputKeySelectors);

    return new ConfiguredEventProcessor<>(staticParam, MergeStartAndEnd::new);
  }
}
