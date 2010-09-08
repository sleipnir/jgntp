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
package com.google.code.jgntp.internal;

import static org.junit.Assert.*;

import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.junit.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.util.*;
import com.google.common.base.*;

public class GntpPasswordDefaultImplTest {

	@Test
	public void testKeyGeneration() throws Exception {
		GntpPassword password = new GntpPasswordDefaultImpl("test") {
			@Override
			protected byte[] getSalt(String randomSaltAlgorithm) {
				return Hex.fromHexadecimal("26EFBFBD0EEFBFBD206359");
			}
		};
		byte[] expectedKey = Hex.fromHexadecimal("FC164AC0B12DFAF6D796C61BE16542A1108FA442997FDC447D569C03820605113D23C4AE42387FE6060EE1ED4F6872BC30F67A63C2CE4F02695B061631230815");
		assertArrayEquals(password.getKeyHash(), expectedKey);

		byte[] data = "test".getBytes(Charsets.UTF_8);

		byte[] keyToEncrypt = Arrays.copyOf(password.getKey(), 8);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(new DESKeySpec(keyToEncrypt));
		IvParameterSpec iv = new IvParameterSpec(keyToEncrypt);

		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		byte[] ciphered = cipher.doFinal(data);

		Cipher decipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		decipher.init(Cipher.DECRYPT_MODE, keyFactory.generateSecret(new DESKeySpec(keyToEncrypt)), iv);
		byte[] deciphered = decipher.doFinal(ciphered);
		
		System.out.println(new String(deciphered, Charsets.UTF_8));
	}
}
