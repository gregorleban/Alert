package com.jsi.alert.utils;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.alert.model.AlertUser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * A utilities class which contacts a web service to fetch the logged in users data
 * and see if they are authenticated.
 */
public class UserAuthenticator {
	
	private static final Logger log = LoggerFactory.getLogger(UserAuthenticator.class);
	private static Client client = Client.create(new DefaultClientConfig());
	
	/**
	 * Contacts the web service to see, if the user is authenticated.
	 * 
	 * @param email
	 * @return
	 */
	private static AlertUser getUserInfo(String email) {
		if (log.isDebugEnabled())
			log.debug("Fetching info of user: " + email);
		
		WebResource service = client.resource(Configuration.AUTHENTICATE_URL);
		Builder builder = service.accept(MediaType.APPLICATION_JSON);

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("email", email);
		
		ClientResponse response = builder.post(ClientResponse.class, formData);
		
		if (response.getStatus() == 200) {
			// success, now parse the response
			String responseStr = response.getEntity(String.class);
			
			if (log.isDebugEnabled())
				log.debug("Received user info: " + responseStr);
			
			JSONObject resultJSon = (JSONObject) JSONValue.parse(responseStr);
			JSONObject userJson = (JSONObject) resultJSon.get("authenticationInfo");
			
			boolean isAuthenticated = (boolean) userJson.get("authenticated");
			if (isAuthenticated) {
				AlertUser user = new AlertUser();
				user.setEmail((String) userJson.get("email"));
				user.setUuid((String) userJson.get("uuid"));
				user.setAdmin((Boolean) userJson.get("admin"));
				
				return user;
			}
			return null;
		} else {
			log.error("Failed to contact web service for user: " + email + ", error code " + response.getStatus());
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
		AlertUser user = session.getAttribute(Configuration.USER_PRINCIPAL) != null ? (AlertUser) session.getAttribute(Configuration.USER_PRINCIPAL) : null;
		if (user == null) return false;
		
		String email = user.getEmail();
		if (log.isDebugEnabled()) log.debug("Authenticating user: " + email + "...");
		AlertUser userInfo = getUserInfo(email);
		
		if (userInfo == null) session.removeAttribute(Configuration.USER_PRINCIPAL);
		else session.setAttribute(Configuration.USER_PRINCIPAL, userInfo);
		
		return userInfo != null;
	}
}
