package fixClient;

import main.JmsApplication;
import main.SendFromQueueThread;
import main.UpdatesQueue;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

/**
 */
public class FixClient {

	private static String FIX_CONFIG = "conf/Integral.cfg";
	SessionSettings settings;
	FixApp app = null;
	boolean running = true;
	JmsApplication jmsApp = null;
	UpdatesQueue eurUpdatesQ = new UpdatesQueue();
	UpdatesQueue cablUpdatesQ = new UpdatesQueue();
	UpdatesQueue audUpdatesQ = new UpdatesQueue();
	UpdatesQueue jpyUpdatesQ = new UpdatesQueue();
	UpdatesQueue cadUpdatesQ = new UpdatesQueue();
	UpdatesQueue chfUpdatesQ = new UpdatesQueue();
	SendFromQueueThread thread1 = new SendFromQueueThread(eurUpdatesQ, jmsApp, "EUR/USD");
	SendFromQueueThread thread2 = new SendFromQueueThread(cablUpdatesQ, jmsApp, "GBP/USD");
	SendFromQueueThread thread3 = new SendFromQueueThread(audUpdatesQ, jmsApp, "AUD/USD");
	SendFromQueueThread thread4 = new SendFromQueueThread(jpyUpdatesQ, jmsApp, "USD/JPY");
	SendFromQueueThread thread5 = new SendFromQueueThread(cadUpdatesQ, jmsApp, "USD/CAD");
	SendFromQueueThread thread6 = new SendFromQueueThread(chfUpdatesQ, jmsApp, "USD/CHF");

	public FixClient(JmsApplication jmsappl) {
		jmsApp = jmsappl;
	}

	public void beginFixStream() {
		try {
			settings = new SessionSettings(FixClient.class.getClassLoader()
					.getResourceAsStream(FIX_CONFIG));
			String username = settings.getString("username");
			String password = settings.getString("password");
			String pin = settings.getDefaultProperties().getProperty("pin",
					null);
			openConnection(username, password, pin);
		} catch (ConfigError | FieldConvertError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void openConnection(String username, String password, String pin) {
		try {
			app = new FixApp(username, password, pin, this);
			MessageStoreFactory storeFactory = new MemoryStoreFactory();
			LogFactory logFactory = new ScreenLogFactory();
			MessageFactory messageFactory = new DefaultMessageFactory();
			SocketInitiator initiator = new SocketInitiator(app, storeFactory,
					settings, logFactory, messageFactory);
			initiator.start();
			Thread.sleep(1000);
			while (true) {
				if (running = true) {
					Thread.sleep(1000);
					app.sendMdSubRequest("EUR/USD");
					app.sendMdSubRequest("GBP/USD");
					app.sendMdSubRequest("AUD/USD");
					app.sendMdSubRequest("USD/JPY");
					app.sendMdSubRequest("USD/CAD");
					app.sendMdSubRequest("USD/CHF");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		running = false;
	}
	
	public SendFromQueueThread[] getQueueThreads(){
		SendFromQueueThread[] array = {thread1,thread2,thread3,thread4,thread5,thread6};
		return array;
	}

	public void queuePriceUpdate(String theUpdate, String symbol) {
		switch (symbol) {
		case "EUR/USD":
			eurUpdatesQ.enqueue(theUpdate);
			break;
		case "GBP/USD":
			cablUpdatesQ.enqueue(theUpdate);
			break;
		case "AUD/USD":
			audUpdatesQ.enqueue(theUpdate);
			break;
		case "USD/JPY":
			jpyUpdatesQ.enqueue(theUpdate);
			break;
		case "USD/CAD":
			cadUpdatesQ.enqueue(theUpdate);
			break;
		case "USD/CHF":
			chfUpdatesQ.enqueue(theUpdate);
			break;
		default:
			System.out.println("Invalid symbol: broker queue not found");
			break;
		}
	}
}