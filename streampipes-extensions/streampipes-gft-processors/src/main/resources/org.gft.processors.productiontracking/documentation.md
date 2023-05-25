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

## Production Tracking

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description
The Production Tracking processor maps two signal synchronising two timestamps (from two different data source) 
filtering values based on a threshold value and on the input boolean values. 

***

## Required Inputs
The processor works with any input event that has at least four fields
containing one timestamp and one numerical value from the first data source, 
one timestamp and one boolean value from the second data source.

***

## Configuration

### First Timestamp
Time at which the value to observe was taken (from numerical signal).

### Value to Observe
Specifies the field name (from first data source(numerical signal)) that should be observed and
where the filter operation should be applied on.

### Second Timestamp
Time at which the boolean value was taken (from boolean-like signal).

### Boolean value to observe
Specifies the boolean value field name from second data source (boolean-like signal).

### Threshold value
Specifies the threshold value that will be use for filtering.


## Output
The processor outputs the events with the following payload scheme:

(``{

    "time" : 154768794010791,
    
    "value": 1725.22,
    
    "task": true,
    
    "production": true

}``)


where:

- ``time``variable is a timestamp in milliseconds resolution.

- ``value`` variable represents the value to observe input.

- ``task`` variable is the Boolean value which, when defined as true, means that the machine is plugged in for the project (project task).

- ``production`` variable is the Boolean value which, when defined as true, means that we are in the production phase of the project task (with false -> idle state and true -> active state){

