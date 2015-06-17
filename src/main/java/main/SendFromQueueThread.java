package main;

public class SendFromQueueThread implements Runnable {
	
	UpdatesQueue updatesQ = null;
	JmsApplication jmsApp = null;
	String symbol = null;
	
	public SendFromQueueThread(UpdatesQueue q, JmsApplication app, String sym){
		symbol = sym;
		updatesQ = q;
		jmsApp = app;
	}

	@Override
	public void run() {
		int sleeptime = 5000;
		try {
			while(true){
				if(!updatesQ.isEmpty()){
					System.out.println(updatesQ.peek());
					//updatesQ.dequeue();
					jmsApp.sendPriceUpdate(updatesQ.dequeue(), symbol);
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
}
