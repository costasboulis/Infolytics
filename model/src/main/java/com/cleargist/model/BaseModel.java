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
	
	public List<Catalog.Products.Product> getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception {
		
		String sourceItemId = null;
		MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        	List<Catalog.Products.Product> cacheCollection = new ArrayList<Catalog.Products.Product>();
        	try {
        		System.out.println("Step 1.");
        		StringBuffer sb = new StringBuffer();
        		sb.append(filter.getName()); sb.append("_"); sb.append(getDomainBasename()); sb.append(tenantID);
        		for (String p : productIds) {
        			sb.append("_"); sb.append(p);
        		}
        		sourceItemId = sb.toString();
        		cacheCollection = (List<Catalog.Products.Product>) client.get(sourceItemId);
        		System.out.println(sourceItemId);
        		if (cacheCollection != null) {
        			System.out.println("Cache Hit.");
        			System.out.println(cacheCollection);
        			List<Catalog.Products.Product> productList = (List<Catalog.Products.Product>) cacheCollection;
        			
        			// Update health metric
        			String eventID = productList.size() > 0 ? "OK" : "EMPTY";
        			String key = getHealthMetricKey(eventID, tenantID, "RECS");
        			client.incr(key, 1);
        			
        			
                	return productList;
                } 
        	}
        	catch (OperationTimeoutException ex) {
        		System.out.println("Timeout accessing memcached.");
        	}
        }
        catch (IOException ex) {
        	System.out.println("Cannot insantiate memcached client");
        }
        
    	System.out.println("Cache Miss.");
        
        List<Catalog.Products.Product> recommendedProducts = new ArrayList<Catalog.Products.Product>();
        try {
        	recommendedProducts = getRecommendedProductsInternal(productIds, tenantID, filter);
        	System.out.println(recommendedProducts);
        }
        catch (Exception ex) {
        	// Update health metric
    		String key = getHealthMetricKey("FAILED", tenantID, "RECS");
    		client.incr(key, 1);
    		
        	throw new Exception();
        }
        
        if (client != null) {
        	try {
        		client.set(sourceItemId, TTL_CACHE, new Integer(1));
        		//client.set(sourceItemId, TTL_CACHE, (Object)recommendedProducts);
        		
        		// Update health metric
        		String eventID = recommendedProducts.size() > 0 ? "OK" : "EMPTY";
        		String key = getHealthMetricKey(eventID, tenantID, "RECS");
        		client.incr(key, 1);
        		
        		
        		client.shutdown(10, TimeUnit.SECONDS);
        	}
        	catch (Exception ex) {
        		System.out.println("Cannot write to memcached " + MEMCACHED_SERVER + " port " + MEMCACHED_PORT);
        	}
        }
        
        
        
    	/*Integer integ = new Integer(288);
        System.out.println(integ);
        client.set("myNewKey", 900, integ);
        Object myObject=client.get("joe");
        System.out.println(myObject);*/
        
        return recommendedProducts;
	}
	
	public List<Catalog.Products.Product> getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception {
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
        			logger.debug("Cache Hit.");
                	return (List<Catalog.Products.Product>) cacheCollection;
                } 
        	}
        	catch (OperationTimeoutException ex) {
        		logger.warn("Timeout accessing memcached.");
        	}
            
        }
        catch (IOException ex) {
        	logger.warn("Cannot insantiate memcached client");
        }
        
        logger.debug("Cache Miss.");
        
        List<Catalog.Products.Product> recommendedProducts = getPersonalizedRecommendedProductsInternal(userId, tenantID, filter);
        
        if (client != null) {
        	try {
        		client.set(sourceItemId, TTL_CACHE, new Integer(1));
        		client.shutdown(10, TimeUnit.SECONDS);
        	}
        	catch (Exception ex) {
        		logger.warn("Cannot write to memcached " + MEMCACHED_SERVER + " port " + MEMCACHED_PORT);
        	}
        }
        
        return recommendedProducts;
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
