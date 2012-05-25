package com.cleargist.profile;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;


public abstract class ProfileProcessor {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	public static String newline = System.getProperty("line.separator");
	private static final int FIXED_NO_OF_THREADS_OPER = 50;
	private ProfileDAO profileDAO;
	
	
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
	
	public void setProfileDAO(String profileDAOString) {
		if (profileDAOString.equals("SIMPLEDB")) {
			this.profileDAO = new ProfileDAOSimpleDB();
		}
		else if (profileDAOString.equals("S3")) {
			this.profileDAO = new ProfileDAOImplS3();
		}
		
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
		List<Profile> incrementalProfilesList = createProfile(incrementalItems);
		List<Profile> decrementalProfilesList = createProfile(decrementalItems);
		
		profileDAO.updateProfiles(tenantID, incrementalProfilesList, decrementalProfilesList);
	}
	
	public void updateProfiles(String tenantID, Calendar currentDateFrom, Calendar lastUpdate, Calendar lastUpdateFrom) throws Exception {
		List<Future<List<Item>>>  newData = getDataSinceLastUpdate(tenantID, currentDateFrom, lastUpdate, lastUpdateFrom);
		List<Item> incrementalData = newData.get(0).get();
		List<Item> decrementalData = newData.get(1).get();
		
		List<Profile> incrementalProfilesList = createProfile(incrementalData);
		List<Profile> decrementalProfilesList = createProfile(decrementalData);
		 
		profileDAO.updateProfiles(tenantID, incrementalProfilesList, decrementalProfilesList);
	}
	
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
	
	/**
	 * Adds all the data found in the ACTIVITY domain to create profiles 
	 * @param tenantID
	 * @throws Exception
	 */
	public void createProfiles(String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		String userActivityDomain = "ACTIVITY_" + tenantID;
		String selectExpression = "select * from `" + userActivityDomain + "` ";
		List<Profile> incrementalProfilesList = createProfile(ProfileProcessor.querySimpleDB(selectExpression));
		List<Profile> decrementalProfilesList = new ArrayList<Profile>();
		
		profileDAO.initProfiles(tenantID);
		profileDAO.updateProfiles(tenantID, incrementalProfilesList, decrementalProfilesList);
	}
	
}
