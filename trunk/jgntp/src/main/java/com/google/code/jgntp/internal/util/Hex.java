package com.google.code.jgntp.internal.util;

import com.google.common.base.*;

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

	public static byte[] fromHexadecimal(String s) {
		if (s == null) {
			return null;
		}
		Preconditions.checkArgument(s.length() % 2 == 0, "Invalid hex string [%s]", s);
		byte[] result = new byte[s.length() / 2];
		for (int i = 0; i < s.length(); i = i + 2) {
			int first = Character.digit(s.charAt(i), 16);
			int second = Character.digit(s.charAt(i + 1), 16);
			result[i / 2] = (byte) (0x0 + ((first & 0xff) << 4) + (second & 0xff));
		}
		return result;
	}

}
