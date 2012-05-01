package com.cleargist.profile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

public class IncrementalDataThread implements Callable<List<Item>> {
	
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final String DATE_PATTERN = "yyMMddHHmmssSSSZ";
	private static TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
	private static final String TEST_DATE_STR = "120406190000000+0200";
	private static final String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private Calendar calendar;
	private String tenantID;
	private SimpleDateFormat formatter;

	public IncrementalDataThread(String tenantID, Calendar cal) {
		this.formatter = new SimpleDateFormat(DATE_PATTERN);
    	this.formatter.setTimeZone(TIME_ZONE);
    	
		this.tenantID = tenantID;
		this.calendar = cal;
	}
	

	@Override
	public List<Item> call() throws Exception {
		// Now form the SELECT statement for incremental data
		String userActivityDomain = "ACTIVITY_" + tenantID;
		String dateString = formatter.format(calendar.getTime());
		String selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + dateString +  "'";
        List<Item> incrementalData = querySimpleDB(selectExpression);
        
		return incrementalData;
	}
	
	private List<Item> querySimpleDB(String selectExpression) throws AmazonServiceException, AmazonClientException, Exception{
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

	public String getCalendarInString() {
		return formatter.format(calendar.getTime());
	}


	public void setCalendat(Calendar cal) {
		this.calendar = cal;
	}


	public String getTenantID() {
		return tenantID;
	}


	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}
	
	
}
