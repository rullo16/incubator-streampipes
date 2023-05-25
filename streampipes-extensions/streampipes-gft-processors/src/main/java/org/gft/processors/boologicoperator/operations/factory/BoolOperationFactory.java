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

package org.gft.processors.boologicoperator.operations.factory;

import org.gft.processors.boologicoperator.enums.BoolOperatorType;
import org.gft.processors.boologicoperator.operations.*;

import static org.gft.processors.boologicoperator.enums.BoolOperatorType.*;

public class BoolOperationFactory {

    public static IBoolOperation<Boolean> getBoolOperation(BoolOperatorType type) {
        if (type == AND) {
            return new ANDBoolOperation();
        } else if (type == OR) {
            return new ORBooleanOperation();
        } else if (type == XOR) {
            return new XORBooleanOperation();
        } else if (type == NOT) {
            return new NOTBooleanOperation();
        } else if (type == X_NOR) {
            return new XNORBoolOperation();
        } else if (type == NOR) {
            return new NORBoolOperation();
        } else {
            throw new UnsupportedOperationException("Operation " + type.operator() + " is not supported");
        }
    }
}
