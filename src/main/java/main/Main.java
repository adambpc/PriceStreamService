package main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fixClient.FixClient;

public class Main {

	public static void main(String[] args) {
		
		FixClient client = null;
		try {
			JmsApplication jmsApp = new JmsApplication();
			jmsApp.getConnection();
			ExecutorService es = Executors.newFixedThreadPool(6);
			client = new FixClient(jmsApp); 
			SendFromQueueThread[] threadArray = client.getQueueThreads();
			for(SendFromQueueThread thread : threadArray){
				es.execute(thread);
			}
			client.beginFixStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}