package com.cleargist.profile;



import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.AttributeDoesNotExistException;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DuplicateItemNameException;
import com.amazonaws.services.simpledb.model.InvalidParameterValueException;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.MissingParameterException;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;

public abstract class ProfileProcessor {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final String DATE_PATTERN = "yyMMddHHmmssSSSz";
	private Logger logger = Logger.getLogger(getClass());
	
	protected List<List<Item>> getDataSinceLastUpdate(String tenantID) throws Exception {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
    	
		// Retrieve the date of the last profile update
    	Calendar lastUpdate = Calendar.getInstance();
    	lastUpdate.set(2012, 0, 6, 16, 56, 20);       // Last update, retrieve this from tenant profile
		
		Calendar lastUpdateFrom = Calendar.getInstance();
		lastUpdateFrom.setTime(lastUpdate.getTime());     
		lastUpdateFrom.add(Calendar.MONTH, -6);      // Profile horizon, retrieve this from tenant profile
		
		Calendar currentDateFrom = Calendar.getInstance();
		Date currentDate = new Date();
		currentDateFrom.setTime(currentDate);     
		currentDateFrom.add(Calendar.MONTH, -6);      // Retrieve this from tenant profile
		
		
		// Now form the SELECT statement for incremental data
		String userActivityDomain = "DATA_" + tenantID;
		String selectExpression = "select * from `" + userActivityDomain + "` where DATE > '" + formatter.format(lastUpdate.getTime()) + "'";
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        List<Item> incrementalData = sdb.select(selectRequest).getItems();
        
		// Now form the SELECT statement for decremental data
        selectExpression = "select * from `" + userActivityDomain + "` where DATE > '" + formatter.format(lastUpdateFrom.getTime()) + 
        "' and DATE < '" + formatter.format(currentDateFrom.getTime()) + "'";
        selectRequest = new SelectRequest(selectExpression);
        List<Item> decrementalData = sdb.select(selectRequest).getItems();
        
		List<List<Item>> newData = new ArrayList<List<Item>>();
		newData.add(incrementalData);
		newData.add(decrementalData);
		
		return newData;
	}
	
	// Gets as input the raw data, implements custom weighting, filtering logic and produces a profile of the form UID, <PID, VALUE>+
	protected abstract List<Profile> createProfile(List<Item> rawData) throws Exception;
	
	public void updateProfiles(String tenantID) throws Exception {
		List<List<Item>> newData = getDataSinceLastUpdate(tenantID);
		List<Item> incrementalData = newData.get(0);
		List<Item> decrementalData = newData.get(1);
		
		List<Profile> incrementalProfiles = createProfile(incrementalData);
		List<Profile> decrementalProfiles = createProfile(decrementalData);
		
		// Retrieve existing profiles and merge / write to SimpleDB
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	String profileDomain = "PROFILE_" + tenantID;
    	
    	// Do the incremental profiles
		for (Profile incrementalProfile : incrementalProfiles) {
			String userID = incrementalProfile.getUserID();
			boolean newUser = true;
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			HashSet<String> productIDs = new HashSet<String>();
			
			String selectExpression = "select * from `" + profileDomain + "` where USER_ID = '" + userID + "' limit 1";
			SelectRequest selectRequest = new SelectRequest(selectExpression);
			List<Item> items = sdb.select(selectRequest).getItems();
			if (items != null && items.size() > 0) {
				newUser = false;
				Item existingItem = items.get(0);
				for (Attribute attribute : existingItem.getAttributes()) {
					if (attribute.getName().startsWith("Attribute")) {
						String value = attribute.getValue();
						String[] parsedValue = value.split(";");
						String productID = parsedValue[0];
						float score = Float.parseFloat(parsedValue[1]);
		            	
						productIDs.add(productID);
		            		
						Float incrementalScore = incrementalProfile.getAttributes().get(productID);
						if (incrementalScore == null) {
							continue;   // Nothing to add in this attribute
						}
						else {
							// Update the score of the attribute
							float updatedScore = score + incrementalScore.floatValue();
							StringBuffer sb = new StringBuffer();
							sb.append(productID); sb.append(";"); sb.append(updatedScore);
		            			
							ReplaceableAttribute att = new ReplaceableAttribute(attribute.getName(), sb.toString(), true);
							attributes.add(att);
						}
					}
				}
			}
			
			if (newUser) {
				// Add new user
				ReplaceableItem item = new ReplaceableItem();
				item.setName(userID);
				ReplaceableAttribute nameAttribute = new ReplaceableAttribute("USER_ID", userID, true);
				attributes.add(nameAttribute);
				for (Map.Entry<String, Float> incrementalProfileAttributes : incrementalProfile.getAttributes().entrySet()) {
					String productID = incrementalProfileAttributes.getKey();
					float score = incrementalProfileAttributes.getValue().floatValue();
        			StringBuffer sb = new StringBuffer();
        			sb.append(productID); sb.append(";"); sb.append(score);
        			String attributeName = "Attribute_" + productID;
					ReplaceableAttribute att = new ReplaceableAttribute(attributeName, sb.toString(), true);
        			attributes.add(att);
				}
				item.setAttributes(attributes);
				addUserProfile(sdb, profileDomain, item);
			}
			else {
				// Update only the attributes
				// Get the new attributes
				for (Map.Entry<String, Float> incrementalProfileAttributes : incrementalProfile.getAttributes().entrySet()) {
					String productID = incrementalProfileAttributes.getKey();
					if (!productIDs.contains(productID)) {
						float score = incrementalProfileAttributes.getValue().floatValue();
	        			StringBuffer sb = new StringBuffer();
	        			sb.append(productID); sb.append(";"); sb.append(score);
	        			String attributeName = "Attribute_" + productID;
						ReplaceableAttribute att = new ReplaceableAttribute(attributeName, sb.toString(), true);
	        			attributes.add(att);
					}
				}
				
				// Update the profiles
				updateAttributes(sdb, profileDomain, userID, attributes);
			}
			
		
		}
		
		// Now do the decremental profiles
		for (Profile decrementalProfile : decrementalProfiles) {
			String userID = decrementalProfile.getUserID();
			
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			List<Attribute> deleteAttributes = new LinkedList<Attribute>();
			
			String selectExpression = "select * from `" + profileDomain + "` where USER_ID = '" + userID + "' limit 1";
			SelectRequest selectRequest = new SelectRequest(selectExpression);
			Item existingItem = sdb.select(selectRequest).getItems().get(0);
			for (Attribute attribute : existingItem.getAttributes()) {
				if (attribute.getName().startsWith("Attribute")) {
					String value = attribute.getValue();
					String[] parsedValue = value.split(";");
					String productID = parsedValue[0];
					float score = Float.parseFloat(parsedValue[1]);
	            		
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
							StringBuffer sb = new StringBuffer();
							sb.append(productID); sb.append(";"); sb.append(updatedScore);
		            			
							ReplaceableAttribute att = new ReplaceableAttribute(attribute.getName(), sb.toString(), true);
							attributes.add(att);
						}
						
					}
				}
			}
			
			// Update the profiles
			updateAttributes(sdb, profileDomain, userID, attributes);
			
			// Delete attributes
			if (deleteAttributes.size() > 0) {
				try {
					sdb.deleteAttributes(new DeleteAttributesRequest(profileDomain, userID, deleteAttributes));
				}
				catch (InvalidParameterValueException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " because of invalid parameter value" + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new InvalidParameterValueException(errorMessage);
		    	}
				catch (NoSuchDomainException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new NoSuchDomainException(errorMessage);
		    	}
				catch (AttributeDoesNotExistException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new AttributeDoesNotExistException(errorMessage);
		    	}
				catch (MissingParameterException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new MissingParameterException(errorMessage);
		    	}
				catch (AmazonServiceException ase) {
		    		String errorMessage = "Cannot delete from domain, Amazon Service error (" + ase.getErrorType().toString() + ")" + " " + ase.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new AmazonServiceException(errorMessage);
		        }
		    	catch (AmazonClientException ace) {
		            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
		                + "a serious internal problem while trying to communicate with SimpleDB, "
		                + "such as not being able to access the network " + " " + ace.getStackTrace();
		            logger.error(errorMessage);
		    		throw new AmazonClientException(errorMessage);
		        }
		    	catch (Exception ex) {
		    		String errorMessage = "Cannot delete from SimpleDB";
		    		logger.error(errorMessage);
		    		throw new Exception();
		    	}
			}
			
		}
	}
	
	private void updateAttributes(AmazonSimpleDB sdb, String profileDomain, String userID, List<ReplaceableAttribute> attributes) throws Exception {
		if (attributes.size() == 0) {
			return;
		}
		try {
			sdb.putAttributes(new PutAttributesRequest(profileDomain, userID, attributes));
		}
		catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because of invalid parameter value" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new InvalidParameterValueException(errorMessage);
    	}
		catch (NumberDomainBytesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because max number of domain bytes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainBytesExceededException(errorMessage);
    	}
		catch (NumberDomainAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because max number of domain attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainAttributesExceededException(errorMessage);
    	}
		catch (NumberItemAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because max number of item attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberItemAttributesExceededException(errorMessage);
    	}
    	catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + profileDomain + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NoSuchDomainException(errorMessage);
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot write in SimpleDB, Amazon Service error (" + ase.getErrorType().toString() + ")" + " " + ase.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonServiceException(errorMessage);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network " + " " + ace.getStackTrace();
            logger.error(errorMessage);
    		throw new AmazonClientException(errorMessage);
        }
    	catch (Exception ex) {
    		String errorMessage = "Cannot write to SimpleDB";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
	}
	
	private void addUserProfile(AmazonSimpleDB sdb, String SimpleDBDomain, ReplaceableItem profile) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
		
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		items.add(profile);
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(SimpleDBDomain, items));
    	}
		catch (DuplicateItemNameException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because of duplicate item names" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new DuplicateItemNameException(errorMessage);
    	}
		catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because of invalid parameter value" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new InvalidParameterValueException(errorMessage);
    	}
		catch (NumberDomainBytesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of domain bytes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainBytesExceededException(errorMessage);
    	}
		catch (NumberSubmittedItemsExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of submitted items exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberSubmittedItemsExceededException(errorMessage);
    	}
		catch (NumberSubmittedAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of submitted attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberSubmittedAttributesExceededException(errorMessage);
    	}
		catch (NumberDomainAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of domain attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainAttributesExceededException(errorMessage);
    	}
		catch (NumberItemAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of item attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberItemAttributesExceededException(errorMessage);
    	}
    	catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + SimpleDBDomain + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NoSuchDomainException(errorMessage);
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot write in SimpleDB, Amazon Service error (" + ase.getErrorType().toString() + ")" + " " + ase.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonServiceException(errorMessage);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network " + " " + ace.getStackTrace();
            logger.error(errorMessage);
    		throw new AmazonClientException(errorMessage);
        }
    	catch (Exception ex) {
    		String errorMessage = "Cannot write to SimpleDB";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    }
}
