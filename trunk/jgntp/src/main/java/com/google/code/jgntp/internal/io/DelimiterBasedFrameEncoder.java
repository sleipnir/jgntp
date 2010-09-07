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

import static org.jboss.netty.buffer.ChannelBuffers.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.ChannelHandler.*;
import org.jboss.netty.handler.codec.oneone.*;

@Sharable
public class DelimiterBasedFrameEncoder extends OneToOneEncoder {

	private ChannelBuffer delimiter;

	public DelimiterBasedFrameEncoder(ChannelBuffer delimiter) {
		this.delimiter = copiedBuffer(delimiter);
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}
		ChannelBuffer buf = copiedBuffer((ChannelBuffer) msg, delimiter);
		
/*		FileOutputStream fos = new FileOutputStream("dump.out");
		while (buf.readable()) {
			fos.write(buf.readByte());
		}
		fos.close();
		buf.resetReaderIndex();
*/		return buf;
	}

}
