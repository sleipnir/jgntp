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
import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.jboss.netty.buffer.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.message.GntpMessage.*;
import com.google.code.jgntp.internal.util.*;

/**
 * Growl keeps returning "Bad data" for encrypted messages.
 * Maybe JGNTP will support message encryption in the future. 
 */
public class EncryptedGntpMessageWriter implements GntpMessageWriter {

	public static final String DEFAULT_ALGORITHM = "DES";
	public static final String DEFAULT_TRANSFORMATION = "DES/CBC/PKCS5Padding";

	private OutputStream output;
	private OutputStreamWriter writer;
	private GntpPassword password;

	private Cipher cipher;
	private SecretKey secretKey;
	private IvParameterSpec iv;
	private ChannelBuffer buffer;

	@Override
	public void prepare(OutputStream output, GntpPassword password) {
		this.output = output;
		this.buffer = ChannelBuffers.dynamicBuffer();
		this.writer = new OutputStreamWriter(output, GntpMessage.ENCODING);
		this.password = password;

		try {
			byte[] keyToEncrypt = Arrays.copyOf(password.getKey(), 8);

			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_ALGORITHM);
			secretKey = keyFactory.generateSecret(new DESKeySpec(keyToEncrypt));
			iv = new IvParameterSpec(keyToEncrypt);

			cipher = Cipher.getInstance(DEFAULT_TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeStatusLine(GntpMessageType type) throws IOException {
		writer.append(GntpMessage.PROTOCOL_ID).append('/').append(GntpVersion.ONE_DOT_ZERO.toString());
		writer.append(' ').append(type.toString());
		writer.append(' ').append(DEFAULT_ALGORITHM).append(':').append(Hex.toHexadecimal(iv.getIV()));

		if (password != null) {
			writer.append(' ').append(password.getKeyHashAlgorithm());
			writer.append(':').append(Hex.toHexadecimal(password.getKey()));
			writer.append('.').append(Hex.toHexadecimal(password.getSalt()));
		}
		writer.flush();
	}

	@Override
	public void startHeaders() throws IOException {
		writer.flush();
		writer = new OutputStreamWriter(new ChannelBufferOutputStream(buffer), GntpMessage.ENCODING);
	}

	@Override
	public void writeHeaderLine(String line) throws IOException {
		writer.append(line);
	}

	@Override
	public void finishHeaders() throws IOException {
		writer.flush();
		byte[] headerData = new byte[buffer.readableBytes()];
		buffer.getBytes(0, headerData);
		byte[] encryptedHeaderData = encrypt(headerData);
		output.write(encryptedHeaderData);
		
		writer = new OutputStreamWriter(output, GntpMessage.ENCODING);
		writeSeparator();
		writeSeparator();
	}

	@Override
	public void writeBinarySection(BinarySection binarySection) throws IOException {
		byte[] encryptedData = encrypt(binarySection.getData());

		writer.append(GntpMessage.BINARY_SECTION_ID).append(' ').append(binarySection.getId());
		writeSeparator();
		writer.append(GntpMessage.BINARY_SECTION_LENGTH).append(' ').append(Long.toString(encryptedData.length));
		writeSeparator();
		writeSeparator();
		writer.flush();

		output.write(encryptedData);
	}

	@Override
	public void writeSeparator() throws IOException {
		writer.append(GntpMessage.SEPARATOR);
	}

	protected byte[] encrypt(byte[] data) {
		try {
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
