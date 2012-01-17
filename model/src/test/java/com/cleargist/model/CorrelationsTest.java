package com.cleargist.model;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;



public class CorrelationsTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	private String PROFILE_DOMAIN = "PROFILE_test";
	private String MODEL_DOMAIN = "MODEL_CORRELATIONS_test";
	private String OTHER_MODEL_DOMAIN = "MODEL_OTHER_CORRELATIONS_test";
	private String STATS_BUCKET = "tmpstatstest";
	public static String newline = System.getProperty("line.separator");
		
	private void loadProfiles(String profilesFilename) throws FileNotFoundException, IOException {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		try {
			sdb.deleteDomain(new DeleteDomainRequest(PROFILE_DOMAIN));
			sdb.createDomain(new CreateDomainRequest(PROFILE_DOMAIN));
		}
		catch (AmazonServiceException ase) {
			if (ase.getStatusCode() != 200) {
				logger.error(ase.getErrorCode() + " while deleting/creating profile domain");
				throw new IOException();
			}
		}
		catch (AmazonClientException ace) {
			logger.error(ace.getMessage());
			throw new IOException();
		}
		
		
		logger.info("Reading profiles " + profilesFilename);
		File file = new File(profilesFilename);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		int profilesLoaded = 0;
		List<ReplaceableItem> items = new LinkedList<ReplaceableItem>();
		while ((line = reader.readLine()) != null) {
			if (profilesLoaded % 1000 == 0) {
				logger.info("Read " + profilesLoaded + " profiles");
			}
			String[] fields = line.split(";");
			String userID = fields[0];
			int len = fields.length > 256 ? 256 : fields.length;
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			for (int i = 1; i < len; i ++) {
				StringBuffer sb = new StringBuffer();
				sb.append("Attribute_"); sb.append(fields[i]);
				
				StringBuffer sb2 = new StringBuffer();
				sb2.append(fields[i]); sb2.append(";1.0");
				attributes.add(new ReplaceableAttribute(sb.toString(), sb2.toString(), true));
			}
			
			items.add(new ReplaceableItem(userID).withAttributes(attributes));
			profilesLoaded ++;
			
			if (items.size() == 25) {
				try {
		    		sdb.batchPutAttributes(new BatchPutAttributesRequest(PROFILE_DOMAIN, items));
		    	}
				catch (AmazonServiceException ase) {
					if (ase.getStatusCode() != 200) {
						logger.error(ase.getErrorCode() + " while inserting profiles");
						throw new IOException();
					}
				}
				catch (AmazonClientException ace) {
					logger.error(ace.getMessage());
					throw new IOException();
				}
				catch (Exception ex) {
					throw new IOException();
				}
				
				items = new LinkedList<ReplaceableItem>();
			}
		}
		reader.close();
		
		if (items.size() > 0) {
			try {
	    		sdb.batchPutAttributes(new BatchPutAttributesRequest(PROFILE_DOMAIN, items));
	    	}
			catch (AmazonServiceException ase) {
				if (ase.getStatusCode() != 200) {
					logger.error(ase.getErrorCode());
					System.exit(-1);
				}
			}
			catch (AmazonClientException ace) {
				logger.error(ace.getMessage());
				System.exit(-1);
			}
			catch (Exception ex) {
				throw new IOException();
			}
		}
		logger.info("Finished reading profiles");
	}
	
	@After
	public void cleanUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.deleteDomain(new DeleteDomainRequest(PROFILE_DOMAIN));
		sdb.deleteDomain(new DeleteDomainRequest(MODEL_DOMAIN));
		sdb.deleteDomain(new DeleteDomainRequest(OTHER_MODEL_DOMAIN));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		List<S3ObjectSummary> objSummaries = s3.listObjects(STATS_BUCKET).getObjectSummaries();
		int i = 0;
    	while (i < objSummaries.size()) {
    		s3.deleteObject(STATS_BUCKET, objSummaries.get(i).getKey());
    		i ++;
    	}
    	s3.deleteBucket(STATS_BUCKET);
	}
	
	private void showModel() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String selectExpression = "select * from `" + OTHER_MODEL_DOMAIN + "` limit 2500";
		String resultNextToken = null;
		SelectRequest selectRequest = new SelectRequest(selectExpression);
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
		    
		    
		    List<Item> items = selectResult.getItems();
			for (Item item : items) {
				StringBuffer sb = new StringBuffer();
				sb.append(item.getName());
				for (Attribute attribute : item.getAttributes()) {
					String value = attribute.getValue();
					sb.append(";"); sb.append(value);
				}
//				sb.append(newline);
				logger.warn(sb.toString());
			}
		    
		} while (resultNextToken != null);
		
	}
	
	private boolean areCorrelationsEqual(String bucketName, String filenameA, String filenameB) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Correlations.class.getResourceAsStream(AWS_CREDENTIALS)));
		
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
	public void testMerging() {
		String filename = "smallSintagesPareasProfiles.csv";
//		String filename = "fewProfiles.txt";
		String profiles = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator 
		+ "resources" + File.separator + filename;
		try {
			loadProfiles(profiles);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		
		// Use a single chunk
		Correlations model = new Correlations();
		model.setProfilesPerChunk(25000);
		try {
			model.updateModel("test");
			model.writeModelToFile("test", "sintagespareas", "singleChunk.txt", OTHER_MODEL_DOMAIN);
			cleanUp();
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		
		
		// Use multiple chunks
		try {
			loadProfiles(profiles);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		model = new Correlations();
		model.setProfilesPerChunk(3000);
		try {
			model.updateModel("test");
			model.writeModelToFile("test", "sintagespareas", "multipleChunks.txt", OTHER_MODEL_DOMAIN);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		// Compare correlations
		try {
			areCorrelationsEqual("sintagespareas", "singleChunk.txt", "multipleChunks.txt");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		
		assertTrue(true);
	}
}