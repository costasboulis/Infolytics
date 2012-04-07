package com.cleargist.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;
import javax.xml.datatype.DatatypeFactory;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.cleargist.data.jaxb.ActionType;
import com.cleargist.data.jaxb.Collection;
import com.cleargist.data.jaxb.Collection.DataList;
import com.cleargist.data.jaxb.DataType;
import com.cleargist.data.jaxb.MainActionType;
import com.cleargist.data.jaxb.RatingActionType;
import com.cleargist.data.jaxb.RatingType;





public class DataHandlerTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private static final String DATE_PATTERN = "yyMMddHHmmssSSSZ";
	private static TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
	public static String newline = System.getProperty("line.separator");
	private Logger logger = Logger.getLogger(getClass());
	
	
	
	@Test
	public void createValidDataEntries() throws Exception {
		
		Collection collection = new Collection();
		// Calendar conversions
		Date date = new Date();
		GregorianCalendar gc1 = new GregorianCalendar();
        gc1.setTime(date);
        DatatypeFactory df = DatatypeFactory.newInstance();
        collection.setCreatedAt(df.newXMLGregorianCalendar(gc1));
        collection.setDataList(new DataList());
        List<DataType> dataList = collection.getDataList().getData();
        
        DataType d1 = new DataType();
        d1.setUserId("userA");
        d1.setSession("sessionAA");
        d1.setItemId("productA");
        ActionType action1 = new ActionType();
        action1.setName(MainActionType.PURCHASE);
        d1.setEvent(action1);
        gc1.add(Calendar.MINUTE, -5);
        d1.setTimeStamp(df.newXMLGregorianCalendar(gc1));
        dataList.add(d1);
        
        DataType d2 = new DataType();
        d2.setUserId("userB");
        d2.setSession("sessionB");
        d2.setItemId("productB");
        ActionType action2 = new ActionType();
        RatingType ratingAction2 = new RatingType();
        ratingAction2.setName(RatingActionType.RATE);
        ratingAction2.setRating(1);
        action2.setRatingAction(ratingAction2);
//        action2.setName(MainActionType.ITEM_PAGE);
        d2.setEvent(action2);
        GregorianCalendar gc2 = new GregorianCalendar();
        gc2.setTime(date);
        gc2.add(Calendar.DAY_OF_MONTH, -3);
        gc2.add(Calendar.HOUR_OF_DAY, -5);
        d2.setTimeStamp(df.newXMLGregorianCalendar(gc2));
        dataList.add(d2);
        
        DataType d3 = new DataType();
        d3.setUserId("userA");
        d3.setSession("sessionAB");
        d3.setItemId("productC");
        ActionType action3 = new ActionType();
        action3.setName(MainActionType.PURCHASE);
        d3.setEvent(action3);
        GregorianCalendar gc3 = new GregorianCalendar();
        gc3.setTime(date);
        gc3.add(Calendar.MONTH, -3); 
        d3.setTimeStamp(df.newXMLGregorianCalendar(gc3));
        dataList.add(d3);
        
        DataType d4 = new DataType();
        d4.setUserId("userA");
        d4.setSession("sessionAC");
        d4.setItemId("productD");
        ActionType action4 = new ActionType();
        action4.setName(MainActionType.PURCHASE);
        d4.setEvent(action4);
        dataList.add(d4);
        
        DataType d5 = new DataType();
        d5.setSession("sessionFromUnknownUser");
        d5.setItemId("productD");
        ActionType action5 = new ActionType();
        action5.setName(MainActionType.ITEM_PAGE_VIEW);
        d5.setEvent(action5);
        GregorianCalendar gc5 = new GregorianCalendar();
        gc5.setTime(date);
        gc5.add(Calendar.MONTH, -1); 
        d5.setTimeStamp(df.newXMLGregorianCalendar(gc5));
        dataList.add(d5);
        
        DataHandler dh = new DataHandler();
        try {
        	dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "dataSample.xml");
        }
        catch (Exception ex) {
        	assertTrue(false);
        }
        
        assertTrue(true);
	}

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
	
	@Test
	public void createSampleDataFromRealactivity() throws Exception {
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
    	lastUpdate.set(2012, 3, 3, 4, 56, 20);       // Last update, retrieve this from tenant profile
//		lastUpdate.setTime(currentDate); 
		
		Calendar lastUpdateFrom = Calendar.getInstance();
		lastUpdateFrom.setTimeZone(TIME_ZONE);
		lastUpdateFrom.setTimeInMillis(lastUpdate.getTime().getTime());    
		lastUpdateFrom.add(Calendar.MONTH, -6);      // Profile horizon, retrieve this from tenant profile
		
		String tenantID = "104";
		String userActivityDomain = "ACTIVITY_" + tenantID;
		String selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + formatter.format(lastUpdate.getTime()) + "'";
		List<Item> incrementalData = querySimpleDB(selectExpression);
		
		// Now form the SELECT statement for decremental data
        selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE < '" + formatter.format(currentDateFrom.getTime()) + 
        															"' and ACTDATE > '" + formatter.format(lastUpdateFrom.getTime()) + "'" ;
        List<Item> decrementalData = querySimpleDB(selectExpression);
	}
}
