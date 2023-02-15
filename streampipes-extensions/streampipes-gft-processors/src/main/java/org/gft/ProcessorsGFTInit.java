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

package org.gft;

import org.apache.streampipes.container.extensions.ExtensionsModelSubmitter;
import org.apache.streampipes.container.model.SpServiceDefinition;
import org.apache.streampipes.container.model.SpServiceDefinitionBuilder;
import org.apache.streampipes.dataformat.cbor.CborDataFormatFactory;
import org.apache.streampipes.dataformat.fst.FstDataFormatFactory;
import org.apache.streampipes.dataformat.json.JsonDataFormatFactory;
import org.apache.streampipes.dataformat.smile.SmileDataFormatFactory;
import org.apache.streampipes.messaging.jms.SpJmsProtocolFactory;
import org.apache.streampipes.messaging.kafka.SpKafkaProtocolFactory;
import org.apache.streampipes.messaging.mqtt.SpMqttProtocolFactory;

import org.gft.adapters.backend.BackendHttpStreamProtocol;
import org.gft.adapters.plm.PLMHttpStreamProtocol;
import org.gft.processors.interpolation.InterpolationDataProcessor;
import org.gft.processors.loessinterpolation.LoessInterpolationDataProcessor;
import org.gft.processors.powertracking.PowerTrackingProcessor;
import org.gft.processors.powertrackingdwm.PowerTrackingDWM;
import org.gft.processors.trendfiltered.TrendFilteredController;
import org.gft.processors.waterflowtracking.WaterFlowTrackingDWM;

public class ProcessorsGFTInit extends ExtensionsModelSubmitter {

  public static void main (String[] args) {
    new ProcessorsGFTInit().init();
  }

  @Override
  public SpServiceDefinition provideServiceDefinition() {
    return SpServiceDefinitionBuilder.create("org.gft",
                    "GFT Processors",
                    "", 8090)
            .registerPipelineElements(
                    new InterpolationDataProcessor(),
                    new TrendFilteredController(),
                    new LoessInterpolationDataProcessor(),
                    new PowerTrackingProcessor(),
                    new PowerTrackingDWM())
            .registerPipelineElement(new WaterFlowTrackingDWM())
            .registerAdapter(new BackendHttpStreamProtocol())
            .registerAdapter(new PLMHttpStreamProtocol())
            .registerMessagingFormats(
                    new JsonDataFormatFactory(),
                    new CborDataFormatFactory(),
                    new SmileDataFormatFactory(),
                    new FstDataFormatFactory())
            .registerMessagingProtocols(
                    new SpKafkaProtocolFactory(),
                    new SpJmsProtocolFactory(),
                    new SpMqttProtocolFactory())
            .build();
  }
}
