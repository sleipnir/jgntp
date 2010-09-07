/*
 * Copyright (C) 2010 Leandro de Oliveira Aparecido <lehphyro@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.jgntp;

import java.net.*;
import java.util.concurrent.*;

import com.google.code.jgntp.internal.*;
import com.google.code.jgntp.internal.io.*;
import com.google.common.base.*;

public class Gntp {

	public static final String DEFAULT_HOST = "localhost";
	public static final int WINDOWS_PORT = 23053;
	public static final int MAC_PORT = 23052;
	public static final long DEFAULT_RETRY_TIME = 3;
	public static final TimeUnit DEFAULT_RETRY_TIME_UNIT = TimeUnit.SECONDS;
	public static final int DEFAULT_NOTIFICATION_RETRIES = 3;

	private GntpApplicationInfo applicationInfo;
	private SocketAddress growlAddress;
	private String growlHost;
	private int growlPort;
	private Executor executor;
	private GntpListener listener;
	private GntpPassword password;
	private long retryTime;
	private TimeUnit retryTimeUnit;
	private int notificationRetryCount;

	private Gntp(GntpApplicationInfo applicationInfo) {
		Preconditions.checkNotNull(applicationInfo, "Application info must not be null");
		this.applicationInfo = applicationInfo;
		growlHost = DEFAULT_HOST;
		growlPort = getPort();
		retryTime = DEFAULT_RETRY_TIME;
		retryTimeUnit = DEFAULT_RETRY_TIME_UNIT;
		notificationRetryCount = DEFAULT_NOTIFICATION_RETRIES;
	}

	public static GntpApplicationInfoBuilder appInfo(String name) {
		return new GntpApplicationInfoBuilder(name);
	}

	public static GntpNotificationInfoBuilder notificationInfo(GntpApplicationInfo applicationInfo, String name) {
		return new GntpNotificationInfoBuilder(applicationInfo, name);
	}

	public static GntpNotificationBuilder notification(GntpNotificationInfo info, String title) {
		return new GntpNotificationBuilder(info, title);
	}

	public static GntpPassword password(String textPassword) {
		return new GntpPasswordDefaultImpl(textPassword);
	}

	public static Gntp client(GntpApplicationInfo applicationInfo) {
		return new Gntp(applicationInfo);
	}

	public Gntp forAddress(SocketAddress address) {
		growlAddress = address;
		return this;
	}

	public Gntp forHost(String host) {
		Preconditions.checkNotNull(host, "Growl host must not be null");
		growlHost = host;
		return this;
	}

	public Gntp onPort(int port) {
		Preconditions.checkArgument(port > 0, "Port must not be negative");
		growlPort = port;
		return this;
	}

	public Gntp withExecutor(Executor executor) {
		this.executor = executor;
		return this;
	}

	public Gntp withListener(GntpListener listener) {
		this.listener = listener;
		return this;
	}

	public Gntp withPassword(String textPassword) {
		this.password = password(textPassword);
		return this;
	}

	public Gntp withPassword(GntpPassword password) {
		this.password = password;
		return this;
	}

	public Gntp retryingAtFixedRate(long time, TimeUnit unit) {
		Preconditions.checkArgument(time > 0, "Retrying time must be greater than zero");
		Preconditions.checkArgument(unit != null, "Retrying time unit must not be null");
		retryTime = time;
		retryTimeUnit = unit;
		return this;
	}

	public Gntp retryingNotificationsAtMost(int notificationRetryCount) {
		Preconditions.checkArgument(notificationRetryCount > 0, "Notification retries must be greater than zero");
		this.notificationRetryCount = notificationRetryCount;
		return this;
	}

	public Gntp withoutRetry() {
		retryTime = 0;
		retryTimeUnit = DEFAULT_RETRY_TIME_UNIT;
		return this;
	}

	public GntpClient build() {
		if (growlAddress == null) {
			growlAddress = new InetSocketAddress(growlHost, growlPort);
		}
		Executor executorToUse = executor == null ? Executors.newCachedThreadPool() : executor;
		return new NioGntpClient(applicationInfo, growlAddress, executorToUse, listener, password, retryTime, retryTimeUnit, notificationRetryCount);
	}

	private int getPort() {
		String osName = System.getProperty("os.name");
		if (osName != null) {
			osName = osName.toLowerCase();
			if (osName.contains("mac")) {
				return MAC_PORT;
			}
		}
		return WINDOWS_PORT;
	}
}
