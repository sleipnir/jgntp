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

import java.io.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.ChannelHandler.*;
import org.slf4j.*;

import com.google.code.jgntp.internal.message.*;

@Sharable
public class GntpMessageEncoder extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(GntpMessageEncoder.class);

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		GntpMessage message = (GntpMessage) e.getMessage();

		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		message.append(new ChannelBufferOutputStream(buffer));

		if (logger.isDebugEnabled()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			message.append(baos);
			logger.debug("Sending message\n{}", new String(baos.toByteArray(), GntpMessage.ENCODING));
		}

		Channels.write(ctx, e.getFuture(), buffer);
	}

}
