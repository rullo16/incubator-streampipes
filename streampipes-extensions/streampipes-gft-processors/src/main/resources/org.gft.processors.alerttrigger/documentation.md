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

## Alert Trigger 

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description
The Alert Trigger processor filters numerical values based on a given threshold 
and/or tracks duplicated values and/or timestamp events in order to issue alerts.
It forwards events in any case with three integrated boolean values representing the state of the alerts.

***

## Required inputs
The processor works with any input event that at least three fields, 
two fields containing a timestamp and a numerical value from your data source, 
and one field containing a simulated timestamp from a data source simulator.

***

## Configuration

### Timestamp
Specifies the field name with timestamp value.

### Value
Specifies the field name where the filter operation should be applied on.

### Operation
Specifies the filter operation that should be applied on the field.

### Threshold value
Specifies the threshold value for the filter operation.

### Max Duration For Duplicated Values.
Specifies the max duration for setting up the  alert.
If we receive duplicated values for a time greater than or equal to Max Duration,
the alert will be triggered and will remain up until the value changes.

### Duration Unit.
Specifies the unit that should be applied on the duration field.

### Max Delay Between Two Event.
Specifies the max delay for setting up the alert. 
If the delay between two consecutive event is greater than or equal to Max Delay, 
or if we receive duplicated timestamp for a time greater than or equal to Max Delay
the alert will be triggered and will remain up until a different data arrives or until the timestamp changes.

### Delay Unit.
Specifies the unit that should be applied on the delay field.

## Further information on the configuration
It is possible to activate only one alerts or two alerts or all the three.
- To disable the ``Alert_threshold``, set up the Filter operator field to ``None``
- To disable the ``Alert_duplicate``, set up the Duration unit field to ``None``
  and Max duration field to ``0``
- To disable the ``Alert_delay``, set up the Delay unit field to ``None``
  and Max delay field to ``0``
- 
## Output
Outputs the incoming event while appending:
- a field with a Boolean value (``Alert_threshold``) indicating whether the filtering operation is satisfied (true) or not (false).
- a fields with the boolean value (``Alert_duplicate``) indicating whether there is a successive duplicated values(true) or not(false).
- a fields with the boolean value (``Alert_delay``) indicating whether there is a delay (true) or not(false).
