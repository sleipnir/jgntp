package com.google.code.jgntp.internal.util;

public class Hex {

	private static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private Hex() {
	}

	public static String toHexadecimal(byte[] array) {
		if (array == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer(array.length * 2);
		for (int i = 0; i < array.length; i++) {
			int curByte = array[i] & 0xff;
			buffer.append(HEX_DIGITS[(curByte >> 4)]);
			buffer.append(HEX_DIGITS[curByte & 0xf]);
		}
		return buffer.toString();
	}

}
