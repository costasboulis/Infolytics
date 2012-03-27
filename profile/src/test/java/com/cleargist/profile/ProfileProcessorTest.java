package com.cleargist.profile;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.cleargist.recommendations.dao.RecommendationsDAO;
import com.cleargist.recommendations.dao.RecommendationsDAOImpl;
import com.cleargist.recommendations.entity.CatalogStatus;
import com.cleargist.recommendations.entity.Tenant;


public class ProfileProcessorTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	private String rawDataDomain = "ACTIVITY_TEST";
	private String profileDomain = "PROFILE_TEST";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyMMddHHmmssSSSZ");
	private RecommendationsDAO dao;
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	
	@Before
	public void createSimpleDBDomain() {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
    		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		return;
    	}
    	   	
    	
    	try {
    		sdb.createDomain(new CreateDomainRequest(rawDataDomain));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot create domain " + rawDataDomain + " in SimpleDB");
    		return;
    	}
    	
    	
    	Tenant t = new Tenant();
    	t.setFirstName("Γιώργος");
    	t.setLastName("Παπαδογιάννης");
    	t.setActive(0);
    	t.setCatalogStatus(CatalogStatus.WAITING);
    	t.setCompany("Τέστ");
    	t.setEmail("papado_ge@hotmail.com");
    	t.setId("TEST");
    	t.setToken(1060101);
    	t.setUsername("testX");
    	t.setPassword("testX");
    	Calendar lastUpdate = Calendar.getInstance();
    	lastUpdate.set(2012, 0, 6, 16, 56, 20);   
		t.setLatestProfile(lastUpdate.getTime());
		t.setProfileHorizon(-6);
//   	dao.saveTenant(t);
    	
    	
		
    	// Add dummy data here
    	List<ReplaceableItem> recsPairs = new ArrayList<ReplaceableItem>();
    	Calendar date = Calendar.getInstance();
    	date.set(2012, 0, 6, 16, 56, 20);      
    	String productID = "1";
    	String event = "PURCHASE";
    	String userID = "John";
    	String dateString = dateFormatter.format(date.getTime());
    	String itemName = userID + "_" + productID + "_" + event + "_" + date;
    	ReplaceableItem item1 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	
    	recsPairs.add(item1);
    	
    	Calendar dateA = Calendar.getInstance();
    	dateA.setTime(date.getTime());
    	dateA.set(Calendar.SECOND, -1);
    	productID = "2";
    	event = "VIEW";
    	userID = "John";
    	dateString = dateFormatter.format(dateA.getTime());
    	itemName = userID + "_" + productID + "_" + event + "_" + date;
    	ReplaceableItem item2 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	recsPairs.add(item2);
    	
    	Calendar dateB = Calendar.getInstance();
    	dateB.setTime(date.getTime());
    	dateB.set(Calendar.SECOND, -1);
    	productID = "1";
    	event = "PURCHASE";
    	userID = "Jane";
    	dateString = dateFormatter.format(dateB.getTime());
    	itemName = userID + "_" + productID + "_" + event + "_" + date;
    	ReplaceableItem item3 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	recsPairs.add(item3);
    	
    	Calendar dateC = Calendar.getInstance();
    	dateC.setTime(date.getTime());
    	dateC.set(Calendar.SECOND, -5);
    	productID = "3";
    	event = "PURCHASE";
    	userID = "John";
    	dateString = dateFormatter.format(dateC.getTime());
    	itemName = userID + "_" + productID + "_" + event + "_" + date;
    	ReplaceableItem item4 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	recsPairs.add(item4);
    	
    	Calendar dateD = Calendar.getInstance();
    	dateD.setTime(date.getTime());
    	dateD.set(Calendar.MONTH, -7);
    	productID = "5";
    	event = "PURCHASE";
    	userID = "Jake";
    	dateString = dateFormatter.format(dateD.getTime());
    	itemName = userID + "_" + productID + "_" + event + "_" + date;
    	ReplaceableItem item5 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	recsPairs.add(item5);
    	
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(rawDataDomain, recsPairs));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot write to SimpleDB");
    		return;
    	}
    	
    	// Create dummy profile domain
    	try {
    		sdb.createDomain(new CreateDomainRequest(profileDomain));
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot create domain in SimpleDB, Amazon Service error";
    		logger.error(errorMessage);
    		return;
    	}
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network.";
            logger.error(errorMessage);
            return;
    	}
    	
    	// Add existing profiles
    	List<ReplaceableItem> existingProfiles = new ArrayList<ReplaceableItem>();
    	existingProfiles.add(new ReplaceableItem("John").withAttributes(
    						new ReplaceableAttribute("Attribute_1", "1;1.0", true),
    						new ReplaceableAttribute("Attribute_3", "3;1.0", true)));
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(profileDomain, existingProfiles));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot write to SimpleDB");
    		return;
    	}
	}
	
	@After
	public void cleanUp() {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
    		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		return;
    	}
    	
    	try {
    		sdb.deleteDomain(new DeleteDomainRequest(rawDataDomain));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot delete domain " + rawDataDomain + " in SimpleDB");
    		return;
    	}
    	try {
    		sdb.deleteDomain(new DeleteDomainRequest(profileDomain));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot delete domain " + profileDomain + " in SimpleDB");
    		return;
    	}
    	try {
    		Thread.sleep(5000);
    	}
    	catch (Exception ex) {
    		logger.error("Error while sleeping");
    		return;
    	}
    	
  //TODO : Add deleteTenant  	
 //   	RecommendationsDAO dao = new RecommendationsDAOImpl();
 //   	dao.deleteTenantByID("TEST");
    	
	}
	
	private Profile getProfile(String userID) {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
    		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		return null;
    	}
    	GetAttributesRequest request = new GetAttributesRequest();
		request.setDomainName(profileDomain);
		request.setItemName(userID);
		GetAttributesResult result = sdb.getAttributes(request);
		if (result.getAttributes().size() == 0) {
			return null;
		}
		Profile profile = new Profile();
		profile.setUserID(userID);
		for (Attribute attribute : result.getAttributes()) {
			if (attribute.getName().startsWith("Attribute")) {
				String value = attribute.getValue();
				String[] parsedValue = value.split(";");
				String productID = parsedValue[0];
				float score = Float.parseFloat(parsedValue[1]);
				
				profile.add(productID, score);
			}
		}
		
		return profile;
	}
	
	@Ignore("need proper tenant access")
	@Test
    public void addNewUser() {
		// Add new data
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
    		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		return;
    	}
    	   	
		List<ReplaceableItem> newData = new ArrayList<ReplaceableItem>();
		Calendar dateD = Calendar.getInstance();
		Date currentDate = new Date();
    	dateD.setTime(currentDate);
    	dateD.set(Calendar.SECOND, -10);
    	String productID = "5";
    	String event = "PURCHASE";
    	String userID = "NewUser";
    	String dateString = dateFormatter.format(dateD.getTime());
    	String itemName = userID + "_" + productID + "_" + event + "_" + dateString;
    	ReplaceableItem item5 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	newData.add(item5);
    	
    	Calendar dateE = Calendar.getInstance();
    	dateE.setTime(currentDate);
    	dateE.set(Calendar.SECOND, -15);
    	productID = "2";
    	event = "PURCHASE";
    	userID = "NewUser";
    	dateString = dateFormatter.format(dateE.getTime());
    	itemName = userID + "_" + productID + "_" + event + "_" + dateString;
    	ReplaceableItem item6 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	newData.add(item6);
    	
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(rawDataDomain, newData));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot write to SimpleDB");
    		return;
    	}
		// Build reference profile
		Profile referenceProfile = new Profile();
		referenceProfile.setUserID("NewUser");
		referenceProfile.add("5", 1.0f);
		referenceProfile.add("2", 1.0f);
		
		PurchaseProfileProcessor purchaseProfileProcessor = new PurchaseProfileProcessor();
		try {
			purchaseProfileProcessor.updateProfiles("TEST");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		
		// Check with the profile that is persisted
		Profile profile = getProfile("NewUser");
		assertTrue(referenceProfile.equals(profile));
	}
	
	@Ignore("need proper tenant access")
	@Test
	public void deleteUser() {
		PurchaseProfileProcessor purchaseProfileProcessor = new PurchaseProfileProcessor();
		try {
			purchaseProfileProcessor.updateProfiles("TEST");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		Profile profile = getProfile("Jake");
		assertTrue(profile == null);
	}
	
	@Ignore("need proper tenant access")
	@Test
	public void addAttributes() {
		PurchaseProfileProcessor purchaseProfileProcessor = new PurchaseProfileProcessor();
		try {
			purchaseProfileProcessor.updateProfiles("TEST");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		Profile profile = getProfile("John");
		assertTrue(profile.getAttributes().size() == 2);
		
		// Add new data
		List<ReplaceableItem> recsPairs = new ArrayList<ReplaceableItem>();
		Calendar date = Calendar.getInstance();
		Date currentDate = new Date();
		date.setTime(currentDate);
		date.set(Calendar.SECOND, -1);
    	String productID = "10";
    	String event = "PURCHASE";
    	String userID = "John";
    	String dateString = dateFormatter.format(date.getTime());
    	String itemName = userID + "_" + productID + "_" + event + "_" + date;
    	ReplaceableItem item1 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", dateString, true));
    	
    	recsPairs.add(item1);
    	
    	AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
    		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		return;
    	}
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(rawDataDomain, recsPairs));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot write to SimpleDB");
    		return;
    	}
    	
    	
    	// Now do a profile update
    	try {
			purchaseProfileProcessor.updateProfiles("TEST");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		Profile updatedProfile = getProfile("John");
		assertTrue(updatedProfile.getAttributes().size() == 3);
	}
	
	@Ignore("need proper tenant access")
	@Test
	public void deleteAttributes() {
		// Add past data
		List<ReplaceableItem> recsPairs = new ArrayList<ReplaceableItem>();
		Calendar calendar = Calendar.getInstance();
		calendar.set(2012, 0, 6, 16, 56, 22);
		calendar.add(Calendar.MONTH, -6);
    	String productID = "10";
    	String event = "PURCHASE";
    	String userID = "John";
    	String date = dateFormatter.format(calendar.getTime());
    	String itemName = userID + "_" + productID + "_" + event + "_" + date;
    	ReplaceableItem item1 = new ReplaceableItem(itemName).withAttributes(
                new ReplaceableAttribute("PRODUCTID", productID, true),
                new ReplaceableAttribute("EVENT", event, true), 
                new ReplaceableAttribute("USERID", userID, true),
                new ReplaceableAttribute("DATE", date, true));
    	
    	recsPairs.add(item1);
    	
    	AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				ProfileProcessorTest.class.getResourceAsStream(AWS_CREDENTIALS)));
    		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		logger.error(errorMessage);
    		return;
    	}
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(rawDataDomain, recsPairs));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot write to SimpleDB");
    		return;
    	}
    	
    	
    	// Create existing profiles
    	List<ReplaceableItem> existingProfiles = new ArrayList<ReplaceableItem>();
    	ReplaceableItem existingProfile = new ReplaceableItem("John").withAttributes(
                new ReplaceableAttribute("Attribute_1", "1;1.0", true),
                new ReplaceableAttribute("Attribute_10", "10;1.0", true), 
                new ReplaceableAttribute("Attribute_3", "3;1.0", true));
    	
    	existingProfiles.add(existingProfile);
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(profileDomain, existingProfiles));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot write to SimpleDB");
    		return;
    	}
    	
    	
    	// Do the update
    	PurchaseProfileProcessor purchaseProfileProcessor = new PurchaseProfileProcessor();
		try {
			purchaseProfileProcessor.updateProfiles("TEST");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
    	
		Profile profile = getProfile("John");
		assertTrue(profile.getAttributes().size() == 2);
	}

	@Test
	public void test104() {
		SessionDetailViewProfileProcessor pr = new SessionDetailViewProfileProcessor();
	
		try {
			pr.updateProfiles("104");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


