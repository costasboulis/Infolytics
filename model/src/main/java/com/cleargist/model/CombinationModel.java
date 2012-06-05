package com.cleargist.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
	private AmazonS3 s3;
	
	public CombinationModel() {
		this.correlationsModel = new CorrelationsModel();
		this.semanticModel = new SemanticModel();
		this.semanticModel.setTopCorrelations(DEFAULT_TOP_N_SEMANTIC);
		this.correlationsModel.setTopCorrelations(DEFAULT_TOP_N_CORRELATIONS);
		this.SS0Normalization = DEFAULT_SS0_NORMALIZATION;
		
		try {
			s3 = new AmazonS3Client(new PropertiesCredentials(
					CombinationModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		}
		catch (IOException ex) {
			logger.error("Could not read credentials for S3 .. s3 client not initialized");
		}
		
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
		
		this.semanticModel.createModel(tenantID);
		
		String rawCountsBucketName = this.correlationsModel.getStatsBucketName(tenantID);
		this.correlationsModel.calculateSufficientStatistics(rawCountsBucketName, STATS_BASE_FILENAME, tenantID);
		
		String semanticAssociationsBucket = BaseModel.STATS_BASE_BUCKETNAME + "semantic" + tenantID;
		String semanticAssociationsKey = SemanticModel.getAssociationsKey(tenantID);
		convertSemanticAssociationsToCounts(semanticAssociationsBucket, semanticAssociationsKey,
											rawCountsBucketName, STATS_BASE_FILENAME + "semantic");
	}
	
	protected void mergeSufficientStatistics(String tenantID) throws Exception {
		
		this.correlationsModel.mergeSufficientStatistics(tenantID);
    	
	}
	
	private void convertSemanticAssociationsToCounts(String semanticAssociationsBucket, String semanticAssociationsKey,
													 String countsBucket, String semanticCountsKey) throws Exception {
		
		File localSS0MergedFile = new File(semanticCountsKey + "SS0" + UUID.randomUUID().toString());
		File localSS1MergedFile = new File(semanticCountsKey + "SS1" + UUID.randomUUID().toString());
		BufferedWriter outSS0 = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(localSS0MergedFile))));
		BufferedWriter outSS1 = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(localSS1MergedFile))));
		
		S3Object semanticAssociationsFile = s3.getObject(semanticAssociationsBucket, semanticAssociationsKey);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(semanticAssociationsFile.getObjectContent())));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			String sourceId = fields[0];
			float sum  = 0.0f;
			StringBuffer sb = new StringBuffer();
			sb.append(sourceId);
			for (int i = 1; i < fields.length -1 ; i = i + 2) {
				String targetId = fields[i];
				float score = 0.0f;
				try {
					score = Float.parseFloat(fields[i + 1]);
				}
				catch (NumberFormatException ex) {
					logger.error("Could not parse asscoaition value \"" + fields[i + 1] + "\" between source " + sourceId + " and target " + targetId);
					continue;
				}
				float normalizedScore = score * this.SS0Normalization;
				
				sb.append(";"); sb.append(targetId); sb.append(";"); sb.append(normalizedScore); 
				
				sum += normalizedScore;
			}
			sb.append(newline);
			if (sum > 0.0f) {
				outSS1.write(sb.toString());
				outSS0.write(sourceId + ";" + sum + newline);
			}
		}
		reader.close();
		outSS0.close();
		outSS1.close();
		
		
		// Merge the SS0 and SS1 files to one file
		File outFile = new File(semanticCountsKey + UUID.randomUUID().toString());
		mergeAndZipFiles(localSS0MergedFile, localSS1MergedFile, outFile);
		
		// Upload the updated counts
		PutObjectRequest r = new PutObjectRequest(countsBucket, semanticCountsKey, outFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	localSS0MergedFile.delete();
    	localSS1MergedFile.delete();
    	outFile.delete();
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
		
		// The stats are in the bucket of correlations model
		String statsBucket = this.correlationsModel.getStatsBucketName(tenantID);
    	ObjectListing objectListing = s3.listObjects(statsBucket);
    	List<S3ObjectSummary> objSummaries = objectListing.getObjectSummaries();
    	
    	if (objSummaries.size() != 1) {
    		logger.warn("Found more than one stats files for tenant " + tenantID + " in bucket " + statsBucket);
    		return;
    	}
    	
    	String statsKey = objSummaries.get(0).getKey();
    	
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
