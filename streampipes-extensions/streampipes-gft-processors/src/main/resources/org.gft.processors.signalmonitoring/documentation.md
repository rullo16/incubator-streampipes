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

## Signal Monitoring

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description
The Monitoring processor filter and handle the numerical signal 
(negative and positive) to create the Boolean-like signal based on a given threshold
and on the variation of a numerical value over a customizable time window.

***

## Required inputs
The processor works with any input event that has at least two fields containing a timestamp and a numerical value to observe.
***

## Configuration
### Timestamp
Time of the input event.

### Input Value to Filter and Observe
Specifies the field name Specifies that should be tracked and
where the filter operation should be applied on.

### Operators
Specifies the filter operator(s) that should be applied on the value field for the filter operation(s).
It is valid for both the negative and the positive signal. 
"< / >" means that if your signal alternate or not between the both side of the horizontal axis,
the component will use both or one operator.

### Threshold value
Specifies the threshold value that will be use for filtering,
valid for both the negative and the positive signal.
Values will be taken on the numbers below the negative of the threshold and above the positive of the threshold.
For example: if we have 30, we will be take values above 30 and below -30.
### Percentage of Increase/Decrease
Specifies the increase/decrease in percent within the specified time window.
(e.g., 10 indicates an increase/decrease by 10 percent within the specified time window).

### Time Window Length (Minutes)
Specifies the size of the time window. It is used to track the variation of the signal
in order to decide if there is operation or not. If the processor does not 
note variations (based on the percentage and the filter operation) 
inside this dynamic time range, the boolean_like signal will be set to false 
until the next considerable change (based on the percentage and the filter operation)

## Output
The processor outputs the input event while appending the boolean-like signal (``monitored_signal``).

