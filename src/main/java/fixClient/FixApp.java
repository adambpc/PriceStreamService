package fixClient;

import java.util.Date;
import java.util.UUID;

import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.DeliverToCompID;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.MsgType;
import quickfix.field.NoMDEntries;
import quickfix.field.NoRelatedSym;
import quickfix.field.Password;
import quickfix.field.Product;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TestReqID;
import quickfix.field.TradSesReqID;
import quickfix.field.UserStatus;
import quickfix.field.Username;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.CollateralInquiryAck;
import quickfix.fix44.Heartbeat;
import quickfix.fix44.Logout;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequestReject;
import quickfix.fix44.SecurityStatus;
import quickfix.fix44.TestRequest;
import quickfix.fix44.TradingSessionStatusRequest;
import quickfix.fix44.UserResponse;

public class FixApp extends MessageCracker implements Application {

	private Date mStartSession;
	private long mMsgSent;
	private long mRequestID;
	private String mMsgReqID;
	private String mUsername;
	private String mPassword;
	private SessionID mSessionID;
	private FixClient client;

	public FixApp(String aUsername, String aPassword, String aPIN, FixClient cli) {
		mUsername = aUsername;
		mPassword = aPassword;
		client = cli;
	}

	public void fromAdmin(Message aMessage, SessionID aSessionID) {
		try {
			crack(aMessage, aSessionID);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public void fromApp(Message aMessage, SessionID aSessionID) {
		try {
			crack(aMessage, aSessionID);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private synchronized long nextID() {
		mRequestID++;
		if (mRequestID > 0x7FFFFFF0) {
			mRequestID = 1;
		}
		return mRequestID;
	}

	public void onCreate(SessionID aSessionID) {
		mSessionID = aSessionID;
	}

	public void onLogon(SessionID aSessionID) {
		mStartSession = new Date();
		System.out.println("got logon " + aSessionID);
		this.client.running = true;
		sendTestRequest();
	}

	public void onLogout(SessionID aSessionID) {
		this.client.running = false;
		System.out.println("\n\ngot logout " + aSessionID);
		System.out.println("StartSession = " + mStartSession);
		System.out.println("StopSession = " + new Date());
		System.out.println("\n\n");
	}

	public void onMessage(UserResponse aUserResponse, SessionID aSessionID)
			throws FieldNotFound {
		System.out.println("<< UserResponse = " + aUserResponse);
		if (aUserResponse.getInt(UserStatus.FIELD) == UserStatus.LOGGED_IN) {
			TradingSessionStatusRequest msg = new TradingSessionStatusRequest();
			msg.set(new TradSesReqID("TSSR REQUEST ID " + nextID()));
			msg.set(new SubscriptionRequestType(
					SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
			send(msg);
		}
	}

	public void onMessage(CollateralInquiryAck aCollateralInquiryAck,
			SessionID aSessionID) {
		System.out.println("<< Collateral Inquiry Ack = "
				+ aCollateralInquiryAck);
	}

	public void onMessage(BusinessMessageReject aBusinessMessageReject,
			SessionID aSessionID) {
		System.out.println("<< Business Message Reject = "
				+ aBusinessMessageReject);
	}

	public void onMessage(Heartbeat aMessage, SessionID aSessionID)
			throws FieldNotFound {
		if (mMsgReqID.equals(aMessage.getTestReqID().getValue())) {
			System.out.println("<< Heartbeat = " + aMessage);
			System.out.println("ROUNDTRIP MS : "
					+ (System.nanoTime() - mMsgSent) / 1000000);
		}
	}
	
	public void onMessage(MarketDataIncrementalRefresh aMarketDataIncrementalRefresh, SessionID aSessionID){
		StringBuilder sb = new StringBuilder();
		try{
			quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries parentgroup = new quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries();
			int numOfGroups = aMarketDataIncrementalRefresh.getField(new NoMDEntries()).getValue();
			Group childGroup = aMarketDataIncrementalRefresh.getGroup(1, parentgroup);
			String symbol = childGroup.getField(new Symbol()).getValue();
			char entryType = childGroup.getField(new MDEntryType("0".charAt(0))).getValue();
			Double thePrice = childGroup.getField(new MDEntryPx()).getValue();
			sb.append("{\"symbol\":\""+symbol+"\","
						+ "\"entryType\":\""+entryType+"\","
						+ "\"price\":\""+thePrice+"\"}");
			if(numOfGroups == 2){
				Group childGroup2 = aMarketDataIncrementalRefresh.getGroup(2, parentgroup);
				char entryType2 = childGroup2.getField(new MDEntryType("0".charAt(0))).getValue();
				Double thePrice2 = childGroup2.getField(new MDEntryPx()).getValue();
				sb.append("{\"symbol\":\""+symbol+"\","
						+ "\"entryType\":\""+entryType2+"\","
						+ "\"price\":\""+thePrice2+"\"}");
			}
			//System.out.println(sb.toString());
			client.publishPriceUpdate(sb.toString(), symbol);
		} catch (FieldNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onMessage(MarketDataRequestReject aReject,
			SessionID aSessionID) throws FieldNotFound {
		System.out.println("<< MarketDataRequestReject = " + aReject);
	}

	public void onMessage(SecurityStatus aSecurityStatus,
			SessionID aSessionID) throws FieldNotFound {
		System.out.println("<< SecurityStatus = " + aSecurityStatus);
	}

	public void onMessage(Logout aLogout, SessionID aSessionID) {
		System.out.println("got logout = " + aLogout);
	}

	void send(Message aMessage) {
		try {
			Session.sendToTarget(aMessage, mSessionID);
		} catch (Exception aException) {
			aException.printStackTrace();
		}
	}

	public void sendTestRequest() {
		try {
			TestRequest req = new TestRequest();
			req.set(new TestReqID(String.valueOf(nextID())));
			send(req);
			mMsgReqID = req.getTestReqID().getValue();
			mMsgSent = System.nanoTime();
		} catch (FieldNotFound aFieldNotFound) {
			aFieldNotFound.printStackTrace();
		}
	}
	
	public void sendMdSubRequest(String sym) {
		quickfix.fix44.MarketDataRequest message = new quickfix.fix44.MarketDataRequest();
		//267
		quickfix.fix44.MarketDataRequest.NoMDEntryTypes group1 = new quickfix.fix44.MarketDataRequest.NoMDEntryTypes();
		quickfix.fix44.MarketDataRequest.NoMDEntryTypes group2 = new quickfix.fix44.MarketDataRequest.NoMDEntryTypes();
		//269
		group1.set(new MDEntryType(Integer.toString(0).charAt(0)));
		group2.set(new MDEntryType(Integer.toString(1).charAt(0)));
		Group relatedSymGroup = new quickfix.fix42.QuoteRequest.NoRelatedSym();
		//55
		relatedSymGroup.setField(new Symbol(sym));
		//460
		relatedSymGroup.setField(new Product(4));
		//128
		message.setField(new DeliverToCompID("ALL"));
		//262
		message.set(new MDReqID(UUID.randomUUID().toString()));
		//263
		message.setField(new SubscriptionRequestType(Integer.toString(1).charAt(0)));
		//264
		message.setField(new MarketDepth(1));
		//265
		message.setField(new MDUpdateType(1));
		//146
		message.setField(new NoRelatedSym(1));
		
		message.addGroup(relatedSymGroup);
		message.addGroup(group1);
		message.addGroup(group2);
		System.out.println("This is the message :" + message.toString());
		send(message);
	}

	public void toAdmin(Message aMessage, SessionID aSessionID) {
		String msgType;
		try {
			msgType = aMessage.getHeader().getString(MsgType.FIELD);
			if (MsgType.LOGON.compareTo(msgType) == 0) {
				aMessage.setString(Username.FIELD, mUsername);
				aMessage.setString(Password.FIELD, mPassword);
			}
		} catch (FieldNotFound e) {
			e.printStackTrace();
		}
	}

	public void toApp(Message aMessage, SessionID aSessionID) {
	}
}