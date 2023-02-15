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

## Water Flow Tracking

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description
It Computes Daily, weekly and Monthly Water Consumption based on the given instant water flows and timestamps values.

***

## Required inputs
#### value and date_unix_ts

***

## Configuration
#### Instant Water Flow : value
Amount of water flowing (as past a valve) per unit of time (express in cubic meter per hour, cm/h) .

#### Timestamp : date_unix_ts
The field containing the time value (in millisecond) at which the water flow was taken.

## Output
The Water Flow Tracking processor appends the results of the calculated consumptions 
(daily, weekly and monthly), as a double in Cubic meter.
