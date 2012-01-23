package com.cleargist.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.cleargist.catalog.entity.jaxb.Catalog;


public abstract class Model {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	protected static final String STATS_BASE_BUCKETNAME = "tmpstats";      // Base name of the S3 bucket name
	private static final String MERGED_STATS_FILENAME = "merged.txt";    // Name of the merged suff. stats file in S3 and local file system
	private static final String STATS_BASE_FILENAME = "partialStats";    // Base name of the suff. stats file in S3 and local file system 
	private static final String MODEL_STATES_DOMAIN = "MODEL_STATES";    // SimpleDB domain where model states are stored
	
	public void createModel(String tenantID) 
	throws AmazonServiceException, AmazonClientException, Exception {
		
		String bucketName = getStatsBucketName(tenantID);
		
		calculateSufficientStatistics(bucketName, STATS_BASE_FILENAME, tenantID);
		
		mergeSufficientStatistics(bucketName, MERGED_STATS_FILENAME, tenantID);
		
		estimateModelParameters(bucketName, MERGED_STATS_FILENAME, tenantID);
		
    	swapModelDomainNames(getDomainBasename(), tenantID);
	}
	
	protected abstract void calculateSufficientStatistics(String bucketName, String baseFilename, String tenantID) throws Exception;
	
	protected abstract void mergeSufficientStatistics(String bucketName, String mergedStatsFilename, String tenantID) throws Exception;
	
	protected abstract void estimateModelParameters(String bucketName, String filename, String tenantID) throws Exception;
	
	public abstract List<Catalog.Products.Product> getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public abstract List<Catalog.Products.Product> getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception;
	
    protected abstract String getDomainBasename();
    
    protected abstract String getStatsBucketName(String tenantID);
    
    protected String getProfileDomainName(String tenantID) {
    	return "PROFILE_" + tenantID;
    }
    
    protected List<AttributeObject> getUserProfile(String userID, String tenantID) throws Exception {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	
    	String profileDomain = getProfileDomainName(tenantID);
		String selectExpression = "select * from `" + profileDomain + "` where itemName() = '" + userID + "' limit 1";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		List<Item> items = sdb.select(selectRequest).getItems();
		if (items == null || items.size() == 0) {
			return new LinkedList<AttributeObject>();
		}
		List<AttributeObject> profile = new LinkedList<AttributeObject>();
		Item existingItem = items.get(0);
		for (Attribute attribute : existingItem.getAttributes()) {
			if (attribute.getName().startsWith("Attribute")) {
				String value = attribute.getValue();
				String[] parsedValue = value.split(";");
				String productID = parsedValue[0];
				float score = Float.parseFloat(parsedValue[1]);
				
				profile.add(new AttributeObject(productID, (double)score));
			}
		}
		return profile;
	}
    
    protected String getPrimaryModelDomainName(String baseModelName, String tenantID) {
		AmazonSimpleDB sdb = null;
		try {
			sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				Model.class.getResourceAsStream(AWS_CREDENTIALS)));
		}
		catch (Exception ex) {
			logger.warn("Could not connect to SimpleDB");
			StringBuffer sb = new StringBuffer();
	    	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
        	return sb.toString();
		}
		
		String selectExpression = "select * from `" + MODEL_STATES_DOMAIN + "` where itemName() = '" + tenantID + "' limit 1";
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        SelectResult selectResult = null;
        try {
        	selectResult = sdb.select(selectRequest);
        }
        catch (Exception ex) {
			logger.warn("Error while searching for state for model " + baseModelName + " for tenant " + tenantID);
			StringBuffer sb = new StringBuffer();
	    	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
        	return sb.toString();
		}
        List<Item> items = selectResult.getItems();
        if (items == null || items.size() == 0) {
        	logger.warn("Did not find state for model " + baseModelName + " for tenant " + tenantID);
        	StringBuffer sb = new StringBuffer();
        	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
        	return sb.toString();
        }
        Item item = items.get(0);
        for (Attribute attribute : item.getAttributes()) {
        	if (attribute.getName().startsWith(baseModelName)) {
        		return attribute.getValue();
        	}
        }
        logger.warn("Did not find state for model " + baseModelName + " for tenant " + tenantID);
        StringBuffer sb = new StringBuffer();
    	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
    	return sb.toString();
    }
    
    protected void setModelDomainName(String baseModelName, String value, String tenantID) {
		AmazonSimpleDB sdb = null;
		try {
			sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				Model.class.getResourceAsStream(AWS_CREDENTIALS)));
		}
		catch (Exception ex) {
			logger.warn("Could not set value for model " + baseModelName + " for tenant " + tenantID);
			return;
		}
		
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
		attributes.add(new ReplaceableAttribute(baseModelName, value, true));
		try {
			sdb.putAttributes(new PutAttributesRequest("MODEL_STATES", tenantID, attributes));
		}
		catch (Exception ex) {
			logger.warn("Could not set value for model " + baseModelName + " for tenant " + tenantID);
			return;
		}
	}
	
    protected String getBackupModelDomainName(String baseModelName, String tenantID) {
    	String primaryDomainName = getPrimaryModelDomainName(baseModelName, tenantID);
    	StringBuffer sb = new StringBuffer();
    	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
    	if (primaryDomainName.equals(sb.toString())) {
    		sb = new StringBuffer();
        	sb.append(baseModelName); sb.append(tenantID); sb.append("_B");
        	return sb.toString();
    	}
    	sb = new StringBuffer();
    	sb.append(baseModelName); sb.append(tenantID); sb.append("_B");
    	if (primaryDomainName.equals(sb.toString())) {
    		sb = new StringBuffer();
        	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
        	return sb.toString();
    	}
    	else {
    		logger.warn("Could not find bacup model for " + baseModelName + " for tenant " + tenantID);
    		sb = new StringBuffer();
        	sb.append(baseModelName); sb.append(tenantID); sb.append("_B");
        	return sb.toString();
    	}
    }
    
    protected void swapModelDomainNames(String baseModelName, String tenantID) {
    	String backupDomainName = getBackupModelDomainName(baseModelName, tenantID);
    	setModelDomainName(baseModelName, backupDomainName, tenantID);
    }
}
