package main;

import java.util.HashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsApplication {

    private final String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private Session session = null;
    private Connection connection = null;
    
    private HashMap<String, MessageProducer> producers = new HashMap<String, MessageProducer>();
    
    public void getConnection() throws JMSException {
    	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        producers.put("EUR/USD", session.createProducer(session.createTopic("EUR/USD")));
        producers.put("GBP/USD", session.createProducer(session.createTopic("GBP/USD")));
        producers.put("AUD/USD", session.createProducer(session.createTopic("AUD/USD")));
        producers.put("USD/JPY", session.createProducer(session.createTopic("USD/JPY")));
        producers.put("USD/CAD", session.createProducer(session.createTopic("USD/CAD")));
        producers.put("USD/CHF", session.createProducer(session.createTopic("USD/CHF")));
        try {
    		TextMessage textMessage = session.createTextMessage();
			textMessage.setText("STARTED PRICE STREAMS");
			String symbol = "EUR/USD";
			producers.get(symbol).send(textMessage);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void closeConnection(){
    	try{
    		 connection.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public TextMessage createMessage(String theUpdate){
    	try{
        	TextMessage textMessage = session.createTextMessage();
    		textMessage.setText(theUpdate);
    		return textMessage;
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public MessageProducer getProducer(String sym){
    	return producers.get(sym);
    }
}