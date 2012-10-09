package com.jsi.alert.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.alert.model.UserPrincipal;
import com.jsi.alert.service.AuthenticatorService;
import com.jsi.alert.utils.Configuration;
import com.jsi.alert.utils.MessageParser;
import com.jsi.alert.utils.MessageUtils;
import com.jsi.alert.utils.Utils;

/**
 * Servlet implementation class QueryServlet
 */
public class QueryServlet extends MQServlet {
	
	private enum QueryType {
		PEOPLE("peopleData"),
		KEYWORD("keywordData"),
		TIMELINE("timelineData"),
		ITEM("itemData"),
		ISSUE_DETAILS("issueDetails"),
		COMMIT_DETAILS("commitDetails"),
		ITEM_DETAILS("itemDetails"),
		DUPLICATE_ISSUE("duplicateIssue"),
		SUGGEST_MY_CODE("suggestMyCode"),
		SUGGEST_FOR_PEOPLE("suggestPeople");
		
		public final String value;
		
		private QueryType(String value) {
			this.value = value;
		}
	}

	private static final long serialVersionUID = 1079144340811966229L;
	
	private static final Logger log = LoggerFactory.getLogger(QueryServlet.class);

	public static final String USER_KEY = "user";
	
	private static final String TYPE_PARAM = "type";
	private static final String QUERY_PARAM = "query";


	/**
	 * @throws JMSException
	 *             If a connection to ActiveMQ cannot be established.
	 * @see HttpServlet#HttpServlet()
	 */
	public QueryServlet() throws JMSException {
		super();
	}

	/*
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> parameterMap = request.getParameterMap();
		
		if (log.isDebugEnabled())
			log.debug("Received query request, params: " + parameterMap.toString() + "...");
		
		try {
			if (!parameterMap.containsKey(TYPE_PARAM))
				throw new IllegalArgumentException("The request doesn't contain parameter '" + TYPE_PARAM + "'!");
	
			String type = request.getParameter(TYPE_PARAM);
	
			if (QueryType.PEOPLE.value.equals(type))
				processPeopleRq(request, response);
			else if (QueryType.KEYWORD.value.equals(type))
				processKeywordRq(request, response);
			else if (QueryType.TIMELINE.value.equals(type))
				processTimelineRq(request, response);
			else if (QueryType.ITEM.value.equals(type))
				processItemsRq(request, response);
			else if (QueryType.ISSUE_DETAILS.value.equals(type))
				processIssueDetailsRq(request, response);
			else if (QueryType.COMMIT_DETAILS.value.equals(type))
				processCommitDetailsRq(request, response);
			else if (QueryType.ITEM_DETAILS.value.equals(type))
				processItemDetailsRq(request, response);
			else if (QueryType.DUPLICATE_ISSUE.value.equals(type))
				processDuplicateIssueRq(request, response);
			else if (QueryType.SUGGEST_MY_CODE.value.equals(type))
				processRelatedMyCodeRq(request, response);
			else if (QueryType.SUGGEST_FOR_PEOPLE.value.equals(type))
				processSuggestForPeopleRq(request, response);
			else
				throw new IllegalArgumentException("An unexpected query type: " + type + "!");
		} catch (Throwable ex) {
			log.error("An unexpected exception occurred!", ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private Properties createRequestProps(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		
		Properties props = new Properties();
		for (String key : parameterMap.keySet()) {
			String value = request.getParameter(key);
			if (value != null && !value.isEmpty())
				props.put(key, value);
		}
		
		return props;
	}

	private void processPeopleRq(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Properties props = createRequestProps(request);
		
		final String requestId = Utils.genRequestID();
		String requestMsg = MessageUtils.genKEUIPeopleMessage(props, requestId);
		String responseMsg = getKEUIResponse(requestMsg, requestId);
		String resultJSon = MessageParser.parseKEUIPeopleResponse(responseMsg);
		
		writeJSon(resultJSon, response);
	}
	
	private void processKeywordRq(HttpServletRequest request, HttpServletResponse response)  throws Exception {
		Properties props = createRequestProps(request);
		
		final String requestId = Utils.genRequestID();
		String requestMsg = MessageUtils.genKEUIKeywordMessage(props, requestId);
		String responseMsg = getKEUIResponse(requestMsg, requestId);
		String resultJSon = MessageParser.parseKEUIKeywordsResponse(responseMsg);
		
		writeJSon(resultJSon, response);
	}

	private void processTimelineRq(HttpServletRequest request, HttpServletResponse response)  throws Exception {
		Properties props = createRequestProps(request);
		
		final String requestId = Utils.genRequestID();
		String requestMsg = MessageUtils.genKEUITimelineMessage(props, requestId);
		String responseMsg = getKEUIResponse(requestMsg, requestId);
		String resultJSon = MessageParser.parseKEUITimelineResponse(responseMsg);
		
		writeJSon(resultJSon, response);
	}

	
	private void processItemsRq(HttpServletRequest request, HttpServletResponse response)  throws Exception {
		Properties props = createRequestProps(request);
		
		final String requestId = Utils.genRequestID();
		String requestMsg = MessageUtils.getKEUIItemsMessage(props, requestId);
		String responseMsg = getKEUIResponse(requestMsg, requestId);
		String resultJSon = MessageParser.parseKEUIItemsResponse(responseMsg);
		
		writeJSon(resultJSon, response);
	}
	
	private void processIssueDetailsRq(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// check if the ID is a number
		String itemId = request.getParameter(QUERY_PARAM);
		String requestId = Utils.genRequestID();
		
		String requestMsg = MessageUtils.genIssueDetailsMsg(itemId, requestId);
		String responseMsg = getAPIResponse(requestMsg, requestId);
		
		String resultJSon = MessageParser.parseAPIIssueDetailsMsg(responseMsg);
		writeJSon(resultJSon, response);
	}
	
	/**
	 * Processes the clients commit details request.
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void processCommitDetailsRq(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String itemId = request.getParameter(QUERY_PARAM);
		String requestId = Utils.genRequestID();

		String requestMsg = MessageUtils.getCommitDetailsMsg(itemId, requestId);
		String responseMsg = getAPIResponse(requestMsg, requestId);
		
		String resultJSon = MessageParser.parseAPICommitDetailsMessage(responseMsg);
		writeJSon(resultJSon, response);
	}
	
	/**
	 * Processes the clients Item details request.
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void processItemDetailsRq(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String itemId = request.getParameter(QUERY_PARAM);
		String requestId = Utils.genRequestID();
		String requestMsg = MessageUtils.genKEUIItemDetailsMessage(itemId, requestId);
		
		String responseMsg = getKEUIResponse(requestMsg, requestId);
		String resultJSon = MessageParser.parseKEUIItemDetailsMsg(responseMsg);
		writeJSon(resultJSon, response);
	}
	
	/**
	 * Processes the clients Duplicate issue detection request.
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void processDuplicateIssueRq(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Properties props = createRequestProps(request);
		
		String requestId = Utils.genRequestID();
		String requestMsg = MessageUtils.genKEUIDuplicateIssueMsg(props, requestId);
		
		String responseMsg = getKEUIResponse(requestMsg, requestId);
		String resultJSon = MessageParser.parseKEUIDuplicateResponse(responseMsg);
		
		writeJSon(resultJSon, response);
	}
	
	/**
	 * Processes the Issues related to my code request.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException 
	 * @throws JMSException 
	 */
	private void processRelatedMyCodeRq(HttpServletRequest request, HttpServletResponse response) throws IOException, JMSException, ServletException {
		// first check if the user is authenticated
		HttpSession session = request.getSession();
		
		boolean isAuthenticated = AuthenticatorService.authenticateUser(session);
		
		if (isAuthenticated) {
			UserPrincipal user = (UserPrincipal) session.getAttribute(Configuration.USER_PRINCIPAL);
			
			// send a message to Recommender to get the IDs of issues
			String uuid = user.getUuid();
			String requestId = Utils.genRequestID();
			
			String recommenderRq = MessageUtils.genRecommenderIssuesMsg(Arrays.asList(new String[] {uuid}), requestId);
			String recommenderResp = getRecommenderIssuesResponse(recommenderRq, requestId);
			
			List<Long> issueIds = MessageParser.parseRecommenderIssueIdsMsg(recommenderResp);
			
			// now that I have the issueIDs I have to send them to the KEUI component
			requestId = Utils.genRequestID();
			String keuiRq = MessageUtils.genKEUIIssueListMsg(issueIds, requestId);
			String keuiResp = "";	//getKEUIResponse(keuiRq, requestId);	TODO
			
			String resultJSon = MessageParser.parseKEUIItemsResponse(keuiResp);
			writeJSon(resultJSon, response);
		}
		else {
			// if no user => redirect to login
			if (log.isDebugEnabled())
				log.debug("User with no session searching for issues related to their code, redirecting to login!");
			response.sendRedirect(Configuration.LOGIN_URL);
		}
	}

	private void processSuggestForPeopleRq(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}

	
	
	private void writeJSon(String json, HttpServletResponse response) throws IOException {
		response.setContentType("text/json");
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(json);
		writer.flush();
		writer.close();
	}
	
	
	// TODO remove me
	private String getRecommenderIssuesResponse(String requestMsg, String requestId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><soap:Header>Header</soap:Header><soap:Body><wsnt:Notify><wsnt:NotificationMessage><wsnt:Topic>ALERT.Recommender.IssueRecommendation</wsnt:Topic><wsnt:ProducerReference><wsa:Address>http://www.alert-project.eu/socrates</wsa:Address></wsnt:ProducerReference><wsnt:Message><ns1:event xmlns:ns1=\"http://www.alert-project.eu/\" xmlns:o=\"http://www.alert-project.eu/ontoevents-mdservice\" xmlns:r=\"http://www.alert-project.eu/rawevents-forum\" xmlns:r1=\"http://www.alert-project.eu/rawevents-mailinglist\" xmlns:r2=\"http://www.alert-project.eu/rawevents-wiki\" xmlns:s=\"http://www.alert-project.eu/strevents-kesi\" xmlns:sm=\"http://www.alert-project.eu/stardom\" xmlns:s1=\"http://www.alert-project.eu/strevents-keui\" xmlns:sc=\"http://www.alert-project.eu/socrates\" xmlns:p=\"http://www.alert-project.eu/panteon\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.alert-project.eu/alert-root.xsd\" ><ns1:head><ns1:sender>SOCRATES</ns1:sender><ns1:timestamp>1331676396932</ns1:timestamp><ns1:sequencenumber>570749432</ns1:sequencenumber></ns1:head><ns1:payload><ns1:meta><ns1:startTime>1331676396932</ns1:startTime><ns1:endTime>1331676396937</ns1:endTime><ns1:eventName>ALERT.Recommender.IssueRecommendation</ns1:eventName><ns1:eventId>1818516212</ns1:eventId><ns1:eventType>Reply</ns1:eventType></ns1:meta><ns1:eventData><sc:issues><sc:issue><sc:id>1010</sc:id><o:bug>owl#1</o:bug></sc:issue><sc:issue><sc:id>2050</sc:id><o:bug>owl#2</o:bug></sc:issue><sc:issue><sc:id>2030</sc:id><o:bug>owl#3</o:bug></sc:issue><sc:issue><sc:id>2040</sc:id><o:bug>owl#4</o:bug></sc:issue></sc:issues></ns1:eventData></ns1:payload></ns1:event></wsnt:Message></wsnt:NotificationMessage></wsnt:Notify></soap:Body></soap:Envelope>";
	}
}
