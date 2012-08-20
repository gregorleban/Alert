package com.jsi.keui;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.keui.mq.MQSessionProvider;

/**
 * An abstract <code>Servlet</code> which send a sync request to the KEUI component.
 */
public abstract class KEUIServlet extends HttpServlet {
       
	private static final long serialVersionUID = -5462790358676407606L;
	
	private static final Logger log = LoggerFactory.getLogger(KEUIServlet.class);

	private static final long MAX_KEUI_TIME = 10000L;
	
	protected static final String REQUEST_ID = "requestID";
	
	private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("<requestID>\\d+</requestID>");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
	
	protected Session mqSession;
	protected MessageConsumer mqConsumer;
	protected MessageProducer mqProducer;
	
    /**
     * @throws JMSException 
     * @see HttpServlet#HttpServlet()
     */
    public KEUIServlet() throws JMSException {
        super();
        initMQ();
    }
    
    /**
     * Creates the MQ producer and consumer.
     * @throws JMSException 
     */
    private void initMQ() throws JMSException {
    	MQSessionProvider provider = MQSessionProvider.getInstance();
    	mqSession = provider.getSession();
    	mqConsumer = provider.createConsumer();
    	mqProducer = provider.createProducer();
    }
	
	/**
	 * Receives a message, with request ID matching the parameter, from the in topic.
	 * 
	 * @param requestID
	 * @return The received message
	 * @throws ServletException 
	 * @throws JMSException 
	 */
	protected String receiveMessage(String requestID) throws ServletException, JMSException {
		
		// loop until you get the response with the correct ID
		String receivedID = null;
		String responseXML = null;
		long beginTime = System.currentTimeMillis();
		while (!requestID.equals(receivedID)) {
			// check if KEUI is taking too long
			if (System.currentTimeMillis() - beginTime > MAX_KEUI_TIME)
				throw new ServletException("KEUI timed out!");
			
			Message receivedMsg = mqConsumer.receive(2000);
			if (receivedMsg instanceof TextMessage) {
				TextMessage received = (TextMessage) receivedMsg;
				responseXML = received.getText();
				
				// check the ID
				Matcher matcher = REQUEST_ID_PATTERN.matcher(responseXML);
				
				if (matcher.find()) {
					String idTag = matcher.group(0);
					
					Matcher idMatcher = NUMBER_PATTERN.matcher(idTag);
					if (idMatcher.find())
						receivedID = idMatcher.group(0);
				}
			}
		}
		
		return responseXML;
	}
	
	protected String getKEUIResponse(String requestMsg, String requestId) throws JMSException, ServletException {
		if (log.isDebugEnabled()) log.debug("Sending message to KEUI component...");
		
		Message mqMsg = mqSession.createTextMessage(requestMsg);
		mqProducer.send(mqMsg);
		String responseXML = receiveMessage(requestId);
		
		if (log.isDebugEnabled()) log.debug("Received response from KEUI component...");
		
		return responseXML;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
