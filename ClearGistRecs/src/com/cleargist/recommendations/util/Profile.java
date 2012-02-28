package com.cleargist.recommendations.util;

import java.util.HashMap;
import java.util.Map;

public class Profile {
	private String userID;
	private HashMap<String, Float> attributes;
	
	public Profile() {
		this.attributes = new HashMap<String, Float>();
	}
	
	public void add(String productID, float score) {
		Float f = this.attributes.get(productID);
		if (f == null) {
			this.attributes.put(productID, score);
		}
		else {
			this.attributes.put(productID, score + f.floatValue());
		}
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	
	public String getUserID() {
		return this.userID;
	}
	
	public HashMap<String, Float> getAttributes() {
		return this.attributes;
	}
	
	public boolean equals(Profile otherProfile) {
		if (otherProfile == null || otherProfile.getAttributes() == null) {
			return false;
		}
		if (this.userID == null || otherProfile.getUserID() == null || !otherProfile.getUserID().equals(this.userID)) {
			return false;
		}
		
		HashMap<String, Float> otherAttributes = otherProfile.getAttributes();
		
		if (this.attributes == null) {
			return false;
		}
		
		if (this.attributes.size() != otherAttributes.size()) {
			return false;
		}
		
		for (Map.Entry<String, Float> me : this.attributes.entrySet()) {
			Float otherValue = otherAttributes.get(me.getKey());
			if (otherValue == null || otherValue.floatValue() != me.getValue().floatValue()) {
				return false;
			}
		}
		return true;
	}
}
