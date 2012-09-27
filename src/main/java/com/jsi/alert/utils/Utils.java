package com.jsi.alert.utils;

import java.util.Random;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * A utility class with various static methods.
 * 
 * @author Luka Stopar
 *
 */
public class Utils {
	
	private static final Random rand = new Random();
	
	/**
	 * Generates and returns a request id.
	 */
	public static String genRequestID() {
		return System.nanoTime() + "" + rand.nextInt(10000);
	}
	
	/**
	 * Returns true if the parameter represents an integer.
	 */
	public static Long parseLong(String number) {
		try {
			return Long.parseLong(number);
		} catch (NumberFormatException nx) {
			return null;
		}
	}
	
	public static boolean parseBoolean(String bool) {
		try {
			return Boolean.parseBoolean(bool);
		} catch (Throwable t) {
			return false;
		}
	}
	
	/**
	 * Escapes <code>String</code> to HTML.
	 */
	public static String escapeHtml(String input) {
		return StringEscapeUtils.escapeHtml4(input).replaceAll("\n", "<br />");
	}
}
