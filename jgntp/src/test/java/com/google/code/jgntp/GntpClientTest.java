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

public class GntpClientTest {

	private static Logger logger = LoggerFactory.getLogger(GntpClientTest.class);

	@Test
	public void test() throws Exception {
		String iconUrl = "http://android-notifier-desktop.googlecode.com/svn/trunk/android-notifier-desktop/src/main/resources/com/google/code/notifier/desktop/icon.png";
		GntpApplicationInfo info = Gntp.appInfo("Test").icon(URI.create(iconUrl)).build();
		GntpNotificationInfo notif1 = Gntp.notificationInfo(info, "Notify 1").icon(ImageIO.read(getClass().getResourceAsStream("icon.png"))).build();

		GntpClient client = Gntp.client(info).withListener(new GntpListener() {
			@Override
			public void onRegistrationSuccess() {
				logger.info("Registered");
			}

			@Override
			public void onClickCallback(Object context) {
				logger.info("Click callback: " + context);
			}

			@Override
			public void onCloseCallback(Object context) {
				logger.info("Close callback: " + context);
			}

			@Override
			public void onTimeoutCallback(Object context) {
				logger.info("Timeout callback: " + context);
			}

			@Override
			public void onRegistrationError(GntpErrorStatus status, String description) {
				logger.info("Registration Error: " + status + " - desc: " + description);
			}

			@Override
			public void onNotificationError(GntpErrorStatus status, String description) {
				logger.info("Notification Error: " + status + " - desc: " + description);
			}

			@Override
			public void onCommunicationError(Throwable t) {
				logger.error("Communication error", t);
			}
		}).build();

		client.register();
		client.notify(Gntp.notification(notif1, "Title").text("Message").withCallback().context(12345).build(), 5, SECONDS);
		client.notify(Gntp.notification(notif1, "Title 2").text("Message 2").callbackTarget(URI.create("http://slashdot.org/")).build(), 5, SECONDS);
		SECONDS.sleep(5);
		client.shutdown(1, SECONDS);
	}
}
