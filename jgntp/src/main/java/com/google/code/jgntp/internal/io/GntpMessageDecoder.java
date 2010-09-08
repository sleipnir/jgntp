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

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.ChannelHandler.*;
import org.jboss.netty.handler.codec.oneone.*;
import org.slf4j.*;

import com.google.code.jgntp.internal.message.*;
import com.google.code.jgntp.internal.message.read.*;

@Sharable
public class GntpMessageDecoder extends OneToOneDecoder {

	private static final Logger logger = LoggerFactory.getLogger(GntpMessageDecoder.class);

	private final GntpMessageResponseParser parser;

	public GntpMessageDecoder() {
		parser = new GntpMessageResponseParser();
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer) msg;
		String s = new String(buffer.array(), GntpMessage.ENCODING);

		if (logger.isDebugEnabled()) {
			logger.debug("Message received\n{}", s);
		}

		return parser.parse(s);
	}

}
