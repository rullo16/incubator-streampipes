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

## Multi-Field Rename
<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>


***

## Description

Replaces the runtime names of two event properties with a custom defined names.
Useful for data ingestion purposes where a specific event schema is required.


***
## Configuration

### Two Old Field Names  
Specifies the fields to rename.

### Two New Field Names
Specifies the new runtime names of the field.

## Output
Example:

Old Output:
```
{
  'timestamp': 16003000, 
  'real value': 12
}
```

New Ouput:
```
{
  'time': 16003000, 
  'value': 12
}
```