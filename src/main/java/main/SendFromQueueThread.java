package main;

import javax.jms.MessageProducer;
import javax.jms.TextMessage;

public class SendFromQueueThread implements Runnable {
	
	UpdatesQueue updatesQ = null;
	JmsApplication jmsApp = null;
	String symbol = null;
	MessageProducer producer = null;
	
	public SendFromQueueThread(UpdatesQueue q, JmsApplication app, String sym){
		symbol = sym;
		updatesQ = q;
		jmsApp = app;
		producer = app.getProducer(sym);
	}

	@Override
	public void run() {
		int sleeptime = 5000;
		try {
			while(true){
				if(!updatesQ.isEmpty()){
					try {
						String smessage = updatesQ.dequeue();
						TextMessage message = createMessage(smessage);
						//System.out.println(message.toString());
						producer.send(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					Thread.sleep(sleeptime);
					sleeptime = 500;
				}
			}
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private TextMessage createMessage(String theUpdate){
		return jmsApp.createMessage(theUpdate);
	}
}
