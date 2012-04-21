package com.cleargist.model;

import static org.junit.Assert.assertTrue;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.cleargist.data.DataHandler;
import com.cleargist.data.jaxb.Collection;
import com.cleargist.profile.Profile;
import com.cleargist.profile.SessionDetailViewProfileProcessor;

public class CorrelationsModelUpdateTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private Logger logger = Logger.getLogger(getClass());
	private String ACTIVITY_DOMAIN = "ACTIVITY_test";
	private String PROFILE_DOMAIN = "PROFILE_test";
	private String MODEL_DOMAIN = "MODEL_CORRELATIONS_test_A";
	private String OTHER_MODEL_DOMAIN = "MODEL_CORRELATIONS_test_B";
	private String STATS_BUCKET = "tmpstatscorrelationstest";
	public static String newline = System.getProperty("line.separator");
	
	
	public void cleanUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		sdb.deleteDomain(new DeleteDomainRequest(ACTIVITY_DOMAIN));
		sdb.deleteDomain(new DeleteDomainRequest(PROFILE_DOMAIN));
		sdb.deleteDomain(new DeleteDomainRequest(MODEL_DOMAIN));
		sdb.deleteDomain(new DeleteDomainRequest(OTHER_MODEL_DOMAIN));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		boolean found = false;
		for (Bucket bucket : s3.listBuckets()) {
			if (bucket.getName().equals(STATS_BUCKET)) {
				found = true;
				break;
			}
		}
		if (found) {
			List<S3ObjectSummary> objSummaries = s3.listObjects(STATS_BUCKET).getObjectSummaries();
			int i = 0;
	    	while (i < objSummaries.size()) {
	    		s3.deleteObject(STATS_BUCKET, objSummaries.get(i).getKey());
	    		i ++;
	    	}
	    	s3.deleteBucket(STATS_BUCKET);
		}
    	
        DeleteAttributesRequest deleteAttributesRequest = new DeleteAttributesRequest();
        deleteAttributesRequest.setItemName("test");
        deleteAttributesRequest.setDomainName("MODEL_STATES");
        Set<Attribute> attributes = new HashSet<Attribute>();
        Attribute att = new Attribute();
        att.setName("MODEL_CORRELATIONS_");
        deleteAttributesRequest.setAttributes(attributes);
        sdb.deleteAttributes(deleteAttributesRequest);
	}
	
	
	@Test
	public void updateModelA() throws Exception {
		
		createFullModel("activity104new.xml.gz");
		
		createIncrementalModel("activity104incremental.xml.gz", "activity104decremental.xml.gz", "activity104existing.xml.gz");
		
		String incrementalCorrelationsKey = "statsIncremental.txt";
		String batchCorrelationsKey = "statsBatch.txt";
		boolean b = areStatsEqual("cleargist", incrementalCorrelationsKey, batchCorrelationsKey);;
		
		assertTrue(b);
	}
	
	@Ignore
	@Test
	public void updateModelDummyData() throws Exception {
		// Create model from scratch
		cleanUp();
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		createFullModel("new.xml.gz");
		
		
		// Create model from incremental profiles
		createIncrementalModel("incremental.xml.gz", "decremental.xml.gz", "existing.xml.gz");
		
		String incrementalCorrelationsKey = "correlations104incremental.txt";
		String batchCorrelationsKey = "correlations104batch.txt";
		boolean b = areStatsEqual("cleargist", incrementalCorrelationsKey, batchCorrelationsKey);
		
		assertTrue(b);
		
		try {
			AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
					CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
			s3.deleteObject("cleargist", incrementalCorrelationsKey);
			s3.deleteObject("cleargist", batchCorrelationsKey);
		}
		catch (Exception ex) {
			logger.error("Could not delete S3 files " + incrementalCorrelationsKey + " and " + batchCorrelationsKey);
		}
		
	}
	
	
	private void createFullModel(String activityKey) throws Exception {
		cleanUp();
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		CreateDomainRequest createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(ACTIVITY_DOMAIN);
		sdb.createDomain(createDomainRequest);
		createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(PROFILE_DOMAIN);
		sdb.createDomain(createDomainRequest);
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", activityKey);
		dh.insertInSimpleDB(collection, "test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		CorrelationsModel model = new CorrelationsModel();
		model.createModel("test", false);
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.copyObject(STATS_BUCKET, "partialStats1", "cleargist", "statsBatch.txt");
		s3.deleteObject(STATS_BUCKET, "partialStats1");
	}
	
	private void createIncrementalModel(String incrementalKey, String decrementalKey, String existingKey) throws Exception {
		
		// Start from a clean slate
		cleanUp();
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Get incremental / decremental data
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", incrementalKey);
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		List<Profile> incrementalProfiles = null;
		if (incrementalKey != null) {
			collection = dh.unmarshallData("cleargist", incrementalKey);
			incrementalProfiles = pr.createProfile(dh.toItems(collection));
		}
		else {
			incrementalProfiles = new ArrayList<Profile>();
		}
		List<Profile> decrementalProfiles = null;
		if (decrementalKey != null) {
			collection = dh.unmarshallData("cleargist", decrementalKey);
			decrementalProfiles = pr.createProfile(dh.toItems(collection));
		}
		else {
			decrementalProfiles = new ArrayList<Profile>();
		}
		
		
		
		// Get existing profiles
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		CreateDomainRequest createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(ACTIVITY_DOMAIN);
		sdb.createDomain(createDomainRequest);
		createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(PROFILE_DOMAIN);
		sdb.createDomain(createDomainRequest);
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		if (existingKey != null) {
			DataHandler dh2 = new DataHandler();
			Collection collection2 = dh.unmarshallData("cleargist", existingKey);
			dh2.insertInSimpleDB(collection2, "test");
			Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
			
			SessionDetailViewProfileProcessor pr2 = new SessionDetailViewProfileProcessor();
			pr2.createProfiles("test");
			Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
			
			CorrelationsModel model = new CorrelationsModel();
			model.createModel("test", false);
			Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
			
			AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
					CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
			if (!s3.doesBucketExist(STATS_BUCKET)) {
				s3.createBucket(STATS_BUCKET, Region.EU_Ireland);
			}
			s3.copyObject(STATS_BUCKET, "partialStats1", STATS_BUCKET, "merged.txt");
			s3.deleteObject(STATS_BUCKET, "partialStats1");
		}
		
		
		
		
		// Now train the model
		CorrelationsModel model = new CorrelationsModel();
		model.setModelDomainName("MODEL_CORRELATIONS_", MODEL_DOMAIN, "test");
		model.updateModel("test", incrementalProfiles, decrementalProfiles);
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.copyObject(STATS_BUCKET, "merged.txt", "cleargist", "statsIncremental.txt");
	}
	
	private boolean areStatsEqual(String bucketName, String filenameA, String filenameB) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		S3Object statsObject = s3.getObject(bucketName, filenameA);
		BufferedReader reader = new BufferedReader(new InputStreamReader(statsObject.getObjectContent()));
		String line = reader.readLine();
		float numberOfProfilesA = Float.parseFloat(line);
		HashMap<String, Float> SS0A = new HashMap<String, Float>();
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			if (fields.length != 2) {
				break;
			}
			String sourceID = fields[0];
			float count = Float.parseFloat(fields[1]);
			
			SS0A.put(sourceID, count);
		}
		HashMap<String, HashMap<String, Float>> SS1A = new HashMap<String, HashMap<String, Float>>();
		String[] fields = line.split(";");
		String sourceID = fields[0];
		
		HashMap<String, Float> hm = new HashMap<String, Float>();
		SS1A.put(sourceID, hm);
		for (int i = 1; i < fields.length; i = i + 2) {
			hm.put(fields[i], Float.parseFloat(fields[i+1]));
		}
		
		while ((line = reader.readLine()) != null) {
			fields = line.split(";");
			sourceID = fields[0];
			
			hm = new HashMap<String, Float>();
			SS1A.put(sourceID, hm);
			for (int i = 1; i < fields.length; i = i + 2) {
				hm.put(fields[i], Float.parseFloat(fields[i+1]));
			}
		}
		reader.close();
		
		statsObject = s3.getObject(bucketName, filenameB);
		reader = new BufferedReader(new InputStreamReader(statsObject.getObjectContent()));
		line = reader.readLine();
		float numberOfProfilesB = Float.parseFloat(line);
		if (numberOfProfilesA != numberOfProfilesB) {
			assertTrue(numberOfProfilesA == numberOfProfilesB);
		}
		int sizeSS0B = 0;
		while ((line = reader.readLine()) != null) {
			fields = line.split(";");
			if (fields.length != 2) {
				break;
			}
			sourceID = fields[0];
			float countB = Float.parseFloat(fields[1]);
			
			Float countA = SS0A.get(sourceID);
			assertTrue(countA != null);
			assertTrue(countA.floatValue() == countB);
			
			sizeSS0B ++;
		}
		assertTrue(sizeSS0B == SS0A.size());
		
		int sizeSS1B = 0;
		do {
			fields = line.split(";");
			sourceID = fields[0];
			
			HashMap<String, Float> hmA = SS1A.get(sourceID);
			if (hmA == null) {
				return false;
			}
			HashMap<String, Float> hmB = new HashMap<String, Float>();
			for (int i = 1; i < fields.length; i = i + 2) {
				Float fA = hmA.get(fields[i]);
				if (fA == null) {
					return false;
				}
				
				Float fB = Float.parseFloat(fields[i+1]);
				if (fA.floatValue() != fB.floatValue()) {
					return false;
				}
				hmB.put(fields[i], fB);
			}
			
			if (hmA.size() != hmB.size()) {
				return false;
			}
			
			sizeSS1B ++;
		} while ((line = reader.readLine()) != null);
		
		reader.close();
		
		if (sizeSS1B != SS1A.size()) {
			return false;
		}
		
		return true;
	}
}
