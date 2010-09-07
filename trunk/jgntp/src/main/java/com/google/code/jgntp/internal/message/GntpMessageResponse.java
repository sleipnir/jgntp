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

public abstract class GntpMessageResponse extends GntpMessage {

	private final GntpMessageType respondingType;

	public GntpMessageResponse(GntpMessageType type, GntpMessageType respondingType) {
		super(type, null);
		this.respondingType = respondingType;
	}

	public GntpMessageType getRespondingType() {
		return respondingType;
	}

	@Override
	public void append(OutputStream output) throws IOException {
		throw new UnsupportedOperationException("This is a response message");
	}

}