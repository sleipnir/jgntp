package com.google.code.jgntp.internal;

import java.security.*;
import java.util.*;

import com.google.code.jgntp.*;
import com.google.common.base.*;

public class GntpPasswordDefaultImpl implements GntpPassword {

	public static final String DEFAULT_RANDOM_SALT_ALGORITHM = "SHA1PRNG";
	public static final int DEFAULT_SALT_SIZE = 16;
	public static final String DEFAULT_KEY_HASH_ALGORITHM = "SHA-512";

	private final byte[] key;
	private final byte[] salt;

	public GntpPasswordDefaultImpl(String textPassword) {
		byte[] passwordBytes = textPassword.getBytes(Charsets.UTF_8);
		salt = getSalt(DEFAULT_RANDOM_SALT_ALGORITHM);
		byte[] keyBasis = getKeyBasis(passwordBytes, salt);
		key = getHashedKey(keyBasis, DEFAULT_KEY_HASH_ALGORITHM);
	}

	@Override
	public byte[] getKey() {
		return Arrays.copyOf(key, key.length);
	}

	@Override
	public String getKeyHashAlgorithm() {
		return DEFAULT_KEY_HASH_ALGORITHM.replaceAll("-", "");
	}

	@Override
	public byte[] getSalt() {
		return Arrays.copyOf(salt, salt.length);
	}

	protected byte[] getSalt(String randomSaltAlgorithm) {
		SecureRandom random;
		try {
			random = SecureRandom.getInstance(randomSaltAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		random.setSeed(System.currentTimeMillis());
		byte[] salt = new byte[DEFAULT_SALT_SIZE];
		random.nextBytes(salt);

		return salt;
	}

	protected byte[] getKeyBasis(byte[] password, byte[] salt) {
		byte[] keyBasis = new byte[password.length + salt.length];
		System.arraycopy(password, 0, keyBasis, 0, password.length);
		System.arraycopy(salt, 0, keyBasis, password.length, salt.length);
		return keyBasis;
	}

	protected byte[] getHashedKey(byte[] keyBasis, String hashAlgorithm) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
			byte[] key = messageDigest.digest(keyBasis);
			return messageDigest.digest(key);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
