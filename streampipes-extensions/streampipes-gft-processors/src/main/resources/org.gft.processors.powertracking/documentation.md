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

## Power Tracking (Hourly)

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description
This processor computes waiting time and hourly Energy consumption, based on the given instantaneous powers/timestamps values that are transmitted as fields from events.


***

## Required inputs
#### value, date_unix_ts and number

***

## Configuration
### Instantaneous Power: value
The field containing the power value as a double in Kilowatt (kW or kJ/s).
### Timestamp :  date_unix_ts
The field containing the time value (in millisecond) at which the power was taken.
### Waiting Time : number
The field containing the period or time value (in minute as a double) after which an output will be computed.

***

## Output
The Power Tracking processor appends the results of the calculated consumptions
(daily, weekly and monthly), as a double in kWh.