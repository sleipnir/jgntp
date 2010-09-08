package com.google.code.jgntp.internal;

import static org.junit.Assert.*;

import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.junit.*;

import com.google.code.jgntp.*;
import com.google.code.jgntp.internal.util.*;

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
		
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(new DESKeySpec(password.getKey()));
		IvParameterSpec iv = new IvParameterSpec(secretKey.getEncoded());

		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

		System.out.println(Arrays.toString(cipher.getIV()));
	}
}
