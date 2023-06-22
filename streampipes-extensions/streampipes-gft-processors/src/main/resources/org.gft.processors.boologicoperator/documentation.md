<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  -->

## Logical Operation

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description

This processor performs the logical Boolean operation between the values of a set of properties. 
The user can choose the type of operation and the set of properties.

***

## Required inputs

The logical operation processor requires to work with any event that has two
field containing boolean values.

***

## Configurations

### First Property
The field from the input event that should be used as the first boolean.

### First Property
The field from the input event that should be used as the second boolean.

### Boolean Operator
The multi option field that enable to select only one of the operator for the logical operation

## Output

Outputs the incoming event while appending the result (``boolean-operations-result``) to the incoming event.
