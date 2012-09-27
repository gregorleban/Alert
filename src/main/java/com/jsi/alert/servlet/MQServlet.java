package com.jsi.alert.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

import com.jsi.alert.mq.MQSessionProvider;
import com.jsi.alert.mq.MQSessionProvider.ComponentKey;
import com.jsi.alert.utils.Configuration;

/**
 * An abstract <code>Servlet</code> which send a sync request to the KEUI component.
 */
public abstract class MQServlet extends HttpServlet {
       
	private static final long serialVersionUID = -5462790358676407606L;
	
	private static final Logger log = LoggerFactory.getLogger(MQServlet.class);

	private static final long MAX_RESPONSE_TIME = 10000L;
	
	private static final String REQUEST_ID_TAG = "<ns1:eventId>\\d+</ns1:eventId>";
	private static final Pattern REQUEST_ID_PATTERN = Pattern.compile(REQUEST_ID_TAG);
	
	private Session mqSession;
	protected Map<ComponentKey, MessageProducer> producerH;
	protected Map<ComponentKey, MessageConsumer> consumerH;
    
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
    public void init() throws ServletException {
		if (log.isDebugEnabled())
			log.debug("Initialising a new Servlet...");
		
    	try {
			initMQ();
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
			throw new ServletException(t);
		}
    }
    
    /**
     * Creates the MQ producer and consumer.
     * @throws JMSException 
     * @throws IOException 
     */
    private void initMQ() throws JMSException, IOException {
    	MQSessionProvider provider = MQSessionProvider.getInstance();
    	
    	mqSession = provider.getSession();
    	
    	producerH = new HashMap<>();
    	consumerH = new HashMap<>();
    	
    	producerH.put(ComponentKey.KEUI, provider.getKEUIProducer());
    	producerH.put(ComponentKey.API, provider.getAPIProducer());
    	
    	consumerH.put(ComponentKey.KEUI, provider.getKEUIConsumer());
    	consumerH.put(ComponentKey.API, provider.getAPIConsumer());
    }
    
    private void sendMessage(String requestMsg, ComponentKey componentKey) throws JMSException {
    	if (log.isDebugEnabled()) {
    		log.debug("Sending message to " + componentKey + " component...");
    		if (Configuration.LOG_EVENTS)
    			log.debug(requestMsg);
    	}
    	
    	MessageProducer producer = producerH.get(componentKey);
    	Message msg = mqSession.createTextMessage(requestMsg);
    	producer.send(msg);
    	
    	if (log.isDebugEnabled())
    		log.debug("Message sent!");
    }
	
	/**
	 * Receives a message, with request ID matching the parameter, from the in topic.
	 * 
	 * @param requestID
	 * @return The received message
	 * @throws ServletException 
	 * @throws JMSException 
	 */
	private String receiveMessage(String requestID, ComponentKey componentKey) throws ServletException, JMSException {
		if (log.isDebugEnabled())
			log.debug("Receiving response from the " + componentKey + " component...");
		
		MessageConsumer consumer = consumerH.get(componentKey);
		
		// loop until you get the response with the correct ID
		String receivedID = null;
		String responseMsg = null;
		long beginTime = System.currentTimeMillis();
		while (!requestID.equals(receivedID)) {
			// check if KEUI is taking too long
			if (System.currentTimeMillis() - beginTime > MAX_RESPONSE_TIME)
				throw new ServletException(componentKey + " timed out!");
			
			Message receivedMsg = consumer.receive(2000);
			if (receivedMsg instanceof TextMessage) {
				TextMessage received = (TextMessage) receivedMsg;
				responseMsg = received.getText();
				
				// check the ID
				Matcher matcher = REQUEST_ID_PATTERN.matcher(responseMsg);				
				if (matcher.find()) {
					String idTag = matcher.group(0);
					if (REQUEST_ID_TAG.replace("\\d+", requestID).equals(idTag))
						break;
				}
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Received response from the " + componentKey + " component!");
			if (Configuration.LOG_EVENTS)
				log.debug(responseMsg);
		}
		
		return responseMsg;
	}
	
	private String getMqResponse(String requestMsg, String requestId, ComponentKey componentKey) throws JMSException, ServletException {
		sendMessage(requestMsg, componentKey);
		return receiveMessage(requestId, componentKey);
	}
	
	/**
	 * Sends a message to the KEUI component and receives the response.
	 */
	protected String getKEUIResponse(String requestMsg, String requestId) throws JMSException, ServletException {
		return getMqResponse(requestMsg, requestId, ComponentKey.KEUI);
	}
	
	/**
	 * Sends a message to the API component and receives the response.
	 */
	protected String getAPIResponse(String requestMsg, String requestId) throws JMSException, ServletException {
		return getMqResponse(requestMsg, requestId, ComponentKey.API);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		if (log.isDebugEnabled()) log.debug("Destroying a servlet, closing ActiveMQ consumers and producers...");
		try {
			// clean up
			for (ComponentKey key : producerH.keySet()) {
				producerH.get(key).close();
				consumerH.get(key).close();
			}
			
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
		}
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
