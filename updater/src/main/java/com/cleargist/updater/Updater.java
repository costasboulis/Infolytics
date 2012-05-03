package com.cleargist.updater;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.DuplicateItemNameException;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.InvalidParameterValueException;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.cleargist.model.BaseModel;
import com.cleargist.profile.BatchDataProcessorThread;
import com.cleargist.profile.Profile;
import com.cleargist.profile.ProfileProcessor;


public class Updater {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	public static String newline = System.getProperty("line.separator");
	protected List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
	protected List<DeletableItem> deletedItems = new ArrayList<DeletableItem>();
	private Logger logger = Logger.getLogger(getClass());
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private static final int NO_OF_THREADS_TO_RUN = 6;
	private static final int MAX_RECORDS_TO_PROCESS = 25;
	private BaseModel model;
	private ProfileProcessor profile;
	
	public void setModel(BaseModel model) {
		this.model = model;
	}
	
	public void setProfileProcessor(ProfileProcessor processor) {
		this.profile = processor;
	}
	
	public BaseModel getModel() {
		return this.model;
	}
	
	public ProfileProcessor getProfileProcessor() {
		return this.profile;
	}
	
	public void update(String tenantID, String latestProfile, int profHorizon) throws Exception {
		profile.updateProfiles(tenantID, latestProfile, profHorizon);
		model.createModel(tenantID);
	}
	
    public void incrementalUpdate(String tenantID, String latestProfile, int profHorizon) throws Exception {
    	
    	List<Future<List<Item>>>  newData = profile.getDataSinceLastUpdate(tenantID, latestProfile, profHorizon);
		List<Item> incrementalData = newData.get(0).get();
		List<Item> decrementalData = newData.get(1).get();
		
		List<Profile> incrementalProfiles = profile.createProfile(incrementalData);
		List<Profile> decrementalProfiles = profile.createProfile(decrementalData);
		
		// Update the model
		model.updateModel(tenantID, incrementalProfiles, decrementalProfiles);
		
		// Now update the profiles
		updateProfilesSimpleDB(incrementalProfiles, decrementalProfiles, tenantID);
    }
    
    private String getProfileDomainName(String tenantID) {
		return "PROFILE_" + tenantID;
	}
    
    private void updateProfilesSimpleDB(List<Profile> incrementalProfiles, List<Profile> decrementalProfiles, String tenantID) 
	throws Exception {
		
		// Retrieve existing profiles and merge / write to SimpleDB
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
    	String profileDomain = getProfileDomainName(tenantID);
    	
    	// Do the incremental profiles
		for (Profile incrementalProfile : incrementalProfiles) {
			String userID = incrementalProfile.getUserID();
			ReplaceableItem item = new ReplaceableItem();
			item.setName(userID);
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			HashSet<String> productIDs = new HashSet<String>();
			
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(profileDomain);
			request.setItemName(userID);
			GetAttributesResult result = sdb.getAttributes(request);
			
			// Update attributes
			for (Attribute attribute : result.getAttributes()) {
				String productID = attribute.getName();
            	Float score = null;
            	try {
            		score = Float.parseFloat(attribute.getValue());
            	}
            	catch (NumberFormatException ex) {
            		logger.error("Could not parse value " + attribute.getValue() + " ... skipping");
            		continue;
            	}
            	
				productIDs.add(productID);
            		
				Float incrementalScore = incrementalProfile.getAttributes().get(productID);
				if (incrementalScore == null) {
					continue;   // Nothing to add in this attribute
				}
				else {
					// Update the score of the attribute
					float updatedScore = score + incrementalScore.floatValue();
            			
					ReplaceableAttribute att = new ReplaceableAttribute(attribute.getName(), Float.toString(updatedScore), true);
					attributes.add(att);
				}
			}
			
			// Add new attributes
			for (Map.Entry<String, Float> incrementalProfileAttributes : incrementalProfile.getAttributes().entrySet()) {
				String productID = incrementalProfileAttributes.getKey();
				if (!productIDs.contains(productID)) {
					float score = incrementalProfileAttributes.getValue().floatValue();
					ReplaceableAttribute att = new ReplaceableAttribute(productID, Float.toString(score), true);
        			attributes.add(att);
				}
			}
			item.setAttributes(attributes);
			items.add(item);
		
		}
		// Increment profiles
		batchInsert(profileDomain);
		
		
		// Now do the decremental profiles
		for (Profile decrementalProfile : decrementalProfiles) {
			String userID = decrementalProfile.getUserID();
			
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			List<Attribute> deleteAttributes = new LinkedList<Attribute>();
			
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(profileDomain);
			request.setItemName(userID);
			GetAttributesResult result = sdb.getAttributes(request);
			for (Attribute attribute : result.getAttributes()) {
				String productID = attribute.getName();
            	Float score = null;
            	try {
            		score = Float.parseFloat(attribute.getValue());
            	}
            	catch (NumberFormatException ex) {
            		logger.error("Could not parse value " + attribute.getValue() + " ... skipping");
            		continue;
            	}
            		
				Float decrementalScore = decrementalProfile.getAttributes().get(productID);
				if (decrementalScore == null) {
					continue;  
				}
				else {
					// Update the score of the attribute
					float updatedScore = score - decrementalScore.floatValue();
					if (updatedScore <= 0.0f) {
						// delete attribute
						deleteAttributes.add(attribute);
					}
					else {	
						ReplaceableAttribute att = new ReplaceableAttribute(productID, Float.toString(updatedScore), true);
						attributes.add(att);
					}
					
				}
			}
			
			if (attributes.size() > 0) {
				// Update attributes
				ReplaceableItem item = new ReplaceableItem();
				item.setName(userID);
				item.setAttributes(attributes);
				items.add(item);
			}
			
			if (deleteAttributes.size() > 0) {
				// Delete attributes and possibly profiles
				DeletableItem item = new DeletableItem();
				item.setName(userID);
				item.setAttributes(deleteAttributes);
				// Delete any attributes with zero counts
				batchDelete(sdb, profileDomain, item, false);
			}
			
		}
		// Update the attributes that have decreased in value
		batchInsert(profileDomain);
		
		// Delete any attributes with zero counts
		batchDelete(sdb, profileDomain, null, true);
		
	}
    
    private void batchDelete(AmazonSimpleDB sdb, String domain, DeletableItem profile, boolean forceDelete) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
		
		
		if (profile != null) {
			deletedItems.add(profile);
		}
		
		if (!forceDelete) {
			if (deletedItems.size() < 25) {
				return;
			}
		}
		
		if (deletedItems.size() == 0) {
			return;
		}
		
		BatchDeleteAttributesRequest batchDeleteArgumentsRequest = new BatchDeleteAttributesRequest();
		batchDeleteArgumentsRequest.setDomainName(domain);
		batchDeleteArgumentsRequest.setItems(deletedItems);
		sdb.batchDeleteAttributes(batchDeleteArgumentsRequest);
    	
		deletedItems = new ArrayList<DeletableItem>();
    }
	
	private void batchInsert(String domain) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
		
		if (items == null || items.size() == 0) {
			items = new ArrayList<ReplaceableItem>();
			return;
		}
		
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		List<ReplaceableItem> itemsToProcess = new ArrayList<ReplaceableItem>();

		if (items.size() > MAX_RECORDS_TO_PROCESS) {
			int itemsPerThread = items.size() / NO_OF_THREADS_TO_RUN;
			for (ReplaceableItem profile : items) {
				itemsToProcess.add(profile);
				if (itemsToProcess.size() == itemsPerThread) {
					Thread batchDateProc = new BatchDataProcessorThread(sdb, itemsToProcess, domain);
					itemsToProcess = new ArrayList<ReplaceableItem>();
					batchDateProc.start();
				}
				
			}
			//write any remaining
			if (itemsToProcess.size() > 0) {
				Thread batchDateProc = new BatchDataProcessorThread(sdb, itemsToProcess, domain);
				batchDateProc.start();
			}
		} else {
			sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, items));
		}
    	
    	items = new ArrayList<ReplaceableItem>();
    }
}
