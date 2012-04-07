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
				DataHandlerTest.class.getResourceAsStream(AWS_CREDENTIALS)));
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
    	
    	// dummy last update
		Calendar currentDateFrom = Calendar.getInstance();
		currentDateFrom.setTimeZone(TIME_ZONE);
		Date currentDate = new Date();
		currentDateFrom.setTimeInMillis(currentDate.getTime());     
		currentDateFrom.add(Calendar.MONTH, -6);      // Profile horizon, retrieve this from tenant profile
		
		Calendar lastUpdate = Calendar.getInstance();
		lastUpdate.setTimeZone(TIME_ZONE);
    	lastUpdate.set(2012, 3, 7, 15, 56, 20);       // Last update, retrieve this from tenant profile
//		lastUpdate.setTime(currentDate); 
		
		Calendar lastUpdateFrom = Calendar.getInstance();
		lastUpdateFrom.setTimeZone(TIME_ZONE);
		lastUpdateFrom.setTimeInMillis(lastUpdate.getTime().getTime());    
		lastUpdateFrom.add(Calendar.MONTH, -6);      // Profile horizon, retrieve this from tenant profile
		
		String tenantID = "104";
		String userActivityDomain = "ACTIVITY_" + tenantID;
		String selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + formatter.format(lastUpdate.getTime()) + "'";
		List<Item> incrementalData = querySimpleDB(selectExpression);
		DataHandler dh = new DataHandler();
		Collection collection = dh.readFromSimpleDB(incrementalData);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104incremental.xml.gz");
		
		// Now form the SELECT statement for decremental data
        selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE < '" + formatter.format(currentDateFrom.getTime()) + 
        															"' and ACTDATE > '" + formatter.format(lastUpdateFrom.getTime()) + "'" ;
        List<Item> decrementalData = querySimpleDB(selectExpression);
        collection = dh.readFromSimpleDB(decrementalData);
		dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "activity104decremental.xml.gz");
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
