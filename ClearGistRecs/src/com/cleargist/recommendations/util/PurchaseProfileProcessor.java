package com.cleargist.recommendations.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;

public class PurchaseProfileProcessor extends ProfileProcessor {

	protected List<Profile> createProfile(List<Item> rawData) throws Exception {
		HashMap<String, HashMap<String, Float>> attributes = new HashMap<String, HashMap<String, Float>>();
		for (Item item : rawData) {
			String userID = null;
			String productID = null;
			boolean purchaseFound = false;
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equalsIgnoreCase("USERID")) {
					userID = attribute.getValue();
				}
				else if (attribute.getName().equalsIgnoreCase("PRODUCTID")) {
					productID = attribute.getValue();
				}
				else if (attribute.getName().equalsIgnoreCase("EVENT") && attribute.getValue().equalsIgnoreCase("PURCHASE")) {
					purchaseFound = true;
				}
				
				if (userID != null && productID != null && purchaseFound) {
					HashMap<String, Float> hs = attributes.get(userID);
					if (hs == null) {
						hs = new HashMap<String, Float>();
						attributes.put(userID, hs);
					}
					hs.put(productID, 1.0f);
					
					break;
				}
			}
		}
		
		List<Profile> profiles = new LinkedList<Profile>();
		for (Map.Entry<String, HashMap<String, Float>> pr : attributes.entrySet()) {
			Profile profile = new Profile();
			String userID = pr.getKey();
			HashMap<String, Float> hs = pr.getValue();
			profile.setUserID(userID);
			for (Map.Entry<String, Float> me : hs.entrySet()) {
				profile.add(me.getKey(), me.getValue());
			}
			profiles.add(profile);
		}
		
		return profiles;
	}
}
