package com.cleargist.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
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
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.catalog.dao.CatalogDAOImpl;
import com.cleargist.catalog.entity.jaxb.Catalog;
import com.cleargist.profile.Profile;


public class SemanticModel extends BaseModel {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final String RAW_PROFILES_FILENAME = "raw_profiles.gz";
	private static final String MERGED_STATS_FILENAME = "merged.gz";
	private static final String TFIDF_FILENAME = "tfidf.gz";
	private static final String IDF_FILENAME = "idf.txt";
	private static final String ASSOCIATIONS_FILENAME = "semantic_associations_";
	private static final String INCREMENTAL_ASSOCIATIONS_FILENAME = "incremental_semantic_associations_";
	private static final float THRESHOLD = 0.01f;
	private static final int MIN_DESCRIPTION_LENGTH = 5;
	private static final Locale locale = new Locale("el", "GR"); 
	private int topCorrelations;
	public static String newline = System.getProperty("line.separator"); 
	private Logger logger = Logger.getLogger(getClass());
	private CatalogDAO catalog;
	private AmazonS3 s3;
	private AmazonSimpleDB sdb;
	
	
	public SemanticModel() {
		try {
			this.s3 = new AmazonS3Client(new PropertiesCredentials(
					SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		}
		catch (IOException ex) {
			logger.error("Could not read credentials for S3 ... client not initialized");
		}
		
		try {
			this.sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
					SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
			this.sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		}
		catch (IOException ex) {
			logger.error("Could not read credentials for SimpleDB ... client not initialized");
		}
		
		
		this.topCorrelations = 10;
	}
	
	protected String getDomainBasename() {
		return "MODEL_SEMANTIC_";
	}
	
	protected String getStatsBucketName(String tenantID) {
		return STATS_BASE_BUCKETNAME + "semantic" + tenantID;
	}
	
	public void setTopCorrelations(int n) {
		this.topCorrelations = n > 0 ? n : 10;
	}
	
	private String removeSpecialChars(String in) {
		String out = in.replaceAll("\\d+\\.\\d+", "NUMBER");
		out = out.replaceAll("\\d+", "NUMBER");
		out = out.toLowerCase(locale);
		out = out.replaceAll("<li>", "");
		out = out.replaceAll("[\\.,\\(\\)\\?;!:\\[\\]\\{\\}\"%&\\*'\\+/>-]", "");
		out = out.replace('ά', 'α');
		out = out.replace('ό', 'ο');
		out = out.replace('ή', 'η');
		out = out.replace('ώ', 'ω');
		out = out.replace('ύ', 'υ');
		out = out.replace('έ', 'ε');
		out = out.replace('ί', 'ι');
		out = out.replace('"', ' ');
		out = out.replaceAll("\\s+", " ");
		out = out.trim();
		
		return out;
	}
	
	public void updateModel(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) 
	throws AmazonServiceException, AmazonClientException, Exception {
		
	}
	
	public static String getAssociationsKey(String tenantID) {
		return ASSOCIATIONS_FILENAME + tenantID + ".gz";
	}
	
	public void createModel(String tenantID) 
	throws AmazonServiceException, AmazonClientException, Exception {
		
		String bucketName = getStatsBucketName(tenantID);
		
		calculateSufficientStatistics(bucketName, STATS_BASE_FILENAME, tenantID);
		
		mergeSufficientStatistics(tenantID);
		
		estimateModelParameters(tenantID);
		
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
	
	protected void calculateSufficientStatistics(String bucketName, String baseFilename, String tenantID) throws Exception {
		
		logger.info("Initiating calculation of sufficient stats for tenant ID " + tenantID);
		
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
		// Create the raw data, get the description field of each item
		catalog = new CatalogDAOImpl();
		String rawProfilesFilename = RAW_PROFILES_FILENAME;
		extractDescriptionField(catalog.getAllProducts("", tenantID), bucketName, rawProfilesFilename, tenantID);
		
		/*
		PutObjectRequest rTemp = new PutObjectRequest(bucketName, RAW_PROFILES_FILENAME, new File("C:\\Users\\kboulis\\Downloads\\" + rawProfilesFilename));
		rTemp.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(rTemp);
    	*/
		logger.info("Description fields extracted for tenantID " + tenantID);
		
		// Compute the inverse document frequency
		HashMap<String, Float> idf = new HashMap<String, Float>();
		S3Object rawProfilesFile = s3.getObject(bucketName, RAW_PROFILES_FILENAME);
//		BufferedReader reader = new BufferedReader(new InputStreamReader(rawProfilesFile.getObjectContent()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(rawProfilesFile.getObjectContent())));
		String line = null;
		double totalItems = 0.0;
		while ((line = reader.readLine()) != null) {
			String[] topFields = line.split("\";\"");
			if (topFields.length != 2) {
				logger.warn("Skipping line " + line);
				continue;
			}
			topFields[1] = topFields[1].replaceAll("\"", "");
//			topFields[1] = removeSpecialChars(topFields[1]);
			String[] fields = topFields[1].split(" ");
			
			HashSet<String> uniqueTerms = new HashSet<String>();
			for (int i = 0; i < fields.length; i ++) {
				uniqueTerms.add(fields[i]);
			}
			for (String uniqueTerm : uniqueTerms) {
				Float cnt = idf.get(uniqueTerm);
				if (cnt == null) {
					idf.put(uniqueTerm, 1.0f);
				}
				else {
					idf.put(uniqueTerm, cnt.floatValue() + 1.0f);
				}
			}
			
			totalItems += 1.0;
		}
		reader.close();
		Set<String> keys = idf.keySet();
		for (String term : keys) {
			Float cnt = idf.get(term);
			
			float f = (float)Math.log(totalItems / (double)cnt.floatValue());
			
			idf.put(term, f);
		}
		idf.put("_NEW_TERM_", (float)Math.log(totalItems));
		
		// Persist the idf file
    	File localIdfFile = new File(IDF_FILENAME);
    	BufferedWriter out = new BufferedWriter(new FileWriter(localIdfFile));
    	StringBuffer sb = new StringBuffer();
    	for (Map.Entry<String, Float> me : idf.entrySet()) {
    		sb.append(me.getKey()); sb.append(";"); sb.append(me.getValue()); sb.append(newline);
    	}
    	out.write(sb.toString());
    	out.flush();
    	out.close();
    	
    	// Now copy the local idf file to S3
    	PutObjectRequest r = new PutObjectRequest(bucketName, IDF_FILENAME, localIdfFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	localIdfFile.delete();
    	logger.info("Finished with the inverse document frequency part");
		
    	
		// For each item, create the tfidf representation and persist it
    	HashMap<String, Integer> vocab = new HashMap<String, Integer>();
		rawProfilesFile = s3.getObject(bucketName, RAW_PROFILES_FILENAME);
//		reader = new BufferedReader(new InputStreamReader(rawProfilesFile.getObjectContent()));
		reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(rawProfilesFile.getObjectContent())));
		String tmpFilename = TFIDF_FILENAME + tenantID;
		File localTfidfFile = new File(tmpFilename);
		out = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(localTfidfFile))));
//		out = new BufferedWriter(new FileWriter(localTfidfFile));
		while ((line = reader.readLine()) != null) {
			String[] topFields = line.split("\";\"");
			if (topFields.length != 2) {
				logger.warn("Skipping line " + line);
				continue;
			}
			topFields[0] = topFields[0].replaceAll("\"", "");
			String itemName = topFields[0];
//			topFields[1] = removeSpecialChars(topFields[1]);
			
			HashMap<String, Float> hm = createTfIdf(topFields[1], idf);
			if (hm == null || hm.entrySet().size() == 0) {
				continue;
			}
			
			sb = new StringBuffer();
			sb.append(itemName);
			for (Map.Entry<String, Float> me : hm.entrySet()) {
				String word = me.getKey();
				Integer indx = vocab.get(word);
				if (indx == null) {
					indx = vocab.size();
					vocab.put(word, indx);
				}
				
				float score = me.getValue();
				float newScore = ((float)Math.round(score * 1000.0f)) / 1000.0f;
				sb.append(";"); sb.append(indx); sb.append(";"); sb.append(newScore);
			}
			sb.append(newline);
			out.write(sb.toString());
			out.flush();
		}
		reader.close();
		out.close();
		
		
		// Now copy the local tfidf file to S3
		r = new PutObjectRequest(bucketName, TFIDF_FILENAME, localTfidfFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	ObjectMetadata metadata = new ObjectMetadata();
    	metadata.setContentType("application/x-gzip");
    	r.setMetadata(metadata);
    	s3.putObject(r);
    	localTfidfFile.delete();
    	logger.info("Finished with the tfidf representation part");
	}
	
	private HashMap<String, Float> createTfIdf(String rawProfile, HashMap<String, Float> idf) {
		
		String[] fields = rawProfile.split(" ");
		
		HashMap<String, Float> hm = new HashMap<String, Float>();
		for (int i = 0; i < fields.length; i ++) {
			Float cnt = hm.get(fields[i]);
			if (cnt == null) {
				hm.put(fields[i], 1.0f);
			}
			else {
				hm.put(fields[i], cnt + 1.0f);
			}
		}
	
		Set<String> keys = hm.keySet();
		for (String term : keys) {
			Float v = idf.get(term);
			float idfValue = v == null ? idf.get("_NEW_TERM_").floatValue() : v.floatValue();
			
			float tf = hm.get(term).floatValue() / (float)fields.length;
			
			float tfidf = tf * idfValue;
			
			hm.put(term, tfidf);
		}
		
		return hm;
	}
	
	protected void mergeSufficientStatistics(String tenantID) throws Exception {
		
		String tfidfBucket = getStatsBucketName(tenantID);
		CopyObjectRequest copyRequest = new CopyObjectRequest(tfidfBucket, TFIDF_FILENAME, tfidfBucket, MERGED_STATS_FILENAME);
		s3.copyObject(copyRequest);
	}
	
	/*
	 *  Compute cosine similarity for each pair of items and persist the top-N both in S3 and in SimpleDB
	 */
	private void estimateModelParameters(List<HashMap<String, Float>> vectors, HashMap<Integer, String> itemNames, int k, 
			String bucketName, String filename, String tenantID) throws Exception {
		
		// First compute the denominators
		List<Double> denom = new ArrayList<Double>();
		for (HashMap<String, Float> hm : vectors) {
			double d = 0.0;
			for (Float f : hm.values()) {
				d += f * f;
			}
			denom.add(Math.sqrt(d));
		}
		
		// Now compute the cosine similarity
		String associationsFilename = getAssociationsKey(tenantID);
		File localAssociationsFile = new File(associationsFilename);
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(localAssociationsFile))));
//		BufferedWriter out = new BufferedWriter(new FileWriter(localAssociationsFile));
		for (int i = 0; i < k; i ++) {
			if ((i+1) % 100 == 0) {
				logger.info("Processed " + i + " items");
			}
			List<AttributeObject> topN = new ArrayList<AttributeObject>();
			HashMap<String, Float> hmI = vectors.get(i);
			Set<Map.Entry<String, Float>> hmISet = hmI.entrySet();
			int hmILen = hmI.size();
			for (int j = 0; j < vectors.size(); j ++) {
				if (j == i) {
					continue;
				}
				HashMap<String, Float> hmJ = vectors.get(j);
				int hmJLen = hmJ.size();
				if ((float)Math.abs(hmILen - hmJLen) / (float)hmILen > 0.5) {
					continue;
				}
				
				double d = 0.0;
				for (Map.Entry<String, Float> me : hmISet) {
					Float fJ = hmJ.get(me.getKey());
					if (fJ == null) {
						continue;
					}
					Float fI = me.getValue();
					
					d += fI * fJ;
				}
				if (d <= 0.0) {
					continue;
				}
				d /= (denom.get(i) * denom.get(j));  // To speed up calculations and since you are keeping the top-N you don't need the denom(i)
				
				if (d < THRESHOLD) {
					continue;
				}
				topN.add(new AttributeObject(itemNames.get(j), d));
			}
			Collections.sort(topN);
			
			if (topN.size() == 0) {
				continue;
			}
			List<AttributeObject> l = topN.size() < this.topCorrelations ? topN : topN.subList(0, this.topCorrelations);
			StringBuffer sb = new StringBuffer();
			sb.append(itemNames.get(i));
			for (AttributeObject attObject : l) {
				sb.append(";"); sb.append(attObject.getUID()); sb.append(";"); sb.append(Math.round(attObject.getScore() * 100.0));
			}
			sb.append(newline);
			
			out.write(sb.toString());
			out.flush();
		}
		out.close();
		
		
		PutObjectRequest r = new PutObjectRequest(bucketName, associationsFilename, localAssociationsFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	ObjectMetadata metadata = new ObjectMetadata();
    	metadata.setContentType("application/x-gzip");
    	r.setMetadata(metadata);
    	s3.putObject(r);
    	localAssociationsFile.delete();
    	
    	// Load into domain
    	loadFromS3File2Domain(bucketName, associationsFilename, getBackupModelDomainName(getDomainBasename(), tenantID));
	}
	
	protected void estimateModelParameters(String tenantID) throws Exception {
		
		List<HashMap<String, Float>> vectors = new ArrayList<HashMap<String, Float>>();
		HashMap<Integer, String> itemNames = new HashMap<Integer, String>();
		
		String bucketName = getStatsBucketName(tenantID);
		String key = MERGED_STATS_FILENAME;
		S3Object statsFile = s3.getObject(bucketName, key);
		if (statsFile == null) {
			logger.error("Could not read from key " + key + " bucket " + bucketName);
			throw new Exception();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(statsFile.getObjectContent())));
//		BufferedReader reader = new BufferedReader(new InputStreamReader(statsFile.getObjectContent()));
		int k = 0;
		String line = null;
		logger.info("Entering tfidf file");
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			
			itemNames.put(k, fields[0]);
			HashMap<String, Float> hm = new HashMap<String, Float>();
			for (int i = 1; i < fields.length - 1; i = i + 2) {
				float f = Float.parseFloat(fields[i+1]);
				hm.put(fields[i], f);
			}
			vectors.add(hm);
			
			k ++;
		}
		reader.close();
		
		
		estimateModelParameters(vectors, itemNames, vectors.size(), bucketName, key, tenantID);
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
	
	
	private void extractDescriptionField(List<Catalog.Products.Product> products, String bucketName, String filename, String tenantID) throws Exception {
		File localDescriptionsFile = new File(bucketName + filename);
//		BufferedWriter out = new BufferedWriter(new FileWriter(localDescriptionsFile));
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(localDescriptionsFile))));
		for (Catalog.Products.Product product : products) {
			String uid = product.getUid();
			String description = product.getDescription();
			
			if (uid != null && description != null && description.length() > 0) {
				StringBuffer sb = new StringBuffer();
				description = description.replace(System.getProperty("line.separator"), "");
				description = removeSpecialChars(description);
				if (description.length() < MIN_DESCRIPTION_LENGTH) {
					logger.info("Skipping description field for product " + uid + " tenant " + tenantID);
					continue;
				}
				sb.append("\""); sb.append(uid); sb.append("\";\""); sb.append(description); sb.append("\""); sb.append(newline);
				out.write(sb.toString());
				
				out.flush();
			}
		}
		out.close();
		
		// move to S3
		PutObjectRequest r = new PutObjectRequest(bucketName, filename, localDescriptionsFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	ObjectMetadata metadata = new ObjectMetadata();
    	metadata.setContentType("application/x-gzip");
    	r.setMetadata(metadata);
    	s3.putObject(r);
    	localDescriptionsFile.delete();
	}
	
	private void loadFromS3File2Domain(String bucketName, String filename, String domainName) throws Exception {
		
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		
		sdb.deleteDomain(new DeleteDomainRequest(domainName));
		Thread.sleep(5000);
		sdb.createDomain(new CreateDomainRequest(domainName));
		Thread.sleep(5000);
		
		
		S3Object associationsFile = s3.getObject(bucketName, filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(associationsFile.getObjectContent())));
		String line = null;
		File localOutFile = new File(associationsFile.getBucketName() + "_" + associationsFile.getKey() + "_");
		BufferedWriter out = new BufferedWriter(new FileWriter(localOutFile));
		while ((line = reader.readLine()) != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(line); sb.append(newline);
			out.write(sb.toString());
		}
		out.close();
		reader.close();
		
		reader = new BufferedReader(new FileReader(localOutFile));
		int tot = 0;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			
			String itemName = fields[0];
			List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
			int len = fields.length < 512 ? fields.length : 512; 
			for (int i = 1; i < len - 1; i = i + 2) {
				ReplaceableAttribute attribute = new ReplaceableAttribute(fields[i], fields[i+1], true);
				attributes.add(attribute);
			}
			items.add(new ReplaceableItem(itemName, attributes));
			if (items.size() == 25) {
        		writeSimpleDB(domainName, items);
        		items = new ArrayList<ReplaceableItem>();
        		
        		tot += 25;
        		if (tot % 100 == 0) {
        			logger.info("Written associations for " + tot + " products");
        		}
        		
        	}
		}
		reader.close();
		localOutFile.delete();
		
		if (items.size() > 0) {
			writeSimpleDB(domainName, items);
			items = new ArrayList<ReplaceableItem>();
		}
		
	}
	
	private void writeSimpleDB(String SimpleDBDomain, List<ReplaceableItem> recsPairs) 
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
	/* 
	 * This is used to incrementally update the model. Reduces calculations from N^2 to N. Valid only for small changes (insertions / deletions) 
	 * of catalog
	 */
	public void updateModel(String tenantID) throws Exception {
		
		List<Catalog.Products.Product> newProducts = catalog.getAllProducts("", tenantID);
		
		List<HashMap<String, Float>> vectors = new ArrayList<HashMap<String, Float>>();
		HashMap<Integer, String> itemNames = new HashMap<Integer, String>();
		
		// Load the old tfidf file in memory
		String oldTfidfFilename = TFIDF_FILENAME;
		S3Object olfTfIdfFile = s3.getObject(getStatsBucketName(tenantID), oldTfidfFilename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(olfTfIdfFile.getObjectContent()));
		String line = null;
		HashMap<String, HashMap<String, Float>> oldProfiles = new HashMap<String, HashMap<String, Float>>();
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			HashMap<String, Float> hm = new HashMap<String, Float>();
			for (int i = 1; i < fields.length; i = i + 2) {
				String item = fields[i];
				Float value = Float.parseFloat(fields[i+1]);
				
				hm.put(item, value);
			}
			oldProfiles.put(fields[0], hm);
		}
		reader.close();
		int numberOfOriginalProducts = oldProfiles.size();
		
		
		// Load the old idf in memory
		HashMap<String, Float> idf = new HashMap<String, Float>();
		String idfFilename = IDF_FILENAME;
		S3Object oldIdfFile = s3.getObject(getStatsBucketName(tenantID), idfFilename);
		reader = new BufferedReader(new InputStreamReader(oldIdfFile.getObjectContent()));
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			String oldProductId = fields[0];
			float value = Float.parseFloat(fields[1]);
			
			idf.put(oldProductId, value);
		}
		reader.close();
		
		// Find the new items
		int indx = 0;
		HashSet<String> newProductsId = new HashSet<String>();
		for (Catalog.Products.Product newProduct : newProducts) {
			newProductsId.add(newProduct.getUid());
			if (!oldProfiles.containsKey(newProduct.getUid())) {
				vectors.add(createTfIdf(newProduct.getDescription(), idf));
				itemNames.put(indx, newProduct.getUid());
				
				indx ++;
			}
		}
		int numberOfNewItems = indx;
		
		
		// Determine the items that will be deleted
		HashSet<String> deletedProducts = new HashSet<String>();
		Set<String> keys = oldProfiles.keySet();
		for (String oldProductId : keys) {
			if (!newProductsId.contains(oldProductId)) {
				
				oldProfiles.remove(oldProductId);
				
				deletedProducts.add(oldProductId);
			}
		}
		
		int numDiffProducts = numberOfNewItems + deletedProducts.size();
		if (((float)numDiffProducts / (float)numberOfOriginalProducts) > 0.05f) {
			logger.warn("Too big of a change between incremental updates of semantic model...skipping update");
			throw new Exception();
		}
		
		indx = vectors.size();
		keys = oldProfiles.keySet();
		for (String key : keys) {
			vectors.add(oldProfiles.get(key));
			itemNames.put(indx, key);
			oldProfiles.remove(key);
			
			indx ++;
		}
		
		// Now compute the cosine similarity between newProfiles <-> oldProfiles
		estimateModelParameters(vectors, itemNames, numberOfNewItems, getStatsBucketName(tenantID), INCREMENTAL_ASSOCIATIONS_FILENAME, tenantID);
		HashMap<String, HashMap<String, Float>> newAssociations = new HashMap<String, HashMap<String, Float>>();
		S3Object incrementalAssociationsFile = s3.getObject(getStatsBucketName(tenantID), INCREMENTAL_ASSOCIATIONS_FILENAME);
		reader = new BufferedReader(new InputStreamReader(incrementalAssociationsFile.getObjectContent()));
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			
			String productID = fields[0];
			HashMap<String, Float> hm = new HashMap<String, Float>();
			for (int i = 1; i < fields.length; i = i + 2) {
				String targetProductId = fields[i];
				float value = Float.parseFloat(fields[i+1]);
				
				hm.put(targetProductId, value);
			}
			newAssociations.put(productID, hm);
		}
		reader.close();
		
		
		// Delete from the old associations file the deleted items and merge with new ones
		File localAssociationsFile = new File(ASSOCIATIONS_FILENAME);
		BufferedWriter out = new BufferedWriter(new FileWriter(localAssociationsFile));
		S3Object oldAssociationsFile = s3.getObject(getStatsBucketName(tenantID), ASSOCIATIONS_FILENAME);
		reader = new BufferedReader(new InputStreamReader(oldAssociationsFile.getObjectContent()));
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			
			String productID = fields[0];
			if (deletedProducts.contains(productID)) {
				continue;
			}
			
			List<AttributeObject> rankedList = new ArrayList<AttributeObject>();
			for (int i = 1; i < fields.length; i = i + 2) {
				String targetProductId = fields[i];
				float value = Float.parseFloat(fields[i+1]);
				
				if (deletedProducts.contains(targetProductId)) {
					continue;
				}
				
				rankedList.add(new AttributeObject(targetProductId, value));
			}
			HashMap<String, Float> hm = newAssociations.get(productID);
			if (hm != null && hm.size() > 0) {
				for (Map.Entry<String, Float> me : hm.entrySet()) {
					rankedList.add(new AttributeObject(me.getKey(), me.getValue()));
				}
				Collections.sort(rankedList);
				if (rankedList.size() > this.topCorrelations) {
					rankedList = rankedList.subList(0, this.topCorrelations);
				}
			}
			
			
			StringBuffer sb = new StringBuffer();
			sb.append(productID);
			for (AttributeObject attObject : rankedList) {
				sb.append(";"); sb.append(attObject.getUID()); sb.append(";"); sb.append(attObject.getScore());
			}
			sb.append(newline);
			
			out.write(sb.toString());
			out.flush();
		}
		reader.close();
		out.close();
		
		// Now copy the local associations file to S3
    	PutObjectRequest r = new PutObjectRequest(getStatsBucketName(tenantID), ASSOCIATIONS_FILENAME, localAssociationsFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	localAssociationsFile.delete();
    	
		// Load into domain
    	loadFromS3File2Domain(getStatsBucketName(tenantID), ASSOCIATIONS_FILENAME, getBackupModelDomainName(getDomainBasename(), tenantID));
    	
		// Write the new tfidf file
		
		// Write the new idf file
    	
		// Swap models
    	swapModelDomainNames(getDomainBasename(), tenantID);
	}
}
