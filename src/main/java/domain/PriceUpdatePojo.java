package domain;

import java.io.Serializable;

public class PriceUpdatePojo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StringBuffer updates;
	private String symbol;
	
	public void setSymbol(String sym){
		this.symbol = sym;
	}
	
	public String getSymbol(){
		return symbol;
	}
	
	public void addUpdate(String theUpdate){
		if(updates.length() == 0){
			updates.append("{\"updates\":[\n");
		}
		updates.append(theUpdate);
		updates.append("\n");
	}
	
	public String toJSON(){
		updates.append("]}");
		return updates.toString();
	}
}