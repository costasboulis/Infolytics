package com.cleargist.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.cleargist.catalog.entity.jaxb.Catalog;
import com.cleargist.profile.Profile;

/**
 * The CombinationModel uses the associations from the SemanticModel as priors for building the CorrelationsModel 
 * 
 * @author kboulis
 *
 */
public class CombinationModel extends BaseModel {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private int DEFAULT_TOP_N_SEMANTIC = 100;
	private int DEFAULT_TOP_N_CORRELATIONS = 20;
	private float DEFAULT_SS0_NORMALIZATION = 0.5f;
	private SemanticModel semanticModel;
	private CorrelationsModel correlationsModel;
	private Logger logger = Logger.getLogger(getClass());
	private float SS0Normalization;
	
	public CombinationModel() {
		this.correlationsModel = new CorrelationsModel();
		this.semanticModel = new SemanticModel();
		this.semanticModel.setTopCorrelations(DEFAULT_TOP_N_SEMANTIC);
		this.correlationsModel.setTopCorrelations(DEFAULT_TOP_N_CORRELATIONS);
		this.SS0Normalization = DEFAULT_SS0_NORMALIZATION;
	}
	
	public void setSS0Normalization(float f) {
		this.SS0Normalization = f;
	}
	
	public void updateModel(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) 
	throws AmazonServiceException, AmazonClientException, Exception {
		
	}
	
	public void createModel(String tenantID) 
	throws AmazonServiceException, AmazonClientException, Exception {
		
		calculateSufficientStatistics("", "", tenantID);
		
		mergeSufficientStatistics(tenantID);
		
		estimateModelParameters(tenantID);
		
    	swapModelDomainNames(getDomainBasename(), tenantID);
	}
	
	protected void calculateSufficientStatistics(String bucketName, String baseFilename, String tenantID) throws Exception {
		String rawCountsBucketName = this.correlationsModel.getStatsBucketName(tenantID);
		this.correlationsModel.calculateSufficientStatistics(rawCountsBucketName, STATS_BASE_FILENAME, tenantID);
		this.correlationsModel.mergeSufficientStatistics(tenantID);
		
		this.semanticModel.createModel(tenantID);
	}
	
	protected void mergeSufficientStatistics(String tenantID) throws Exception {
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CombinationModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		// Load raw counts in memory
		HashMap<String, Float> SS0 = new HashMap<String, Float>();
		HashMap<String, HashMap<String, Float>> SS1 = new HashMap<String, HashMap<String, Float>>();
		String rawCountsBucketName = this.correlationsModel.getStatsBucketName(tenantID);
		S3Object rawCountsFile = s3.getObject(rawCountsBucketName, "partialStats1.gz");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(rawCountsFile.getObjectContent())));
		String line = null;
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
		while (true) {
			String[] fields = line.split(";");
			if (fields.length % 2 == 0) {
				logger.error("Cannot parse line \"" + line + "\" at file " + rawCountsFile.getKey() 
						+ " at bucket " + rawCountsFile.getBucketName() + "... skipping");
				if ((line = reader.readLine()) == null) {
					break;
				}
				continue;
			}
			
			HashMap<String, Float> hm = new HashMap<String, Float>();
			for (int i = 1; i < fields.length - 1; i = i + 2) {
				String targetID = fields[i];
				float cooccurrenceCount = Float.parseFloat(fields[i + 1]);
				
				hm.put(targetID, cooccurrenceCount);
			}
			String sourceID = fields[0];
			SS1.put(sourceID, hm);
			
			if ((line = reader.readLine()) == null) {
				break;
			}
		}
		reader.close();
		
		
		// Set up the writing of new merged stats file
		File localSS1file = new File("localSS1file" + tenantID + ".gz");
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(localSS1file))));
		
		
		// Traverse serially the semantic associations and update counts
		String semanticBucketName = this.semanticModel.getStatsBucketName(tenantID);
		String key = SemanticModel.getAssociationsKey(tenantID);
		S3Object statsFile = s3.getObject(semanticBucketName, key);
		reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(statsFile.getObjectContent())));
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			if (fields.length % 2 == 0) {
				logger.warn("Cannot parse line \"" + line + "\"");
				continue;
			}
			
			float totSum = 0.0f;
			HashMap<String, Float> hm = new HashMap<String, Float>();
			String sourceItemId = fields[0];
			for (int i = 1; i < fields.length ; i = i + 2) {
				String targetItemID = fields[i];
				float score = Float.parseFloat(fields[i + 1]) * this.SS0Normalization;
				
				hm.put(targetItemID, score);
				
				totSum += score;
			}
			
			
			// Now merge stats with raw counts
			Float f = SS0.get(sourceItemId);
			if (f == null) {
				SS0.put(sourceItemId, totSum);
			}
			else {
				SS0.put(sourceItemId, totSum + f);
			}
			
			HashMap<String, Float> rawCounts = SS1.get(sourceItemId);
			if (rawCounts == null) {
				rawCounts = hm;
			}
			else {
				for (Map.Entry<String, Float> me : hm.entrySet()) {
					String targetItemId = me.getKey();
					Float score = me.getValue();
					
					Float s = rawCounts.get(targetItemId);
					if (s == null) {
						rawCounts.put(targetItemId, score);
					}
					else {
						rawCounts.put(targetItemId, score + s);
					}
						
				}
			}
			
			// Persist the rawCounts vector
			StringBuffer sb = new StringBuffer();
			sb.append(sourceItemId);
			for (Map.Entry<String, Float> me : hm.entrySet()) {
				sb.append(";"); sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue());
			}
			sb.append(newline);
			
			out.write(sb.toString());
			out.flush();
			
		}
		reader.close();
		out.close();
		
		// Persist the SS0 counts
		File localSS0file = new File("localSSOFile.gz");
		out = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(localSS0file))));
		for (Map.Entry<String, Float> me : SS0.entrySet()) {
			StringBuffer sb = new StringBuffer();
			sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue()); sb.append(newline);
			
			out.write(sb.toString());
			out.flush();
		}
		out.close();
		
		// Merge the SS0 and SS1 files
		File localMergedFile = new File("mergedstatscombined" + tenantID + ".gz");
		mergeAndZipFiles(localSS0file, localSS1file, localMergedFile);
		
		
		// Upload the updated counts
		PutObjectRequest r = new PutObjectRequest(getStatsBucketName(tenantID), MERGED_STATS_FILENAME + ".gz", localMergedFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	localMergedFile.delete();
    	
	}
	
	private void mergeAndZipFiles(File fileA, File fileB, File outFile) throws Exception {
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(outFile))));
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileA))));
		String line = null;
		while ((line = reader.readLine()) != null) {
			out.write(line + newline);
		}
		reader.close();
		
		reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileB))));
		while ((line = reader.readLine()) != null) {
			out.write(line + newline);
		}
		reader.close();
		out.close();
		
	    
		fileA.delete();
		fileB.delete();
	}
	
	protected void estimateModelParameters(String tenantID) throws Exception {
		
		String statsBucket = getStatsBucketName(tenantID);
		String statsKey = MERGED_STATS_FILENAME + ".gz";
		String parametersDomain = getDomainBasename() + tenantID;
		this.correlationsModel.estimateModelParameters(tenantID, statsBucket, statsKey, parametersDomain);
	}
	
	protected String getDomainBasename() {
		return "MODEL_COMBINED_";
	}
	
	protected String getStatsBucketName(String tenantID) {
		return STATS_BASE_BUCKETNAME + "combined" + tenantID;
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
				CombinationModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		HashSet<String> sourceIDs = new HashSet<String>();
    	for (AttributeObject attObject : productIds) {
    		sourceIDs.add(attObject.getUID());
    	}
    	
    	String semanticModelDomainName = getPrimaryModelDomainName(getDomainBasename(), tenantID);
    	HashMap<String, Double> targetIds = new HashMap<String, Double>();
    	for (AttributeObject attObject : productIds) {
    		String sourceItemId = attObject.getUID();
    		GetAttributesRequest request = new GetAttributesRequest();
    		request.setDomainName(semanticModelDomainName);
    		request.setItemName(sourceItemId);
    		GetAttributesResult result = sdb.getAttributes(request);
            
            String targetItemId = null;
        	double score = 0.0;
            for (Attribute attribute : result.getAttributes()) {
            	targetItemId = attribute.getName();
            	if (sourceIDs.contains(targetItemId)) {
            		continue;
            	}
            	score = Double.parseDouble(attribute.getValue());
            	
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
}
