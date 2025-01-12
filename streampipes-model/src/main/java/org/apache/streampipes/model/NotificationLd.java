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

package org.apache.streampipes.model;

import org.apache.commons.lang3.RandomStringUtils;

public class NotificationLd {

    private static final String prefix = "urn:streampipes.org:spi:";

    private String elementId;

    private String title;

    private String description;

    private String additionalInformation;

    public NotificationLd() {
        this.elementId = prefix
                + this.getClass().getSimpleName().toLowerCase()
                + ":"
                + RandomStringUtils.randomAlphabetic(6);
        this.additionalInformation = "";
    }

    public NotificationLd(NotificationLd other) {
        this();
        this.title = other.getTitle();
        this.description = other.getDescription();
        this.additionalInformation = other.getAdditionalInformation();
    }

    public NotificationLd(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public NotificationLd(String title, String description,
                        String additionalInformation) {
        this();
        this.title = title;
        this.description = description;
        this.additionalInformation = additionalInformation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
}
