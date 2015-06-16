package main;

import fixClient.FixClient;

public class Main {

	public static void main(String[] args) {
		
		FixClient client = null;
		
		try {
			JmsApplication jmsApp = new JmsApplication();
			//jmsApp.getConnection();
			client = new FixClient(jmsApp); 
			client.beginFixStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}