package com.jsi.alert.utils;

public class LoginUtils {
	
	private int count;
	
	private static LoginUtils instance = new LoginUtils();
	
	public static synchronized LoginUtils getInstance() {
		return instance;
	}
	
	private LoginUtils() {
		count = 0;
	};
	
	public boolean isAdmin() {
		return count++ % 2 == 0;
	}
}
