package com.jsi.alert.utils;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
	
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);
	
	public static final String USER_PRINCIPAL = "user";
	
	public static String ACTIVEMQ_URL;
	public static String KEUI_REQUEST_TOPIC, KEUI_RESPONSE_TOPIC;
	public static String API_REQUEST_TOPIC, API_RESPONSE_TOPIC;
	
	public static boolean LOG_EVENTS;
	
	public static String LOGIN_URL, LOGOUT_URL, AUTHENTICATE_URL;
	public static String NOTIFICATION_URL, NOTIFICATION_PARAMETER, NOTIFICATION_DEFAULT_USER;
	
	public static long REQUEST_TIMEOUT;

	static {
		// read the properties
		if (log.isDebugEnabled()) log.debug("Reading properties file...");
		
		Properties props = new Properties();
		try {
			props.load(Configuration.class.getClassLoader().getResourceAsStream("alert.properties"));

			ACTIVEMQ_URL = props.getProperty("activemq.url");
			KEUI_REQUEST_TOPIC = props.getProperty("topic.keui.request");
			KEUI_RESPONSE_TOPIC = props.getProperty("topic.keui.response");
			API_REQUEST_TOPIC = props.getProperty("topic.api.request");
			API_RESPONSE_TOPIC = props.getProperty("topic.api.response");
			
			LOG_EVENTS = Boolean.parseBoolean(props.getProperty("log_events"));
			
			LOGIN_URL = props.getProperty("login.form.url");
			LOGOUT_URL = props.getProperty("logout.form.url");
			AUTHENTICATE_URL = props.getProperty("login.authenticate.url");
			
			NOTIFICATION_URL = props.getProperty("notifications.url");
			NOTIFICATION_PARAMETER = props.getProperty("notifications.param");
			NOTIFICATION_DEFAULT_USER = props.containsKey("notifications.param.value") ? props.getProperty("notifications.param.value") : null;
		
			REQUEST_TIMEOUT = props.containsKey("request.timeout") ? Long.parseLong((String) props.get("request.timeout")) : 10000;
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
