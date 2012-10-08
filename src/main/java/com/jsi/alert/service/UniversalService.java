package com.jsi.alert.service;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class UniversalService {
	
	private static final Logger log = LoggerFactory.getLogger(UniversalService.class);
	private static Client client = Client.create(new DefaultClientConfig());
	
	enum RequestType {
		GET,
		POST
	}
	
	/**
	 * Fetches a response from
	 * 
	 * @param url
	 * @param params
	 * @param mediaType
	 * @return
	 */
	public static String fetchUrl(String url, Map<String, String> params, String mediaType, RequestType requestType) {
		if (log.isDebugEnabled())
			log.debug("Fetching web resource: [url: " + url + ", params: " + params.toString() + ", mediaType: " + mediaType + "]");
		
		// build parameters
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		for (String key : params.keySet())
			formParams.add(key, params.get(key));
		
		Builder builder = client.resource(url).queryParams(formParams).accept(mediaType);

		// get response
		ClientResponse response;
		switch(requestType) {
		case GET:
			response = builder.get(ClientResponse.class);
			break;
		case POST:
			response = builder.post(ClientResponse.class);
			break;
		default:
			log.warn("Unknown request type: " + requestType);
			return null;
		}
		
		if (response.getStatus() == 200) {
			// success, now parse the response
			String responseStr = response.getEntity(String.class);
			
			if (log.isDebugEnabled())
				log.debug("Received response:\n" + responseStr);
			
			return responseStr;
		} else {
			log.warn("Failed to fetch resource, status code: " + response.getStatus());
			return null;
		}
	}
}
