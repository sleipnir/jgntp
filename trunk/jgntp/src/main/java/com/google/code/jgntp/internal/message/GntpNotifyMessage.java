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
package com.google.code.jgntp.internal.message;

import java.io.*;

import com.google.code.jgntp.*;

public class GntpNotifyMessage extends GntpMessage {

	private final GntpNotification notification;
	private final long contextId;

	public GntpNotifyMessage(GntpNotification notification, long contextId, GntpPassword password) {
		super(GntpMessageType.NOTIFY, password);
		this.notification = notification;
		this.contextId = contextId;
	}

	@Override
	public void append(OutputStream output) throws IOException {
		GntpMessageWriter writer = getWriter(output);
		appendStatusLine(writer);
		appendSeparator(writer);

		writer.startHeaders();

		appendHeader(GntpMessageHeader.APPLICATION_NAME, notification.getApplicationName(), writer);
		appendSeparator(writer);

		appendHeader(GntpMessageHeader.NOTIFICATION_NAME, notification.getName(), writer);
		appendSeparator(writer);

		appendHeader(GntpMessageHeader.NOTIFICATION_TITLE, notification.getTitle(), writer);
		appendSeparator(writer);

		if (notification.getId() != null) {
			appendHeader(GntpMessageHeader.NOTIFICATION_ID, notification.getId(), writer);
			appendSeparator(writer);
		}

		if (notification.getText() != null) {
			appendHeader(GntpMessageHeader.NOTIFICATION_TEXT, notification.getText(), writer);
			appendSeparator(writer);
		}

		if (notification.isSticky() != null) {
			appendHeader(GntpMessageHeader.NOTIFICATION_STICKY, notification.isSticky(), writer);
			appendSeparator(writer);
		}

		if (notification.getPriority() != null) {
			appendHeader(GntpMessageHeader.NOTIFICATION_PRIORITY, notification.getPriority().getCode(), writer);
			appendSeparator(writer);
		}

		if (appendIcon(GntpMessageHeader.NOTIFICATION_ICON, notification.getIconImage(), notification.getIconUri(), writer)) {
			appendSeparator(writer);
		}

		if (notification.getCoalescingId() != null) {
			appendHeader(GntpMessageHeader.NOTIFICATION_COALESCING_ID, notification.getCoalescingId(), writer);
			appendSeparator(writer);
		}

		if (notification.getCallbackTarget() != null) {
			appendHeader(GntpMessageHeader.NOTIFICATION_CALLBACK_TARGET, notification.getCallbackTarget(), writer);
			appendSeparator(writer);
		}

		if (notification.isCallbackRequested()) {
			appendHeader(GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT, contextId, writer);
			appendSeparator(writer);

			appendHeader(GntpMessageHeader.NOTIFICATION_CALLBACK_CONTEXT_TYPE, "int", writer);
			appendSeparator(writer);
		}
		writer.finishHeaders();

		appendBinarySections(writer);
		clearBinarySections();
	}
}
