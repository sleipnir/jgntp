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

import javax.imageio.*;

import org.junit.*;
import org.slf4j.*;

import static java.util.concurrent.TimeUnit.*;

public class GntpClientIntegrationTest {

	private static Logger logger = LoggerFactory.getLogger(GntpClientIntegrationTest.class);

	@Test
	public void test() throws Exception {
		GntpApplicationInfo info = Gntp.appInfo("Test").icon(ImageIO.read(getClass().getResourceAsStream("icon.png"))).build();
		GntpNotificationInfo notif1 = Gntp.notificationInfo(info, "Notify 1").icon(ImageIO.read(getClass().getResourceAsStream("icon.png"))).build();
		GntpNotificationInfo notif2 = Gntp.notificationInfo(info, "Notify 2").icon(ImageIO.read(getClass().getResourceAsStream("sms.png"))).build();

		GntpClient client = Gntp.client(info).withPassword("test").listener(new GntpListener() {
			@Override
			public void onRegistrationSuccess() {
				logger.info("Registered");
			}

			@Override
			public void onNotificationSuccess(GntpNotification notification) {
				logger.info("Notification success: " + notification);
			}

			@Override
			public void onClickCallback(GntpNotification notification) {
				logger.info("Click callback: " + notification.getContext());
			}

			@Override
			public void onCloseCallback(GntpNotification notification) {
				logger.info("Close callback: " + notification.getContext());
			}

			@Override
			public void onTimeoutCallback(GntpNotification notification) {
				logger.info("Timeout callback: " + notification.getContext());
			}

			@Override
			public void onRegistrationError(GntpErrorStatus status, String description) {
				logger.info("Registration Error: " + status + " - desc: " + description);
			}

			@Override
			public void onNotificationError(GntpNotification notification, GntpErrorStatus status, String description) {
				logger.info("Notification Error: " + status + " - desc: " + description);
			}

			@Override
			public void onCommunicationError(Throwable t) {
				logger.error("Communication error", t);
			}
		}).build();

		client.register();
		client.notify(Gntp.notification(notif1, "Title")
						.text("Message")
						.withCallback()
						.context(12345)
						.header(Gntp.APP_SPECIFIC_HEADER_PREFIX + "Filename", "file.txt")
						.build(), 5, SECONDS);

		client.notify(Gntp.notification(notif2, "Title 2")
						.text("Message 2")
						.icon(ImageIO.read(getClass().getResourceAsStream("icon.png")))
						.callbackTarget(URI.create("http://slashdot.org/"))
						.header(Gntp.CUSTOM_HEADER_PREFIX + "Payload", getClass().getResourceAsStream("sms.png"))
						.build(), 5, SECONDS);

		SECONDS.sleep(5);
		client.shutdown(50, SECONDS);
	}
}
