package com.cleargist.profile;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;


import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.cleargist.data.DataHandler;
import com.cleargist.data.jaxb.Collection;



public class ProfileProcessorTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private String rawDataDomain = "ACTIVITY_test";
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	
	
	
	
	
	public void cleanUp(String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	
		sdb.deleteDomain(new DeleteDomainRequest(rawDataDomain));
//		sdb.deleteDomain(new DeleteDomainRequest(profileDomain));
		Thread.sleep(5000);
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		String bucketName = "profiles" + tenantID;
		if (s3.doesBucketExist(bucketName)) {
			List<S3ObjectSummary> objSummaries = s3.listObjects(bucketName).getObjectSummaries();
			if (objSummaries != null && objSummaries.size() > 0) {
				String key = objSummaries.get(0).getKey();
				s3.deleteObject("profilestest", key);
			}
			s3.deleteBucket(bucketName);
			
		}
		
	}
	
	public void setUp(String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		sdb.createDomain(new CreateDomainRequest(rawDataDomain));
		Thread.sleep(5000);
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String bucketName = "profiles" + tenantID;
		if (!s3.doesBucketExist(bucketName)) {
			s3.createBucket(bucketName, Region.EU_Ireland);
		}
	}
	
	
	private void writeProfilesInFile(String key) throws Exception {
		File localFile = new File(key);
		BufferedWriter writer = new BufferedWriter(new FileWriter(localFile));
		
		String selectExpression = "select * from `PROFILE_test`";
		List<Item> items = ProfileProcessor.querySimpleDB(selectExpression);
		for (Item item : items) {
			StringBuffer sb = new StringBuffer();
			sb.append(item.getName());
			for (Attribute attribute : item.getAttributes()) {
				String attributeName = attribute.getName();
				String attributeValue = attribute.getValue();
				sb.append(";"); sb.append(attributeName); sb.append(";"); sb.append(attributeValue);
			}
			sb.append(ProfileProcessor.newline);
			
			writer.write(sb.toString());
			writer.flush();
		}
		writer.close();
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		PutObjectRequest r = new PutObjectRequest("cleargist", key, localFile);  
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		localFile.delete();
		
	}
	
	private void copyProfile(String tenantID, String key) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		String sourceBucketName = "profiles" + tenantID;
		String sourceKey = s3.listObjects(sourceBucketName).getObjectSummaries().get(0).getKey();
		s3.copyObject(sourceBucketName, sourceKey, "cleargist", key);
	}
	
	private boolean areCorrelationsEqual(String bucketName, String filenameA, String filenameB) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		S3Object statsObject = s3.getObject(bucketName, filenameA);
		BufferedReader reader = new BufferedReader(new InputStreamReader(statsObject.getObjectContent()));
		String line = null;
		HashMap<String, HashMap<String, Float>> correlationsA = new HashMap<String, HashMap<String, Float>>();
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			String sourceID = fields[0];
			
			HashMap<String, Float> hm = new HashMap<String, Float>();
			correlationsA.put(sourceID, hm);
			for (int i = 1; i < fields.length; i = i + 2) {
				hm.put(fields[i], Float.parseFloat(fields[i+1]));
			}
		}
		reader.close();
		
		statsObject = s3.getObject(bucketName, filenameB);
		reader = new BufferedReader(new InputStreamReader(statsObject.getObjectContent()));
		int cnt = 0;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(";");
			String sourceID = fields[0];
			
			HashMap<String, Float> hmA = correlationsA.get(sourceID);
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
			
			cnt ++;
		}
		reader.close();
		
		if (cnt != correlationsA.size()) {
			return false;
		}
		
		return true;
	}
	
	
	@Test
	public void testWithRealDataA() throws Exception {
		
		cleanUp("test");
		setUp("test");
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		
		// Create profiles
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", "activity104incremental.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Add incremental & decremental data
		pr.updateProfiles("test", new ArrayList<Item>(), new ArrayList<Item>());
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Write profiles in file
		writeProfilesInFile("profilesIncremental.txt");
		
		// Create batch profiles
		cleanUp("test");
		setUp("test");
		collection = dh.unmarshallData("cleargist", "activity104incremental.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		writeProfilesInFile("profilesBatch.txt");
		
		assertTrue(areCorrelationsEqual("cleargist", "profilesIncremental.txt", "profilesBatch.txt"));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.deleteObject("cleargist", "profilesIncremental.txt");
		s3.deleteObject("cleargist", "profilesBatch.txt");
		
		cleanUp("test");
	}

	
	@Test
	public void testWithRealDataB() throws Exception {
		
		cleanUp("test");
		setUp("test");
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		
		// Create profiles
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", "activity104existing.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Add incremental & decremental data
		Collection incrementalCollection = dh.unmarshallData("cleargist", "activity104incremental.xml.gz");
		List<Item> incItems = dh.toItems(incrementalCollection);
		pr.updateProfiles("test", incItems, new ArrayList<Item>());
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Write profiles in file
		writeProfilesInFile("profilesIncremental.txt");
		
		// Create batch profiles
		cleanUp("test");
		setUp("test");
		collection = dh.unmarshallData("cleargist", "activity104existingPlusIncremental.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		writeProfilesInFile("profilesBatch.txt");
		
		assertTrue(areCorrelationsEqual("cleargist", "profilesIncremental.txt", "profilesBatch.txt"));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.deleteObject("cleargist", "profilesIncremental.txt");
		s3.deleteObject("cleargist", "profilesBatch.txt");
		
		cleanUp("test");
	}
	
	
	@Test
	public void testWithRealDataC() throws Exception {
		
		cleanUp("test");
		setUp("test");
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		
		// Create profiles
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", "activity104existing.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Add incremental & decremental data
		Collection decrementalCollection = dh.unmarshallData("cleargist", "activity104decremental.xml.gz");
		pr.updateProfiles("test", new ArrayList<Item>(), dh.toItems(decrementalCollection));
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Write profiles in file
		writeProfilesInFile("profilesIncremental.txt");
		
		// Create batch profiles
		cleanUp("test");
		setUp("test");
		collection = dh.unmarshallData("cleargist", "activity104existingMinusDecremental.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		writeProfilesInFile("profilesBatch.txt");
		
		assertTrue(areCorrelationsEqual("cleargist", "profilesIncremental.txt", "profilesBatch.txt"));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.deleteObject("cleargist", "profilesIncremental.txt");
		s3.deleteObject("cleargist", "profilesBatch.txt");
		
		cleanUp("test");
	}
	
	
	@Test
	public void testWithRealDataD() throws Exception {
		
		cleanUp("test");
		setUp("test");
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		
		// Create profiles
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", "activity104existing.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Add incremental & decremental data
		Collection incrementalCollection = dh.unmarshallData("cleargist", "activity104incremental.xml.gz");
		Collection decrementalCollection = dh.unmarshallData("cleargist", "activity104decremental.xml.gz");
		pr.updateProfiles("test", dh.toItems(incrementalCollection), dh.toItems(decrementalCollection));
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		// Write profiles in file
		writeProfilesInFile("profilesIncremental.txt");
		
		// Create batch profiles
		cleanUp("test");
		setUp("test");
		collection = dh.unmarshallData("cleargist", "activity104new.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		pr.createProfiles("test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		writeProfilesInFile("profilesBatch.txt");
		
		assertTrue(areCorrelationsEqual("cleargist", "profilesIncremental.txt", "profilesBatch.txt"));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.deleteObject("cleargist", "profilesIncremental.txt");
		s3.deleteObject("cleargist", "profilesBatch.txt");
		
		cleanUp("test");
	}

	@Ignore
	@Test
	public void testWithRealDataIncremental() throws Exception {
		
		cleanUp("test");
		setUp("test");
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		
		// Create profiles
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", "activity104existing.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		pr.createProfiles("test");
		
		
		// Add incremental & decremental data
		Collection incrementalCollection = dh.unmarshallData("cleargist", "activity104incremental.xml.gz");
		List<Item> incItems = dh.toItems(incrementalCollection);
		pr.updateProfiles("test", incItems, new ArrayList<Item>());
		
		copyProfile("test", "profilesIncremental.txt");
		
		// Create batch profiles
		cleanUp("test");
		setUp("test");
		collection = dh.unmarshallData("cleargist", "activity104existingPlusIncremental.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		pr.createProfiles("test");
		
		copyProfile("test", "profilesBatch.txt");
		
		assertTrue(areCorrelationsEqual("cleargist", "profilesIncremental.txt", "profilesBatch.txt"));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.deleteObject("cleargist", "profilesIncremental.txt");
		s3.deleteObject("cleargist", "profilesBatch.txt");
		
		cleanUp("test");
	}
	
	@Ignore
	@Test
	public void testWithRealData() throws Exception {
		
		cleanUp("test");
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		
		// Create profiles
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", "activity104existing.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		pr.createProfiles("test");
		
		// Add incremental & decremental data
		Collection incrementalCollection = dh.unmarshallData("cleargist", "activity104incremental.xml.gz");
		Collection decrementalCollection = dh.unmarshallData("cleargist", "activity104decremental.xml.gz");
		pr.updateProfiles("test", dh.toItems(incrementalCollection), dh.toItems(decrementalCollection));
		
		copyProfile("test", "profilesIncremental.txt");
		
		
		
		// Create batch profiles
		cleanUp("test");
		collection = dh.unmarshallData("cleargist", "activity104new.xml.gz");
		dh.insertInSimpleDB(collection, "test");
		Thread.sleep(5000); // SimpleDB is eventually consistent, wait till we are sure that insertions are in
		
		pr.createProfiles("test");
		
		copyProfile("test", "profilesBatch.txt");
		
		assertTrue(areCorrelationsEqual("cleargist", "profilesIncremental.txt", "profilesBatch.txt"));
		
		/*
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.deleteObject("cleargist", "profilesIncremental.txt");
		s3.deleteObject("cleargist", "profilesBatch.txt");
		*/
	}
}


