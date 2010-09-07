package com.google.code.jgntp;

import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.junit.*;

import com.google.common.base.*;

public class GntpPasswordTest {

	 private static char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
 

	@Test
	public void test() throws Exception {
		String password = "teste";
		byte[] array = password.getBytes(Charsets.UTF_8);
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(System.currentTimeMillis());
		byte[] salt = new byte[16];
		salt = fromHexadecimal("3A2EEFBFBD297C33EFBFBD");
		//random.nextBytes(salt);

		byte[] keyBasis = new byte[array.length + salt.length];
		System.arraycopy(array, 0, keyBasis, 0, array.length);
		System.arraycopy(salt, 0, keyBasis, array.length, salt.length);
		
		MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
		byte[] key = messageDigest.digest(keyBasis);
		byte[] keyHash = messageDigest.digest(key);
		
		String keyHashHex = toHexadecimal(keyHash);

		System.out.println(keyHashHex);
		
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(new DESKeySpec(key));
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedPassword = cipher.doFinal(array);
		
		System.out.println(Arrays.toString(encryptedPassword));
		System.out.println(toHexadecimal(cipher.getIV()));
	}
	
    public static String toHexadecimal(byte[] message) {
        if (message == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < message.length; i++) {
            int curByte = message[i] & 0xff;
            buffer.append(hexDigits[(curByte >> 4)]);
            buffer.append(hexDigits[curByte & 0xf]);
        }
        return buffer.toString();
    }
    
    
    public static byte[] fromHexadecimal(String message) throws Exception {
        if (message == null) {
            return null;
        }
        if ((message.length() % 2) != 0) {
            throw new Exception();
        }
        try {
            byte[] result = new byte[message.length() / 2];
            for (int i = 0; i < message.length(); i = i + 2) {
                int first = Integer.parseInt("" + message.charAt(i), 16);
                int second = Integer.parseInt("" + message.charAt(i + 1), 16);
                result[i/2] = (byte) (0x0 + ((first & 0xff) << 4) + (second & 0xff));
            }
            return result;
        } catch (Exception e) {
            throw new Exception();
        }
    }

}
