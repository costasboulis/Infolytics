package com.cleargist.model;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


import org.apache.log4j.Logger;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
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
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.cleargist.catalog.entity.jaxb.Catalog;
import com.cleargist.profile.Profile;


public class CorrelationsModel extends BaseModel {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final float COOCCURRENCE_THRESHOLD = 2.0f;
	private static final double CORRELATION_THRESHOLD = 0.05;
	private int topCorrelations;
	private Logger logger = Logger.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	
	
	
	private float profilesPerChunk;
	
	
	public CorrelationsModel() {
		this.profilesPerChunk = 25000;
		this.topCorrelations = 10;
	}
	
	public void createModel(String tenantID) 
	throws AmazonServiceException, AmazonClientException, Exception {
		
		String bucketName = getStatsBucketName(tenantID);
		
		calculateSufficientStatistics(bucketName, STATS_BASE_FILENAME, tenantID);
		
		mergeSufficientStatistics(tenantID);
		
		estimateModelParameters(tenantID);
		
		// Delete suff. stats directory
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		String statsBucketName = getStatsBucketName(tenantID);
		ObjectListing objListing = s3.listObjects(statsBucketName);
		if (objListing.getObjectSummaries().size() > 0) {
			for (S3ObjectSummary objSummary : objListing.getObjectSummaries()) {
				s3.deleteObject(bucketName, objSummary.getKey());
			}
		}
		s3.deleteBucket(statsBucketName);
		
		
		// Now that the new model is ready swap the domain names
    	swapModelDomainNames(getDomainBasename(), tenantID);
    	
  /* Don't clear cache since it holds responses from all tenants. Better set the expiration time of each new entry  	
    	// Clear cache
    	MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
    	}
    	catch (IOException ex) {
        	logger.warn("Cannot insantiate memcached client");
        }
    	OperationFuture<Boolean> success = client.flush();

    	try {
    	    if (!success.get()) {
    	        logger.warn("Delete failed!");
    	    }
    	}
    	catch (Exception e) {
    	    logger.warn("Failed to delete " + e);
    	}
    	
    	resetHealthCounters(tenantID);
    	*/
	}
	
	public void updateModel(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) 
					throws AmazonServiceException, AmazonClientException, Exception {
		
		String incrStatsKey = updateSufficientStatistics(tenantID, incrementalProfiles, decrementalProfiles);
		
		String statsBucketName = getStatsBucketName(tenantID);
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		boolean doesStatsFileExist = isValidFile(s3, statsBucketName, MERGED_STATS_FILENAME);
		if (doesStatsFileExist) {
			S3Object statsObject = s3.getObject(statsBucketName, incrStatsKey);
			mergeSufficientStatistics(tenantID, MERGED_STATS_FILENAME, statsObject);
			s3.deleteObject(statsBucketName, incrStatsKey);
		}
		else {
			s3.copyObject(statsBucketName, incrStatsKey, statsBucketName, MERGED_STATS_FILENAME);
			s3.deleteObject(statsBucketName, incrStatsKey);
		}
		
		
		estimateModelParameters(tenantID);
	
    	swapModelDomainNames(getDomainBasename(), tenantID);
	}
	
	public static boolean isValidFile(AmazonS3 s3,
	        String bucketName,
	        String path) throws AmazonClientException, AmazonServiceException {
	    boolean isValidFile = true;
	    try {
	        ObjectMetadata objectMetadata = s3.getObjectMetadata(bucketName, path);
	    } catch (AmazonS3Exception s3e) {
	        if (s3e.getStatusCode() == 404) {
	        // i.e. 404: NoSuchKey - The specified key does not exist
	            isValidFile = false;
	        }
	        else {
	            throw s3e;    // rethrow all S3 exceptions other than 404   
	        }
	    }

	    return isValidFile;
	}
	
	public void setProfilesPerChunk(int n) {
		this.profilesPerChunk = n < 2500 ? 2500 : n;
	}
	
	public void setTopCorrelations(int n) {
		this.topCorrelations = n > 0 ? n : 10;
	}
	
	protected  String getDomainBasename() {
		return "MODEL_CORRELATIONS_";
	}
	
	protected String getStatsBucketName(String tenantID) {
		return STATS_BASE_BUCKETNAME + "correlations" + tenantID;
	}
	
	public List<Catalog.Products.Product> getRecommendedProductsInternal(List<String> productIDs, String tenantID, Filter filter) throws Exception {
		double weight = (double)productIDs.size();
		List<AttributeObject> productIDsInternal = new LinkedList<AttributeObject>();
		for (String productID : productIDs) {
			productIDsInternal.add(new AttributeObject(productID, weight));
			weight -= 1.0;
		}
		
		return getRecommendedProductsList(productIDsInternal, tenantID, filter);
	}
	
	private List<Catalog.Products.Product> getRecommendedProductsList(List<AttributeObject> productIds, String tenantID, Filter filter) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		HashSet<String> sourceIDs = new HashSet<String>();
    	for (AttributeObject attObject : productIds) {
    		sourceIDs.add(attObject.getUID());
    	}
    	
    	String correlationsModelDomainName = getPrimaryModelDomainName(getDomainBasename(), tenantID);
    	HashMap<String, Double> targetIds = new HashMap<String, Double>();
    	for (AttributeObject attObject : productIds) {
    		String sourceItemId = attObject.getUID();
    		GetAttributesRequest request = new GetAttributesRequest();
    		request.setDomainName(correlationsModelDomainName);
    		request.setItemName(sourceItemId);
    		GetAttributesResult result = sdb.getAttributes(request);
    		
            String targetItemId = null;
        	double score = 0.0;
            for (Attribute attribute : result.getAttributes()) {
            	String[] fields = attribute.getValue().split(";");
            	targetItemId = fields[0];
            	if (sourceIDs.contains(targetItemId)) {
            		continue;
            	}
            	score = Double.parseDouble(fields[1]);
            	
            	double weight = attObject.getScore();
            	double weightedScore = weight * score;
            	Double s = targetIds.get(targetItemId);
            	if (s == null) {
            		targetIds.put(targetItemId, weightedScore);
            	}
            	else {
            		targetIds.put(targetItemId, s.doubleValue() + weightedScore);
            	}
            }
            
    	}
    	
    	List<AttributeObject> rankedList = new ArrayList<AttributeObject>();
    	for (Map.Entry<String, Double> me : targetIds.entrySet()) {
    		rankedList.add(new AttributeObject(me.getKey(), me.getValue()));
    	}
    	Collections.sort(rankedList);
    	
    	List<String> unfilteredIDs = new ArrayList<String>();
    	for (AttributeObject attObject : rankedList) {
    		unfilteredIDs.add(attObject.getUID());
    	}
    	
    	return filter.applyFiltering(unfilteredIDs, tenantID);
	}
	
	
	
	public List<Catalog.Products.Product> getPersonalizedRecommendedProductsInternal(String userID, String tenantID, Filter filter) throws Exception {
		// Retrieve the user profile
		List<AttributeObject> sourceIDs = getUserProfile(userID, tenantID);
		return getRecommendedProductsList(sourceIDs, tenantID, filter);
	}
	
	public void mergeSufficientStatistics(String tenantID) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
		String statsBucketName = getStatsBucketName(tenantID);
    	ObjectListing objectListing = s3.listObjects(statsBucketName);
    	List<S3ObjectSummary> objSummaries = objectListing.getObjectSummaries();
    	
    	if (objSummaries.size() == 0) {
    		logger.warn("No stats files found for tenant " + tenantID + " in bucket " + statsBucketName);
    		return;
    	}
    	
    	String key = objSummaries.get(0).getKey();
    	int i = 1;
    	while (i < objSummaries.size()) {
    		String statsFilename = objSummaries.get(i).getKey();
    		S3Object statsObject = s3.getObject(statsBucketName, statsFilename);
        	
    		mergeSufficientStatistics(tenantID, key, statsObject);
    		
    		s3.deleteObject(statsBucketName, statsFilename);
    		i ++;
    	}
	}
	
	private void mergeSufficientStatistics(String tenantID, String mergedStatsFilename, S3Object statsFile) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
    	// Read stats file into memory
    	HashMap<String, HashMap<String, Float>> SS1 = new HashMap<String, HashMap<String, Float>>();
		HashMap<String, Float> SS0 = new HashMap<String, Float>();
		float numberOfProfiles = 0.0f;
		try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(statsFile.getObjectContent()));
    		String line = reader.readLine();
        	try {
        		numberOfProfiles = Float.parseFloat(line);
        	}
        	catch (NumberFormatException ex) {
        		logger.error("Cannot parse number of profiles \"" + line + "\" from file " + statsFile.getKey() 
        				+ " from bucket " + statsFile.getBucketName() + "... skipping");
        		throw new Exception();
        	}
        	// Read the SS0 part
    		while ((line = reader.readLine()) != null) {
    			String[] fields = line.split(";");
    			if (fields.length != 2) {
    				break;
    			}
                String sourceItemId = fields[0];
                float score = 0.0f;
            	try {
            		score = Float.parseFloat(fields[1]);
            	}
            	catch (NumberFormatException ex) {
            		logger.error("Cannot parse float " + fields[1] + " ... skipping");
            		throw new Exception();
            	}
            	
            	SS0.put(sourceItemId, score);
    		}
    		
    		// Read the SS1 part
    		while (true) {
    			String[] fields = line.split(";");
    			if (fields.length % 2 == 0) {
    				logger.error("Cannot parse line " + line + " of file " + statsFile.getKey() + " from bucket " 
    						+ statsFile.getBucketName() + " ... skipping");
    				continue;
    			}
    			String sourceID = fields[0];
    			HashMap<String, Float> hm = new HashMap<String, Float>();
    			for (int i = 1; i < fields.length - 1; i = i + 2) {
    				float f = 0.0f;
    				try {
    					f = Float.parseFloat(fields[i+1]);
    				}
    				catch (NumberFormatException ex) {
    					logger.error("Cannot parse float " + fields[i+1] + " from line " + line + " of file " + statsFile.getKey() 
    							+ " from bucket " + statsFile.getBucketName() + " ... skipping");
    					continue;
    				}
    				hm.put(fields[i], f);
    			}
    			SS1.put(sourceID, hm);
    			
    			if ((line = reader.readLine()) == null) {
    				break;
    			}
    		}
    		reader.close();
    	}
    	catch (Exception ex) {
    		String errorMessage = "Error while reading file " + statsFile.getKey() + " from bucket " + statsFile.getBucketName();
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	
    	
    	// open the S3 merged file and create new local merged file
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
		BufferedWriter out = null;
		String tmpFilename = "correlations" + mergedStatsFilename + tenantID;
		File localMergedFile = new File(tmpFilename);
		try {
			out = new BufferedWriter(new FileWriter(localMergedFile));
		}
		catch (IOException ex) {
			logger.error("Cannot write to file " + localMergedFile.getAbsolutePath());
			throw new Exception();
		}
		
		String bucketName = statsFile.getBucketName();
    	S3Object mergedFile = s3.getObject(bucketName, mergedStatsFilename);
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(mergedFile.getObjectContent()));
    		String line = reader.readLine();
    		try {
				numberOfProfiles += Float.parseFloat(line);
        	}
        	catch (NumberFormatException ex) {
        		logger.error("Cannot parse number of profiles \"" + line + "\" from file " + mergedFile.getKey() 
        				+ " from bucket " + mergedFile.getBucketName() + "... skipping");
        		throw new Exception();
        	}
        	out.write(Float.toString(numberOfProfiles) + newline);
    		while ((line = reader.readLine()) != null) {
    			
    			String[] fields = line.split(";");
    			if (fields.length != 2) {
    				break;
    			}
            	
    			String sourceID = fields[0];
    			float score = 0.0f;
            	try {
            		score = Float.parseFloat(fields[1]);
            	}
            	catch (NumberFormatException ex) {
            		logger.error("Cannot parse float " + fields[1] + " ... skipping");
            		throw new Exception();
            	}
            	
            	Float f = SS0.get(sourceID);
            	if (f == null) {
            		out.write(line + newline);
            	}
            	else {
            		float newValue = f.floatValue() + score;
            		if (newValue != 0.0f) {
            			StringBuffer sb = new StringBuffer();
            			sb.append(sourceID); sb.append(";"); sb.append(newValue);
                		sb.append(newline);
                		out.write(sb.toString());
            		}
            		
            		
            		SS0.remove(sourceID);
            	}
    		}
    		// Write the remaining entries of SS0 here
    		for (Map.Entry<String, Float> me : SS0.entrySet()) {
    			StringBuffer sb = new StringBuffer();
    			sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue());
    			sb.append(newline);
    			
    			out.write(sb.toString());
    		}
    		out.flush();
    		
    		while (true) {
    			String[] fields = line.split(";");
    			if (fields.length % 2 == 0) {
    				logger.error("Cannot parse line " + line + " of file " + mergedFile.getKey() + " from bucket " 
    						+ mergedFile.getBucketName() + " ... skipping");
    				continue;
    			}
    			
    			String sourceID = fields[0];
    			HashMap<String, Float> hm = SS1.get(sourceID);
    			if (hm == null) {
    				out.write(line + newline);
    			}
    			else {
    				// merge the two lines
    				StringBuffer sb = new StringBuffer();
    				sb.append(sourceID);
    				boolean itemFound = false;
    				for (int i = 1; i < fields.length - 1; i = i + 2) {
    					String targetID = fields[i];
    					float score = Float.parseFloat(fields[i+1]);
    					
    					Float f = hm.get(targetID);
    					if (f == null) {
    						itemFound = true;
    						sb.append(";"); sb.append(targetID); sb.append(";"); sb.append(score);
    					}
    					else {
    						float newValue = f.floatValue() + score;
    						if (newValue != 0.0f) {
    							itemFound = true;
    							sb.append(";"); sb.append(targetID); sb.append(";"); sb.append(newValue);
    						}
    						
    						
    						hm.remove(targetID);
    					}
    				}
    				for (Map.Entry<String, Float> me : hm.entrySet()) {
    					sb.append(";"); sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue());
    				}
    				sb.append(newline);
    				if (itemFound) {
    					out.write(sb.toString());
    				}
    				
    				
    				SS1.remove(sourceID);
    			}
    			out.flush();
    			
    			if ((line = reader.readLine()) == null) {
    				break;
    			}
    		}
    		// Write the remaining entries of SS1 here
    		for (Map.Entry<String, HashMap<String, Float>> me : SS1.entrySet()) {
    			String sourceID = me.getKey();
    			StringBuffer sb = new StringBuffer();
    			sb.append(sourceID);
    			for (Map.Entry<String, Float> me2 : me.getValue().entrySet()) {
    				sb.append(";"); sb.append(me2.getKey()); sb.append(";"); sb.append(me2.getValue());
    			}
    			sb.append(newline);
    			out.write(sb.toString());
    		}
    		out.flush();
    		out.close();
    		reader.close();
    	}
    	catch (Exception ex) {
    		String errorMessage = "Error while reading file " + mergedStatsFilename + " from bucket " + bucketName;
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	
    	// Now copy the local merged file to S3
    	PutObjectRequest r = new PutObjectRequest(bucketName, mergedStatsFilename, localMergedFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
    	localMergedFile.delete();
	}
	
	private Profile getExistingProfile(String userID, String tenantID) throws Exception {
		
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		GetAttributesRequest request = new GetAttributesRequest();
		request.setDomainName(getProfileDomainName(tenantID));
		request.setItemName(userID);
		GetAttributesResult result = sdb.getAttributes(request);
		List<Attribute> attributes = result.getAttributes();
		
		Profile existingProfile = null;
		if (attributes != null && attributes.size() > 0) {
			existingProfile = new Profile();
			existingProfile.setUserID(userID);
			for (Attribute attribute : attributes) {
            	String productID = attribute.getName();
            	Float score = null;
            	try {
            		score = Float.parseFloat(attribute.getValue());
            	}
            	catch (NumberFormatException ex) {
            		logger.error("Could not parse value " + attribute.getValue() + " ... skipping");
            		continue;
            	}
            	
            	existingProfile.add(productID, score);
			}
		}
		
		return existingProfile;
	}
	
	private String updateSufficientStatistics(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) 
														throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	  
		HashMap<String, HashMap<String, Float>> SS1 = new HashMap<String, HashMap<String, Float>>();
		HashMap<String, Float> SS0 = new HashMap<String, Float>();
		float numberOfProfiles = 0.0f;
		HashMap<String, Profile> hm = new HashMap<String, Profile>();
		for (Profile incrementalProfile : incrementalProfiles) {
			hm.put(incrementalProfile.getUserID(), incrementalProfile);
			
			// Retrieve existing profile
			Profile existingProfile = getExistingProfile(incrementalProfile.getUserID(), tenantID);
            
    		// Now update the stats 
    		updateIncrementalStats(existingProfile, incrementalProfile, SS1, SS0);
    		if (existingProfile == null) {
    			numberOfProfiles += 1.0f;
    		}
    		
		}
		
		for (Profile decrementalProfile : decrementalProfiles) {
    		
    		Profile existingProfile = getExistingProfile(decrementalProfile.getUserID(), tenantID);
    		
            if (existingProfile == null) {
            	logger.error("Encountered NULL existing profile while in the decremental profiles");
            	continue;
            }
            Profile incrementalProfile = hm.get(decrementalProfile.getUserID());
    		if (incrementalProfile != null) {
    			existingProfile.merge(incrementalProfile);
    		}
            
    		// Now update the stats
    		boolean deletedExistingProfile = updateDecrementalStats(existingProfile, decrementalProfile, SS1, SS0);
    		if (deletedExistingProfile && !hm.containsKey(decrementalProfile.getUserID())) {
    			numberOfProfiles -= 1.0f;
    		}
		}
		
		
		// write the stats
		String bucketName = getStatsBucketName(tenantID);
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		if (!s3.doesBucketExist(bucketName)) {
			s3.createBucket(bucketName, Region.EU_Ireland);
		}
		String incrStatsKeyBase = STATS_BASE_FILENAME + "_INCREMENTAL_";
		String incrStatsKey = UUID.randomUUID().toString();
		writeSufficientStatistics(tenantID, bucketName, incrStatsKeyBase, numberOfProfiles, SS1, SS0, incrStatsKey);
		
		return incrStatsKeyBase + incrStatsKey;
	}
	
	/**
	 * Returns true if the entire existing profile has been deleted
	 * 
	 * @param existingProfile
	 * @param decrementalProfile
	 * @param SS1
	 * @param SS0
	 * @return
	 */
	private boolean updateDecrementalStats(Profile existingProfile, Profile decrementalProfile,
			HashMap<String, HashMap<String, Float>> SS1, HashMap<String, Float> SS0) {
	
		// Determine the attributes that are removed
		List<String> attributes = new ArrayList<String>();
		HashSet<String> removedAttributes = new HashSet<String>();
		for (Map.Entry<String, Float> me : decrementalProfile.getAttributes().entrySet()) {
			String att = me.getKey();
			float decrementalValue = me.getValue();
			
			float existingValue = existingProfile.getAttributes().get(att);
			
			if (existingValue == decrementalValue) {
				// Remove attribute
				attributes.add(att);
				removedAttributes.add(att);
			}
		}
		
		int numRemovedAttributes = attributes.size();
		if (numRemovedAttributes == 0) {
			return false;
		}
		
		for (String existingAttribute : existingProfile.getAttributes().keySet()) {
			if (!attributes.contains(existingAttribute)) {
				attributes.add(existingAttribute);
			}
		}
		Collections.sort(attributes);
		
		for (int i = 0; i < attributes.size(); i ++) {
			String productI = attributes.get(i);
			boolean isDecrementalProductI = removedAttributes.contains(productI) ? true : false;
			if (isDecrementalProductI) {
				Float f = SS0.get(productI);
				if (f == null) {
					SS0.put(productI, -1.0f);
				}
				else {
					float newValue = f.floatValue() - 1.0f;
					if (newValue != 0.0f) {
						SS0.put(productI, newValue);
					}
					else {
						SS0.remove(productI);
					}
				}
			}
			
			
			HashMap<String, Float> hm = SS1.get(productI);
			if (hm == null) {
				hm = new HashMap<String, Float>();
				SS1.put(productI, hm);
			}
			for (int j = i + 1; j < attributes.size(); j ++) {
				String productJ = attributes.get(j);
				boolean isDecrementalProductJ = removedAttributes.contains(productJ) ? true : false;
				if (isDecrementalProductI || isDecrementalProductJ) {
					Float count = hm.get(productJ);
					if (count == null) {
						hm.put(productJ, -1.0f);
					}
					else {
						float newValue = count.floatValue() - 1.0f;
						if (newValue != 0.0f) {
							hm.put(productJ, newValue);
						}
						else {
							hm.remove(productJ);
						}
					}
				}
			}
			if (hm.size() == 0) {
				SS1.remove(productI);
			}
		}
		
		if (numRemovedAttributes == existingProfile.getAttributes().size()) {
			// Deleted the entire existing profile
			return true;
		}
		else {
			return false;
		}
	}
	
	private void updateIncrementalStats(Profile existingProfile, Profile incrementalProfile,
										HashMap<String, HashMap<String, Float>> SS1, HashMap<String, Float> SS0) {
		
		HashSet<String> hs = new HashSet<String>();
		if (existingProfile != null) {
			for (String s : existingProfile.getAttributes().keySet()) {
				hs.add(s);
			}
		}
		for (String s : incrementalProfile.getAttributes().keySet()) {
			hs.add(s);
		}
		List<String> productIDs = new ArrayList<String>();
		for (String s : hs) {
			productIDs.add(s);
		}
		Collections.sort(productIDs);
		
		for (int i = 0; i < productIDs.size(); i ++) {
			String productI = productIDs.get(i);
			boolean isIncrementalProductI = incrementalProfile.getAttributes().get(productI) != null &&
											(existingProfile == null || existingProfile.getAttributes().get(productI) == null) ? true : false;
			if (isIncrementalProductI) {
				Float f = SS0.get(productI);
				if (f == null) {
					SS0.put(productI, 1.0f);
				}
				else {
					SS0.put(productI, f.floatValue() + 1.0f);
				}
			}
			
			
			HashMap<String, Float> hm = SS1.get(productI);
			if (hm == null) {
				hm = new HashMap<String, Float>();
				SS1.put(productI, hm);
			}
			for (int j = i + 1; j < productIDs.size(); j ++) {
				String productJ = productIDs.get(j);
				boolean isIncrementalProductJ = incrementalProfile.getAttributes().get(productJ) != null &&
												(existingProfile == null || existingProfile.getAttributes().get(productJ) == null) ? true : false;
				if (isIncrementalProductI || isIncrementalProductJ) {
					Float count = hm.get(productJ);
					if (count == null) {
						hm.put(productJ, 1.0f);
					}
					else {
						float newValue = count.floatValue() + 1.0f;
						hm.put(productJ, newValue);
					}
				}
				
			}
		}
		
	}
	
	private void calculateSufficientStatistics(String tenantID, List<Item> items, 
			HashMap<String, HashMap<String, Float>> SS1, 
			HashMap<String, Float> SS0) throws Exception {
		
		for (Item item : items) {
			List<String> productIDs = new ArrayList<String>();
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().startsWith("Attribute")) {
					String value = attribute.getValue();
					String[] parsedValue = value.split(";");
					String productID = parsedValue[0];
					
					productIDs.add(productID);
				}
			}
			Collections.sort(productIDs);
			
			for (int i = 0; i < productIDs.size(); i ++) {
				String productI = productIDs.get(i);
				Float f = SS0.get(productI);
				if (f == null) {
					SS0.put(productI, 1.0f);
				}
				else {
					SS0.put(productI, f.floatValue() + 1.0f);
				}
				
				HashMap<String, Float> hm = SS1.get(productI);
				if (hm == null) {
					hm = new HashMap<String, Float>();
					SS1.put(productI, hm);
				}
				for (int j = i + 1; j < productIDs.size(); j ++) {
					String productJ = productIDs.get(j);
					
					Float count = hm.get(productJ);
					if (count == null) {
						hm.put(productJ, 1.0f);
					}
					else {
						hm.put(productJ, count.floatValue() + 1.0f);
					}
				}
			}
		}
	}
	
	public void calculateSufficientStatistics(String bucketName, String baseFilename, String tenantID) throws Exception {
		
		// Delete stats bucket
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		
		if (!s3.doesBucketExist(bucketName)) {
			s3.createBucket(bucketName, Region.EU_Ireland);
		}
		else {
			ObjectListing objListing = s3.listObjects(bucketName);
			if (objListing.getObjectSummaries().size() > 0) {
				for (S3ObjectSummary objSummary : objListing.getObjectSummaries()) {
					s3.deleteObject(bucketName, objSummary.getKey());
				}
			}
		}
		
		// Now calculate new suff stats
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		String profileDomain = getProfileDomainName(tenantID);
		String selectExpression = "select * from `" + profileDomain + "` limit 2500";
		
		HashMap<String, HashMap<String, Float>> SS1 = new HashMap<String, HashMap<String, Float>>();
		HashMap<String, Float> SS0 = new HashMap<String, Float>();
		String resultNextToken = null;
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		int chunk = 1;
		int profilesInChunk = 0;
		boolean statsCreated =  false;
		do {
		    if (resultNextToken != null) {
		    	selectRequest.setNextToken(resultNextToken);
		    }
		    
		    SelectResult selectResult = sdb.select(selectRequest);
		    
		    String newToken = selectResult.getNextToken();
		    if (newToken != null && !newToken.equals(resultNextToken)) {
		    	resultNextToken = selectResult.getNextToken();
		    }
		    else {
		    	resultNextToken = null;
		    }
		    List<Item> items = selectResult.getItems();
		    
		    calculateSufficientStatistics(tenantID, items, SS1, SS0);
		    
		    profilesInChunk += items.size();
		    
		    if (profilesInChunk >= this.profilesPerChunk) {
		    	statsCreated = true;
		    	writeSufficientStatistics(tenantID, bucketName, baseFilename, profilesInChunk, SS1, SS0, Integer.toString(chunk));
		    	SS1 = new HashMap<String, HashMap<String, Float>>();
		    	SS0 = new HashMap<String, Float>();
		    	profilesInChunk = 0;
		    	chunk ++;
		    }
		    
		} while (resultNextToken != null);
		
		if (profilesInChunk > 0) {
			statsCreated = true;
	    	writeSufficientStatistics(tenantID, bucketName, baseFilename, profilesInChunk, SS1, SS0, Integer.toString(chunk));
	    	SS1 = new HashMap<String, HashMap<String, Float>>();
	    	SS0 = new HashMap<String, Float>();
	    	profilesInChunk = 0;
	    	chunk ++;
	    }
		
		if (! statsCreated) {
			logger.warn("No statistics files were created, because no new data have been found since last update");
			throw new Exception();
		}
	}
	
	private void writeSufficientStatistics(String tenantID, String bucketName, String baseFilename, float numberOfProfiles,
			HashMap<String, HashMap<String, Float>> SS1, 
			HashMap<String, Float> SS0, String chunkID) throws Exception {
		
		String localStatsFilename = baseFilename + "_" + tenantID + "_" + chunkID;
		File localSSFile = new File(localStatsFilename);
		BufferedWriter out = new BufferedWriter(new FileWriter(localSSFile));
		out.write(Float.toString(numberOfProfiles) + newline); 
		for (Map.Entry<String, Float> me : SS0.entrySet()) {
			StringBuffer sb = new StringBuffer();
			sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue());
			sb.append(newline);
			
			out.write(sb.toString());
		}
		out.flush();
		
		HashSet<String> keySet = new HashSet<String>();
		for (String sourceID : SS0.keySet()) {
			keySet.add(sourceID);
		}
		for (Map.Entry<String, HashMap<String, Float>> me : SS1.entrySet()) {
			keySet.addAll(me.getValue().keySet());
			keySet.add(me.getKey());
		}
		for (String sourceID : keySet) {
			StringBuffer sb = new StringBuffer();
			sb.append(sourceID);
			boolean found = false;
			HashMap<String, Float> hm = SS1.get(sourceID);
			if (hm != null) {
				found = true;
				for (Map.Entry<String, Float> me : hm.entrySet()) {
					sb.append(";"); sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue());
				}
			}
			for (Map.Entry<String, HashMap<String, Float>> me2 : SS1.entrySet()) {
				HashMap<String, Float> hm2 = me2.getValue();
				Float f = hm2.get(sourceID);
				if (f != null) {
					found = true;
					sb.append(";"); sb.append(me2.getKey()); sb.append(";"); sb.append(f);
				}
			}
			sb.append(newline);
			
			if (found) {
				out.write(sb.toString());
			}
			out.flush();
		}
		out.close();
		
		
		// Copy to S3
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	String statsFilename = baseFilename + chunkID;
		PutObjectRequest r = new PutObjectRequest(bucketName, statsFilename, localSSFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localSSFile.delete();
	}
	
	public void estimateModelParameters(String tenantID) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
    	AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	String statsBucketName = getStatsBucketName(tenantID);
    	ObjectListing objectListing = s3.listObjects(statsBucketName);
    	List<S3ObjectSummary> objSummaries = objectListing.getObjectSummaries();
    	
    	if (objSummaries.size() != 1) {
    		logger.warn("Expecting one stats file in bucket " + statsBucketName + " but found " + objSummaries.size());
    		throw new Exception();
    	}
    	
    	
    	String key = objSummaries.get(0).getKey();
    	S3Object mergedStats = s3.getObject(statsBucketName, key);
    	
    	
    	// Read in memory SS0
    	float numberOfProfiles = 0.0f;
    	HashMap<String, Float> SS0 = new HashMap<String, Float>();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(mergedStats.getObjectContent()));
		String line = reader.readLine();
		numberOfProfiles = Float.parseFloat(line);
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			if (fields.length != 2) {
				break;
			}
            String sourceItemId = fields[0];
            float score = 0.0f;
        	try {
        		score = Float.parseFloat(fields[1]);
        	}
        	catch (NumberFormatException ex) {
        		logger.error("Cannot parse float " + fields[1] + " ... skipping");
        		throw new Exception();
        	}
        	
        	SS0.put(sourceItemId, score);
		}
    	
    	
    	// Read line-by-line the SS1 compute the correlation coefficient and write to SimpleDB
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
    	String correlationsModelDomainName = getBackupModelDomainName(getDomainBasename(), tenantID);
    	sdb.deleteDomain(new DeleteDomainRequest(correlationsModelDomainName));
    	sdb.createDomain(new CreateDomainRequest(correlationsModelDomainName));
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		while (true) {
			String[] fields = line.split(";");
			if (fields.length % 2 == 0) {
				logger.error("Cannot parse line \"" + line + "\" at file " + mergedStats.getKey() 
						+ " at bucket " + mergedStats.getBucketName() + "... skipping");
				if ((line = reader.readLine()) == null) {
					break;
				}
				continue;
			}
			List<AttributeObject> itemsList = new LinkedList<AttributeObject>(); 
			String sourceID = fields[0];
			Float fs = SS0.get(sourceID);
			float sourceOccurrences = fs == null ? 0.0f : fs;
			double tmpSourceNorm = Math.sqrt(sourceOccurrences - ((sourceOccurrences * sourceOccurrences) / numberOfProfiles));
			double sourceNorm = tmpSourceNorm == 0.0 ? 1.0e+05 : tmpSourceNorm;
			
			for (int i = 1; i < fields.length - 1; i = i + 2) {
				String targetID = fields[i];
				float cooccurrenceCount = Float.parseFloat(fields[i + 1]);
				
				if (targetID.equals(sourceID) || cooccurrenceCount <= 0.0f) {
					continue;
				}
				
				float cc = 0.0f;
				try {
					cc = Float.parseFloat(fields[i+1]);
				}
				catch (NumberFormatException ex) {
	        		logger.error("Cannot parse co-occurrence count " + fields[i+1] + " ... skipping");
	        		continue;
	        	}
            	
            	if (cc < COOCCURRENCE_THRESHOLD) {
					continue;
				}
            	Float f = SS0.get(targetID);
            	float targetOccurrences = f == null ? 0.0f : f;
            	double tmpTargetNorm = Math.sqrt(targetOccurrences - ((targetOccurrences * targetOccurrences) / numberOfProfiles));
				double targetNorm = tmpTargetNorm == 0.0 ? 1.0e+05 : tmpTargetNorm;
				
				double nominator = (double)cc - (((double)sourceOccurrences*(double)targetOccurrences)/(double)numberOfProfiles);
				double score = nominator / (sourceNorm*targetNorm);
				
				if (score < CORRELATION_THRESHOLD) {
					continue;
				}
				itemsList.add(new AttributeObject(targetID, Math.round(score * 100.0)));
			}
			if (itemsList.size() == 0) {
				if ((line = reader.readLine()) == null) {
					break;
				}
				continue;
			}
			Collections.sort(itemsList);
			
			// Now write to SimpleDB domain that holds the model parameters
			List<AttributeObject> l = itemsList.size() < this.topCorrelations ? itemsList : itemsList.subList(0, this.topCorrelations);
			
			List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
			for (AttributeObject attObject : l) {
				String attributeName = "ATTRIBUTE_" + attObject.getUID();
				StringBuffer sb = new StringBuffer();
				sb.append(attObject.getUID()); sb.append(";"); sb.append(attObject.getScore());
				ReplaceableAttribute attribute = new ReplaceableAttribute(attributeName, sb.toString(), true);
				attributes.add(attribute);
			}
			String itemName = sourceID;
			items.add(new ReplaceableItem(itemName, attributes));
			
			if (items.size() == 25) {
        		writeSimpleDB(sdb, correlationsModelDomainName, items);
        		items = new ArrayList<ReplaceableItem>();
        	}
			
			if ((line = reader.readLine()) == null) {
				break;
			}
		}
		reader.close();
		
		if (items.size() > 0) {
			writeSimpleDB(sdb, correlationsModelDomainName, items);
			items = new ArrayList<ReplaceableItem>();
		}
	}
	

	private void writeSimpleDB(AmazonSimpleDB sdb, String simpleDBDomain, List<ReplaceableItem> recsPairs) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, recsPairs));
    	}
		catch (DuplicateItemNameException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + simpleDBDomain + " because of duplicate item names" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new DuplicateItemNameException(errorMessage);
    	}
		catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + simpleDBDomain + " because of invalid parameter value" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new InvalidParameterValueException(errorMessage);
    	}
		catch (NumberDomainBytesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + simpleDBDomain + " because max number of domain bytes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainBytesExceededException(errorMessage);
    	}
		catch (NumberSubmittedItemsExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + simpleDBDomain + " because max number of submitted items exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberSubmittedItemsExceededException(errorMessage);
    	}
		catch (NumberSubmittedAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + simpleDBDomain + " because max number of submitted attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberSubmittedAttributesExceededException(errorMessage);
    	}
		catch (NumberDomainAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + simpleDBDomain + " because max number of domain attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainAttributesExceededException(errorMessage);
    	}
		catch (NumberItemAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + simpleDBDomain + " because max number of item attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberItemAttributesExceededException(errorMessage);
    	}
    	catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + simpleDBDomain + " " + ex.getStackTrace();
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
    
	public void writeModelToFile(String tenantID, String bucketName, String modelFilename, String modelDomainName) throws Exception {
		
		BufferedWriter out = null;
		String tmpFilename = modelFilename + "_" + tenantID;
		File localFile = new File(tmpFilename);
		try {
			out = new BufferedWriter(new FileWriter(localFile));
		}
		catch (IOException ex) {
			logger.error("Cannot write to file " + localFile.getAbsolutePath());
			throw new Exception();
		}
		
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		boolean found = false;
		for (String domainName : sdb.listDomains().getDomainNames()) {
			if (domainName.equals(modelDomainName)) {
				found = true;
				break;
			}
		}
		if (!found) {
			logger.error("Could not find model domain name " + modelDomainName);
			return;
		}
		String selectExpression = "select * from `" + modelDomainName + "` limit 2500";
		String resultNextToken = null;
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		do {
		    if (resultNextToken != null) {
		    	selectRequest.setNextToken(resultNextToken);
		    }
		    
		    SelectResult selectResult = sdb.select(selectRequest);
		    
		    String newToken = selectResult.getNextToken();
		    if (newToken != null && !newToken.equals(resultNextToken)) {
		    	resultNextToken = selectResult.getNextToken();
		    }
		    else {
		    	resultNextToken = null;
		    }
		    
		    
		    List<Item> items = selectResult.getItems();
			for (Item item : items) {
				StringBuffer sb = new StringBuffer();
				sb.append(item.getName());
				for (Attribute attribute : item.getAttributes()) {
					String value = attribute.getValue();
					sb.append(";"); sb.append(value);
				}
				sb.append(newline);
				
				out.write(sb.toString());
				out.flush();
			}
		    
		} while (resultNextToken != null);
		out.close();
		
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
		
		PutObjectRequest r = new PutObjectRequest(bucketName, modelFilename, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localFile.delete();
	}
	
	public String getProfileDomainName(String tenantID) {
    	return "PROFILE_" + tenantID;
    }
}
