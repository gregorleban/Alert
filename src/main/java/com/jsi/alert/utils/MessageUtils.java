package com.jsi.alert.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;


/**
 * A class which generates messages which are ment to be sent to the Message Broker. 
 *
 */
public class MessageUtils {
	
	private static final Logger log = LoggerFactory.getLogger(MessageUtils.class);
	
	private static final String KEUI_ITEM_SNIP_LEN = "200";
	
	private static final String[] keuiIgnoreKeys = {
		"issuesChk",
		"commitsChk",
		"forumsChk",
		"mailsChk",
		"wikisChk"
	};
	
	private static final Set<String> availableResolutions = new HashSet<>(Arrays.asList(new String[] {
			"None",
			"Fixed",
			"WontFix",
			"Invalid",
			"Duplicate",
			"WorksForMe",
			"Unknown"
	}));
	private static final Set<String> availableStatuses = new HashSet<>(Arrays.asList(new String[] {
			"Open",
			"Verified",
			"Assigned",
			"Resolved",
			"Closed"
	}));

	private static MessageFactory msgFactory;
	
	static {
		try {
			msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		} catch (SOAPException e) {
			log.error("Failed to initialize MessageFactory!", e);
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Generates a SOAPMessage with the envelope prefix set to 's' and 2 new namespaces added. The last child element
	 * added is 'eventData'
	 * 
	 * @throws SOAPException 
	 * @throws DOMException 
	 */
	private static SOAPMessage getMsgTemplate(String address, String eventName, String requestId) throws DOMException, SOAPException {
		// set the namespaces
		SOAPMessage soapMsg = msgFactory.createMessage();
		soapMsg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
		soapMsg.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "utf-8");
		
		SOAPPart soapPart = soapMsg.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		
		envelope.removeNamespaceDeclaration("env");
		envelope.setPrefix("s");
		envelope.getHeader().setPrefix("s");
		envelope.getBody().setPrefix("s");
		
		envelope.addNamespaceDeclaration("wsnt", "http://docs.oasis-open.org/wsn/b-2");
		envelope.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
		
		// construct the body
		SOAPBody soapBody = soapMsg.getSOAPBody();
		SOAPElement notify = soapBody.addChildElement("Notify", "wsnt");
		SOAPElement notificationMsg = notify.addChildElement("NotificationMessage", "wsnt");
		notificationMsg.addChildElement("Topic", "wsnt");
		notificationMsg.addChildElement("ProducerReference", "wsnt").addChildElement("Address", "wsa").setTextContent(address);
		SOAPElement msgEl = notificationMsg.addChildElement("Message", "wsnt");
		
		// construct the event
		SOAPElement event = msgEl.addChildElement("event", "ns1", "http://www.alert-project.eu/");
		event.addNamespaceDeclaration("o", "http://www.alert-project.eu/ontoevents-mdservice");
		event.addNamespaceDeclaration("r", "http://www.alert-project.eu/rawevents-forum");
		event.addNamespaceDeclaration("r1", "http://www.alert-project.eu/rawevents-mailinglist");
		event.addNamespaceDeclaration("r2", "http://www.alert-project.eu/rawevents-wiki");
		event.addNamespaceDeclaration("s", "http://www.alert-project.eu/strevents-kesi");
		event.addNamespaceDeclaration("s1", "http://www.alert-project.eu/strevents-keui");
		event.addNamespaceDeclaration("s2", "http://www.alert-project.eu/APIcall-request");
		event.addNamespaceDeclaration("s3", "http://www.alert-project.eu/APIcall-response");
		event.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		event.addNamespaceDeclaration("schemaLocation", "http://www.alert-project.eu/alert-root.xsd");
		
		// head
		SOAPElement head = event.addChildElement("head", "ns1");
		head.addChildElement("sender", "ns1").setTextContent("Alert-UI");
		head.addChildElement("sequencenumber", "ns1").setTextContent("1");
		head.addChildElement("timestamp", "ns1").setTextContent(System.currentTimeMillis() + "");
		
		// payload
		SOAPElement payload = event.addChildElement("payload", "ns1");
		// meta
		SOAPElement meta = payload.addChildElement("meta", "ns1");
		meta.addChildElement("eventName", "ns1").setTextContent(eventName);
		meta.addChildElement("eventType", "ns1").setTextContent("request");
		meta.addChildElement("eventId", "ns1").setTextContent(requestId);
		
		// event data
		payload.addChildElement("eventData", "ns1");
		
		return soapMsg;
	}
	
	private static SOAPMessage getAPITemplate(String apiCall, String requestId) throws DOMException, SOAPException {
		SOAPMessage msg = getMsgTemplate("http://www.alert-project.eu/search", "ALERT.Search.APICallRequest", requestId);
		
		SOAPBody body = msg.getSOAPBody();
		SOAPElement eventData = (SOAPElement) body.getElementsByTagName("ns1:eventData").item(0);
		
		SOAPElement requestEl = eventData.addChildElement("apiRequest", "ns1");
		requestEl.addChildElement("apiCall", "s2").setTextContent(apiCall);
		SOAPElement dataEl = requestEl.addChildElement("requestData", "s2");
		
		dataEl.addChildElement("inputParameter", "s2");
		
		return msg;
	}
	
	/**
	 * Generates a people request message which can be sent to the KEUI component.
	 * 
	 * @param props
	 * @param requestId
	 * @return
	 */
	public static String genKEUIPeopleMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "peopleData");
			params.addAttribute(envelope.createName("maxCountItems"), "1000");
			params.addAttribute(envelope.createName("includePeopleData"), "True");
			params.addAttribute(envelope.createName("sortBy"), props.getProperty("sort"));
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An error occurred while generating KEUI people message!", t);
		}
	}

	/**
	 * Constructs a message for timeline data, which can be sent to the KEUI component.
	 * 
	 * @param props
	 * @param requestId
	 * @return
	 */
	public static String genKEUITimelineMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "timelineData");
			params.addAttribute(envelope.createName("sortBy"), props.getProperty("sort"));
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An error occurred while generating KEUI timeline message!", t);
		}
	}

	/**
	 * Constructs a message for item data, which can be sent to the KEUI component.
	 * 
	 * @param props
	 * @param requestId
	 * @return
	 */
	public static String getKEUIItemsMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "itemData");
			params.addAttribute(envelope.createName("offset"), props.containsKey("offset") ? props.getProperty("offset") : "0");
			params.addAttribute(envelope.createName("maxCount"), props.containsKey("maxCount") ? props.getProperty("maxCount") : "100");
			params.addAttribute(envelope.createName("includeAttachments"), "True");
			params.addAttribute(envelope.createName("sortBy"), "dateDesc");
			params.addAttribute(envelope.createName("itemDataSnipLen"), KEUI_ITEM_SNIP_LEN);
			params.addAttribute(envelope.createName("snipMatchKeywords"), "True");
			params.addAttribute(envelope.createName("keywordMatchOffset"), "25");
			params.addAttribute(envelope.createName("includePeopleData"), "True");
			params.addAttribute(envelope.createName("sortBy"), props.getProperty("sort"));
	
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An error occurred while generating KEUI items message!", t);
		}
	}

	/**
	 * Constructs a message for keyword data, which can be sent to the KEUI component.
	 * 
	 * @param props
	 * @param requestId
	 * @return
	 */
	public static String genKEUIKeywordMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "keywordData");
			params.addAttribute(envelope.createName("keywordCount"), "60");
			params.addAttribute(envelope.createName("sampleSize"), "50000");
			params.addAttribute(envelope.createName("keywordMethod"), "localConceptSpV");
			params.addAttribute(envelope.createName("keywordSource"), "concepts");
			params.addAttribute(envelope.createName("sortBy"), props.getProperty("sort"));
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An error occurred while generating KEUI keyword message!", t);
		}
	}
	
	/**
	 * Constructs a message for requesting suggestions, which can be sent to the KEUI component.
	 * 
	 * @param term
	 * @param suggestionTypes
	 * @param requestId
	 * @return
	 */
	public static String genKEUISuggestionMessage(String term, String suggestionTypes, String requestId) {
		try {
			SOAPMessage msg = getKEUITemplate("GetSuggestions", requestId);
			SOAPElement data = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
			
			SOAPElement query = data.addChildElement("query");
			query.setAttribute("prefix", term);
			query.setAttribute("suggestionTypes", suggestionTypes);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An error occurred while generating KEUI suggestion message!", t);
		}
	}
	
	public static String genKEUIItemDetailsMessage(String itemId, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryTemplate("Query", "generalQuery", requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			
			SOAPElement data = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
			
			SOAPElement query = (SOAPElement) data.getElementsByTagName("query").item(0);
			SOAPElement queryArgs = query.addChildElement("queryArgs");
			SOAPElement conditions = queryArgs.addChildElement("conditions");
			conditions.addChildElement("itemIds").setTextContent(itemId);
			
			SOAPElement params = query.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "itemData");
			params.addAttribute(envelope.createName("includePeopleData"), "True");
			params.addAttribute(envelope.createName("offset"), "0");
			params.addAttribute(envelope.createName("maxCount"), "1");
			params.addAttribute(envelope.createName("sortBy"), "dateDesc");
			params.addAttribute(envelope.createName("itemDataSnipLen"), "-1");
			params.addAttribute(envelope.createName("snipMatchKeywords"), "True");
			params.addAttribute(envelope.createName("roleTypes"), "from,to,cc,bcc,author,originalFrom");
			params.addAttribute(envelope.createName("includeAttachments"), "False");
			params.addAttribute(envelope.createName("keywordMatchOffset"), "25");
			params.addAttribute(envelope.createName("maxCountItems"), "1000");
			params.addAttribute(envelope.createName("sampleSize"), "-1");
			params.addAttribute(envelope.createName("keywordCount"), "30");
			params.addAttribute(envelope.createName("keywordSource"), "text");
			params.addAttribute(envelope.createName("keywordMethod"), "localConceptSpV");

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An error occurred while generating KEUI item details message!", t);
		}
	}
	
	/**
	 * Generates a message for requesting duplicate issue which can be sent to the KEUI component.
	 * 
	 * @param issueId
	 * @param requestId
	 * @return
	 */
	public static String genKEUIDuplicateIssueMsg(Properties props, String requestId) {
		try {
			String issueId = props.getProperty("issues");
			String offset = props.getProperty("offset");
			String limit = props.getProperty("limit");
			
			SOAPMessage msg = getKEUIQueryTemplate("Query", "customQuery", requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement requestData = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
			
			SOAPElement query = (SOAPElement) requestData.getElementsByTagName("query").item(0);
			SOAPElement params = query.addChildElement("params");
			params.addAttribute(envelope.createName("type"), "similarThreads");
			params.addAttribute(envelope.createName("threadId"), "-1");
			params.addAttribute(envelope.createName("bugId"), issueId);
			params.addAttribute(envelope.createName("count"), "100");
			params.addAttribute(envelope.createName("includeItemIds"), "True");
			params.addAttribute(envelope.createName("includeItemData"), "True");
			params.addAttribute(envelope.createName("itemDataSnipLen"), KEUI_ITEM_SNIP_LEN);
			params.addAttribute(envelope.createName("includeOnlyFirstInThread"), "True");
			params.addAttribute(envelope.createName("maxCount"), limit);
			params.addAttribute(envelope.createName("offset"), offset);
			params.addAttribute(envelope.createName("includePeopleData"), "True");
			
			// add condition to only return issues
			SOAPElement conditions = query.addChildElement("queryArgs")
										  .addChildElement("conditions");
			SOAPElement postTypes = conditions.addChildElement("postTypes");
			
			postTypes.setTextContent("issues");
			
			// add status and resolution conditions
			List<String> resolutions = getResolutions(props);
			List<String> statuses = getStatuses(props);
			
			if (resolutions.size() != availableResolutions.size()) {
				String resolutionsStr = Utils.toCommaSepStr(resolutions);
				conditions.addChildElement("issueResolution").setTextContent(resolutionsStr);
			}
			if (statuses.size() != availableStatuses.size()) {
				String statusesStr = Utils.toCommaSepStr(statuses);
				conditions.addChildElement("issueStatus").setTextContent(statusesStr);
			}		
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while generating duplicate issue message!", t);
		}
	}
	
	private static SOAPMessage getKEUIQueryTemplate(String requestType, String queryType, String requestId) throws DOMException, SOAPException {
		SOAPMessage msg = getKEUITemplate(requestType, requestId);
		SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
		
		SOAPElement requestData = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
		SOAPElement query = requestData.addChildElement("query");
		query.addAttribute(envelope.createName("type"), queryType);
		
		return msg;
	}
	
	private static SOAPMessage getKEUITemplate(String requestType, String requestId) throws DOMException, SOAPException {
		SOAPMessage msg = getMsgTemplate("http://www.alert-project.eu/keui", "KEUIRequest", requestId);
		
		SOAPBody body = msg.getSOAPBody();
		SOAPElement eventData = (SOAPElement) body.getElementsByTagName("ns1:eventData").item(0);
		
		SOAPElement keuiRequest = eventData.addChildElement("keuiRequest", "s1");
		keuiRequest.addChildElement("requestType", "s1").setTextContent(requestType);
		keuiRequest.addChildElement("requestData", "s1");
		
		return msg;
	}
	
	private static List<String> getResolutions(Properties props) {
		List<String> result = new ArrayList<>();
		
		for (String resolution : availableResolutions) {
			String resKey = resolution + "Chk";
			if (props.containsKey(resKey) && Utils.parseBoolean(props.getProperty(resKey)))
				result.add(resolution);
		}
		
		return result;
	}
	
	private static List<String> getStatuses(Properties props) {
		List<String> result = new ArrayList<>();
		
		
		for (String status : availableStatuses) {
			String statusKey = status + "Chk";
			if (props.containsKey(statusKey) && Utils.parseBoolean(props.getProperty(statusKey)))
				result.add(status);
		}
		
		return result;
	}
	
	/**
	 * Constructs a template KEUI query message, only the parameters have to be filled in.
	 * 
	 * @param props
	 * @return
	 */
	private static SOAPMessage getKEUIQueryMsg(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryTemplate("Query", "generalQuery", requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			
			SOAPElement requestData = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
			SOAPElement query = (SOAPElement) requestData.getElementsByTagName("query").item(0);
			
			SOAPElement args = query.addChildElement("queryArgs");
			SOAPElement conditions = args.addChildElement("conditions");
			
			if (props.containsKey("keywords")) {
				SOAPElement kwsEl = conditions.addChildElement("enrychableKeywords");
				kwsEl.setTextContent(props.getProperty("keywords"));
				
				if (props.containsKey("optional")) {
					boolean optional = Utils.parseBoolean(props.getProperty("optional"));
					if (optional)
						kwsEl.addAttribute(envelope.createName("optional"), "1");
				}
			}
			
			if (props.containsKey("people")) {
				String[] people = props.getProperty("people").split(",");
				
				for (String personOrV : people) {
					String[] personV = personOrV.split("\\|");
					SOAPElement accounts = conditions.addChildElement("accounts");
					
					for (String person : personV)
						accounts.addChildElement("account").setAttribute("name", person);
				}
			}
			
			if (props.containsKey("issues")) {
				String[] issues = props.getProperty("issues").split(",");
				
				for (String issue : issues) 
					conditions.addChildElement("bugId").setTextContent(issue);
			}
			
			// sources and products go into the same tag
			if (props.containsKey("sources") || props.containsKey("products")) {
				List<String> tagIdV = new ArrayList<String>();
				if (props.containsKey("sources")) tagIdV.addAll(Arrays.asList(props.getProperty("sources").split(",")));
				if (props.containsKey("products")) tagIdV.addAll(Arrays.asList(props.getProperty("products").split(",")));
				
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < tagIdV.size(); i++) {
					builder.append(tagIdV.get(i));
					if (i < tagIdV.size() - 1)
						builder.append("|");	// the delimiter is |
				}
				conditions.addChildElement("tagIdStr").setTextContent(builder.toString());
			}
			
			if (props.containsKey("from") || props.contains("to")) {
				SimpleDateFormat format = Utils.getClientDateFormat();
				
				SOAPElement timeline = conditions.addChildElement("timeline");
				if (props.containsKey("from")) {
					Date from = format.parse(props.getProperty("from"));
					long winTime = Utils.toWindowsTime(from.getTime());
					timeline.addAttribute(envelope.createName("start"), winTime + "");
				}
				if (props.containsKey("to")) {
					Date to = format.parse(props.getProperty("to"));
					long winTime = Utils.toWindowsTime(to.getTime());
					timeline.addAttribute(envelope.createName("end"), winTime + "");
				}
			}
			
			// set which fields to query for
			List<String> qFields = new ArrayList<String>(keuiIgnoreKeys.length);
			for (String key : keuiIgnoreKeys) {
				if (props.containsKey(key) && Utils.parseBoolean(props.getProperty(key)))
					qFields.add(key.substring(0, key.length() - 3));
				
				if ("issuesChk".equals(key) && Utils.parseBoolean(props.getProperty(key))) {
					// get the resolutions and statuses
					List<String> resolutions = getResolutions(props);
					List<String> statuses = getStatuses(props);
					
					if (resolutions.size() != availableResolutions.size()) {
						String resolutionsStr = Utils.toCommaSepStr(resolutions);
						conditions.addChildElement("issueResolution").setTextContent(resolutionsStr);
					}
					if (statuses.size() != availableStatuses.size()) {
						String statusesStr = Utils.toCommaSepStr(statuses);
						conditions.addChildElement("issueStatus").setTextContent(statusesStr);
					}	
				}
			}
			
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < qFields.size(); i++) {
				builder.append(qFields.get(i));
				if (i < qFields.size() - 1)
					builder.append(",");
			}
			conditions.addChildElement("postTypes").setTextContent(builder.toString());
			
			return msg;
		} catch (Throwable t) {
			throw new IllegalStateException("En exception occurred while generating KEUI message!", t);
		}
	}
	
	/**
	 * Generates a commit details request which can be sent to the API component.
	 * 
	 * @param itemId
	 * @return
	 * @throws SOAPException 
	 * @throws IOException 
	 */
	public static String getCommitDetailsMsg(String commitURI, String requestId) throws SOAPException, IOException {
		SOAPMessage msg = getAPITemplate("commit.getInfo", requestId);
		
		SOAPElement inputEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s2:inputParameter").item(0);
		
		inputEl.addChildElement("name", "s2").setTextContent("commitUri");
		inputEl.addChildElement("value", "s2").setTextContent(commitURI);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		msg.writeTo(out);
		return new String(out.toByteArray());
	}
	
	/**
	 * Generates a issue details request which can be sent to the API component.
	 * 
	 * @param issueId
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	public static String genIssueDetailsMsg(String issueId, String requestId) throws SOAPException, IOException {
		SOAPMessage msg = getAPITemplate("issue.getInfo", requestId);
		
		SOAPElement inputEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s2:inputParameter").item(0);
		
		inputEl.addChildElement("name", "s2").setTextContent("issueID");
		inputEl.addChildElement("value", "s2").setTextContent(issueId);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		msg.writeTo(out);
		return new String(out.toByteArray());
	}
	
	/**
	 * Generates a Recommender 'issues for identities' message, which can be sent over the MQ.
	 * 
	 * @param userIds
	 * @return
	 */
	public static String genRecommenderIssuesMsg(Collection<String> userIds, String requestId) {
		try {
			SOAPMessage msg = getMsgTemplate("http://www.alert-project.eu/metadata", "ALERT.*.Recommender.IssueRecommendationRequest", requestId);
			SOAPElement eventData = (SOAPElement) msg.getSOAPBody().getElementsByTagName("ns1:eventData").item(0);
			
			SOAPElement identities = eventData.addChildElement("identities", "sc");
			
			for (String userId : userIds)
				identities.addChildElement("identity", "sc").addChildElement("uuid", "sc").setTextContent(userId);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unecpected exception occurred while generating Recommender suggest issues message!", t);
		}
	}

	/**
	 * Generates a KEUI message to request issue short content from issueIDs.
	 * 
	 * @param issueIds
	 * @param requestId
	 * @return
	 */
	public static String genKEUIIssueListMsg(List<Long> issueIds, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryTemplate("Query", "generalQuery", requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement query = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement args = query.addChildElement("queryArgs");
			SOAPElement conditions = args.addChildElement("conditions");
			SOAPElement bugIds = conditions.addChildElement("bugIds");
			SOAPElement postTypes = conditions.addChildElement("postTypes");
			
			// set the conditions
			// generate a String of issueIDs
			StringBuilder idBuilder = new StringBuilder();
			for (int i = 0; i < issueIds.size(); i++) {
				idBuilder.append(issueIds.get(i));
				if (i < issueIds.size() - 1)
					idBuilder.append(",");
			}
				
			bugIds.setTextContent(idBuilder.toString());
			postTypes.setTextContent("issueDescriptions");
			
			// set the parameters
			SOAPElement params = query.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "itemData");
			params.addAttribute(envelope.createName("offset"), "0");	// TODO get offset from client
			params.addAttribute(envelope.createName("maxCount"), "100");	// TODO get limit from client
			params.addAttribute(envelope.createName("includeAttachments"), "True");
			params.addAttribute(envelope.createName("sortBy"), "dateDesc");
			params.addAttribute(envelope.createName("itemDataSnipLen"), KEUI_ITEM_SNIP_LEN);
			params.addAttribute(envelope.createName("snipMatchKeywords"), "True");
			params.addAttribute(envelope.createName("keywordMatchOffset"), "25");
			params.addAttribute(envelope.createName("includePeopleData"), "True");
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unecpected exception occurred while generating KEUI get issues from IDs message!", t);
		}
	}
}
