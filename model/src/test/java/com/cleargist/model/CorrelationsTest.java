package com.cleargist.model;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;



public class CorrelationsTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	private String PROFILE_DOMAIN = "PROFILE_test";
	
	
	private void loadProfiles(String profilesFilename) throws FileNotFoundException, IOException {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CorrelationsTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		sdb.deleteDomain(new DeleteDomainRequest(PROFILE_DOMAIN));
		sdb.createDomain(new CreateDomainRequest(PROFILE_DOMAIN));
		
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
			catch (Exception ex) {
				throw new IOException();
			}
		}
		
	}
	
	@Test
	public void testA() {
		try {
			String profiles = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator 
			+ "resources" + File.separator + "smallSintagesPareasProfiles.csv";
			loadProfiles(profiles);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		logger.info("Finished reading profiles");
		
		Correlations model = new Correlations();
		try {
			model.updateModel("test");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		assertTrue(true);
	}
}
