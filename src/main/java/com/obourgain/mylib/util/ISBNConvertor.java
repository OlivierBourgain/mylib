/*
 * Based on http://bajishaik.blogspot.fr/2011/01/isbn-10-to-isbn-13-conversion-in-java.html
 * By Baji Shaik
 */
package com.obourgain.mylib.util;

import org.apache.commons.lang3.StringUtils;

public class ISBNConvertor {

	public static boolean isISBN(String s) {
		if (StringUtils.isBlank(s)) return false;
		// I define an ISBN as a string which contain only digits, X, - or
		// spaces
		// and which length is between 10 and 15
		if (s.length() < 10 || s.length() > 15) return false;
		for (Character c : s.toCharArray()) {
			if (c != ' ' && c != '-' && (c < '0' || c > '9') && c != 'X' && c != 'x')
				return false;
		}
		return true;
	}

	/**
	 * Convert an ISBN 13 to an ISBN 10.
	 */
	public static String isbn13to10(String isbn) {
		if (StringUtils.isBlank(isbn)) return "";

		String s9 = clean(isbn).substring(3, 12);
		int n = 0;
		for (int i = 0; i < 9; i++) {
			int v = charToInt(s9.charAt(i));
			if (v == -1) return null;
			else n = n + (10 - i) * v;
		}
		n = 11 - (n % 11);
		return s9 + DIGITS.substring(n, n + 1);
	}

	/**
	 * Clean an ISBN from non numeric char.
	 */
	private static String clean(String isbn) {
		String res = "";
		for (Character c : isbn.toCharArray()) {
			if (c == 'X' || (c >= '0' && c <= '9')) res += c;
		}
		return res;
	}

	static int charToInt(char c) {
		if (c >= '0' && c <= '9') return c - '0';
		return -1;
	}

	private final static String DIGITS = "0123456789X0";

}
