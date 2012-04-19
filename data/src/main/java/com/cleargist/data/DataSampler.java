package com.cleargist.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.cleargist.data.jaxb.Collection;

public class DataSampler {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private static final String DATE_PATTERN = "yyMMddHHmmssSSSZ";
	private static TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
	public static String newline = System.getProperty("line.separator");
	private Logger logger = Logger.getLogger(getClass());
	
	
	private List<Item> querySimpleDB(String selectExpression) throws AmazonServiceException, AmazonClientException, Exception{
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				DataSampler.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		String resultNextToken = null;
		String selectExpressionWithLimit = selectExpression + " limit 2500";
		SelectRequest selectRequest = new SelectRequest(selectExpressionWithLimit);
		List<Item> allItems = new LinkedList<Item>();
		int count  =0;
		do {
			count ++;
			logger.debug("count ::: " + count);
			
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
	
	public void createSampleDataFromRealActivity() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
    	formatter.setTimeZone(TIME_ZONE);
    	
    	Calendar now = Calendar.getInstance();
    	Date currentDate = new Date();
		now.setTimeZone(TIME_ZONE);
		now.setTimeInMillis(currentDate.getTime()); 
		
		Calendar oldest = Calendar.getInstance();
		oldest.setTimeZone(TIME_ZONE);
		oldest.setTimeInMillis(currentDate.getTime());     
		oldest.add(Calendar.MINUTE, -3);
		
		Calendar oldestPlusOne = Calendar.getInstance();
		oldestPlusOne.setTimeZone(TIME_ZONE);
		oldestPlusOne.setTime(oldest.getTime());
		oldestPlusOne.add(Calendar.SECOND, 30);
		
		Calendar nowMinusOne = Calendar.getInstance();
		nowMinusOne.setTimeZone(TIME_ZONE);
		nowMinusOne.setTimeInMillis(currentDate.getTime());     
		nowMinusOne.add(Calendar.SECOND, -30);   
		
		
		
		String tenantID = "104";
		String userActivityDomain = "ACTIVITY_" + tenantID;
		DataHandler dh = new DataHandler();
		
		// Get existing activity
		String selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE < '" + formatter.format(nowMinusOne.getTime()) + 
																		   "' and ACTDATE > '" + formatter.format(oldest.getTime()) + "'" ;
		List<Item> oldData = querySimpleDB(selectExpression);
		Collection collection = dh.readFromSimpleDB(oldData);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104existing.xml.gz");
		oldData = null;
		
		// Get incremental data
		selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + formatter.format(nowMinusOne.getTime()) + "'" +
																	" and ACTDATE < '" + formatter.format(now.getTime()) + "'";
		List<Item> incrementalData = querySimpleDB(selectExpression);
		collection = dh.readFromSimpleDB(incrementalData);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104incremental.xml.gz");
		incrementalData = null;
		
		// Get decremental data
		selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE < '" + formatter.format(oldestPlusOne.getTime()) + 
		   															"' and ACTDATE > '" + formatter.format(oldest.getTime()) + "'" ;
		List<Item> decrementalData = querySimpleDB(selectExpression);
		collection = dh.readFromSimpleDB(decrementalData);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104decremental.xml.gz");
		decrementalData = null;
		
		// Get new activity
		selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + formatter.format(oldestPlusOne.getTime()) + "'" +
																	" and ACTDATE < '" + formatter.format(now.getTime()) + "'";
		List<Item> newData = querySimpleDB(selectExpression);
		collection = dh.readFromSimpleDB(newData);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104new.xml.gz");
		newData = null;
		
		// Get existing + incremental data
		selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + formatter.format(oldest.getTime()) + "'" +
																	" and ACTDATE < '" + formatter.format(now.getTime()) + "'";
		List<Item> existingAndIncremental = querySimpleDB(selectExpression);
		collection = dh.readFromSimpleDB(existingAndIncremental);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104existingPlusIncremental.xml.gz");
		existingAndIncremental = null;
		
		// Get existing - decremental data
		selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + formatter.format(oldestPlusOne.getTime()) + "'" +
																	" and ACTDATE < '" + formatter.format(nowMinusOne.getTime()) + "'";
		List<Item> existingMinusDecremental = querySimpleDB(selectExpression);
		collection = dh.readFromSimpleDB(existingMinusDecremental);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104existingMinusDecremental.xml.gz");
		existingMinusDecremental = null;
	}
	
	public static void main(String[] argv) {
		DataSampler ds = new DataSampler();
		
		try {
			ds.createSampleDataFromRealActivity();
		}
		catch (Exception ex) {
			
		}
	}
}
