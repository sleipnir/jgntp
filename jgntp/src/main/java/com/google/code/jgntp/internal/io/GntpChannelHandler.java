/*
 * Copyright (C) 2010 Leandro Aparecido <lehphyro@gmail.com>
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

import java.io.*;

import org.jboss.netty.channel.*;
import org.slf4j.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.*;
import com.google.common.base.*;

public class GntpChannelHandler extends SimpleChannelUpstreamHandler {

	private Logger logger = LoggerFactory.getLogger(GntpChannelHandler.class);

	private final NioGntpClient gntpClient;
	private final GntpListener listener;

	public GntpChannelHandler(NioGntpClient gntpClient, GntpListener listener) {
		this.gntpClient = gntpClient;
		this.listener = listener;
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.debug("Channel closed [{}]", e.getChannel());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		GntpMessageResponse message = (GntpMessageResponse) e.getMessage();
		Preconditions.checkState(message instanceof GntpOkMessage || message instanceof GntpCallbackMessage || message instanceof GntpErrorMessage);

		if (gntpClient.isRegistered()) {
			GntpNotification notification = (GntpNotification)gntpClient.getNotificationsSent().get(message.getInternalNotificationId());
			if (message instanceof GntpOkMessage) {
				try {
					if (listener != null) {
						listener.onNotificationSuccess(notification);
					}
				} finally {
					if (!notification.isCallbackRequested()) {
						gntpClient.getNotificationsSent().remove(message.getInternalNotificationId());
					}
				}
			} else if (message instanceof GntpCallbackMessage) {
				gntpClient.getNotificationsSent().remove(message.getInternalNotificationId());
				if (listener == null) {
					throw new IllegalStateException("A GntpListener must be set in GntpClient to be able to receive callbacks");
				}
				GntpCallbackMessage callbackMessage = (GntpCallbackMessage) message;
				switch (callbackMessage.getCallbackResult()) {
					case CLICK:
						listener.onClickCallback(notification);
						break;
					case CLOSE:
						listener.onCloseCallback(notification);
						break;
					case TIMEOUT:
						listener.onTimeoutCallback(notification);
						break;
					default:
						throw new IllegalStateException("Unknown callback result: " + callbackMessage.getCallbackResult());
				}
			} else if (message instanceof GntpErrorMessage) {
				GntpErrorMessage errorMessage = (GntpErrorMessage) message;
				if (listener != null) {
					listener.onNotificationError(notification, errorMessage.getStatus(), errorMessage.getDescription());
				}
				if ((GntpErrorStatus.UNKNOWN_APPLICATION == errorMessage.getStatus() ||
					GntpErrorStatus.UNKNOWN_NOTIFICATION == errorMessage.getStatus()) &&
					gntpClient.canRetry()) {
					gntpClient.register();
				}
			}
		} else {
			if (message instanceof GntpOkMessage) {
				try {
					if (listener != null) {
						listener.onRegistrationSuccess();
					}
				} finally {
					gntpClient.setRegistered();
				}
			} else if (message instanceof GntpErrorMessage) {
				GntpErrorMessage errorMessage = (GntpErrorMessage) message;
				if (listener != null) {
					listener.onRegistrationError(errorMessage.getStatus(), errorMessage.getDescription());
				}
				if (GntpErrorStatus.NOT_AUTHORIZED == errorMessage.getStatus()) {
					gntpClient.retryRegistration();
				}
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		try {
			if (listener == null) {
				logger.error("Error in GNTP I/O operation", e.getCause());
			} else {
				listener.onCommunicationError(e.getCause());
			}
			if (e.getCause() instanceof IOException && !gntpClient.isRegistered()) {
				gntpClient.retryRegistration();
			}
		} finally {
			e.getChannel().close();
		}
	}

}
