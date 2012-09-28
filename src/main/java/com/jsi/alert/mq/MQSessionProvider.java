package com.jsi.alert.mq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton class which has a reference to the ActiveMQ session.
 * 
 * @author Luka Stopar
 *
 */
public class MQSessionProvider {
	
	public enum ComponentKey {
		KEUI,
		API
	}
	
	private static final Logger log = LoggerFactory.getLogger(MQSessionProvider.class);
	
	private static MQSessionProvider instance;
	
	private Session mqSession;
	private Map<ComponentKey, Topic> requestTopics, responseTopics;
	
	public static synchronized MQSessionProvider getInstance() throws JMSException, IOException {
		if (instance == null)
			instance = new MQSessionProvider();
		return instance;
	}
	
	private MQSessionProvider() throws JMSException, IOException {
		initMQ();
	}
	
	/**
	 * Initializes the connection to ActiveMQ.
	 * 
	 * @throws JMSException
	 * @throws IOException 
	 */
	private void initMQ() throws JMSException, IOException {
		log.info("Initializing ActiveMQ...");
		
		// read the properties
		if (log.isDebugEnabled()) log.debug("Reading properties...");
		Properties props = new Properties();
		props.load(this.getClass().getClassLoader().getResourceAsStream("alert.properties"));
		
		// init MQ
		if (log.isDebugEnabled()) log.debug("Creating connections...");
		ConnectionFactory factory = new ActiveMQConnectionFactory(props.getProperty("mq_url"));
		javax.jms.Connection mqConnection = factory.createConnection();
		mqConnection.start();

		mqSession = mqConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		if (log.isDebugEnabled()) log.debug("Creating consumers and producers...");
		requestTopics = new HashMap<>();
		responseTopics = new HashMap<>();
		
		// initiate consumers/producers
		requestTopics.put(ComponentKey.KEUI, mqSession.createTopic(props.getProperty("topic.keui.request")));
		requestTopics.put(ComponentKey.API, mqSession.createTopic(props.getProperty("topic.api.request")));
		
		responseTopics.put(ComponentKey.KEUI, mqSession.createTopic(props.getProperty("topic.keui.response")));
		responseTopics.put(ComponentKey.API, mqSession.createTopic(props.getProperty("topic.api.response")));
		
		log.info("Initialization finished!");
	}
	
	/**
	 * Creates and returns a <code>MessageProducer</code> posting on the KEUI request topic.
	 * 
	 * @return
	 * @throws JMSException 
	 */
	public MessageProducer getKEUIProducer() throws JMSException {
		return getProducer(ComponentKey.KEUI);
	}
	
	/**
	 * Creates and returns a <code>MessageConsumer</code> listening on the KEUI response topic.
	 * 
	 * @return
	 * @throws JMSException 
	 */
	public MessageConsumer getKEUIConsumer() throws JMSException {
		return getConsumer(ComponentKey.KEUI);
	}
	
	/**
	 * Creates and returns a <code>MessageProducer</code> posting on the API request topic.
	 * 
	 * @return
	 * @throws JMSException 
	 */
	public MessageProducer getAPIProducer() throws JMSException {
		return getProducer(ComponentKey.API);
	}
	
	/**
	 * Creates and returns a <code>MessageConsumer</code> listening on the API response topic.
	 * 
	 * @return
	 * @throws JMSException 
	 */
	public MessageConsumer getAPIConsumer() throws JMSException {
		return getConsumer(ComponentKey.API);
	}
	
	private MessageProducer getProducer(ComponentKey componentKey) throws JMSException {
		return mqSession.createProducer(requestTopics.get(componentKey));
	}
	
	private MessageConsumer getConsumer(ComponentKey componentKey) throws JMSException {
		return mqSession.createConsumer(responseTopics.get(componentKey));
	}
	
	public Session getSession() {
		return mqSession;
	}
}
