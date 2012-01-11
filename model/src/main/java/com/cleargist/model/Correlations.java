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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
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
import com.amazonaws.services.simpledb.model.InvalidParameterValueException;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.MissingParameterException;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainsExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

public class Correlations implements Learnable {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final float COOCCURRENCE_THRESHOLD = 2.0f;
	private static final double CORRELATION_THRESHOLD = 0.05;
	private static final int TOP_CORRELATIONS = 10;
	private Logger logger = Logger.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private static final String BASE_STATS_BUCKETNAME = "STATS_";
	private static final String MERGED_STATS_FILENAME = "merged.txt";
	private static final String LOCAL_STATS_FILENAME = "stats_";
	
	public List<String> getRecommendedProducts(List<String> productIDs, String tenantID, Filter filter) throws Exception {
		double weight = (double)productIDs.size();
		List<AttributeObject> productIDsInternal = new LinkedList<AttributeObject>();
		for (String productID : productIDs) {
			productIDsInternal.add(new AttributeObject(productID, weight));
			weight -= 1.0;
		}
		
		return getRecommendedProductsInternal(productIDsInternal, tenantID, filter);
	}
	
	private List<String> getRecommendedProductsInternal(List<AttributeObject> productIds, String tenantID, Filter filter) throws Exception {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	String correlationsModelDomainName = getModelDomainName(tenantID);
    	HashMap<String, Double> targetIds = new HashMap<String, Double>();
    	for (AttributeObject attObject : productIds) {
    		String sourceItemId = attObject.getUID();
    		String selectExpression = "select * from `" + correlationsModelDomainName + "` where itemName() = '" + sourceItemId + "'";
            SelectRequest selectRequest = new SelectRequest(selectExpression);
            List<Item> items = sdb.select(selectRequest).getItems();
            if (items == null || items.size() == 0) {
            	continue;
            }
            Item item = items.get(0);
            String targetItemId = null;
        	double score = 0.0;
            for (Attribute attribute : item.getAttributes()) {
            	String[] fields = attribute.getValue().split(";");
            	targetItemId = fields[0];
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
	
	private List<AttributeObject> getUserProfile(String userID, String tenantID) throws Exception {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	
    	String profileDomain = getProfileDomainName(tenantID);
		String selectExpression = "select * from `" + profileDomain + "` where USER_ID = '" + userID + "' limit 1";
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
	
	public List<String> getPersonalizedRecommendedProducts(String userID, String tenantID, Filter filter) throws Exception {
		// Retrieve the user profile
		List<AttributeObject> sourceIDs = getUserProfile(userID, tenantID);
		return getRecommendedProductsInternal(sourceIDs, tenantID, filter);
	}
	
	public void mergeSufficientStatistics(String tenantID) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	String statsBucketName = BASE_STATS_BUCKETNAME + tenantID;
    	ObjectListing objectListing = s3.listObjects(statsBucketName);
    	List<S3ObjectSummary> objSummaries = objectListing.getObjectSummaries();
    	
    	if (objSummaries.size() == 0) {
    		logger.error("No stats files found for tenant " + tenantID + " in bucket " + statsBucketName);
    		return;
    	}
    	
    	String statsFilename = objSummaries.get(0).getKey();
    	s3.copyObject(statsBucketName, statsFilename, statsBucketName, MERGED_STATS_FILENAME);
    	s3.deleteObject(statsBucketName, statsFilename);
    	int i = 1;
    	while (i < objSummaries.size()) {
    		statsFilename = objSummaries.get(i).getKey();
    		S3Object statsObject = s3.getObject(statsBucketName, statsFilename);
        	
    		mergeSufficientStatistics(tenantID, statsObject);
    		
    		s3.deleteObject(statsBucketName, statsFilename);
    		i ++;
    	}
	}
	
	private void mergeSufficientStatistics(String tenantID, S3Object statsFile) 
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
    		while ((line = reader.readLine()) != null) {
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
    		}
    		reader.close();
    	}
    	catch (Exception ex) {
    		String errorMessage = "Error while reading file " + statsFile.getKey() + " from bucket " + statsFile.getBucketName();
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	
    	
    	// open the S3 merged file and create new local merged file
    	String bucketName = BASE_STATS_BUCKETNAME + tenantID;
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
		BufferedWriter out = null;
		String tmpFilename = "tmpMerged_" + tenantID;
		File localMergedFile = new File(tmpFilename);
		try {
			out = new BufferedWriter(new FileWriter(localMergedFile));
		}
		catch (IOException ex) {
			logger.error("Cannot write to file " + localMergedFile.getAbsolutePath());
			throw new Exception();
		}
		
    	S3Object mergedFile = s3.getObject(bucketName, MERGED_STATS_FILENAME);
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
            		StringBuffer sb = new StringBuffer();
            		sb.append(sourceID); sb.append(";"); sb.append(f.floatValue() + score);
            		sb.append(newline);
            		out.write(sb.toString());
            		
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
    				for (int i = 1; i < fields.length - 1; i = i + 2) {
    					String targetID = fields[i];
    					float score = Float.parseFloat(fields[i+1]);
    					
    					Float f = hm.get(targetID);
    					if (f == null) {
    						sb.append(";"); sb.append(targetID); sb.append(";"); sb.append(score);
    					}
    					else {
    						sb.append(";"); sb.append(targetID); sb.append(";"); sb.append(f.floatValue() + score);
    						
    						hm.remove(targetID);
    					}
    				}
    				for (Map.Entry<String, Float> me : hm.entrySet()) {
    					sb.append(";"); sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue());
    				}
    				sb.append(newline);
    				out.write(sb.toString());
    				
    				SS1.remove(sourceID);
    			}
    			
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
    		out.close();
    		reader.close();
    	}
    	catch (Exception ex) {
    		String errorMessage = "Error while reading file " + MERGED_STATS_FILENAME + " from bucket " + bucketName;
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	
    	// Now copy the local merged file to S3
    	PutObjectRequest r = new PutObjectRequest(bucketName, MERGED_STATS_FILENAME, localMergedFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
	}
	
	
	public void calculateSufficientStatistics(String tenantID, String token) throws Exception {
		HashMap<String, HashMap<String, Float>> SS1 = new HashMap<String, HashMap<String, Float>>();
		HashMap<String, Float> SS0 = new HashMap<String, Float>();
		Float numberOfProfiles = 0.0f;
		
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	String profileDomain = getProfileDomainName(tenantID);
		String selectExpression = "select * from `" + profileDomain + "`";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		selectRequest.setNextToken(token);
		SelectResult selectResult = sdb.select(selectRequest);
		List<Item> items = selectResult.getItems();
		if (items == null || items.size() == 0) {
			logger.warn("No data retrieved for token " + token);
			return;
		}
		// Do the processing of profiles here
		numberOfProfiles += items.size();
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
				for (int j = i + 1; i < productIDs.size(); j ++) {
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
		
		// Now write sufficient statistics to local file, expanding the symmetry
		String tmpFilename = LOCAL_STATS_FILENAME + "_" + token + "_" + tenantID;
		File localSSFile = new File(tmpFilename);
		BufferedWriter out = new BufferedWriter(new FileWriter(localSSFile));
		out.write(Float.toString(numberOfProfiles)); out.write(newline);
		for (Map.Entry<String, Float> me : SS0.entrySet()) {
			StringBuffer sb = new StringBuffer();
			sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue());
			sb.append(newline);
			
			out.write(sb.toString());
		}
		for (Map.Entry<String, HashMap<String, Float>> me : SS1.entrySet()) {
			String sourceID = me.getKey();
			StringBuffer sb = new StringBuffer();
			sb.append(sourceID);
			for (Map.Entry<String, Float> me2 : me.getValue().entrySet()) {
				sb.append(";"); sb.append(me2.getKey()); sb.append(";"); sb.append(me2.getValue());
			}
			for (Map.Entry<String, HashMap<String, Float>> me3 : SS1.entrySet()) {
				HashMap<String, Float> hm = me.getValue();
				
				Float f = hm.get(sourceID);
				if (f != null) {
					sb.append(";"); sb.append(me.getKey()); sb.append(f);
				}
			}
			sb.append(newline);
			
			out.write(sb.toString());
			out.flush();
		}
		out.close();
		
		
		// Copy to S3
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	String statsBucketName = BASE_STATS_BUCKETNAME + tenantID;
    	String statsFilename = LOCAL_STATS_FILENAME + token;
		PutObjectRequest r = new PutObjectRequest(statsBucketName, statsFilename, localSSFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localSSFile.delete();
	}
	
	private void estimateModelParameters(S3Object mergedStats, String tenantID) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
    	AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	
    	// Read in memory SS0
    	float numberOfProfiles = 0.0f;
    	HashMap<String, Float> SS0 = new HashMap<String, Float>();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(mergedStats.getObjectContent()));
		String line = reader.readLine();
    	try {
    		numberOfProfiles = Float.parseFloat(line);
    	}
    	catch (NumberFormatException ex) {
    		logger.error("Cannot parse number of profiles \"" + line + "\" from file " + mergedStats.getKey() 
    				+ " from bucket " + mergedStats.getBucketName() + "... skipping");
    		throw new Exception();
    	}
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
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	String correlationsModelDomainName = getBackupModelDomainName(tenantID);
    	deleteDomain(sdb, correlationsModelDomainName);
    	createDomain(sdb, correlationsModelDomainName);
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			if (fields.length % 2 == 0) {
				logger.error("Cannot parse line \"" + line + "\" at file " + mergedStats.getKey() 
						+ " at bucket " + mergedStats.getBucketName() + "... skipping");
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
				
				if (targetID.equals(sourceID)) {
					continue;
				}
				
				float cc = Float.parseFloat(fields[i+1]);
            	
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
				itemsList.add(new AttributeObject(targetID, score));
			}
			if (itemsList.size() == 0) {
				continue;
			}
			Collections.sort(itemsList);
			
			// Now write to SimpleDB domain that holds the model parameters
			List<AttributeObject> l = itemsList.size() < TOP_CORRELATIONS ? itemsList : itemsList.subList(0, TOP_CORRELATIONS);
			
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
			
		}
		reader.close();
		
		if (items.size() > 0) {
			writeSimpleDB(sdb, correlationsModelDomainName, items);
			items = new ArrayList<ReplaceableItem>();
		}
    	
    	
    	// Remove the sufficient statistics files
		s3.deleteObject(new DeleteObjectRequest(mergedStats.getBucketName(), mergedStats.getKey()));
	}
	
	public void updateModel(String tenantID) 
	throws AmazonServiceException, AmazonClientException, Exception {
		// Determine the list of tokens
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
		List<String> tokens = new LinkedList<String>();
		String profileDomain = getProfileDomainName(tenantID);
		String selectExpression = "select count(*) from `" + profileDomain + "`";
		String nextToken = null;
		while (true) {
			SelectRequest selectRequest = new SelectRequest(selectExpression);
			selectRequest.setNextToken(nextToken);
			SelectResult selectResult = sdb.select(selectRequest);
			nextToken = selectResult.getNextToken();
			
			if (nextToken == null) {
				break;
			}
			
			tokens.add(nextToken);
		}	
		
		
		// Delete stats bucket
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
		String bucketName = BASE_STATS_BUCKETNAME + tenantID;
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
		
		
		for (String token : tokens) {
			calculateSufficientStatistics(tenantID, token);
		}
		
		mergeSufficientStatistics(tenantID);
		
		S3Object mergedStats = s3.getObject(bucketName, MERGED_STATS_FILENAME);
		estimateModelParameters(mergedStats, tenantID);
		
		// Now that all work is done, point to the new model
    	swapModelDomainNames(tenantID);
	}
	
	private void writeSimpleDB(AmazonSimpleDB sdb, String SimpleDBDomain, List<ReplaceableItem> recsPairs) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(SimpleDBDomain, recsPairs));
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
	
	private void deleteDomain(AmazonSimpleDB sdb, String domainToDelete) throws Exception {
    	try {
    		sdb.deleteDomain(new DeleteDomainRequest(domainToDelete));
    	}
    	catch (MissingParameterException ex) {
            String errorMessage = "Cannot delete domain in SimpleDB, missing parameter " + ex.getStackTrace();
    		logger.error(errorMessage);
        }
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot delete domain in SimpleDB, Amazon Service error";
    		logger.error(errorMessage);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network.";
            logger.error(errorMessage);
        }
    	
    }
    
    private void createDomain(String domainName) 
    throws InvalidParameterValueException, NumberDomainsExceededException, MissingParameterException, 
    AmazonServiceException, AmazonClientException, IOException {
    	
    	AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
    	sdb.createDomain(new CreateDomainRequest(domainName));
    }
}
