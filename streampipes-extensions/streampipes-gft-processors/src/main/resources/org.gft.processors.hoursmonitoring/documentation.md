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

## Operating Time Monitoring

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description

This processor measures the time during which a Boolean value
(state of an operation) does not change. Once the value changes, 
the event with the measured time is output and accumulated to calculate 
and output daily, weekly and monthly minutes/hours of operation.
***

## Required inputs
The Operating Time processor requires to work with a boolean-like signal   
event that has at least two fields containing a timestamp and a boolean value.

***

## Configuration
### Timestamp
The boolean-like signal related timestamp.

### Boolean Value Field
The boolean field which is monitored for state changes.

### Value to Observe
Define whether it should be measured how long the value is true or how long the value is false.

### Unit
Unit that will be use in the calculations for the outputs. it will be applied as unit of measure.

## Output
Outputs the incoming event while appending:
 - a field with the time how long the value did not change (``measure_time``). 
 - the fields with the daily, weekly and monthly minutes/hours of operating 
(``operating_time_daily``, ``operating_time_weekly``, ``operating_time_monthly``)  

