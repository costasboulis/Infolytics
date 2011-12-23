package gr.infolytics.recommendations.dao;

import gr.infolytics.recommendations.entity.Activity;
import gr.infolytics.recommendations.entity.ActivityEvent;
import gr.infolytics.recommendations.entity.ErrorLog;
import gr.infolytics.recommendations.entity.ErrorType;
import gr.infolytics.recommendations.util.Configuration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;

public class RecommendationsDAOImpl implements RecommendationsDAO {
	
	/*
	 * The Simple DB client class is thread safe so we only ever need one static instance.
	 * While you can have multiple instances it is better to only have one because it's
	 * a relatively heavy weight class.
	 */
	private static AmazonSimpleDB sdb;
	static {
		AWSCredentials creds = new BasicAWSCredentials(getKey(), getSecret());
		sdb = new AmazonSimpleDBClient(creds);
	}

	private DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	String simpleDBDomain;
	
	
	/**
     * User Activity DAO Implementation
     */
	public void saveActivity(List<ReplaceableItem> attributes, String tenantId) {
		simpleDBDomain = "ACTIVITY_"+tenantId;
    	sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));
		
	}
	
    public List<Activity> getActivities(String itemId, String tenantId) {
    	
    	List<Activity> activities = new ArrayList<Activity>();
    	simpleDBDomain = "ACTIVITY_"+tenantId;
    	String selectExpression = itemId==null ? "select * from `" + simpleDBDomain : "select * from `" + simpleDBDomain + "` where PRODUCT = '" + itemId + "'";
    	
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        
        for (Item item : sdb.select(selectRequest).getItems()) {
        	Activity activity = new Activity();
        	
        	for (Attribute attribute : item.getAttributes()) {
            	if (attribute.getName().equals("PRODUCT"))
            		activity.setItemId(attribute.getValue());
            	if (attribute.getName().equals("EVENT"))
            		activity.setActivityEvent(ActivityEvent.valueOf(attribute.getValue()));
            	if (attribute.getName().equals("USER"))
            		activity.setUserId(attribute.getValue());
            	if (attribute.getName().equals("SESSION"))
            		activity.setSession(attribute.getValue());
            	if (attribute.getName().equals("ACTDATE"))
					try {
						activity.setActivityDate(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
            	
            	activities.add(activity);
       
        	}
        }
        return activities;
    }
    
    /**
     * Error Log DAO Implementation
     */
    public void saveErrorLog(List<ReplaceableItem> attributes) {
		simpleDBDomain = "ERROR_LOG";
    	sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));
	}
    
    public List<ErrorLog> getErrorLog(String tenantId) {
    	
    	List<ErrorLog> errorLogs = new ArrayList<ErrorLog>();
    	simpleDBDomain = "ERROR_LOG";
    	String selectExpression = "select * from `" + simpleDBDomain;
    	
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        
        for (Item item : sdb.select(selectRequest).getItems()) {
        	ErrorLog errorLog = new ErrorLog();
        	
        	for (Attribute attribute : item.getAttributes()) {
            	if (attribute.getName().equals("TENANT"))
            		errorLog.setTenant(attribute.getValue());
            	if (attribute.getName().equals("TYPE"))
            		errorLog.setErrorType(ErrorType.valueOf(attribute.getValue()));
            	if (attribute.getName().equals("MESSAGE"))
            		errorLog.setMessage(attribute.getValue());
            	if (attribute.getName().equals("ERRDATE")) {
            		try {
            			errorLog.setErrorDate(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
            	}
            	
            	errorLogs.add(errorLog);
       
        	}
        }
        return errorLogs;
    }

    
    public static String getKey () {
		Configuration config = Configuration.getInstance();
		return config.getProperty("accessKey");
	}

	public static String getSecret () {
		Configuration config = Configuration.getInstance();
		return config.getProperty("secretKey");
	}
	
}
