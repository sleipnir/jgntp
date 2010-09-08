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
		assertArrayEquals(password.getKey(), expectedKey);

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
