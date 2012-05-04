package com.cleargist.model;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.cleargist.catalog.entity.jaxb.Catalog;
import com.cleargist.profile.Profile;

 

public abstract class BaseModel implements Modelable {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	protected static final String STATS_BASE_BUCKETNAME = "tmpstats";      // Base name of the S3 bucket name
	protected static final String MERGED_STATS_FILENAME = "merged.txt";    // Name of the merged suff. stats file in S3 and local file system
	protected static final String STATS_BASE_FILENAME = "partialStats";    // Base name of the suff. stats file in S3 and local file system 
	private static final String MODEL_STATES_DOMAIN = "MODEL_STATES";    // SimpleDB domain where model states are stored
	protected static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	// Cache parameters
	private static final String MEMCACHED_SERVER = "176.34.191.239";
    private static final int MEMCACHED_PORT = 11211;
    private int TTL_CACHE = 60 * 60 * 24;   // This must be the same as the model update rate
    
    public abstract void updateModel(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) 
	throws AmazonServiceException, AmazonClientException, Exception;
    
    public abstract void createModel(String tenantID) throws AmazonServiceException, AmazonClientException, Exception;
    
	protected abstract void calculateSufficientStatistics(String bucketName, String baseFilename, String tenantID) throws Exception;
	
	protected abstract void mergeSufficientStatistics(String tenantID) throws Exception;
	
	protected abstract void estimateModelParameters(String tenantID) throws Exception;
	
	private String getHealthMetricKey(String eventID, String tenantID, String serviceID) {
		StringBuffer sb = new StringBuffer();
		sb.append(eventID); sb.append("_"); sb.append(tenantID); sb.append("_"); sb.append(serviceID);
		return sb.toString();
	}
	
	private void resetHealthCounters(String tenantID) {
		MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
    	}
    	catch (IOException ex) {
        	logger.warn("Cannot insantiate memcached client");
        }
		String healthMetricKey = getHealthMetricKey("OK", tenantID, "RECS");
    	client.set(healthMetricKey, 0, 0);
    	healthMetricKey = getHealthMetricKey("FAILED", tenantID, "RECS");
    	client.set(healthMetricKey, 0, 0);
    	healthMetricKey = getHealthMetricKey("EMPTY", tenantID, "RECS");
    	client.set(healthMetricKey, 0, 0);
	}
	
	public long getHealthCounter(String eventID, String tenantID, String serviceID) {
		MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
    	}
    	catch (IOException ex) {
        	logger.warn("Cannot insantiate memcached client");
        	return -1;
        }
    	String key = getHealthMetricKey(eventID, tenantID, serviceID);
		return client.incr(key, 0);
	}
	
	public ModelResponse getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception {
		
		String sourceItemId = null;
		MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        	try {
        		StringBuffer sb = new StringBuffer();
        		sb.append(filter.getName()); sb.append("_"); sb.append(getDomainBasename()); sb.append(tenantID);
        		for (String p : productIds) {
        			sb.append("_"); sb.append(p);
        		}
        		sourceItemId = sb.toString();
        		ModelResponse cacheCollection = (ModelResponse) client.get(sourceItemId);
        		if (cacheCollection != null) {
        			return cacheCollection;
                } 
        	}
        	catch (OperationTimeoutException ex) {
        		logger.error("Timeout accessing memcached.");
        	}
        }
        catch (IOException ex) {
        	logger.error("Cannot insantiate memcached client");
        }
        
        List<Catalog.Products.Product> recommendedProducts = new ArrayList<Catalog.Products.Product>();
        recommendedProducts = getRecommendedProductsInternal(productIds, tenantID, filter);
        
        if (client != null) {
        	try {
        		ModelResponse modelResponse = new ModelResponse(recommendedProducts, true);
        		client.set(sourceItemId, TTL_CACHE, (Object)modelResponse);
        		client.shutdown(10, TimeUnit.SECONDS);
        	}
        	catch (Exception ex) {
        		logger.error("Cannot write to memcached " + MEMCACHED_SERVER + " port " + MEMCACHED_PORT);
        	}
        }
        
        return recommendedProducts.size() > 0 ? new ModelResponse(recommendedProducts, true) : new ModelResponse(getTopProducts(tenantID), false);
	}
	
	public List<Catalog.Products.Product> getTopProducts(String tenantID) {
		return new LinkedList<Catalog.Products.Product>();
	}
	
	public ModelResponse getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception {
		String sourceItemId = null;
		MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        	Object cacheCollection = null;
        	try {
        		StringBuffer sb = new StringBuffer();
        		sb.append(filter.getName()); sb.append("_"); sb.append(getDomainBasename()); sb.append(tenantID);
        		sb.append("_USER_"); sb.append(userId);
        		sourceItemId = sb.toString();
        		cacheCollection = client.get(sourceItemId);
        		if (cacheCollection != null) {
                	return (ModelResponse) cacheCollection;
                } 
        	}
        	catch (OperationTimeoutException ex) {
        		logger.warn("Timeout accessing memcached.");
        	}
        }
        catch (IOException ex) {
        	logger.warn("Cannot insantiate memcached client");
        }
        
        
        List<Catalog.Products.Product> recommendedProducts = getPersonalizedRecommendedProductsInternal(userId, tenantID, filter);
        
        if (client != null) {
        	try {
        		ModelResponse modelResponse = new ModelResponse(recommendedProducts, true);
        		client.set(sourceItemId, TTL_CACHE, (Object)modelResponse);
        		client.shutdown(10, TimeUnit.SECONDS);
        	}
        	catch (Exception ex) {
        		logger.warn("Cannot write to memcached " + MEMCACHED_SERVER + " port " + MEMCACHED_PORT);
        	}
        }
        
        return recommendedProducts.size() > 0 ? new ModelResponse(recommendedProducts, true) : new ModelResponse(getTopProducts(tenantID), false);
	}
	
	public abstract List<Catalog.Products.Product> getRecommendedProductsInternal(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public abstract List<Catalog.Products.Product> getPersonalizedRecommendedProductsInternal(String userId, String tenantID, Filter filter) throws Exception;
	
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
    	sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	
    	String profileDomain = getProfileDomainName(tenantID);
    	GetAttributesRequest request = new GetAttributesRequest();
		request.setDomainName(profileDomain);
		request.setItemName(userID);
		GetAttributesResult result = sdb.getAttributes(request);
		
		
		List<AttributeObject> profile = new LinkedList<AttributeObject>();
		for (Attribute attribute : result.getAttributes()) {
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
				BaseModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		}
		catch (Exception ex) {
			logger.warn("Could not connect to SimpleDB");
			StringBuffer sb = new StringBuffer();
	    	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
        	return sb.toString();
		}
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		GetAttributesRequest request = new GetAttributesRequest();
		request.setDomainName(MODEL_STATES_DOMAIN);
		request.setItemName(tenantID);
		GetAttributesResult result = sdb.getAttributes(request);
		
		if (result.getAttributes().size() == 0) {
			logger.warn("No entry found in domain " + MODEL_STATES_DOMAIN + " for tenant " + tenantID);
			StringBuffer sb = new StringBuffer();
	    	sb.append(baseModelName); sb.append(tenantID); sb.append("_A");
	    	return sb.toString();
		}
        for (Attribute attribute : result.getAttributes()) {
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
				BaseModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		}
		catch (Exception ex) {
			logger.warn("Could not set value for model " + baseModelName + " for tenant " + tenantID);
			return;
		}
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
		attributes.add(new ReplaceableAttribute(baseModelName, value, true));
		try {
			sdb.putAttributes(new PutAttributesRequest(MODEL_STATES_DOMAIN, tenantID, attributes));
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
    		logger.warn("Could not find backup model for " + baseModelName + " for tenant " + tenantID);
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
