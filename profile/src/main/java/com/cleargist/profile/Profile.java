package com.cleargist.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class Profile {
	private String userID;
	private Logger logger = Logger.getLogger(getClass());
	private HashMap<String, Float> attributes;
	
	public Profile() {
		this.attributes = new HashMap<String, Float>();
	}
	
	public Profile(String profileString) {
		this.attributes = new HashMap<String, Float>();
		
		if (profileString == null) {
			logger.error("Cannot initialize profile since the input string is null");
			return;
		}
		String[] fields = profileString.split(" ");
		
		if (fields.length < 2) {
			logger.error("Profile \"" + profileString + "\" is too short");
			return;
		}
		
		this.userID = fields[0];
		for (int i = 1; i < fields.length - 1; i += 2) {
			String productID = fields[i];
			Float score = null;
			try {
				score = Float.parseFloat(fields[i + 1]);
			}
			catch (NumberFormatException ex) {
				logger.error("Could not parse to float the score " + fields[i + 1]);
				return;
			}
			
			add(productID, score);
		}
	}
	
	public void merge(Profile profile) {
		HashMap<String, Float> otherAttributes = profile.getAttributes();
		Set<String> keys = this.attributes.keySet();
		for (String productID : keys) {
			Float otherScore = otherAttributes.get(productID);
			if (otherScore != null) {
				add(productID, otherScore);
			}
		}
		
		for (Map.Entry<String, Float> entry : otherAttributes.entrySet()) {
			Float f = this.attributes.get(entry.getKey());
			if (f != null) {
				continue;
			}
			this.attributes.put(entry.getKey(), entry.getValue());
		}
	}
	
	public void reduce(Profile profile) {
		HashMap<String, Float> otherAttributes = profile.getAttributes();
		for (Map.Entry<String, Float> entry : otherAttributes.entrySet()) {
			Float f = this.attributes.get(entry.getKey());
			if (f == null || f < entry.getValue()) {
				continue;
			}
			if (f == entry.getValue()) {
				this.attributes.remove(entry.getKey());
			}
			else {
				this.attributes.put(entry.getKey(), f - entry.getValue());
			}
		}
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
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.userID); 
		for (Map.Entry<String, Float> entry : this.attributes.entrySet()) {
			sb.append(";"); sb.append(entry.getKey()); sb.append(";"); sb.append(entry.getValue());
		}
		return sb.toString();
	}
}
