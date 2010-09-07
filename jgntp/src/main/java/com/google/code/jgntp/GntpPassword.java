package com.google.code.jgntp;

public interface GntpPassword {

	byte[] getKey();
	String getKeyHashAlgorithm();
	byte[] getSalt();

}
