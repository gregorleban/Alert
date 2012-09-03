package com.jsi.keui.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.activemq.util.ByteArrayInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * A class which generates messages which are ment to be sent to the Message Broker. 
 *
 */
public class MessageUtils {
	
	private enum ItemType {
		EMAIL(10),
		POST(11),
		BUG_DESCRIPTION(12),
		BUG_COMMENT(13),
		COMMIT(14),
		WIKI_POST(15);
		
		public final int value;
		
		ItemType(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
	}
	
	private static final Comparator<String> MONTH_LABEL_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String m1, String m2) {
			String[] m1Pr = m1.split("-");
			String[] m2Pr = m2.split("-");
			
			int val1 = Integer.parseInt(m1Pr[0])*100 + Integer.parseInt(m1Pr[1]);
			int val2 = Integer.parseInt(m2Pr[0])*100 + Integer.parseInt(m2Pr[1]);
			
			if (val1 > val2)
				return 1;
			else if (val1 < val2)
				return -1;
			else return 0;
		}
	};

	private static MessageUtils instance;
	
	private static MessageFactory msgFactory;
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	
	private static final String DEFAULT_DATE_FORMAT = "yy-mm-dd";
	
	private static final String[] keuiIgnoreKeys = {
		"issuesChk",
		"commitsChk",
		"forumsChk",
		"mailsChk",
		"wikisChk",
		
		"unconfirmedChk",
    	"newChk",
    	"assignedChk",
    	"resolveChk",
    	"invalidChk",
    	"worksChk",
    	"fixedChk",
    	"wondChk",
    	"duplicateChk"
	};
	
	private static final int[] MONTH_LENGTHS = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	
	private MessageUtils() throws SOAPException {
		msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
	}
	
	public static synchronized MessageUtils getInstance() throws SOAPException {
		if (instance == null) instance = new MessageUtils();
		return instance;
	}
	
	public String genKEUIPeopleMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "peopleData");
			params.addAttribute(envelope.createName("maxCountItems"), "1000");
			params.addAttribute(envelope.createName("includePeopleData"), "1");
			
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
	public String genKEUITimelineMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "timelineData");
			
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
	public String getKEUIItemsMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "itemData");
			params.addAttribute(envelope.createName("offset"), props.containsKey("offset") ? props.getProperty("offset") : "0");
			params.addAttribute(envelope.createName("maxCount"), props.containsKey("maxCount") ? props.getProperty("maxCount") : "100");
			params.addAttribute(envelope.createName("includeAttachments"), "1");
			params.addAttribute(envelope.createName("sortBy"), "dateDesc");
			params.addAttribute(envelope.createName("itemDataSnipLen"), "200");
			params.addAttribute(envelope.createName("snipMatchKeywords"), "1");
			params.addAttribute(envelope.createName("keywordMatchOffset"), "25");
			params.addAttribute(envelope.createName("includePeopleData"), "1");
	
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
	public String genKEUIKeywordMessage(Properties props, String requestId) {
		try {
			SOAPMessage msg = getKEUIQueryMsg(props, requestId);
			
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement queryEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("query").item(0);
			
			SOAPElement params = queryEl.addChildElement("params");
			params.addAttribute(envelope.createName("resultData"), "keywordData");
			params.addAttribute(envelope.createName("keywordCount"), "30");
			params.addAttribute(envelope.createName("sampleSize"), "-1");
			params.addAttribute(envelope.createName("keywordMethod"), "localConceptSpV");
			
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
	public String genKEUISuggestionMessage(String term, String suggestionTypes, String requestId) {
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
	
	public String genKEUIItemDetailsMessage(String itemId, String requestId) {
		try {
			SOAPMessage msg = getKEUITemplate("GeneralQuery", requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			
			SOAPElement data = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
			
			SOAPElement query = data.addChildElement("query");
			SOAPElement queryArgs = query.addChildElement("queryArgs");
			SOAPElement conditions = queryArgs.addChildElement("conditions");
			conditions.addChildElement("itemIds").setTextContent(itemId);
			
			SOAPElement params = query.addChildElement("params");
			params.addAttribute(envelope.createName("offset"), "0");
			params.addAttribute(envelope.createName("maxCount"), "1");
			params.addAttribute(envelope.createName("resultData"), "itemData");
			params.addAttribute(envelope.createName("includeAttachments"), "0");
			params.addAttribute(envelope.createName("sortBy"), "dateDesc");
			params.addAttribute(envelope.createName("itemDataSnipLen"), "-1");
			params.addAttribute(envelope.createName("snipMatchKeywords"), "1");
			params.addAttribute(envelope.createName("keywordMatchOffset"), "25");
			params.addAttribute(envelope.createName("includePeopleData"), "1");
			
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
	public String genKEUIDuplicateIssueMsg(Properties props, String requestId) {
		try {
			String issueId = props.getProperty("issues");
			String offset = props.getProperty("offset");
			String limit = props.getProperty("limit");
			
			SOAPMessage msg = getKEUITemplate("CustomQuery", requestId);
			SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
			SOAPElement requestData = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
			
			SOAPElement query = requestData.addChildElement("query");
			query.addAttribute(envelope.createName("type"), "similarThreads");
			query.addAttribute(envelope.createName("threadId"), "-1");
			query.addAttribute(envelope.createName("bugId"), issueId);
			query.addAttribute(envelope.createName("count"), "100");
			query.addAttribute(envelope.createName("includeItemIds"), "1");
			query.addAttribute(envelope.createName("includeItemData"), "1");
			query.addAttribute(envelope.createName("includeOnlyFirstInThread"), "1");
			query.addAttribute(envelope.createName("maxCount"), limit);
			query.addAttribute(envelope.createName("offset"), offset);
			query.addAttribute(envelope.createName("includePeopleData"), "1");
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			return new String(out.toByteArray());
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while generating duplicate issue message!", t);
		}
	}
	
	private SOAPMessage getKEUITemplate(String requestType, String requestId) throws DOMException, SOAPException {
		SOAPMessage msg = getMsgTemplate("http://www.alert-project.eu/keui", "KEUIRequest");
		
		SOAPBody body = msg.getSOAPBody();
		SOAPElement eventData = (SOAPElement) body.getElementsByTagName("ns1:eventData").item(0);
		
		SOAPElement keuiRequest = eventData.addChildElement("keuiRequest", "s1");
		keuiRequest.addChildElement("requestType", "s1").setTextContent(requestType);
		keuiRequest.addChildElement("requestData", "s1");
		
		// set the request id
		((SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:keuiRequest").item(0)).addChildElement("requestID").setTextContent(requestId);
		
		return msg;
	}
	
	/**
	 * Constructs a template KEUI query message, only the parameters have to be filled in.
	 * 
	 * @param props
	 * @return
	 */
	private SOAPMessage getKEUIQueryMsg(Properties props, String requestId) {
		try {
		SOAPMessage msg = getKEUITemplate("GeneralQuery", requestId);
		SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
		
		SOAPElement requestData = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s1:requestData").item(0);
		SOAPElement queryEl = requestData.addChildElement("query");
		
		SOAPElement args = queryEl.addChildElement("queryArgs");
		SOAPElement conditions = args.addChildElement("conditions");
		
		if (props.containsKey("keywords")) {
			String[] keywords = props.getProperty("keywords").split(",");
			
			SOAPElement kwsEl = conditions.addChildElement("keywords");
			for (String keyword : keywords)
				kwsEl.addChildElement("kw").setTextContent(keyword);
		}
		
		if (props.containsKey("concepts")) {
			String[] concepts = props.getProperty("concepts").split(",");
			
			SOAPElement conceptsEl = conditions.addChildElement("concepts");
			for (String c : concepts)
				conceptsEl.addChildElement("concept").setTextContent(c);
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
			SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			SOAPElement timeline = conditions.addChildElement("timeline");
			if (props.containsKey("from")) {
				Date from = format.parse(props.getProperty("from"));
				long winTime = from.getTime() + 11644473600000L; // convert to windows time
				timeline.addAttribute(envelope.createName("start"), winTime + "");
			}
			if (props.containsKey("to")) {
				Date to = format.parse(props.getProperty("to"));
				long winTime = to.getTime() + 11644473600000L;
				timeline.addAttribute(envelope.createName("end"), winTime + "");
			}
		}
		
		// set which fields to query for
		List<String> qFields = new ArrayList<String>(keuiIgnoreKeys.length);
		for (String key : keuiIgnoreKeys) {
			if (props.containsKey(key) && Utils.parseBoolean(props.getProperty(key)))
				qFields.add(key.substring(0, key.length() - 3));
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
	public String getCommitDetailsMsg(String commitURI) throws SOAPException, IOException {
		SOAPMessage msg = getAPITemplate();
		
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
	public String genIssueDetailsMsg(String issueId) throws SOAPException, IOException {
		SOAPMessage msg = getAPITemplate();
		
		SOAPElement inputEl = (SOAPElement) msg.getSOAPBody().getElementsByTagName("s2:inputParameter").item(0);
		
		inputEl.addChildElement("name", "s2").setTextContent("issueID");
		inputEl.addChildElement("value", "s2").setTextContent(issueId);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		msg.writeTo(out);
		return new String(out.toByteArray());
	}
	
	/**
	 * Generates a SOAPMessage with the envelope prefix set to 's' and 2 new namespaces added. 
	 * 
	 * @throws SOAPException 
	 * @throws DOMException 
	 */
	private SOAPMessage getMsgTemplate(String address, String eventName) throws DOMException, SOAPException {
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
		event.addChildElement("head", "ns1");
		// TODO add header tags
		
		// payload
		SOAPElement payload = event.addChildElement("payload", "ns1");
		// meta
		SOAPElement meta = payload.addChildElement("meta", "ns1");
		meta.addChildElement("eventName", "ns1").setTextContent(eventName);
		meta.addChildElement("eventType", "ns1").setTextContent("request");
		// TODO add startTime, endTime, eventId to meta tag
		
		// event data
		payload.addChildElement("eventData", "ns1");
		
		return soapMsg;
	}
	
	private SOAPMessage getAPITemplate() throws DOMException, SOAPException {
		SOAPMessage msg = getMsgTemplate("http://www.alert-project.eu/search", "ALERT.Search.APICallRequest");
		
		SOAPBody body = msg.getSOAPBody();
		SOAPElement eventData = (SOAPElement) body.getElementsByTagName("ns1:eventData").item(0);
		
		SOAPElement requestEl = eventData.addChildElement("apiRequest", "ns1");
		requestEl.addChildElement("apiCall", "s2").setTextContent("issue.getInfo");
		SOAPElement dataEl = requestEl.addChildElement("requestData", "s2");
		
		dataEl.addChildElement("inputParameter", "s2");
		
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public String parseCommitDetailsMessage(String responseMsg) {
		try {
			JSONObject result = new JSONObject();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			JSONArray filesArr = new JSONArray();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
			
			Node respData = doc.getElementsByTagName("s3:responseData").item(0);
			NodeList propNodes = respData.getChildNodes();
			for (int nodeIdx = 0; nodeIdx < propNodes.getLength(); nodeIdx++) {
				Node node = propNodes.item(nodeIdx);
				String nodeName = node.getNodeName();
				
				if ("s3:commitRepositoryUri".equals(nodeName))
					result.put("repositoryUri", node.getTextContent());
				else if ("s3:commitRevisionTag".equals(nodeName)) 
					result.put("revisionTag", Integer.parseInt(node.getTextContent()));
				else if ("s3:commitDate".equals(nodeName))
					result.put("commitDate", dateFormat.parse(node.getTextContent()).getTime());
				else if ("s3:commitMessageLog".equals(nodeName))
					result.put("message", node.getTextContent());
				else if ("s3:commitAuthor".equals(nodeName)) {
					JSONObject authorJSon = new JSONObject();
					
					NodeList authorProps = node.getChildNodes();
					for (int i = 0; i < authorProps.getLength(); i++) {
						Node prop = authorProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						authorJSon.put(label, value);
					}
					result.put("author", authorJSon);
				}
				else if ("s3:commitCommitter".equals(nodeName)) {
					JSONObject authorJSon = new JSONObject();

					NodeList authorProps = node.getChildNodes();
					for (int i = 0; i < authorProps.getLength(); i++) {
						Node prop = authorProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						authorJSon.put(label, value);
					}
					result.put("committer", authorJSon);
				}
				else if ("s3:commitFile".equals(nodeName)) {
					JSONObject file = new JSONObject();
					JSONArray modules = new JSONArray();
					
					NodeList fileProps = node.getChildNodes();
					for (int i = 0; i < fileProps.getLength(); i++) {
						Node fileProp = fileProps.item(i);
						String label = fileProp.getNodeName();
						
						if ("s3:fileId".equals(label))
							file.put("id", Long.parseLong(fileProp.getTextContent()));
						else if ("s3:fileUri".equals(label))
							file.put("uri", fileProp.getTextContent());
						else if ("s3:fileAction".equals(label))
							file.put("action", fileProp.getTextContent());
						else if ("s3:fileName".equals(label))
							file.put("name", fileProp.getTextContent());
						else if ("s3:fileBranch".equals(label))
							file.put("branch", node.getTextContent());
						else if ("s3:fileModules".equals(label)) {
							JSONObject module = new JSONObject();
							JSONArray methods = new JSONArray();
							
							NodeList moduleProps = fileProp.getChildNodes();
							for (int j = 0; j < moduleProps.getLength(); j++) {
								Node moduleProp = moduleProps.item(j);
								String moduleLabel = moduleProp.getNodeName();
								
								if ("s3:moduleUri".equals(moduleLabel))
									module.put("uri", moduleProp.getTextContent());
								else if ("s3:moduleId".equals(moduleLabel))
									module.put("id", Long.parseLong(moduleProp.getTextContent()));
								else if ("s3:moduleName".equals(moduleLabel))
									module.put("name", moduleProp.getTextContent());
								else if ("s3:moduleStartLine".equals(moduleLabel))
									module.put("startLine", Integer.parseInt(moduleProp.getTextContent()));
								else if ("s3:moduleEndLine".equals(moduleLabel))
									module.put("endLine", Integer.parseInt(moduleProp.getTextContent()));
								else if ("s3:moduleMethods".equals(moduleLabel)) {
									// parse the method
									JSONObject method = new JSONObject();
									
									NodeList methodProps = moduleProp.getChildNodes();
									for (int k = 0; k < methodProps.getLength(); k++) {
										Node methodProp = methodProps.item(k);
										String methodLabel = methodProp.getNodeName();
										
										if ("s3:methodId".equals(methodLabel))
											method.put("id", Long.parseLong(methodProp.getTextContent()));
										else if ("s3:methodStartLine".equals(methodLabel))
											method.put("startLine", Integer.parseInt(methodProp.getTextContent()));
										else if ("s3:methodEndLine".equals(methodLabel))
											method.put("endLine", Integer.parseInt(methodProp.getTextContent()));
										else 
											method.put(moduleLabel.substring(3), methodProp.getTextContent());
									}
									
									methods.add(method);
								}
							}
							module.put("methods", methods);
							modules.add(module);
						} 
					}
					
					file.put("modules", modules);
					filesArr.add(file);
				}
				else if ("s3:commitProduct".equals(nodeName)) {
					JSONObject product = new JSONObject();
					
					NodeList prodProps = node.getChildNodes();
					for (int i = 0; i < prodProps.getLength(); i++) {
						Node prodProp = prodProps.item(i);
						String label = prodProp.getNodeName().substring(3);
						
						product.put(label, prodProp.getTextContent());
					}
				}
				
				
				result.put("files", filesArr);
			}
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing commit details response message!", t);
		}
	}
	
	/**
	 * Parses issue details response message.
	 * 
	 * @param responseMsg The message to parse.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@SuppressWarnings("unchecked")
	public String parseIssueDetailsMsg(String responseMsg) throws ParserConfigurationException, SAXException, IOException {
		try {
			JSONObject result = new JSONObject();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			JSONArray commentsJSon = new JSONArray();
			JSONArray activitiesJSon = new JSONArray();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
			
			// parse assigned to
			Node dataEl = doc.getElementsByTagName("s3:responseData").item(0);
			NodeList propNodes = dataEl.getChildNodes();
			for (int nodeIdx = 0; nodeIdx < propNodes.getLength(); nodeIdx++) {
				Node node = propNodes.item(nodeIdx);
				String nodeName = node.getNodeName();
				
				if ("s3:issueDependsOnid".equals(nodeName))	// parse depends on
					result.put("dependsOnId", Utils.parseLong(node.getTextContent()));
				else if ("s3:issueDependsOnUri".equals(nodeName))
					result.put("dependsOnUri", node.getTextContent());
				else if ("s3:issueUrl".equals(nodeName))	// parse URL
					result.put("url", node.getTextContent());
				else if ("s3:issueBlocksid".equals(nodeName))
					result.put("blocksId", node.getTextContent());
				else if ("s3:issueBlocksUri".equals(nodeName))
					result.put("blocksUri", node.getTextContent());
				else if ("s3:issueResolution".equals(nodeName))
					result.put("resolution", node.getTextContent());
				else if ("s3:issueDateOpened".equals(nodeName))
					result.put("dateOpened", dateFormat.parse(node.getTextContent()).getTime());
				else if ("s3:issueDescription".equals(nodeName))
					result.put("description", node.getTextContent());
				else if ("s3:issueLastModified".equals(nodeName))
					result.put("lastModified", dateFormat.parse(node.getTextContent()).getTime());
				else if ("s3:issueStatus".equals(nodeName))
					result.put("status", node.getTextContent());
				else if ("s3:issueAssignedTo".equals(nodeName)) {
					NodeList assignedProps = node.getChildNodes();
					
					JSONObject assignedJSon = new JSONObject();
					for (int i = 0; i < assignedProps.getLength(); i++) {
						Node prop = assignedProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						assignedJSon.put(label, value);
					}
					result.put("assignedTo", assignedJSon);
				} 
				else if ("s3:issueProduct".equals(nodeName)) {
					JSONObject productJSon = new JSONObject();
					
					NodeList prodProps = node.getChildNodes();
					for (int i = 0; i < prodProps.getLength(); i++) {
						Node prop = prodProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						productJSon.put(label, value);
					}
					result.put("product", productJSon);
				} 
				else if ("s3:issueAuthor".equals(nodeName)) {
					JSONObject authorJSon = new JSONObject();
					
					NodeList authorProps = node.getChildNodes();
					for (int i = 0; i < authorProps.getLength(); i++) {
						Node prop = authorProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						authorJSon.put(label, value);
					}
					result.put("author", authorJSon);
				}
				else if ("s3:issueComputerSystem".equals(nodeName)) {
					JSONObject systemJSon = new JSONObject();
					
					NodeList systemProps = node.getChildNodes();
					for (int i = 0; i < systemProps.getLength(); i++) {
						Node prop = systemProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						systemJSon.put(label, value);
					}
					result.put("computerSystem", systemJSon);
				}
				else if ("s3:issueCCPerson".equals(nodeName)) {
					JSONObject ccJSon = new JSONObject();
					
					NodeList ccProps = node.getChildNodes();
					for (int i = 0; i < ccProps.getLength(); i++) {
						Node prop = ccProps.item(i);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						ccJSon.put(label, value);
					}
					result.put("cc", ccJSon);
				}
				else if ("s3:issueActivity".equals(nodeName)) {
					JSONObject activityJSon = new JSONObject();
					
					NodeList activityProps = node.getChildNodes();
					for (int j = 0; j < activityProps.getLength(); j++) {
						Node prop = activityProps.item(j);
						String label = prop.getNodeName().substring(3);
						String value = prop.getTextContent();
						
						activityJSon.put(label, value);
					}
					
					activitiesJSon.add(activityJSon);
				}
				else if ("s3:issueComment".equals(nodeName)) {
					JSONObject commentJSon = new JSONObject();
					
					NodeList commentProps = node.getChildNodes();
					for (int j = 0; j < commentProps.getLength(); j++) {
						Node commentNode = commentProps.item(j);
						String label = commentNode.getNodeName().substring(3);
						
						if ("commentPerson".equals(label)) {
							// person also has properties
							Node person = commentNode;
							JSONObject personJSon = new JSONObject();
							
							NodeList personProps = person.getChildNodes();
							for (int k = 0; k < personProps.getLength(); k++) {
								Node personProp = personProps.item(k);
								String personLabel = personProp.getNodeName().substring(3);
								String personValue = personProp.getTextContent();
								
								personJSon.put(personLabel, personValue);
							}
							
							commentJSon.put("person", personJSon);
						} else if("commentDate".equals(label))
							commentJSon.put("commentDate", dateFormat.parse(commentNode.getTextContent()).getTime());
						else
							commentJSon.put(label, commentNode.getTextContent());
					}
					
					commentsJSon.add(commentJSon);
				}
			}
			
			result.put("activities", activitiesJSon);
			result.put("comments", commentsJSon);		
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing issue details response message!", t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public String parseItemDetailsMsg(String responseMsg) {
		try {
			JSONObject result = new JSONObject();
			
			Map<Long, JSONObject> personH = new HashMap<Long, JSONObject>();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			Element results = (Element) doc.getElementsByTagName("results").item(0);
			
			// parse people
			NodeList peopleNodes = results.getElementsByTagName("person");
			for (int i = 0; i < peopleNodes.getLength(); i++) {
				Node personNode = peopleNodes.item(i);
				JSONObject person = new JSONObject();
				
				NamedNodeMap attributes = personNode.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attribute = attributes.item(j);
					String attrName = attribute.getNodeName();
					
					if ("id".equals(attrName))
						person.put("id", Long.parseLong(attribute.getNodeValue()));
					else person.put(attrName, attribute.getNodeValue());
				}
				
				long id = (Long) person.get("id");
				personH.put(id, person);
			}
			
			// parse content
			Element item = (Element) results.getElementsByTagName("item").item(0);
			
			long id = Long.parseLong(item.getAttribute("id"));
			int type = Integer.parseInt(item.getAttribute("itemType"));
			long time = Long.parseLong(item.getAttribute("time")) - 11644473600000L;
			String entryId = item.getAttribute("entryId");
			long threadId = Long.parseLong(item.getAttribute("threadId"));
			int count = Integer.parseInt(item.getAttribute("count"));
			JSONObject from = personH.get(Long.parseLong(item.getAttribute("from")));
			JSONObject to = personH.get(Utils.parseLong(item.getAttribute("to")));
			String content = item.getElementsByTagName("fullContent").item(0).getTextContent().replaceAll("\n", "<br />");
			String subject = item.getElementsByTagName("subject").item(0).getTextContent();
			
			JSONArray tags = new JSONArray();
			String[] tagsV = item.getAttribute("tags").split(",");
			for (String tag : tagsV)
				tags.add(Long.parseLong(tag));
			
			result.put("id", id);
			result.put("type", type);
			result.put("time", time);
			result.put("entryID", entryId);
			result.put("threadID", threadId);
			result.put("count", count);
			result.put("from", from);
			result.put("to", to);
			result.put("content", content);
			result.put("subject", subject);
			result.put("tags", tags);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing item details response message!", t);
		}
	}

	/**
	 * Parses KEUI people response message.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String parseKEUIPeopleResponse(String responseMsg) {
		int minSize = 12;
		int maxSize = 30;
		
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			JSONObject result = new JSONObject();
			
			// parse all the people
			List<JSONObject> nodeV = new ArrayList<JSONObject>();
			List<JSONObject> edgeV = new ArrayList<JSONObject>();
			Map<Long, JSONObject> nodeH = new HashMap<Long, JSONObject>();
			
			NodeList personNodeList = doc.getElementsByTagName("person");
			for (int i = 0; i < personNodeList.getLength(); i++) {
				Element personNode = (Element) personNodeList.item(i);
				JSONObject personJSon = new JSONObject();
				
				long id = Long.parseLong(personNode.getAttribute("id"));
				
				personJSon.put("id", id);
				personJSon.put("email", personNode.getAttribute("account"));
				personJSon.put("label", personNode.getAttribute("name"));
				personJSon.put("neighbours", new JSONArray());
				
				nodeV.add(personJSon);
				nodeH.put(id, personJSon);
			}
			
			// parse the counts
			int maxCount = 0;
			String fromCountsStr = doc.getElementsByTagName("fromCounts").item(0).getTextContent();
			
			// avoid an exception
			if (fromCountsStr.startsWith("\n")) fromCountsStr = fromCountsStr.substring(1);
			String[] sentDataV = fromCountsStr.split(",");
			for (String sentData : sentDataV) {
				if (sentData.isEmpty()) continue;
				
				String[] idCountPr = sentData.split("-");
				long personId = Long.parseLong(idCountPr[0]);
				int count = Integer.parseInt(idCountPr[1]);
				
				if (count > maxCount) maxCount = count;
				if (nodeH.containsKey(personId)) {
					JSONObject personJSon = nodeH.get(personId);
					personJSon.put("count", count);
				}
			}
			
			// set the size
			for (JSONObject person : nodeV) {
				int count = person.containsKey("count") ? (Integer) person.get("count") : 0;
				int size = (int) (count == 0 ? minSize : Math.min(Math.max(Math.ceil(maxSize*Math.sqrt(count)/Math.sqrt(maxCount)), minSize), maxSize));
				person.put("size", size);
			}
			
			// from/to counts
			String fromToCountsStr = doc.getElementsByTagName("fromToCounts").item(0).getTextContent();
			if (fromToCountsStr.startsWith("\n")) fromToCountsStr = fromToCountsStr.substring(1);
			
			String[] fromToCountsStrV = fromToCountsStr.split(",");
			for (String fromToStr : fromToCountsStrV) {
				if (fromToStr.isEmpty()) continue;
				
				JSONObject edge = new JSONObject();
				
				String[] fromToTup = fromToStr.split("-");
				
				long source = Long.parseLong(fromToTup[0]);
				long target = Long.parseLong(fromToTup[1]);
				
				edge.put("source", source);
				edge.put("target", target);
				
				// data
				JSONObject data = new JSONObject();
				data.put("directed", true);
				data.put("count", Integer.parseInt(fromToTup[2]));
				
				edge.put("data", data);
				edgeV.add(edge);
				
				if (nodeH.containsKey(source)) {
					JSONArray neighbours = (JSONArray) nodeH.get(source).get("neighbours");
					neighbours.add(target);
				}
			}
			
			JSONArray edgesJSon = new JSONArray();
			edgesJSon.addAll(edgeV);
			
			result.put("type", "peopleData");
			result.put("nodeH", new JSONObject(nodeH));
			result.put("edges", edgesJSon);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI people response!!", t);
		}
	}

	/**
	 * Parses KEUI timeline response message
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String parseKEUITimelineResponse(String responseMsg) {
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			NodeList monthEls = doc.getElementsByTagName("month");
			
			Map<String, List<Integer>> monthH = new HashMap<String, List<Integer>>();
			List<String> monthLabelV = new ArrayList<String>(monthEls.getLength());
			for (int i = 0; i < monthEls.getLength(); i++) {
				Element month = (Element) monthEls.item(i);
				
				String label = month.getAttribute("val");
				
				List<Integer> days = new ArrayList<Integer>(31);
				String[] dayStrV = month.getTextContent().split(",");
				for (String dayStr : dayStrV)
					days.add(Integer.parseInt(dayStr));
				
				monthH.put(label, days);
				monthLabelV.add(label);
			}

			Collections.sort(monthLabelV, MONTH_LABEL_COMPARATOR);
			
			// insert missing months
			for (int i = 0; i < monthLabelV.size(); i++) {
				if (i < monthLabelV.size() - 1) {
					String[] currentPr = monthLabelV.get(i).split("-");
					String[] nextPr = monthLabelV.get(i+1).split("-");
					
					int currentYear = Integer.parseInt(currentPr[0]);
					int nextYear = Integer.parseInt(nextPr[0]);
					int currentMonth = Integer.parseInt(currentPr[1]);
					int nextMonth = Integer.parseInt(nextPr[1]);
					
					String newLabel = null;
					if (currentYear == nextYear) {
						if (nextMonth > currentMonth + 1)
							newLabel = currentYear + "-" + (currentMonth + 1);
					} else if (currentYear == nextYear - 1) {
						if (currentMonth < 12)
							newLabel = currentYear + "-" + (currentMonth + 1);
						else if (nextMonth > 1)
							newLabel = nextYear + "-1";
					} else	// a whole year is missing
						newLabel = (currentYear + 1) + "-1";
					
					// insert missing values
					if (newLabel != null) {
						monthLabelV.add(i+1, newLabel);
						
						// missing days
						int monthIdx = Integer.parseInt(newLabel.split("-")[1]) - 1;
						int monthLength = MONTH_LENGTHS[monthIdx];
						
						List<Integer> days = new ArrayList<Integer>(monthLength);
						for (int j = 0; j < monthLength; j++)
							days.add(0);
						monthH.put(newLabel, days);
					}
				}
			}
			
			List<Integer> allDays = new ArrayList<Integer>(monthLabelV.size()*31);
			for (String month : monthLabelV)
				allDays.addAll(monthH.get(month));
			
			JSONObject result = new JSONObject();
			JSONArray allDaysJSon = new JSONArray();
			JSONArray monthsVJSon = new JSONArray();
			
			allDaysJSon.addAll(allDays);
			monthsVJSon.addAll(monthLabelV);
			
			result.put("type", "timelineData");
			result.put("days", allDaysJSon);
			result.put("months", monthsVJSon);
			result.put("monthH", new JSONObject(monthH));
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI timeline response!!", t);
		}
	}

	/**
	 * Parses KEUI keywords response message.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String parseKEUIKeywordsResponse(String responseMsg) {
		try {
			JSONArray kwsJSon = new JSONArray();
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			Element resultsNode = (Element) doc.getElementsByTagName("results").item(0);
			
			NodeList kws = resultsNode.getElementsByTagName("kw");
			for (int i = 0; i < kws.getLength(); i++) {
				Element kw = (Element) kws.item(i);
				
				String word = kw.getAttribute("str");
				double weight = Double.parseDouble(kw.getAttribute("wgt"));
				
				JSONObject kwJSon = new JSONObject();
				kwJSon.put("text", word);
				kwJSon.put("weight", weight);
				
				kwsJSon.add(kwJSon);
			}
			
			JSONObject result = new JSONObject();
			result.put("type", "keywordData");
			result.put("data", kwsJSon);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI keyword response!!", t);
		}
	}

	/**
	 * Parses KEUI items response message.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String parseKEUIItemsResponse(String responseMsg) {
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			JSONArray items = new JSONArray();
			
			// parse people
			Map<Long, JSONObject> peopleH = parsePeopleH(doc.getElementsByTagName("person"));
			
			Element infoEl = (Element) doc.getElementsByTagName("info").item(0);
			int totalCount = Integer.parseInt(infoEl.getAttribute("totalCount"));
			int offset = Integer.parseInt(infoEl.getAttribute("offset"));
			int limit = Integer.parseInt(infoEl.getAttribute("maxCount"));
			
			JSONObject info = new JSONObject();
			info.put("totalCount", totalCount);
			info.put("offset", offset);
			info.put("limit", limit);
			
			List<String> keywords = new ArrayList<String>();
			Element keywordsEl = doc.getElementsByTagName("keywords").getLength() > 0 ? (Element) doc.getElementsByTagName("keywords").item(0) : null;
			if (keywordsEl != null) {
				NodeList kwElV = keywordsEl.getElementsByTagName("kw");
				for (int i = 0; i < kwElV.getLength(); i++) {
					Element kw = (Element) kwElV.item(i);
					
					keywords.add(kw.getTextContent());
				}
			}
			
			
			NodeList itemNodes = doc.getElementsByTagName("item");
			for (int i = 0; i < itemNodes.getLength(); i++) {
				Element itemNode = (Element) itemNodes.item(i);
				JSONObject itemJSon = parseKEUIItem(itemNode);
				
				// emphasize the keywords
				if (!keywords.isEmpty()) {
					String content = (String) itemJSon.get("content");
					for (String keyword : keywords)
						content = content.replaceAll("(?i)(" + keyword + ")", "<em>$1</em>");
					
					itemJSon.put("content", content);
					if (itemJSon.containsKey("subject")) {
						String subject = (String) itemJSon.get("subject");
						for (String keyword : keywords)
							subject = subject.replaceAll("(?i)(" + keyword + ")", "<em>$1</em>");
						itemJSon.put("subject", subject);
					}
					
				}
				
				
				
				items.add(itemJSon);
			}
			
			JSONObject result = new JSONObject();
			result.put("type", "itemData");
			result.put("persons", new JSONObject(peopleH));
			result.put("items", items);
			result.put("info", info);
			
			return result.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI items response!!", t);
		}
	}
	
	/**
	 * Parses KEUI duplicate issue response.
	 * 
	 * @param responseMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String parseKEUIDuplicateResponse(String responseMsg) {
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			Element results = (Element) doc.getElementsByTagName("results").item(0);
			
			JSONArray items = new JSONArray();
			Map<Long, JSONObject> peopleH = parsePeopleH(results.getElementsByTagName("person"));
			
			NodeList itemNodes = results.getElementsByTagName("item");
			for (int i = 0; i < itemNodes.getLength(); i++) {
				Element node = (Element) itemNodes.item(i);
				
				JSONObject item = parseKEUIItem(node);
				items.add(item);
			}
			
			JSONObject info = new JSONObject();
			info.put("totalCount", items.size());
			info.put("offset", 0);
			info.put("limit", items.size());
			
			JSONObject result = new JSONObject();
			result.put("type", "itemData");
			result.put("persons", new JSONObject(peopleH));
			result.put("items", items);
			result.put("info", info);
			
			return result.toJSONString();
		} catch(Throwable t) {
			throw new IllegalArgumentException("An exception occurred while parsing duplicate issue response!", t);
		}
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject parseKEUIItem(Element node) {
		JSONObject result = new JSONObject();
		
		long id = Long.parseLong(node.getAttribute("id"));
		int  type = Integer.parseInt(node.getAttribute("itemType"));
		long time = Long.parseLong(node.getAttribute("time")) - 11644473600000L;
		String[] tags = node.getAttribute("tags").split(",");
		String entryId = node.getAttribute("entryId");
		String content = node.getElementsByTagName("shortContent").item(0).getTextContent();
		
		JSONArray tagsArr = new JSONArray();
		tagsArr.addAll(Arrays.asList(tags));
		
		if (type != ItemType.COMMIT.getValue()) {
			long threadId = Long.parseLong(node.getAttribute("threadId"));
			int count = Integer.parseInt(node.getAttribute("count"));
			long senderId = Long.parseLong(node.getAttribute("from"));
			String url = node.getElementsByTagName("url").getLength() > 0 ? node.getElementsByTagName("url").item(0).getTextContent() : null;
			
			String subject = node.getElementsByTagName("subject").item(0).getTextContent();
			
			Double similarity = null;
			if (node.getElementsByTagName("similarity").item(0) != null)
				similarity = Double.parseDouble(node.getElementsByTagName("similarity").item(0).getTextContent());
			
			JSONArray recipients = new JSONArray();
			if (node.getElementsByTagName("to").item(0) != null) {
				String[] recipientStrV = node.getElementsByTagName("to").item(0).getTextContent().split(",");
				for (String recIdStr : recipientStrV)
					recipients.add(Long.parseLong(recIdStr));
			}
			
			// metadata
			if (node.getElementsByTagName("metaData").getLength() > 0) {
				Element metadata = (Element) node.getElementsByTagName("metaData").item(0);
				if (metadata.getElementsByTagName("issueId").getLength() > 0)
					result.put("issueID", Long.parseLong(metadata.getElementsByTagName("issueId").item(0).getTextContent()));
			}
			
			
			result.put("id", id);
			result.put("type", type);
			result.put("time", time);
			result.put("tags", tagsArr);
			result.put("entryID", entryId);
			result.put("threadID", threadId);
			result.put("count", count);
			result.put("senderID", senderId);
			result.put("recipientIDs", recipients);
			result.put("content", content);
			result.put("subject", subject);
			result.put("url", url);
			result.put("similarity", similarity);
		} else {
			long authorId = Long.parseLong(node.getAttribute("author"));
			result.put("id", id);
			result.put("type", type);
			result.put("time", time);
			result.put("tags", tagsArr);
			result.put("entryID", entryId);
			result.put("authorID", authorId);
			result.put("content", content);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Long, JSONObject> parsePeopleH(NodeList peopleNodes) {
		Map<Long, JSONObject> result = new HashMap<Long, JSONObject>();
		for (int i = 0; i < peopleNodes.getLength(); i++) {
			Element node = (Element) peopleNodes.item(i);
			JSONObject person = new JSONObject();
			
			long id = Long.parseLong(node.getAttribute("id"));
			
			person.put("id", id);
			person.put("account", node.getAttribute("account"));
			person.put("name", node.getAttribute("name"));
			result.put(id, person);
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	public String parseKEUISuggestMessage(String responseMsg) {
		try {
			// parse the suggestions and return JSON
			JSONArray jsonArray = new JSONArray();
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = xmlFactory.newDocumentBuilder();
			Document xmlDoc = builder.parse(new ByteArrayInputStream(responseMsg.getBytes()));
			
			NodeList suggRootNodes = xmlDoc.getElementsByTagName("suggestions");
			if (suggRootNodes.getLength() != 1)
				throw new IllegalArgumentException("The suggestion message has 0 or more then 1 suggestion nodes!");
			
			Node suggRoot = suggRootNodes.item(0);
			NodeList suggNodes = suggRoot.getChildNodes();
			for (int i = 0; i < suggNodes.getLength(); i++) {
				Node suggNode = suggNodes.item(i);
				NamedNodeMap attributes = suggNode.getAttributes();
				
				String tagName = suggNode.getNodeName();
				String label;
				String value;
				String type;
				if ("person".equals(tagName)) {
					label = attributes.getNamedItem("name").getNodeValue();
					value = attributes.getNamedItem("account").getNodeValue();
					type = "person";
				} else if ("concept".equals(tagName)) {
					label = attributes.getNamedItem("label").getNodeValue();
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "concept";
				} else if ("method".equals(tagName) || "file".equals(tagName) || "module".equals(tagName)) {
					label = attributes.getNamedItem("name").getNodeValue();
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "source";
				} else if ("product".equals(tagName)) {
					label = attributes.getNamedItem("label").getNodeValue();
					value = attributes.getNamedItem("uri").getNodeValue();
					type = "product";
				} else if ("issue".equals(tagName)) {
					label = attributes.getNamedItem("label").getNodeValue();
					value = label;
					type = "issue";
				} else throw new IllegalArgumentException("An unexpected suggestion node appeared in the KEUI response: " + tagName);
			
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("label", label);
				jsonObj.put("value", value);
				jsonObj.put("type", type);
				
				jsonArray.add(jsonObj);
			}
			
			return jsonArray.toJSONString();
		} catch (Throwable t) {
			throw new IllegalArgumentException("An unexpected exception occurred while parsing KEUI suggestion response!!", t);
		}
	}
}
