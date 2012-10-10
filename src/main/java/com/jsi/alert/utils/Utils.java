package com.jsi.alert.utils;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * A utility class with various static methods.
 * 
 * @author Luka Stopar
 *
 */
public class Utils {
	
	private static final Random rand = new Random();
	
	private static final String CLIENT_DATE_FORMAT = "yyyy-MM-dd";
	private static final String MESSAGE_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
	
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
	
	/**
	 * Returns a <code>String</code> representation of the list, where the items are comma separated.
	 * 
	 * @param list
	 * @return
	 */
	public static String toCommaSepStr(List<String> list) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<String> it = list.iterator(); it.hasNext();) {
			builder.append(it.next());
			if (it.hasNext())
				builder.append(",");
		}
		return builder.toString();
	}
	
	/**
	 * Extracts the file name from the path.
	 * 
	 * @param fullPath
	 * @return
	 */
	public static String getFileName(String fullPath) {
		int slashIdx = fullPath.lastIndexOf("/");
		if (slashIdx >= 0)
			return fullPath.substring(slashIdx + 1);
		else {
			int backSlashIdx = fullPath.lastIndexOf("\\");
			return backSlashIdx < 0 ? fullPath : fullPath.substring(backSlashIdx);
		}
	}
	
	/**
	 * Extracts the path to the file.
	 * 
	 * @param fullPath
	 * @return
	 */
	public static String getFilePath(String fullPath) {
		return fullPath.replaceAll(getFileName(fullPath), "");
	}

	public static long toWindowsTime(long javaTime) {
		return javaTime + 11644473600000L;
	}
	
	/*public static long toJavaTime(long windowsTime) {
		return windowsTime - 11644473600000L;
	}*/
	
	/**
	 * Returns an instance of <code>SimpleDateFormat</code> with format used on the 
	 * client side.
	 * 
	 * @return
	 */
	public static SimpleDateFormat getClientDateFormat() {
		SimpleDateFormat format = new SimpleDateFormat(CLIENT_DATE_FORMAT);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format;
	}
	
	/**
	 * Returns an instance of <code>SimpleDateFormat</code> with format used in the messages.
	 * 
	 * @return
	 */
	public static SimpleDateFormat getMessageDateFormat() {
		SimpleDateFormat format = new SimpleDateFormat(MESSAGE_DATE_FORMAT, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format;
	}
}
