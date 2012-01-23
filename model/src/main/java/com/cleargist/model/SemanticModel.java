package com.cleargist.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
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
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.catalog.dao.CatalogDAOImpl;
import com.cleargist.catalog.entity.jaxb.Catalog;


public class SemanticModel extends Model {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final String BASE_BUCKET_NAME = "profilessemanticmodel";
	private static final String RAW_PROFILES_FILENAME = "raw_profiles.txt";
	private static final String TFIDF_FILENAME = "tfidf.txt";
	private static final String ASSOCIATIONS_FILENAME = "semantic_associations_";
	private static final float THRESHOLD = 0.01f;
	private static final int TOP_N = 10;
	public static String newline = System.getProperty("line.separator");
	protected static final Locale locale = new Locale("el", "GR"); 
	private Logger logger = Logger.getLogger(getClass());
	private CatalogDAO catalog;
	
	protected String getDomainBasename() {
		return "MODEL_SEMANTIC_";
	}
	
	protected String getStatsBucketName(String tenantID) {
		return STATS_BASE_BUCKETNAME + "semantic" + tenantID;
	}
	
	private String removeSpecialChars(String in) {
		String out = in.replaceAll("\\d+\\.\\d+", "NUMBER");
		out = out.replaceAll("\\d+", "NUMBER");
		out = out.toLowerCase(locale);
		out = out.replaceAll("[\\.,\\(\\)\\?;!:\\[\\]\\{\\}\"%&\\*'\\+/>-]", "");
		out = out.replace('Ü', 'á');
		out = out.replace('Ý', 'å');
		out = out.replace('ß', 'é');
		out = out.replace('ü', 'ï');
		out = out.replace('ý', 'õ');
		out = out.replace('þ', 'ù');
		out = out.replace('Þ', 'ç');
		out = out.replaceAll("\\s+", " ");
		out = out.trim();
		
		return out;
	}
	
	protected void calculateSufficientStatistics(String bucketName, String baseFilename, String tenantID) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		
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
		
		
		// Compute the inverse document frequency
		HashMap<String, Float> df = new HashMap<String, Float>();
		S3Object rawProfilesFile = s3.getObject(bucketName, RAW_PROFILES_FILENAME);
		BufferedReader reader = new BufferedReader(new InputStreamReader(rawProfilesFile.getObjectContent()));
		String line = null;
		double totalItems = 0.0;
		while ((line = reader.readLine()) != null) {
			String[] topFields = line.split("\";\"");
			if (topFields.length != 2) {
				logger.warn("Skipping line " + line);
				continue;
			}
			topFields[1] = topFields[1].replaceAll("\"", "");
			topFields[1] = removeSpecialChars(topFields[1]);
			String[] fields = topFields[1].split(" ");
			
			HashSet<String> uniqueTerms = new HashSet<String>();
			for (int i = 0; i < fields.length; i ++) {
				uniqueTerms.add(fields[i]);
			}
			for (String uniqueTerm : uniqueTerms) {
				Float cnt = df.get(uniqueTerm);
				if (cnt == null) {
					df.put(uniqueTerm, 1.0f);
				}
				else {
					df.put(uniqueTerm, cnt.floatValue() + 1.0f);
				}
			}
			
			totalItems += 1.0;
		}
		reader.close();
		Set<String> keys = df.keySet();
		for (String term : keys) {
			Float cnt = df.get(term);
			
			float f = (float)Math.log(totalItems / (double)cnt.floatValue());
			
			df.put(term, f);
		}
		
    	
		// For each item, create the tfidf representation and persist it
		rawProfilesFile = s3.getObject(bucketName, RAW_PROFILES_FILENAME);
		reader = new BufferedReader(new InputStreamReader(rawProfilesFile.getObjectContent()));
		String tmpFilename = TFIDF_FILENAME + tenantID;
		File localTfidfFile = new File(tmpFilename);
		BufferedWriter out = new BufferedWriter(new FileWriter(localTfidfFile));
		while ((line = reader.readLine()) != null) {
			String[] topFields = line.split("\";\"");
			if (topFields.length != 2) {
				logger.warn("Skipping line " + line);
				continue;
			}
			topFields[0] = topFields[0].replaceAll("\"", "");
			String itemName = topFields[0];
			topFields[1] = removeSpecialChars(topFields[1]);
			String[] fields = topFields[1].split(" ");
			
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
			
			keys = hm.keySet();
			StringBuffer sb = new StringBuffer();
			sb.append(itemName);
			for (String term : keys) {
				Float cnt = df.get(term);
				
				float f = cnt / (float)fields.length;
				
				sb.append(";"); sb.append(term); sb.append(";"); sb.append(f);
			}
			sb.append(newline);
			out.write(sb.toString());
			out.flush();
		}
		reader.close();
		out.close();
		df = null;
		
		
		// Now copy the local tfidf file to S3
    	PutObjectRequest r = new PutObjectRequest(bucketName, TFIDF_FILENAME, localTfidfFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	localTfidfFile.delete();
    	
	}
	
	public void mergeSufficientStatistics(String bucketName, String mergedStatsFilename, String tenantID) throws Exception {
		// Nothing for now, this is not MapReducable for the moment
	}
	
	protected void estimateModelParameters(String bucketName, String filename, String tenantID) throws Exception {
		// Compute cosine similarity for each pair of items and persist the top-N both in S3 and in SimpleDB
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		List<HashMap<String, Float>> vectors = new ArrayList<HashMap<String, Float>>();
		List<Double> denom = new ArrayList<Double>();
		HashMap<Integer, String> itemNames = new HashMap<Integer, String>();
		S3Object tfidfFile = s3.getObject(bucketName, filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(tfidfFile.getObjectContent()));
		int k = 0;
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			
			itemNames.put(k, fields[0]);
			HashMap<String, Float> hm = new HashMap<String, Float>();
			double d = 0.0;
			for (int i = 1; i < fields.length; i = i + 2) {
				float f = Float.parseFloat(fields[i+1]);
				hm.put(fields[i], f);
				
				d += f * f;
			}
			denom.add(Math.sqrt(d));
			vectors.add(hm);
			
			k ++;
		}
		reader.close();
		
		
		
		String associationsFilename = ASSOCIATIONS_FILENAME + tenantID;
		File localAssociationsFile = new File(associationsFilename);
		BufferedWriter out = new BufferedWriter(new FileWriter(localAssociationsFile));
		for (int i = 0; i < vectors.size(); i ++) {
			if (i % 1000 == 0) {
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
			List<AttributeObject> l = topN.size() < TOP_N ? topN : topN.subList(0, TOP_N);
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
    	s3.putObject(r);
    	localAssociationsFile.delete();
		
    	// Load into domain
    	loadFromS3File2Domain(bucketName, associationsFilename, getBackupModelDomainName(getDomainBasename(), tenantID));
	}

	public List<Catalog.Products.Product> getRecommendedProducts(List<String> productIDs, String tenantID, Filter filter) throws Exception {
		double weight = (double)productIDs.size();
		List<AttributeObject> productIDsInternal = new LinkedList<AttributeObject>();
		for (String productID : productIDs) {
			productIDsInternal.add(new AttributeObject(productID, weight));
			weight -= 1.0;
		}
		
		return getRecommendedProductsInternal(productIDsInternal, tenantID, filter);
	}
	
	private List<Catalog.Products.Product> getRecommendedProductsInternal(List<AttributeObject> productIds, String tenantID, Filter filter) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
		HashSet<String> sourceIDs = new HashSet<String>();
    	for (AttributeObject attObject : productIds) {
    		sourceIDs.add(attObject.getUID());
    	}
    	
    	String semanticModelDomainName = getPrimaryModelDomainName(getDomainBasename(), tenantID);
    	HashMap<String, Double> targetIds = new HashMap<String, Double>();
    	for (AttributeObject attObject : productIds) {
    		String sourceItemId = attObject.getUID();
    		String selectExpression = "select * from `" + semanticModelDomainName + "` where itemName() = '" + sourceItemId + "' limit 1";
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
	
	
	
	public List<Catalog.Products.Product> getPersonalizedRecommendedProducts(String userID, String tenantID, Filter filter) throws Exception {
		// Retrieve the user profile
		List<AttributeObject> sourceIDs = getUserProfile(userID, tenantID);
		return getRecommendedProductsInternal(sourceIDs, tenantID, filter);
	}
	
	
	private void dummyCopyProfiles(String tenantID) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String bucketName = BASE_BUCKET_NAME + tenantID;
		s3.copyObject("sintagespareas", "recipesTexts.txt", bucketName, "raw_profiles.txt");
	}
	
	private void extractDescriptionField(List<Catalog.Products.Product> products, String bucketName, String filename, String tenantID) throws Exception {
		File localDescriptionsFile = new File(bucketName + filename);
		BufferedWriter out = new BufferedWriter(new FileWriter(localDescriptionsFile));
		for (Catalog.Products.Product product : products) {
			String uid = product.getUid();
			String description = product.getDescription();
			
			if (uid != null && description != null && description.length() > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("\""); sb.append(uid); sb.append("\";\""); sb.append(description); sb.append("\""); sb.append(newline);
				out.write(sb.toString());
				
				out.flush();
			}
		}
		out.close();
		
		// move to S3
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		PutObjectRequest r = new PutObjectRequest(bucketName, filename, localDescriptionsFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	localDescriptionsFile.delete();
	}
	
	private void loadFromS3File2Domain(String bucketName, String filename, String domainName) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		
		for (String domain : sdb.listDomains().getDomainNames()) {
			if (domain.equals(domainName)) {
				sdb.deleteDomain(new DeleteDomainRequest(domainName));
				break;
			}
		}
		sdb.createDomain(new CreateDomainRequest(domainName));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		S3Object associationsFile = s3.getObject(bucketName, filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(associationsFile.getObjectContent()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			
			String itemName = fields[0];
			List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
			for (int i = 1; i < fields.length; i = i + 2) {
				String attributeName = "Attribute_" + fields[i];
				StringBuffer sb = new StringBuffer();
				sb.append(fields[i]); sb.append(";"); sb.append(fields[i+1]);
				ReplaceableAttribute attribute = new ReplaceableAttribute(attributeName, sb.toString(), true);
				attributes.add(attribute);
			}
			items.add(new ReplaceableItem(itemName, attributes));
			if (items.size() == 25) {
        		writeSimpleDB(sdb, domainName, items);
        		items = new ArrayList<ReplaceableItem>();
        	}
		}
		reader.close();
		
		if (items.size() > 0) {
			writeSimpleDB(sdb, domainName, items);
			items = new ArrayList<ReplaceableItem>();
		}
		
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
	
	public void updateModel(String tenantID) throws Exception {
		// Determine the items that will be removed
		
		// Create the raw data for the new items
		
		// Create the tfidf representation for the new items and persist them
		
		// Compute cosine similarity for the new items only and delete the similarities of deleted items
		
		// Swap models
	}
}
