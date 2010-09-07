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

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.imageio.*;

import com.google.code.jgntp.internal.util.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.*;

public abstract class GntpMessage {

	public static final String PROTOCOL_ID = "GNTP";
	public static final String SEPARATOR = "\r\n";
	public static final char HEADER_SEPARATOR = ':';

	public static final String ENCRYPTION_ALGORITHM = "NONE";
	public static final String BINARY_HASH_FUNCTION = "MD5";
	public static final Charset ENCODING = Charsets.UTF_8;
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DATE_TIME_FORMAT_ALTERNATE = "yyyy-MM-dd HH:mm:ss'Z'";
	public static final String IMAGE_FORMAT = "png";

	public static final String BINARY_SECTION_ID = "Identifier:";
	public static final String BINARY_SECTION_LENGTH = "Length:";

	private final GntpVersion version;
	private final GntpMessageType type;
	private final Map<String, String> headers;
	private final List<BinarySection> binarySections;
	private final DateFormat dateFormat;

	public GntpMessage(GntpMessageType type) {
		this(GntpVersion.ONE_DOT_ZERO, type);
	}

	public GntpMessage(GntpVersion version, GntpMessageType type) {
		this.version = version;
		this.type = type;
		headers = Maps.newHashMap();
		binarySections = Lists.newArrayList();
		dateFormat = new SimpleDateFormat();
	}

	public abstract void append(OutputStream output) throws IOException;

	public GntpId addBinary(InputSupplier<? extends InputStream> input) throws IOException {
		BinarySection binarySection = new BinarySection(input);
		binarySections.add(binarySection);

		return GntpId.of(binarySection.getId());
	}

	public void appendStatusLine(OutputStreamWriter writer) throws IOException {
		writer.append(PROTOCOL_ID).append('/').append(version.toString());
		writer.append(' ').append(type.toString());
		writer.append(' ').append(ENCRYPTION_ALGORITHM);
	}

	public void appendHeader(GntpMessageHeader header, Object value, OutputStreamWriter writer) throws IOException {
		writer.append(header.toString()).append(HEADER_SEPARATOR).append(' ');
		if (value != null) {
			if (value instanceof String) {
				String s = (String) value;
				s = s.replaceAll("\r\n", "\n");
				writer.append(s);
			} else if (value instanceof Number) {
				writer.append(((Number) value).toString());
			} else if (value instanceof Boolean) {
				String s = ((Boolean) value).toString();
				s = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, s);
				writer.append(s);
			} else if (value instanceof Date) {
				String s = dateFormat.format((Date) value);
				writer.append(s);
			} else if (value instanceof URI) {
				writer.append(((URI) value).toString());
			} else if (value instanceof GntpId) {
				writer.append(value.toString());
			} else {
				throw new IllegalArgumentException("Value of header [" + header + "] not supported: " + value);
			}
		}
	}

	public boolean appendIcon(GntpMessageHeader header, RenderedImage image, URI uri, OutputStreamWriter writer) throws IOException {
		if (image == null && uri == null) {
			return false;
		}

		if (image == null) {
			appendHeader(header, uri, writer);
		} else {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (!ImageIO.write(image, IMAGE_FORMAT, output)) {
				throw new IllegalStateException("Could not read icon data");
			}
			GntpId id = addBinary(ByteStreams.newInputStreamSupplier(output.toByteArray()));
			appendHeader(header, id, writer);
		}
		return true;
	}

	public void appendBinarySections(OutputStream output) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(output, ENCODING);
		for (Iterator<BinarySection> iter = binarySections.iterator(); iter.hasNext(); ) {
			BinarySection binarySection = iter.next();
			binarySection.append(output, writer);
			if (iter.hasNext()) {
				appendSeparator(writer);
				appendSeparator(writer);
			}
		}
		writer.flush();
	}

	public void appendSeparator(OutputStreamWriter writer) throws IOException {
		writer.append(SEPARATOR);
	}

	public void clearBinarySections() {
		binarySections.clear();
	}

	public void putHeaders(Map<String, String> map) {
		headers.putAll(map);
	}

	public Map<String, String> getHeaders() {
		return ImmutableMap.copyOf(headers);
	}

	public static class BinarySection {
		private final String id;
		private final byte[] data;

		public BinarySection(InputSupplier<? extends InputStream> input) throws IOException {
			data = ByteStreams.toByteArray(input.getInput());
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance(BINARY_HASH_FUNCTION);
				digest.update(data);
				byte[] digested = digest.digest();
				id = Base64.encodeBytes(digested);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public void append(OutputStream output, OutputStreamWriter writer) throws IOException {
			writer.append(BINARY_SECTION_ID).append(' ').append(id);
			writer.append(SEPARATOR);
			writer.append(BINARY_SECTION_LENGTH).append(' ').append(Long.toString(data.length));
			writer.append(SEPARATOR).append(SEPARATOR);
			writer.flush();

			output.write(data);
		}

		public String getId() {
			return id;
		}

		public byte[] getData() {
			return data;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (id == null ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			BinarySection other = (BinarySection) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}
	}
}
