package com.jsi.alert.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.alert.model.notification.Notification;
import com.jsi.alert.service.UniversalService.RequestType;
import com.jsi.alert.utils.Configuration;
import com.jsi.alert.utils.MessageParser;

public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	
	/**
	 * Fetches notifications for the specified user.
	 * 
	 * @param uuid
	 * @return
	 */
	public static List<Notification> fetchNotifications(String uuid) {
		if (log.isDebugEnabled())
			log.debug("Fetching notifications for user with ID: " + uuid + "...");
		
		try {
			Map<String, String> params = new HashMap<>();
			params.put(Configuration.NOTIFICATION_PARAMETER, uuid);
			
			String response = UniversalService.fetchUrl(Configuration.NOTIFICATION_URL, params, MediaType.TEXT_XML, RequestType.GET);
			
			if (log.isDebugEnabled()) {
				log.debug("Notifications received...");
				if (Configuration.LOG_EVENTS)
					log.debug(response);
			}
			
			if (response != null) {
				return MessageParser.parseNotificationRSS(response);
			} else {
				log.warn("Failed to fetch notifications for user: " + uuid);
				return new ArrayList<>();
			}
		} catch (Throwable t) {
			log.error("An exception occurred while fetching notifications for user: " + uuid, t);
			return new ArrayList<>();
		}
	}
}
