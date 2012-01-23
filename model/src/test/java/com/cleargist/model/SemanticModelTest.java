package com.cleargist.model;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;

public class SemanticModelTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	
	@After
	public void cleanUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		sdb.deleteDomain(new DeleteDomainRequest("SEMANTIC_ASSOCIATIONS_test"));
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModelTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String bucketName = "profilessemanticmodeltest";
		List<S3ObjectSummary> objSummaries = s3.listObjects(bucketName).getObjectSummaries();
		int i = 0;
    	while (i < objSummaries.size()) {
    		s3.deleteObject(bucketName, objSummaries.get(i).getKey());
    		i ++;
    	}
    	s3.deleteBucket(bucketName);
	}
	
	@Test
	public void testCorrelations() {
		SemanticModel model = new SemanticModel();
		
		try {
			model.createModel("test");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		assertTrue(true);
	}
}
