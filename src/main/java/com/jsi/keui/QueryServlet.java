package com.jsi.keui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.keui.utils.MessageUtils;
import com.jsi.keui.utils.Utils;

/**
 * Servlet implementation class QueryServlet
 */
public class QueryServlet extends KEUIServlet {
	
	private enum QueryType {
		PEOPLE("peopleData"),
		KEYWORD("keywordData"),
		TIMELINE("timelineData"),
		ITEM("itemData"),
		ISSUE_DETAILS("issueDetails"),
		COMMIT_DETAILS("commitDetails"),
		ITEM_DETAILS("itemDetails"),
		SUGGEST_PEOPLE("suggestPeople");
		
		private final String value;
		
		private QueryType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}

	private static final long serialVersionUID = 1079144340811966229L;
	
	private static final Logger log = LoggerFactory.getLogger(QueryServlet.class);

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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> parameterMap = request.getParameterMap();
		
		if (log.isDebugEnabled())
			log.debug("Received query request...");

		try {
			if (!parameterMap.containsKey(TYPE_PARAM))
				throw new IllegalArgumentException("The request doesn't contain parameter '" + TYPE_PARAM + "'!");
	
			String type = request.getParameter(TYPE_PARAM);
	
			// compose a message and send it to activeMQ
			MessageUtils msgUtils = MessageUtils.getInstance();
			String resultJSon = "";
			if (QueryType.PEOPLE.getValue().equals(type) || QueryType.KEYWORD.getValue().equals(type) || QueryType.TIMELINE.getValue().equals(type) || QueryType.ITEM.getValue().equals(type)) {
				// gather the parameters
				Properties props = new Properties();
				for (String key : parameterMap.keySet()) {
					String value = request.getParameter(key);
					if (value != null && !value.isEmpty())
						props.put(key, value);
				}
				
				if (log.isDebugEnabled())
					log.debug("Generating KEUI message with parameters: " + props.toString());
				
				final String requestId = Utils.genRequestID();
				if (QueryType.PEOPLE.getValue().equals(type)) {
					String requestMsg = msgUtils.genKEUIPeopleMessage(props, requestId);
					String responseMsg = getKEUIResponse(requestMsg, requestId);
					resultJSon = msgUtils.parseKEUIPeopleResponse(responseMsg);
				}
				else if (QueryType.TIMELINE.getValue().equals(type)) {
					String requestMsg = msgUtils.genKEUITimelineMessage(props, requestId);
					String responseMsg = getKEUIResponse(requestMsg, requestId);
					resultJSon = msgUtils.parseKEUITimelineResponse(responseMsg);
				}
				else if (QueryType.ITEM.getValue().equals(type)) {
					String requestMsg = msgUtils.getKEUIItemsMessage(props, requestId);
					String responseMsg = getKEUIResponse(requestMsg, requestId);
					resultJSon = msgUtils.parseKEUIItemsResponse(responseMsg);
				} 
				else {	// keywords
					String requestMsg = msgUtils.genKEUIKeywordMessage(props, requestId);
					String responseMsg = getKEUIResponse(requestMsg, requestId);
					resultJSon = msgUtils.parseKEUIKeywordsResponse(responseMsg);
				}
			}
			else if (QueryType.ISSUE_DETAILS.getValue().equals(type)) {
				// check if the ID is a number
				String itemId = request.getParameter(QUERY_PARAM);
				String requestMsg = msgUtils.genIssueDetailsMsg(itemId);
				
				String responseMsg = getApiIssueResponse(requestMsg);
				resultJSon = msgUtils.parseIssueDetailsMsg(responseMsg);
			}
			else if (QueryType.COMMIT_DETAILS.getValue().equals(type)) {
				String itemId = request.getParameter(QUERY_PARAM);
				String requestMsg = msgUtils.getCommitDetailsMsg(itemId);
				
				String responseMsg = getApiCommitResponse(requestMsg);
				resultJSon = msgUtils.parseCommitDetailsMessage(responseMsg);
			}
			else if (QueryType.ITEM_DETAILS.getValue().equals(type)) {
				String itemId = request.getParameter(QUERY_PARAM);
				String requestId = Utils.genRequestID();
				String requestMsg = msgUtils.genKEUIItemDetailsMessage(itemId, requestId);
				
				String responseMsg = getKEUIResponse(requestMsg, requestId);
				resultJSon = msgUtils.parseItemDetailsMsg(responseMsg);
			}
			else if (QueryType.SUGGEST_PEOPLE.getValue().equals(type)) {
				String people = request.getParameter("people");
				throw new IllegalArgumentException("NOT IMPLEMENTED YET!!!");
				/*if (log.isDebugEnabled())
					log.debug("Suggesting people: " + people);	// TODO not implemented
				return;*/
			} 
			else
				throw new IllegalArgumentException("An unexpected query type: " + type + "!");
			
			// write to output stream
			response.setContentType("text/json");
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write(resultJSon);
			writer.flush();
			writer.close();
		} catch (Throwable ex) {
			log.error("An unexpected exception occurred!", ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private String getApiCommitResponse(String requestMsg) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\"><soap:Header/><soap:Body><wsnt:Notify><wsnt:NotificationMessage><wsnt:Topic/><wsnt:ProducerReference><wsa:Address>http://www.alert-project.eu/metadata</wsa:Address></wsnt:ProducerReference><wsnt:Message><ns1:event xmlns:ns1=\"http://www.alert-project.eu/\" xmlns:o=\"http://www.alert-project.eu/ontoevents-mdservice\" xmlns:r=\"http://www.alert-project.eu/rawevents-forum\" xmlns:r1=\"http://www.alert-project.eu/rawevents-mailinglist\" xmlns:r2=\"http://www.alert-project.eu/rawevents-wiki\" xmlns:s=\"http://www.alert-project.eu/strevents-kesi\" xmlns:s1=\"http://www.alert-project.eu/strevents-keui\" xmlns:s2=\"http://www.alert-project.eu/APIcall-request\" xmlns:s3=\"http://www.alert-project.eu/APIcall-response\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.alert-project.eu/alert-root.xsd\"><ns1:head><ns1:sender>METADATASERVICE</ns1:sender><ns1:timestamp>10000</ns1:timestamp><ns1:sequencenumber>1</ns1:sequencenumber></ns1:head><ns1:payload><ns1:meta><ns1:startTime>10010</ns1:startTime><ns1:endTime>10020</ns1:endTime><ns1:eventName>ALERT.Metadata.APICallResponse</ns1:eventName><ns1:eventId>9856</ns1:eventId><ns1:eventType>reply</ns1:eventType></ns1:meta><ns1:eventData><ns1:apiResponse><s3:apiCall>commit.getInfo</s3:apiCall><s3:responseData><s3:commitRepositoryUri>http://www.alert-project.eu/ontologies/alert_scm.owl#Repository1</s3:commitRepositoryUri><s3:commitRevisionTag>1</s3:commitRevisionTag><s3:commitAuthor><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person1</s3:uri><s3:name>Sasa Stojanovic</s3:name><s3:id>sasa.stojanovic@cimgrupa.eu</s3:id><s3:email>sasa.stojanovic@cimgrupa.eu</s3:email></s3:commitAuthor><s3:commitCommitter><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person2</s3:uri><s3:name>Ivan Obradovic</s3:name><s3:id>ivan.obradovic@cimgrupa.eu</s3:id><s3:email>ivan.obradovic@cimgrupa.eu</s3:email></s3:commitCommitter><s3:commitDate>Tue, 17 Feb 2009 16:31:37 +0100</s3:commitDate><s3:commitMessageLog>comment of commit</s3:commitMessageLog><s3:commitFile><s3:fileUri>http://www.alert-project.eu/ontologies/alert_scm.owl#File1</s3:fileUri><s3:fileId>11111</s3:fileId><!-- file action (Add, Copy, Delete, Modify, Rename, Replace) --><s3:fileAction>Add</s3:fileAction><s3:fileName>filename1</s3:fileName><s3:fileBranch>branch1</s3:fileBranch><s3:fileModules><s3:moduleUri>http://www.alert-project.eu/ontologies/alert_scm.owl#Module1</s3:moduleUri><s3:moduleId>1111</s3:moduleId><s3:moduleName>Mod111</s3:moduleName><s3:moduleStartLine>100</s3:moduleStartLine><s3:moduleEndLine>199</s3:moduleEndLine><s3:moduleMethods><s3:methodUri>http://www.alert-project.eu/ontologies/alert.owl#Method1</s3:methodUri><s3:methodId>111</s3:methodId><s3:methodName>Meth111</s3:methodName><s3:methodStartLine>100</s3:methodStartLine><s3:methodEndLine>149</s3:methodEndLine></s3:moduleMethods><s3:moduleMethods><s3:methodUri>http://www.alert-project.eu/ontologies/alert.owl#Method2</s3:methodUri><s3:methodId>112</s3:methodId><s3:methodName>Meth112</s3:methodName><s3:methodStartLine>150</s3:methodStartLine><s3:methodEndLine>199</s3:methodEndLine></s3:moduleMethods></s3:fileModules><s3:fileModules><s3:moduleUri>http://www.alert-project.eu/ontologies/alert_scm.owl#Module2</s3:moduleUri><s3:moduleId>1211</s3:moduleId><s3:moduleName>Mod1211</s3:moduleName><s3:moduleStartLine>200</s3:moduleStartLine><s3:moduleEndLine>299</s3:moduleEndLine><s3:moduleMethods><s3:methodUri>http://www.alert-project.eu/ontologies/alert.owl#Method3</s3:methodUri><s3:methodId>121</s3:methodId><s3:methodName>Meth121</s3:methodName><s3:methodStartLine>200</s3:methodStartLine><s3:methodEndLine>299</s3:methodEndLine></s3:moduleMethods></s3:fileModules></s3:commitFile><s3:commitFile><s3:fileUri>http://www.alert-project.eu/ontologies/alert_scm.owl#File2</s3:fileUri><s3:fileId>21111</s3:fileId><!-- file action (Add, Copy, Delete, Modify, Rename, Replace) --><s3:fileAction>Copy</s3:fileAction><s3:fileName>filename2</s3:fileName><s3:fileBranch>branch2</s3:fileBranch><s3:fileModules><s3:moduleUri>http://www.alert-project.eu/ontologies/alert_scm.owl#Module3</s3:moduleUri><s3:moduleId>2111</s3:moduleId><s3:moduleName>Mod2111</s3:moduleName><s3:moduleStartLine>300</s3:moduleStartLine><s3:moduleEndLine>399</s3:moduleEndLine><s3:moduleMethods><s3:methodUri>http://www.alert-project.eu/ontologies/alert.owl#Method4</s3:methodUri><s3:methodId>211</s3:methodId><s3:methodName>Meth211</s3:methodName><s3:methodStartLine>300</s3:methodStartLine><s3:methodEndLine>399</s3:methodEndLine></s3:moduleMethods></s3:fileModules></s3:commitFile><s3:commitProduct><s3:productUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Product1</s3:productUri><s3:productId>solid</s3:productId><s3:productComponentUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Component1</s3:productComponentUri><s3:productComponentId>general</s3:productComponentId><s3:productVersion>unspecified</s3:productVersion></s3:commitProduct><s3:references><s3:issue><s3:issueUri></s3:issueUri><s3:issueDescription></s3:issueDescription></s3:issue></s3:references></s3:responseData></ns1:apiResponse></ns1:eventData></ns1:payload></ns1:event></wsnt:Message></wsnt:NotificationMessage></wsnt:Notify></soap:Body></soap:Envelope>";
	}

	private String getApiIssueResponse(String requestMsg) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\"><soap:Header/><soap:Body><wsnt:Notify><wsnt:NotificationMessage><wsnt:Topic/><wsnt:ProducerReference><wsa:Address>http://www.alert-project.eu/metadata</wsa:Address></wsnt:ProducerReference><wsnt:Message><ns1:event xmlns:ns1=\"http://www.alert-project.eu/\" xmlns:o=\"http://www.alert-project.eu/ontoevents-mdservice\" xmlns:r=\"http://www.alert-project.eu/rawevents-forum\" xmlns:r1=\"http://www.alert-project.eu/rawevents-mailinglist\" xmlns:r2=\"http://www.alert-project.eu/rawevents-wiki\" xmlns:s=\"http://www.alert-project.eu/strevents-kesi\" xmlns:s1=\"http://www.alert-project.eu/strevents-keui\" xmlns:s2=\"http://www.alert-project.eu/APIcall-request\" xmlns:s3=\"http://www.alert-project.eu/APIcall-response\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.alert-project.eu/alert-root.xsd\"><ns1:head><ns1:sender>METADATASERVICE</ns1:sender><ns1:timestamp>10000</ns1:timestamp><ns1:sequencenumber>1</ns1:sequencenumber></ns1:head><ns1:payload><ns1:meta><ns1:startTime>10010</ns1:startTime><ns1:endTime>10020</ns1:endTime><ns1:eventName>ALERT.Metadata.APICallResponse</ns1:eventName><ns1:eventId>9856</ns1:eventId><ns1:eventType>reply</ns1:eventType></ns1:meta><ns1:eventData><ns1:apiResponse><s3:apiCall>issue.getInfo</s3:apiCall><s3:responseData><s3:issueAssignedTo><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person2</s3:uri><s3:email>afiestas@kde.org</s3:email><s3:id>afiestas@kde.org</s3:id><s3:name>Alex Fiestas</s3:name></s3:issueAssignedTo><s3:issueDependsOnid>10001</s3:issueDependsOnid><s3:issueDependsOnUri>http://www.alert-project.eu/ontologies/alert_its.owl#Bug4</s3:issueDependsOnUri><s3:issueId>184671</s3:issueId><s3:issueAttachment><s3:attachmentUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Attachment1</s3:attachmentUri><s3:attachmentCreator><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person3</s3:uri><s3:email>angel_blue_co2004@yahoo.com</s3:email><s3:id>angel_blue_co2004@yahoo.com</s3:id><s3:name>Angel Blue</s3:name></s3:attachmentCreator><s3:attachmentType>doc</s3:attachmentType><s3:attachmentFilename>name</s3:attachmentFilename><s3:attachmentId>3532</s3:attachmentId></s3:issueAttachment><s3:issueUrl>https://bugs.kde.org/show_bug.cgi?id=184671</s3:issueUrl><s3:issueProduct><s3:productComponentUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Component1</s3:productComponentUri><s3:productComponentid>general</s3:productComponentid><s3:productUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Product1</s3:productUri><s3:productid>solid</s3:productid><s3:productVersion>unspecified</s3:productVersion></s3:issueProduct><s3:issueAuthor><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person1</s3:uri><s3:email>cumulus0007@gmail.com</s3:email><s3:id>cumulus0007@gmail.com</s3:id><s3:name>Sander Pientka</s3:name></s3:issueAuthor><s3:issueKeyword>keyword</s3:issueKeyword><s3:issueComputerSystem><s3:computerSystemUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#ComputerSystem1</s3:computerSystemUri><s3:computerSystemPlatform>Ubuntu Packages</s3:computerSystemPlatform><s3:computerSystemOS>Linux</s3:computerSystemOS></s3:issueComputerSystem><s3:issueCCPerson><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person3</s3:uri><s3:email>angel_blue_co2004@yahoo.com</s3:email><s3:id>angel_blue_co2004@yahoo.com</s3:id><s3:name>Angel Blue</s3:name></s3:issueCCPerson><s3:issueDependsOnid>10002</s3:issueDependsOnid><s3:issueDependsOnUri>http://www.alert-project.eu/ontologies/alert_its.owl#Bug5</s3:issueDependsOnUri><s3:issueMilestone><s3:milestoneUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Milestone1</s3:milestoneUri><s3:milestoneid>12355</s3:milestoneid><s3:milestoneTarget>Tue, 17 Feb 2009 16:31:00 +0100</s3:milestoneTarget></s3:issueMilestone><s3:issueBlocksid>20001</s3:issueBlocksid><s3:issueBlocksUri>http://www.alert-project.eu/ontologies/alert_its.owl#Bug2</s3:issueBlocksUri><s3:issueComment><s3:commentUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Comment1</s3:commentUri><s3:commentPerson><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person1</s3:uri><s3:email>cumulus0007@gmail.com</s3:email><s3:id>cumulus0007@gmail.com</s3:id><s3:name>Sander Pientka</s3:name></s3:commentPerson><s3:commentText>Version: (using KDE 4.2.0) OS: Linux Installed from: Ubuntu Packages It would be awesome if KDE notifies te user of newly installed hardware, missing drivers, programs to use the connected device, etc.</s3:commentText><s3:commentDate>Tue, 17 Feb 2009 16:31:37 +0100</s3:commentDate><s3:commentNumber>0</s3:commentNumber></s3:issueComment><s3:issueBlocksid>20002</s3:issueBlocksid><s3:issueBlocksUri>http://www.alert-project.eu/ontologies/alert_its.owl#Bug3</s3:issueBlocksUri><s3:issueResolution>Fixed</s3:issueResolution><s3:issueDateOpened>Tue, 17 Feb 2009 16:31:00 +0100</s3:issueDateOpened><s3:issueDescription>Notify user on hardware changes 2</s3:issueDescription><s3:issueActivity><s3:activityUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Activity2</s3:activityUri><s3:activityWho>http://www.alert-project.eu/ontologies/alert_scm.owl#Person4</s3:activityWho><s3:activityWhen>Fri, 15 Apr 2011 10:55:57 +0200</s3:activityWhen><s3:activityAdded>afiestas@kde.org</s3:activityAdded><s3:activityRemoved>ervin@kde.org</s3:activityRemoved><s3:activityWhat>AssignedTo</s3:activityWhat><s3:activityId>3631</s3:activityId></s3:issueActivity><s3:issueActivity><s3:activityUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Activity1</s3:activityUri><s3:activityWho>http://www.alert-project.eu/ontologies/alert_scm.owl#Person3</s3:activityWho><s3:activityWhen>Wed, 4 Mar 2009 18:11:19 +0100</s3:activityWhen><s3:activityAdded>4</s3:activityAdded><s3:activityRemoved>3</s3:activityRemoved><s3:activityWhat>Priority</s3:activityWhat><s3:activityId>4125</s3:activityId></s3:issueActivity><s3:issueLastModified>Fri, 15 Apr 2011 10:55:57 +0200</s3:issueLastModified><s3:issueComment><s3:commentUri>http://www.ifi.uzh.ch/ddis/evoont/2008/11/bom#Comment2</s3:commentUri><s3:commentPerson><s3:uri>http://www.alert-project.eu/ontologies/alert_scm.owl#Person3</s3:uri><s3:email>angel_blue_co2004@yahoo.com</s3:email><s3:id>angel_blue_co2004@yahoo.com</s3:id><s3:name>Angel Blue</s3:name></s3:commentPerson><s3:commentText>If a driver is missing, clicking on the notification should open the package manager (by default) so the user can find a driver</s3:commentText><s3:commentDate>Wed, 4 Mar 2009 18:11:19 +0100</s3:commentDate><s3:commentNumber>1</s3:commentNumber></s3:issueComment><s3:issueStatus>Resolved</s3:issueStatus><s3:annotations><s3:threadId>12</s3:threadId><s3:issueDescriptionAnnotated><![CDATA[Notify <concept id=\"KDE/user\">user</concept> on <concept id=\"http://ailab.ijs.si/alert/resource/r364\">hardware</concept> changes <concept id=\"http://ailab.ijs.si/alert/resource/r17814\">2</concept>]]></s3:issueDescriptionAnnotated><s3:issueDescriptionConcepts><s3:concept><s3:uri>KDE/user</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r364</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r17814</s3:uri><s3:weight>1</s3:weight></s3:concept></s3:issueDescriptionConcepts><s3:issueComment><s3:commentTextAnnotated><![CDATA[<concept id=\"http://ailab.ijs.si/alert/resource/r18520\">Version</concept>: (using <concept id=\"KDE/KDE_4\">KDE 4</concept>.<concept id=\"http://ailab.ijs.si/alert/resource/r17814\">2</concept>.0) OS: <concept id=\"http://ailab.ijs.si/alert/resource/r14937\">Linux</concept><concept id=\"http://ailab.ijs.si/alert/resource/r18126\">Installed</concept> from: <concept id=\"http://ailab.ijs.si/alert/resource/r18117\">Ubuntu</concept><concept id=\"http://ailab.ijs.si/alert/resource/r17650\">Packages</concept> It would be awesome if <concept id=\"KDE/KDE\">KDE</concept> notifies te <concept id=\"KDE/user\">user</concept> of newly <concept id=\"http://ailab.ijs.si/alert/resource/r18126\">installed</concept><concept id=\"http://ailab.ijs.si/alert/resource/r364\">hardware</concept>, missing <concept id=\"http://ailab.ijs.si/alert/resource/r17629\">drivers</concept>, <concept id=\"KDE/programming\">programs</concept> to use the connected <concept id=\"KDE/devices\">device</concept>, etc.]]></s3:commentTextAnnotated><s3:commentTextConcepts><s3:concept><s3:uri>KDE/devices</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>KDE/user</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r18520</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r17629</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r17650</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r18117</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r364</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>KDE/KDE_4</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r14937</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>KDE/programming</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>KDE/KDE</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r17814</s3:uri><s3:weight>1</s3:weight></s3:concept><s3:concept><s3:uri>http://ailab.ijs.si/alert/resource/r18126</s3:uri><s3:weight>2</s3:weight></s3:concept></s3:commentTextConcepts><s3:itemId>7805</s3:itemId></s3:issueComment></s3:annotations><s3:references><s3:issue><s3:issueUri></s3:issueUri><s3:issueDescription></s3:issueDescription></s3:issue><s3:commit><s3:commitUri></s3:commitUri><s3:commitMessageLog></s3:commitMessageLog></s3:commit><s3:email><s3:emailUri></s3:emailUri><s3:subject></s3:subject></s3:email><s3:forumPost><s3:postUri></s3:postUri><s3:subject></s3:subject></s3:forumPost></s3:references></s3:responseData></ns1:apiResponse></ns1:eventData></ns1:payload></ns1:event></wsnt:Message></wsnt:NotificationMessage></wsnt:Notify></soap:Body></soap:Envelope>";
	}
}
