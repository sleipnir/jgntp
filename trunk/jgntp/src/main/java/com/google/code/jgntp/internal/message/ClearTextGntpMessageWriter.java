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
import com.google.code.jgntp.internal.message.GntpMessage.*;
import com.google.code.jgntp.internal.util.*;

public class ClearTextGntpMessageWriter implements GntpMessageWriter {

	public static final String NONE_ENCRYPTION_ALGORITHM = "NONE";

	private OutputStream output;
	private OutputStreamWriter writer;
	private GntpPassword password;

	@Override
	public void prepare(OutputStream output, GntpPassword password) {
		this.output = output;
		this.writer = new OutputStreamWriter(output, GntpMessage.ENCODING);
		this.password = password;
	}

	@Override
	public void writeStatusLine(GntpMessageType type) throws IOException {
		writer.append(GntpMessage.PROTOCOL_ID).append('/').append(GntpVersion.ONE_DOT_ZERO.toString());
		writer.append(' ').append(type.toString());
		writer.append(' ').append(NONE_ENCRYPTION_ALGORITHM);

		if (password != null) {
			writer.append(' ').append(password.getKeyHashAlgorithm());
			writer.append(':').append(Hex.toHexadecimal(password.getKey()));
			writer.append('.').append(Hex.toHexadecimal(password.getSalt()));
		}
	}

	@Override
	public void startHeaders() throws IOException {
	}

	@Override
	public void writeHeaderLine(String line) throws IOException {
		writer.append(line);
	}

	@Override
	public void finishHeaders() throws IOException {
		writer.flush();
	}

	@Override
	public void writeBinarySection(BinarySection binarySection) throws IOException {
		writer.append(GntpMessage.BINARY_SECTION_ID).append(' ').append(binarySection.getId());
		writeSeparator();
		writer.append(GntpMessage.BINARY_SECTION_LENGTH).append(' ').append(Long.toString(binarySection.getData().length));
		writeSeparator();
		writeSeparator();
		writer.flush();

		output.write(binarySection.getData());
	}

	@Override
	public void writeSeparator() throws IOException {
		writer.append(GntpMessage.SEPARATOR);
	}
}
