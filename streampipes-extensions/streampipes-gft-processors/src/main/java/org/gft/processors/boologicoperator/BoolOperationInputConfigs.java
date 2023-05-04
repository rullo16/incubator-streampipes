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

import org.gft.processors.boologicoperator.enums.BoolOperatorType;

public class BoolOperationInputConfigs {
    private final String second_property;
    private String first_property;
    private BoolOperatorType operator;

    public BoolOperationInputConfigs(String first_property, String second_property, BoolOperatorType operatorType) {
        this.first_property = first_property;
        this.second_property = second_property;
        this.operator = operatorType;
    }

    public BoolOperatorType getOperator() {
        return operator;
    }
    public String getFirstProperty() {
        return first_property;
    }
    public String getSecondProperty() {
        return second_property;
    }
}
