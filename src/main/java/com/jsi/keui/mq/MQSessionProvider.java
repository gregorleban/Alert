package com.jsi.keui.mq;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
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
	
	private static final Logger log = LoggerFactory.getLogger(MQSessionProvider.class);
	
	private static final String DEFAULT_MQ_URL = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static final String DEFAULT_IN_TOPIC = "ALERT.KEUI.Response";
	private static final String DEFAULT_OUT_TOPIC = "ALERT.*.KEUIRequest";
	
	private String mqUrl;
	private String inTopic;
	private String outTopic;
	
	private static MQSessionProvider instance;
	
	private Session mqSession;
	private Destination inDestination, outDestination;
	
	
	private MQSessionProvider() throws JMSException {
		readProps();
		initMQ();
	}
	
	private void readProps() {
		try {
			Properties props = new Properties();
			props.load(this.getClass().getClassLoader().getResourceAsStream("alert.properties"));
			
			mqUrl = props.getProperty("mq_url");
			inTopic = props.getProperty("topic.keui.response");
			outTopic = props.getProperty("topic.keui.request");
		} catch (Throwable ex) {
			log.error("Failed to read properties file! Setting default values.", ex);
			
			mqUrl = DEFAULT_MQ_URL;
			inTopic = DEFAULT_IN_TOPIC;
			outTopic = DEFAULT_OUT_TOPIC;
		}
	}
	
	/**
	 * Initializes the connection to ActiveMQ.
	 * 
	 * @throws JMSException
	 */
	private void initMQ() throws JMSException {
		ConnectionFactory factory = new ActiveMQConnectionFactory(mqUrl);
		javax.jms.Connection mqConnection = factory.createConnection();
		mqConnection.start();

		mqSession = mqConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		outDestination = mqSession.createTopic(outTopic);
		inDestination = mqSession.createTopic(inTopic);
	}
	
	public MessageProducer createProducer() throws JMSException {
		return mqSession.createProducer(outDestination);
	}
	
	public MessageConsumer createConsumer() throws JMSException {
		return mqSession.createConsumer(inDestination);
	}
	
	public Session getSession() {
		return mqSession;
	}
	
	public static synchronized MQSessionProvider getInstance() throws JMSException {
		if (instance == null)
			instance = new MQSessionProvider();
		return instance;
	}
}
