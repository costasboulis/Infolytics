package com.cleargist.profile;




import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.AttributeDoesNotExistException;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DuplicateItemNameException;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.InvalidParameterValueException;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.MissingParameterException;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.cleargist.recommendations.dao.RecommendationsDAO;
import com.cleargist.recommendations.dao.RecommendationsDAOImpl;
import com.cleargist.recommendations.entity.Tenant;

public abstract class ProfileProcessor {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static final String DATE_PATTERN = "yyMMddHHmmssSSSZ";
	private Date currentDate;
	private Logger logger = Logger.getLogger(getClass());
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	
	private List<Item> querySimpleDB(String selectExpression) throws AmazonServiceException, AmazonClientException, Exception{
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
		String resultNextToken = null;
		String selectExpressionWithLimit = selectExpression + " limit 2500";
		SelectRequest selectRequest = new SelectRequest(selectExpressionWithLimit);
		List<Item> allItems = new LinkedList<Item>();
		int count  =0;
		do {
			count ++;
			System.out.println("count ::: " + count);
			System.out.flush();
			
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
	
	protected List<List<Item>> getDataSinceLastUpdate(String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
    	SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
    	
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
    	
    	
    	// dummy last update
		Calendar currentDateFrom = Calendar.getInstance();
		currentDateFrom.setTimeZone(TimeZone.getTimeZone("Europe/London"));
		Date currentDate = new Date();
		currentDateFrom.setTimeInMillis(currentDate.getTime());     
		currentDateFrom.add(Calendar.MONTH, -6);      // Profile horizon, retrieve this from tenant profile
		
		Calendar lastUpdate = Calendar.getInstance();
		lastUpdate.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    	lastUpdate.set(2012, 2, 26, 16, 56, 20);       // Last update, retrieve this from tenant profile
//		lastUpdate.setTime(currentDate); 
		
		Calendar lastUpdateFrom = Calendar.getInstance();
		lastUpdateFrom.setTimeZone(TimeZone.getTimeZone("Europe/London"));
		lastUpdateFrom.setTimeInMillis(lastUpdate.getTime().getTime());    
		lastUpdateFrom.add(Calendar.MONTH, -6);      // Profile horizon, retrieve this from tenant profile
		
		
		
		// Now form the SELECT statement for incremental data
		String userActivityDomain = "ACTIVITY_" + tenantID;
		String selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE > '" + formatter.format(lastUpdate.getTime()) + "'";
        List<Item> incrementalData = querySimpleDB(selectExpression);
        
		// Now form the SELECT statement for decremental data
        selectExpression = "select * from `" + userActivityDomain + "` where ACTDATE < '" + formatter.format(currentDateFrom.getTime()) + 
        															"' and ACTDATE > '" + formatter.format(lastUpdateFrom.getTime()) + "'" ;
        List<Item> decrementalData = querySimpleDB(selectExpression);
        
		List<List<Item>> newData = new ArrayList<List<Item>>();
		newData.add(incrementalData);
		newData.add(decrementalData);
		
		return newData;
	}
	
	// Gets as input the raw data, implements custom weighting, filtering logic and produces a profile of the form UID, <PID, VALUE>+
	protected abstract List<Profile> createProfile(List<Item> rawData) throws Exception;
	
	public void updateProfiles(String tenantID) throws Exception {
		List<List<Item>> newData = getDataSinceLastUpdate(tenantID);
		List<Item> incrementalData = newData.get(0);
		List<Item> decrementalData = newData.get(1);
		
		List<Profile> incrementalProfiles = createProfile(incrementalData);
		List<Profile> decrementalProfiles = createProfile(decrementalData);
		
		// Retrieve existing profiles and merge / write to SimpleDB
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				ProfileProcessor.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
		
    	String profileDomain = "PROFILE_" + tenantID;
    	
    	// Do the incremental profiles
		for (Profile incrementalProfile : incrementalProfiles) {
			String userID = incrementalProfile.getUserID();
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			HashSet<String> productIDs = new HashSet<String>();
			
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(profileDomain);
			request.setItemName(userID);
			GetAttributesResult result = sdb.getAttributes(request);
			boolean newUser = result.getAttributes().size() > 0 ? false : true;
			for (Attribute attribute : result.getAttributes()) {
				String value = attribute.getValue();
				String[] parsedValue = value.split(";");
				String productID = parsedValue[0];
				float score = Float.parseFloat(parsedValue[1]);
            	
				productIDs.add(productID);
            		
				Float incrementalScore = incrementalProfile.getAttributes().get(productID);
				if (incrementalScore == null) {
					continue;   // Nothing to add in this attribute
				}
				else {
					// Update the score of the attribute
					float updatedScore = score + incrementalScore.floatValue();
					StringBuffer sb = new StringBuffer();
					sb.append(productID); sb.append(";"); sb.append(updatedScore);
            			
					ReplaceableAttribute att = new ReplaceableAttribute(attribute.getName(), sb.toString(), true);
					attributes.add(att);
				}
			}
			
			
			
			if (newUser) {
				// Add new user
				ReplaceableItem item = new ReplaceableItem();
				item.setName(userID);
				for (Map.Entry<String, Float> incrementalProfileAttributes : incrementalProfile.getAttributes().entrySet()) {
					String productID = incrementalProfileAttributes.getKey();
					float score = incrementalProfileAttributes.getValue().floatValue();
        			StringBuffer sb = new StringBuffer();
        			sb.append(productID); sb.append(";"); sb.append(score);
        			String attributeName = "Attribute_" + productID;
					ReplaceableAttribute att = new ReplaceableAttribute(attributeName, sb.toString(), true);
        			attributes.add(att);
				}
				item.setAttributes(attributes);
				addUserProfile(sdb, profileDomain, item);
			}
			else {
				// Update only the attributes
				// Get the new attributes
				for (Map.Entry<String, Float> incrementalProfileAttributes : incrementalProfile.getAttributes().entrySet()) {
					String productID = incrementalProfileAttributes.getKey();
					if (!productIDs.contains(productID)) {
						float score = incrementalProfileAttributes.getValue().floatValue();
	        			StringBuffer sb = new StringBuffer();
	        			sb.append(productID); sb.append(";"); sb.append(score);
	        			String attributeName = "Attribute_" + productID;
						ReplaceableAttribute att = new ReplaceableAttribute(attributeName, sb.toString(), true);
	        			attributes.add(att);
					}
				}
				
				// Update the profiles
				updateAttributes(sdb, profileDomain, userID, attributes);
			}
			
		
		}
		
		// Now do the decremental profiles
		for (Profile decrementalProfile : decrementalProfiles) {
			String userID = decrementalProfile.getUserID();
			
			
			List<ReplaceableAttribute> attributes = new LinkedList<ReplaceableAttribute>();
			List<Attribute> deleteAttributes = new LinkedList<Attribute>();
			
			GetAttributesRequest request = new GetAttributesRequest();
			request.setDomainName(profileDomain);
			request.setItemName(userID);
			GetAttributesResult result = sdb.getAttributes(request);
			for (Attribute attribute : result.getAttributes()) {
				String value = attribute.getValue();
				String[] parsedValue = value.split(";");
				String productID = parsedValue[0];
				float score = Float.parseFloat(parsedValue[1]);
            		
				Float decrementalScore = decrementalProfile.getAttributes().get(productID);
				if (decrementalScore == null) {
					continue;  
				}
				else {
					// Update the score of the attribute
					float updatedScore = score - decrementalScore.floatValue();
					if (updatedScore <= 0.0f) {
						// delete attribute
						deleteAttributes.add(attribute);
					}
					else {
						StringBuffer sb = new StringBuffer();
						sb.append(productID); sb.append(";"); sb.append(updatedScore);
	            			
						ReplaceableAttribute att = new ReplaceableAttribute(attribute.getName(), sb.toString(), true);
						attributes.add(att);
					}
					
				}
			}
			
			// Update the profiles
			updateAttributes(sdb, profileDomain, userID, attributes);
			
			// Delete attributes
			if (deleteAttributes.size() > 0) {
				try {
					sdb.deleteAttributes(new DeleteAttributesRequest(profileDomain, userID, deleteAttributes));
				}
				catch (InvalidParameterValueException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " because of invalid parameter value" + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new InvalidParameterValueException(errorMessage);
		    	}
				catch (NoSuchDomainException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new NoSuchDomainException(errorMessage);
		    	}
				catch (AttributeDoesNotExistException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new AttributeDoesNotExistException(errorMessage);
		    	}
				catch (MissingParameterException ex) {
		    		String errorMessage = "Cannot delete from domain " + profileDomain + " " + ex.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new MissingParameterException(errorMessage);
		    	}
				catch (AmazonServiceException ase) {
		    		String errorMessage = "Cannot delete from domain, Amazon Service error (" + ase.getErrorType().toString() + ")" + " " + ase.getStackTrace();
		    		logger.error(errorMessage);
		    		throw new AmazonServiceException(errorMessage);
		        }
		    	catch (AmazonClientException ace) {
		            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
		                + "a serious internal problem while trying to communicate with SimpleDB, "
		                + "such as not being able to access the network " + " " + ace.getStackTrace();
		            logger.error(errorMessage);
		    		throw new AmazonClientException(errorMessage);
		        }
		    	catch (Exception ex) {
		    		String errorMessage = "Cannot delete from SimpleDB";
		    		logger.error(errorMessage);
		    		throw new Exception();
		    	}
			}
			
		}
		
		// Profile is updated
		/*
		RecommendationsDAO recsDAO = new RecommendationsDAOImpl();
    	Tenant tenant = recsDAO.getTenantById(tenantID);
    	if (tenant == null) {
    		logger.error("Could not find tenant with ID " + tenantID);
    		throw new Exception();
    	}
		tenant.setLatestProfile(this.currentDate);
		*/
	}
	
	private void updateAttributes(AmazonSimpleDB sdb, String profileDomain, String userID, List<ReplaceableAttribute> attributes) throws Exception {
		if (attributes.size() == 0) {
			return;
		}
		try {
			sdb.putAttributes(new PutAttributesRequest(profileDomain, userID, attributes));
		}
		catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because of invalid parameter value" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new InvalidParameterValueException(errorMessage);
    	}
		catch (NumberDomainBytesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because max number of domain bytes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainBytesExceededException(errorMessage);
    	}
		catch (NumberDomainAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because max number of domain attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainAttributesExceededException(errorMessage);
    	}
		catch (NumberItemAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + profileDomain + " because max number of item attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberItemAttributesExceededException(errorMessage);
    	}
    	catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + profileDomain + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NoSuchDomainException(errorMessage);
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot write in SimpleDB, Amazon Service error (" + ase.getErrorType().toString() + ")" + " " + ase.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonServiceException(errorMessage);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network " + " " + ace.getStackTrace();
            logger.error(errorMessage);
    		throw new AmazonClientException(errorMessage);
        }
    	catch (Exception ex) {
    		String errorMessage = "Cannot write to SimpleDB";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
	}
	
	private void addUserProfile(AmazonSimpleDB sdb, String SimpleDBDomain, ReplaceableItem profile) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
		
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		items.add(profile);
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(SimpleDBDomain, items));
    	}
		catch (DuplicateItemNameException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because of duplicate item names" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new DuplicateItemNameException(errorMessage);
    	}
		catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because of invalid parameter value" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new InvalidParameterValueException(errorMessage);
    	}
		catch (NumberDomainBytesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of domain bytes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainBytesExceededException(errorMessage);
    	}
		catch (NumberSubmittedItemsExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of submitted items exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberSubmittedItemsExceededException(errorMessage);
    	}
		catch (NumberSubmittedAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of submitted attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberSubmittedAttributesExceededException(errorMessage);
    	}
		catch (NumberDomainAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of domain attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberDomainAttributesExceededException(errorMessage);
    	}
		catch (NumberItemAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of item attributes exceeded" + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NumberItemAttributesExceededException(errorMessage);
    	}
    	catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + SimpleDBDomain + " " + ex.getStackTrace();
    		logger.error(errorMessage);
    		throw new NoSuchDomainException(errorMessage);
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot write in SimpleDB, Amazon Service error (" + ase.getErrorType().toString() + ")" + " " + ase.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonServiceException(errorMessage);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network " + " " + ace.getStackTrace();
            logger.error(errorMessage);
    		throw new AmazonClientException(errorMessage);
        }
    	catch (Exception ex) {
    		String errorMessage = "Cannot write to SimpleDB";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    }
}
