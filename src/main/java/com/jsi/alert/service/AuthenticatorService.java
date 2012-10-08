package com.jsi.alert.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.alert.model.UserPrincipal;
import com.jsi.alert.service.UniversalService.RequestType;
import com.jsi.alert.utils.Configuration;

/**
 * A utilities class which contacts a web service to fetch the logged in users data
 * and see if they are authenticated.
 */
public class AuthenticatorService {
	
	private static final Logger log = LoggerFactory.getLogger(AuthenticatorService.class);
	
	
	/**
	 * Contacts the web service to see, if the user is authenticated.
	 * 
	 * @param email
	 * @return
	 */
	private static UserPrincipal getUserInfo(String email) {
		if (log.isDebugEnabled())
			log.debug("Fetching info of user: " + email + "...");
		
		Map<String, String> params = new HashMap<>();
		params.put("email", email);
		
		String response = UniversalService.fetchUrl(Configuration.AUTHENTICATE_URL, params, MediaType.APPLICATION_JSON, RequestType.POST);
		
		if (response != null) {
			if (log.isDebugEnabled())
				log.debug("Received user info: " + response);
			
			JSONObject resultJSon = (JSONObject) JSONValue.parse(response);
			JSONObject userJson = (JSONObject) resultJSon.get("authenticationInfo");
			
			boolean isAuthenticated = (boolean) userJson.get("authenticated");
			if (isAuthenticated) {
				UserPrincipal user = new UserPrincipal();
				user.setEmail((String) userJson.get("email"));
				user.setUuid((String) userJson.get("uuid"));
				user.setAdmin((Boolean) userJson.get("admin"));
				
				return user;
			}
			return null;
		} else {
			log.error("Failed to contact web service for user: " + email + "!");
			return null;
		}
	}
	
	/**
	 * Checks weather the user is authenticated, if so, it puts the user into the session,
	 * otherwise it removed the user from the session.
	 * 
	 * @param session
	 * @return
	 */
	public static boolean authenticateUser(HttpSession session) {
		UserPrincipal user = session.getAttribute(Configuration.USER_PRINCIPAL) != null ? (UserPrincipal) session.getAttribute(Configuration.USER_PRINCIPAL) : null;
		if (user == null) return false;
		
		String email = user.getEmail();
		if (log.isDebugEnabled()) log.debug("Authenticating user: " + email + "...");
		UserPrincipal userInfo = getUserInfo(email);
		
		if (userInfo == null) session.removeAttribute(Configuration.USER_PRINCIPAL);
		else session.setAttribute(Configuration.USER_PRINCIPAL, userInfo);
		
		return userInfo != null;
	}
}
