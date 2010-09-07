package com.google.code.jgntp;

import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.junit.*;

import com.google.code.jgntp.internal.util.*;
import com.google.common.base.*;

public class GntpPasswordTest {

	@Test
	public void test() throws Exception {
		String password = "teste";
		byte[] array = password.getBytes(Charsets.UTF_8);
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(System.currentTimeMillis());
		byte[] salt = new byte[16];
		random.nextBytes(salt);

		byte[] keyBasis = new byte[array.length + salt.length];
		System.arraycopy(array, 0, keyBasis, 0, array.length);
		System.arraycopy(salt, 0, keyBasis, array.length, salt.length);

		MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
		byte[] key = messageDigest.digest(keyBasis);
		byte[] keyHash = messageDigest.digest(key);

		String keyHashHex = Hex.toHexadecimal(keyHash);

		System.out.println(keyHashHex);

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(new DESKeySpec(key));
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedPassword = cipher.doFinal(array);

		System.out.println(Arrays.toString(encryptedPassword));
		System.out.println(Hex.toHexadecimal(cipher.getIV()));
	}

}
