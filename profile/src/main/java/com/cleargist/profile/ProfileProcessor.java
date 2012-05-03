package com.cleargist.profile;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeletableItem;
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


public abstract class ProfileProcessor {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	public static String newline = System.getProperty("line.separator");
	protected List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
	protected List<DeletableItem> deletedItems = new ArrayList<DeletableItem>();
	private Logger logger = Logger.getLogger(getClass());
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private static final int NO_OF_THREADS_TO_RUN = 6;
	private static final int MAX_RECORDS_TO_PROCESS = 25;
	private static final int FIXED_NO_OF_THREADS_OPER = 50;
	
	
	public static List<Item> querySimpleDB(String selectExpression) throws AmazonServiceException, AmazonClientException, Exception{
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		String resultNextToken = null;
		String selectExpressionWithLimit = selectExpression + " limit 2500";
		SelectRequest selectRequest = new SelectRequest(selectExpressionWithLimit);
		List<Item> allItems = new LinkedList<Item>();
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
		    allItems.addAll(selectResult.getItems());
		    
		} while (resultNextToken != null);
		
		return allItems;
	}
	
	public List<Future<List<Item>>> getDataSinceLastUpdate(String tenantID, String latestProfile, int profHorizon) throws Exception {
		String datePattern = "yyMMddHHmmssSSSZ";
        Date latestProfileDate = (new SimpleDateFormat(datePattern)).parse(latestProfile);
        Calendar lastUpdate = new GregorianCalendar();
        lastUpdate.setTime(latestProfileDate);
        
        Date today = new Date();
        Calendar toDate = new GregorianCalendar();
        toDate.setTime(today);
        toDate.add(Calendar.MONTH, -profHorizon);
        
        Calendar fromDate = new GregorianCalendar();
        fromDate.setTime(lastUpdate.getTime());
        fromDate.add(Calendar.MONTH, -profHorizon);
        
        return getDataSinceLastUpdate(tenantID, toDate, lastUpdate, fromDate);
	}
	
	public List<Future<List<Item>>> getDataSinceLastUpdate(String tenantID, Calendar toDate, Calendar lastUpdate, Calendar fromDate) throws Exception {
		
		
    	/*
    	RecommendationsDAO recsDAO = new RecommendationsDAOImpl();
    	Tenant tenant = recsDAO.getTenantById(tenantID);
    	if (tenant == null) {
    		logger.error("Could not find tenant with ID " + tenantID);
    		throw new Exception();
    	}
    	Calendar lastUpdate = Calendar.getInstance();
    	lastUpdate.setTime(tenant.getLatestProfile()  == null ? new Date() : tenant.getLatestProfile());
    	
    	Calendar lastUpdateFrom = Calendar.getInstance();
		lastUpdateFrom.setTime(lastUpdate.getTime());     
		lastUpdateFrom.add(Calendar.MONTH, tenant.getProfileHorizon());      // Profile horizon, retrieve this from tenant profile
    	
		Calendar currentDateFrom = Calendar.getInstance();
		this.currentDate = new Date();
		currentDateFrom.setTime(this.currentDate);     
		currentDateFrom.add(Calendar.MONTH, tenant.getProfileHorizon());      // Profile horizon, retrieve this from tenant profile
		*/
    	
    	/*
		// Retrieve the date of the last profile update
    	Connection conn = null;
    	Statement stmt = null;
    	ResultSet rs = null;
    	try {
    	    conn =
    	       DriverManager.getConnection("jdbc:mysql://176.34.191.239:3306/sample",
    	                                   "root", "");

    	    stmt = conn.createStatement();
    	    rs = stmt.executeQuery("SELECT * FROM example_timestamp");
    	    while (rs.next()) {
    	    	String message = rs.getString(2);
        	    Date date = rs.getDate(3);
    	    }
    	    

    	   
    	} catch (SQLException ex) {
    	    // handle any errors
    	    logger.error("SQLException: " + ex.getMessage());
    	    logger.error("SQLState: " + ex.getSQLState());
    	    logger.error("VendorError: " + ex.getErrorCode());
    	}
    	finally {
    	    // it is a good idea to release
    	    // resources in a finally{} block
    	    // in reverse-order of their creation
    	    // if they are no-longer needed

    	    if (rs != null) {
    	        try {
    	            rs.close();
    	        } catch (SQLException sqlEx) { } // ignore

    	        rs = null;
    	    }

    	    if (stmt != null) {
    	        try {
    	            stmt.close();
    	        } catch (SQLException sqlEx) { } // ignore

    	        stmt = null;
    	    }
    	}
    	*/
    	
    	
		
		
    	List<Future<List<Item>>> list = new ArrayList<Future<List<Item>>>();

		ExecutorService pool = Executors.newFixedThreadPool(FIXED_NO_OF_THREADS_OPER);
		IncrementalDataThread incCallable = new IncrementalDataThread(tenantID, lastUpdate);
		DecrementalDataThread decCallable = new DecrementalDataThread(tenantID, fromDate, toDate);
		Future<List<Item>> incFuture = pool.submit(incCallable);
		Future<List<Item>> decFuture = pool.submit(decCallable);

		list.add(incFuture);
		list.add(decFuture);
		
		
		
		return list;
	}
	
	// Gets as input the raw data, implements custom weighting, filtering logic and produces a profile of the form UID, <PID, VALUE>+
	public abstract List<Profile> createProfile(List<Item> rawData) throws Exception;
	
	private String getProfileDomainName(String tenantID) {
		return "PROFILE_" + tenantID;
	}
	
	private void updateProfilesSimpleDB(List<Profile> incrementalProfiles, List<Profile> decrementalProfiles, String tenantID) 
	throws Exception {
		
		// Retrieve existing profiles and merge / write to SimpleDB
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
    	String profileDomain = getProfileDomainName(tenantID);
    	
    	// Do the incremental profiles
		for (Profile incrementalProfile : incrementalProfiles) {
			String userID = incrementalProfile.getUserID();
			ReplaceableItem item = new ReplaceableItem();
			item.setName(userID);
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			HashSet<String> productIDs = new HashSet<String>();
			
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(profileDomain);
			request.setItemName(userID);
			GetAttributesResult result = sdb.getAttributes(request);
			
			// Update attributes
			for (Attribute attribute : result.getAttributes()) {
				String productID = attribute.getName();
            	Float score = null;
            	try {
            		score = Float.parseFloat(attribute.getValue());
            	}
            	catch (NumberFormatException ex) {
            		logger.error("Could not parse value " + attribute.getValue() + " ... skipping");
            		continue;
            	}
            	
				productIDs.add(productID);
            		
				Float incrementalScore = incrementalProfile.getAttributes().get(productID);
				if (incrementalScore == null) {
					continue;   // Nothing to add in this attribute
				}
				else {
					// Update the score of the attribute
					float updatedScore = score + incrementalScore.floatValue();
            			
					ReplaceableAttribute att = new ReplaceableAttribute(attribute.getName(), Float.toString(updatedScore), true);
					attributes.add(att);
				}
			}
			
			// Add new attributes
			for (Map.Entry<String, Float> incrementalProfileAttributes : incrementalProfile.getAttributes().entrySet()) {
				String productID = incrementalProfileAttributes.getKey();
				if (!productIDs.contains(productID)) {
					float score = incrementalProfileAttributes.getValue().floatValue();
					ReplaceableAttribute att = new ReplaceableAttribute(productID, Float.toString(score), true);
        			attributes.add(att);
				}
			}
			item.setAttributes(attributes);
			items.add(item);
		
		}
		// Increment profiles
		batchInsert(profileDomain);
		
		
		// Now do the decremental profiles
		for (Profile decrementalProfile : decrementalProfiles) {
			String userID = decrementalProfile.getUserID();
			
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			List<Attribute> deleteAttributes = new LinkedList<Attribute>();
			
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(profileDomain);
			request.setItemName(userID);
			GetAttributesResult result = sdb.getAttributes(request);
			for (Attribute attribute : result.getAttributes()) {
				String productID = attribute.getName();
            	Float score = null;
            	try {
            		score = Float.parseFloat(attribute.getValue());
            	}
            	catch (NumberFormatException ex) {
            		logger.error("Could not parse value " + attribute.getValue() + " ... skipping");
            		continue;
            	}
            		
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
						ReplaceableAttribute att = new ReplaceableAttribute(productID, Float.toString(updatedScore), true);
						attributes.add(att);
					}
					
				}
			}
			
			if (attributes.size() > 0) {
				// Update attributes
				ReplaceableItem item = new ReplaceableItem();
				item.setName(userID);
				item.setAttributes(attributes);
				items.add(item);
			}
			
			if (deleteAttributes.size() > 0) {
				// Delete attributes and possibly profiles
				DeletableItem item = new DeletableItem();
				item.setName(userID);
				item.setAttributes(deleteAttributes);
				// Delete any attributes with zero counts
				batchDelete(sdb, profileDomain, item, false);
			}
			
		}
		// Update the attributes that have decreased in value
		batchInsert(profileDomain);
		
		// Delete any attributes with zero counts
		batchDelete(sdb, profileDomain, null, true);
		
	}
	
	private void updateProfilesS3(HashMap<String, Profile> incrementalProfiles, HashMap<String, Profile> decrementalProfiles, 
								String profilesBucket, int maxProfilesPerFile) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		if (!s3.doesBucketExist(profilesBucket)) {
			CreateBucketRequest createBucketRequest = new CreateBucketRequest(profilesBucket, Region.EU_Ireland);
			s3.createBucket(createBucketRequest);
		}
		
		List<String> profileKeys = new LinkedList<String>();
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
		listObjectsRequest.setBucketName(profilesBucket);
		
		String marker = null;
		do {
			listObjectsRequest.setMarker(marker);
			ObjectListing listing = s3.listObjects(listObjectsRequest);
			for (S3ObjectSummary summary : listing.getObjectSummaries() ) {
				profileKeys.add(summary.getKey());
			}
			marker = listing.getNextMarker();
			
		} while (marker != null);
		
		String filename = "PROFILES_" + UUID.randomUUID().toString();
		File localFile = new File(filename);
		BufferedWriter writer = new BufferedWriter(new FileWriter(localFile));
		int linesWritten = 0;
		
		for (String key : profileKeys) {
			
			S3Object profile = s3.getObject(profilesBucket, key);
			BufferedReader reader = new BufferedReader(new InputStreamReader(profile.getObjectContent()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(" ");
				String userId = fields[0];
				
				Profile incr = incrementalProfiles.get(userId);
				Profile decr = decrementalProfiles.get(userId);
				if (incr == null && decr == null) {                      // Profiles that haven't changed
					StringBuffer sb = new StringBuffer();
					sb.append(line); sb.append(newline);
					writer.write(line);
					writer.flush();
					linesWritten ++;
					if (linesWritten > maxProfilesPerFile) {
						// Copy the local file to S3
				    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);  
				    	r.setStorageClass(StorageClass.ReducedRedundancy);
				    	s3.putObject(r);
						linesWritten = 0;
						writer.close();
						boolean localFileDeleted = localFile.delete();
				    	if (!localFileDeleted) {
				    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
				    	}
						filename = "PROFILES_" + UUID.randomUUID().toString();
						localFile = new File(filename);
						writer = new BufferedWriter(new FileWriter(localFile));
					}
					
					continue;
				}
				
				Profile existing = new Profile(line);
				if (incr != null) {
					existing.merge(incr);
					incrementalProfiles.remove(userId);
				}
				if (decr != null) {
					existing.reduce(decr);
					if (existing.getAttributes().size() == 0) {  // Profile needs to be deleted
						continue;
					}
				}
				
				StringBuffer sb = new StringBuffer();
				sb.append(existing.toString()); sb.append(newline);
				writer.write(line);
				writer.flush();
				linesWritten ++;
				if (linesWritten > maxProfilesPerFile) {
					// Copy the local file to S3
			    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);
			    	r.setStorageClass(StorageClass.ReducedRedundancy);
			    	s3.putObject(r);
					linesWritten = 0;
					writer.close();
					boolean localFileDeleted = localFile.delete();
			    	if (!localFileDeleted) {
			    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
			    	}
					filename = "PROFILES_" + UUID.randomUUID().toString();
					localFile = new File(filename);
					writer = new BufferedWriter(new FileWriter(localFile));
				}
				
			}
			reader.close();
			
			s3.deleteObject(profilesBucket, key);
		}
		
		
		// Write the new users
		for (Profile newUserProfile : incrementalProfiles.values()) {
			StringBuffer sb = new StringBuffer();
			sb.append(newUserProfile.toString()); sb.append(newline);
			writer.write(sb.toString());
			writer.flush();
			linesWritten ++;
			if (linesWritten > maxProfilesPerFile) {
				// Copy the local file to S3
		    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);
		    	r.setStorageClass(StorageClass.ReducedRedundancy);
		    	s3.putObject(r);
				linesWritten = 0;
				writer.close();
				boolean localFileDeleted = localFile.delete();
		    	if (!localFileDeleted) {
		    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
		    	}
				filename = "PROFILES_" + UUID.randomUUID().toString();
				localFile = new File(filename);
				writer = new BufferedWriter(new FileWriter(localFile));
			}
		}
		writer.close();
		
		// Copy the local file to S3
    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	boolean localFileDeleted = localFile.delete();
    	if (!localFileDeleted) {
    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
    	}
	}
	
	public void updateProfiles(String tenantID, String latestProfile, int profileHorizon) throws Exception {
		String datePattern = "yyMMddHHmmssSSSZ";
        Date latestProfileDate = (new SimpleDateFormat(datePattern)).parse(latestProfile);
        Calendar lastUpdate = new GregorianCalendar();
        lastUpdate.setTime(latestProfileDate);
        
        Date today = new Date();
        Calendar toDate = new GregorianCalendar();
        toDate.setTime(today);
        toDate.add(Calendar.MONTH, -profileHorizon);
        
        Calendar fromDate = new GregorianCalendar();
        fromDate.setTime(lastUpdate.getTime());
        fromDate.add(Calendar.MONTH, -profileHorizon);
		
		updateProfiles(tenantID, toDate, lastUpdate, fromDate);
	}
	
	public void updateProfiles(String tenantID, List<Item> incrementalItems, List<Item> decrementalItems) throws Exception {
		List<Profile> incrementalProfiles = createProfile(incrementalItems);
		List<Profile> decrementalProfiles = createProfile(decrementalItems);
		
		updateProfilesSimpleDB(incrementalProfiles, decrementalProfiles, tenantID);
	}
	
	public void updateProfiles(String tenantID, Calendar currentDateFrom, Calendar lastUpdate, Calendar lastUpdateFrom) throws Exception {
		List<Future<List<Item>>>  newData = getDataSinceLastUpdate(tenantID, currentDateFrom, lastUpdate, lastUpdateFrom);
		List<Item> incrementalData = newData.get(0).get();
		List<Item> decrementalData = newData.get(1).get();
		
		List<Profile> incrementalProfiles = createProfile(incrementalData);
		/*
		HashMap<String, Profile> incrementalProfiles = new HashMap<String, Profile>();
		for (Profile profile : incrementalProfilesList) {
			String userID = profile.getUserID();
			incrementalProfiles.put(userID, profile);
		}
		incrementalProfilesList = null;
		*/
		
		List<Profile> decrementalProfiles = createProfile(decrementalData);
		/*
		HashMap<String, Profile> decrementalProfiles = new HashMap<String, Profile>();
		for (Profile profile : decrementalProfilesList) {
			String userID = profile.getUserID();
			decrementalProfiles.put(userID, profile);
		}
		decrementalProfilesList = null;
		 */
//		String profilesBucket = "profiles" + tenantID;
//		updateProfilesS3(incrementalProfiles, decrementalProfiles, profilesBucket, MAX_PROFILES_PER_FILE);
		
		updateProfilesSimpleDB(incrementalProfiles, decrementalProfiles, tenantID);
		
		// Profile is updated
		/*
		RecommendationsDAO recsDAO = new RecommendationsDAOImpl();
    	Tenant tenant = recsDAO.getTenantById(tenantID);
    	if (tenant == null) {
    		logger.error("Could not find tenant with ID " + tenantID);
    		throw new Exception();
    	}
		tenant.setLatestProfile(this.currentDate);
		*/
	}
	
	/**
	 * Adds all the data found in the ACTIVITY domain to create profiles in PROFILE domain
	 * @param tenantID
	 * @throws Exception
	 */
	public void createProfiles(String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		String profileDomainName = getProfileDomainName(tenantID);
		
		DeleteDomainRequest deleteDomainRequest = new DeleteDomainRequest();
		deleteDomainRequest.setDomainName(profileDomainName);
		sdb.deleteDomain(deleteDomainRequest);
		CreateDomainRequest createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(profileDomainName);
		sdb.createDomain(createDomainRequest);
		String userActivityDomain = "ACTIVITY_" + tenantID;
		String selectExpression = "select * from `" + userActivityDomain + "`";
		List<Profile> incrementalProfiles = createProfile(querySimpleDB(selectExpression));
		List<Profile> decrementalProfiles = new ArrayList<Profile>();
		
		updateProfilesSimpleDB(incrementalProfiles, decrementalProfiles, tenantID);
	}
	
	
	private void batchDelete(AmazonSimpleDB sdb, String domain, DeletableItem profile, boolean forceDelete) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
		
		
		if (profile != null) {
			deletedItems.add(profile);
		}
		
		if (!forceDelete) {
			if (deletedItems.size() < 25) {
				return;
			}
		}
		
		if (deletedItems.size() == 0) {
			return;
		}
		
		BatchDeleteAttributesRequest batchDeleteArgumentsRequest = new BatchDeleteAttributesRequest();
		batchDeleteArgumentsRequest.setDomainName(domain);
		batchDeleteArgumentsRequest.setItems(deletedItems);
		sdb.batchDeleteAttributes(batchDeleteArgumentsRequest);
    	
		deletedItems = new ArrayList<DeletableItem>();
    }
	
	private void batchInsert(String domain) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
		
		if (items == null || items.size() == 0) {
			items = new ArrayList<ReplaceableItem>();
			return;
		}
		
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		List<ReplaceableItem> itemsToProcess = new ArrayList<ReplaceableItem>();

		if (items.size() > MAX_RECORDS_TO_PROCESS) {
			int itemsPerThread = items.size() / NO_OF_THREADS_TO_RUN;
			for (ReplaceableItem profile : items) {
				itemsToProcess.add(profile);
				if (itemsToProcess.size() == itemsPerThread) {
					Thread batchDateProc = new BatchDataProcessorThread(sdb, itemsToProcess, domain);
					itemsToProcess = new ArrayList<ReplaceableItem>();
					batchDateProc.start();
				}
				
			}
			//write any remaining
			if (itemsToProcess.size() > 0) {
				Thread batchDateProc = new BatchDataProcessorThread(sdb, itemsToProcess, domain);
				batchDateProc.start();
			}
		} else {
			sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, items));
		}
    	
    	items = new ArrayList<ReplaceableItem>();
    }
}
