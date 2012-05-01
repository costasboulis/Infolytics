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

public class DecrementalDataThread implements Callable<List<Item>> {

	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final String DATE_PATTERN = "yyMMddHHmmssSSSZ";
	private static final String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private static TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
	private String tenantID;
	private Calendar fromDate;
	private Calendar toDate;
	private SimpleDateFormat formatter;
	
	public DecrementalDataThread(String tenantID, Calendar fromDate, Calendar toDate) {
		this.formatter = new SimpleDateFormat(DATE_PATTERN);
    	this.formatter.setTimeZone(TIME_ZONE);
    	
		this.tenantID = tenantID;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}


	@Override
	public List<Item> call() throws Exception {
		
		// Now form the SELECT statement for incremental data
		String userActivityDomain = "ACTIVITY_" + tenantID;
		String selectExpression = "select * from `" + userActivityDomain + "` " 
		+ "where ACTDATE < '" + formatter.format(toDate.getTime()) + "' "
		+ "and ACTDATE > '" + formatter.format(fromDate.getTime()) + "'";
		
        List<Item> decrementalData = querySimpleDB(selectExpression);

		return decrementalData;
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


	public String getTenantID() {
		return tenantID;
	}


	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}


	public Calendar getLastUpdateFrom() {
		return this.fromDate;
	}


	public void setLastUpdateFrom(Calendar lastUpdateFrom) {
		this.fromDate = lastUpdateFrom;
	}


	public Calendar getLastUpdateTo() {
		return this.toDate;
	}


	public void setLastUpdateTo(Calendar lastUpdateTo) {
		this.toDate = lastUpdateTo;
	}
	

}

