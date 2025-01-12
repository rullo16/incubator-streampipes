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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageLd {

	private static final String prefix = "urn:streampipes.org:spi:";

	private String elementId;

	private boolean success;

	private String elementName;

	private List<NotificationLd> notifications;

	public MessageLd() {
		this.elementId = prefix
				+ this.getClass().getSimpleName().toLowerCase()
				+ ":"
				+ RandomStringUtils.randomAlphabetic(6);
		this.elementName = "";
	}

	public MessageLd(MessageLd other) {
		this();
		this.success = other.isSuccess();
		this.elementName = other.getElementName();
		this.notifications = other.getNotifications();
	}

	public MessageLd(boolean success){
		this();
		this.success = success;
		this.notifications = null;
	}

	public MessageLd(boolean success, List<NotificationLd> notifications) {
		this();
		this.success = success;
		this.notifications = notifications;
	}

	public MessageLd(boolean success, List<NotificationLd> notifications, String elementName) {
		this(success, notifications);
		this.elementName = elementName;
	}


	public MessageLd(boolean success, NotificationLd...notifications) {
		this();
		this.success = success;
		this.notifications = new ArrayList<>();
		this.notifications.addAll(Arrays.asList(notifications));
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public List<NotificationLd> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<NotificationLd> notifications) {
		this.notifications = notifications;
	}
	
	public boolean addNotification(NotificationLd notification)
	{
		return notifications.add(notification);
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	
	
}
