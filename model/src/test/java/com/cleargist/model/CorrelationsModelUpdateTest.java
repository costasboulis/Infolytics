package com.cleargist.model;

import static org.junit.Assert.assertTrue;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Region;
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
	private static TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");	
	
	
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
	public void updateModel() {
		
		// Create model from scratch
		/*
		try {
			createFullModel();
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		*/
		// Create model from incremental profiles
		try {
			createIncrementalModel("incremental.xml.gz", "decremental.xml.gz", "existing.xml.gz", "existingStats.txt");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		assertTrue(true);
	}
	
	private void createFullModel() throws Exception {
		cleanUp();
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", "activity104new.xml.gz");
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		CreateDomainRequest createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(ACTIVITY_DOMAIN);
		sdb.createDomain(createDomainRequest);
		createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(PROFILE_DOMAIN);
		sdb.createDomain(createDomainRequest);
		
		dh.insertInSimpleDB(collection, "test");
		
		
		
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		pr.createProfiles("test");
		
		CorrelationsModel model = new CorrelationsModel();
		model.createModel("test");
		
		String modelDomainName = model.getPrimaryModelDomainName("MODEL_CORRELATIONS_", "test");
		model.writeModelToFile("test", "cleargist", "correlations104new.txt", modelDomainName);
	}
	
	private void createIncrementalModel(String incrementalKey, String decrementalKey, String existingKey, String existingStatsKey) throws Exception {
		
		// Start from a clean slate
		cleanUp();
		
		// Get incremental / decremental data
		DataHandler dh = new DataHandler();
		Collection collection = dh.unmarshallData("cleargist", incrementalKey);
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
		List<Profile> incrementalProfiles = pr.createProfile(dh.toItems(collection));
		collection = dh.unmarshallData("cleargist", decrementalKey);
		List<Profile> decrementalProfiles = pr.createProfile(dh.toItems(collection));
		
		// Get existing sufficient statistics
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CorrelationsModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.createBucket(STATS_BUCKET, Region.EU_Ireland);
		s3.copyObject("cleargist", existingStatsKey, STATS_BUCKET, "merged.txt");
		
		// Get existing profiles
		DataHandler dh2 = new DataHandler();
		Collection collection2 = dh.unmarshallData("cleargist", existingKey);
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsModelUpdateTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		CreateDomainRequest createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(ACTIVITY_DOMAIN);
		sdb.createDomain(createDomainRequest);
		createDomainRequest = new CreateDomainRequest();
		createDomainRequest.setDomainName(PROFILE_DOMAIN);
		sdb.createDomain(createDomainRequest);
		dh2.insertInSimpleDB(collection2, "test");
		SessionDetailViewProfileProcessor pr2 = new SessionDetailViewProfileProcessor();
		pr2.createProfiles("test");
		
		
		
		// Now train the model
		CorrelationsModel model = new CorrelationsModel();
		model.setModelDomainName("MODEL_CORRELATIONS_", MODEL_DOMAIN, "test");
		model.updateModel("test", incrementalProfiles, decrementalProfiles);
		
//		String modelDomainName = model.getPrimaryModelDomainName("MODEL_CORRELATIONS_", "test");
//		model.writeModelToFile("test", "cleargist", "correlations104update.txt", modelDomainName);
		
	}
}
