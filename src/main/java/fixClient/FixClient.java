package fixClient;
import main.JmsApplication;
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
	
	public FixClient(JmsApplication jmsappl){
		jmsApp = jmsappl;
	}
			
	public void beginFixStream(){
		try {
			settings = new SessionSettings(FixClient.class.getClassLoader().getResourceAsStream(FIX_CONFIG));
			String username = settings.getString("username");
			String password = settings.getString("password");
			String pin = settings.getDefaultProperties().getProperty("pin", null);
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
			SocketInitiator initiator = new SocketInitiator(app, storeFactory, settings, logFactory, messageFactory);
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
	
	public void shutdown(){
		running = false;
	}

	public void publishPriceUpdate(String theUpdate, String symbol) {
			jmsApp.sendPriceUpdate(theUpdate, symbol);
	}
}