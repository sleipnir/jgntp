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
package com.google.code.jgntp.internal.io;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.nio.*;
import org.slf4j.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.collect.*;

public class NioGntpClient implements GntpClient {

	private static final Logger logger = LoggerFactory.getLogger(NioGntpClient.class);

	private final GntpApplicationInfo applicationInfo;
	private final GntpPassword password;
	private final long retryTime;
	private final TimeUnit retryTimeUnit;
	private final int notificationRetryCount;

	private final ClientBootstrap bootstrap;
	private final ChannelGroup channelGroup;
	private final CountDownLatch registrationLatch;
	private final ScheduledExecutorService retryExecutorService;

	private final AtomicLong contextIdGenerator;
	private final Map<Long, Object> callbackContexts;
	private final Map<GntpNotification, Integer> notificationRetries;
	private volatile boolean closed;

	public NioGntpClient(GntpApplicationInfo applicationInfo, SocketAddress growlAddress, Executor executor, GntpListener listener, GntpPassword password, long retryTime, TimeUnit retryTimeUnit,
			int notificationRetryCount) {
		this.applicationInfo = applicationInfo;
		this.password = password;
		this.retryTime = retryTime;
		this.retryTimeUnit = retryTimeUnit;
		this.notificationRetryCount = notificationRetryCount;

		if (retryTime > 0) {
			retryExecutorService = Executors.newSingleThreadScheduledExecutor();
		} else {
			retryExecutorService = null;
		}

		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executor, executor));
		bootstrap.setPipelineFactory(new GntpChannelPipelineFactory(new GntpChannelHandler(this, listener)));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("remoteAddress", growlAddress);
		channelGroup = new DefaultChannelGroup("jgntp");
		contextIdGenerator = new AtomicLong();
		callbackContexts = Maps.newConcurrentMap();
		registrationLatch = new CountDownLatch(1);
		notificationRetries = Maps.newConcurrentMap();
	}

	public void register() {
		logger.debug("Registering GNTP application [{}]", applicationInfo);
		bootstrap.connect().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					channelGroup.add(future.getChannel());
					GntpMessage message = new GntpRegisterMessage(applicationInfo, password);
					future.getChannel().write(message);
				} else {
					if (retryExecutorService != null) {
						logger.debug("Registration failed, retrying in [{}] [{}]", retryTime, retryTimeUnit);
						retryExecutorService.schedule(new Runnable() {
							@Override
							public void run() {
								register();
							}
						}, retryTime, retryTimeUnit);
					}
				}
			}
		});
	}

	@Override
	public boolean isRegistered() {
		return registrationLatch.getCount() == 0 && !closed;
	}

	@Override
	public void waitRegistration() throws InterruptedException {
		registrationLatch.await();
	}

	@Override
	public boolean waitRegistration(long time, TimeUnit unit) throws InterruptedException {
		return registrationLatch.await(time, unit);
	}

	@Override
	public void notify(GntpNotification notification) {
		boolean interrupted = false;
		while (!closed) {
			try {
				waitRegistration();
				notifyInternal(notification);
				break;
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void notify(GntpNotification notification, long time, TimeUnit unit) throws InterruptedException {
		if (waitRegistration(time, unit)) {
			notifyInternal(notification);
		}
	}

	@Override
	public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
		closed = true;
		registrationLatch.countDown();
		if (retryExecutorService != null) {
			retryExecutorService.shutdownNow();
			retryExecutorService.awaitTermination(timeout, unit);
		}
		channelGroup.close().await(timeout, unit);
		bootstrap.releaseExternalResources();
	}

	void setRegistered() {
		registrationLatch.countDown();
	}

	Map<Long, Object> getCallbackContexts() {
		return callbackContexts;
	}

	protected void notifyInternal(final GntpNotification notification) {
		if (!closed) {
			logger.debug("Sending notification [{}]", notification);
			bootstrap.connect().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						channelGroup.add(future.getChannel());

						long contextId;
						if (notification.isCallbackRequested()) {
							contextId = contextIdGenerator.getAndIncrement();
							if (notification.getContext() != null) {
								callbackContexts.put(contextId, notification.getContext());
							}
						} else {
							contextId = -1;
						}

						GntpMessage message = new GntpNotifyMessage(notification, contextId, password);
						future.getChannel().write(message);
					} else {
						if (retryExecutorService != null) {
							Integer count = notificationRetries.get(notification);
							if (count == null) {
								count = 1;
							}
							if (count <= notificationRetryCount) {
								logger.debug("Failed to send notification [{}], retrying [{}/{}]", new Object[] { notification, count, notificationRetryCount });
								notificationRetries.put(notification, ++count);
								retryExecutorService.schedule(new Runnable() {
									public void run() {
										NioGntpClient.this.notify(notification);
									}
								}, retryTime, retryTimeUnit);
							} else {
								logger.debug("Failed to send notification [{}], giving up", notification);
								notificationRetries.remove(notification);
							}
						}
					}
				}
			});
		}
	}
}