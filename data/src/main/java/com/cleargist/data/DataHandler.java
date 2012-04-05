package com.cleargist.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.cleargist.data.jaxb.Collection;
import com.cleargist.data.jaxb.DataType;


public class DataHandler {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private static String SIMPLEDB_ENDPOINT = "https://sdb.eu-west-1.amazonaws.com";
	private static String ACTIVITY_DOMAIN = "ACTIVITY_";
	private static final String DATE_PATTERN = "yyMMddHHmmssSSSZ";
	private static TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
	private Logger logger = Logger.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private static String LOCAL_FILE = "c:\\recs\\data.xml";
	private static String LOCAL_SCHEMA = "c:\\recs\\dataTmp.xsd";
	private static String USER_STRING = "USER";
	private static String SESSION_STRING = "SESSION";
	private static String ITEM_STRING = "ITEM";
	private static String EVENT_STRING = "EVENT";
	private static String RATING_STRING = "RATING";
	private static String DATE_STRING = "ACTDATE";
	
	
	public void marshallData(Collection collection, 
							String schemaBucket, String schemaKey, 
							String bucket, String key) 
	throws JAXBException, IOException, Exception {
		
		// Copy the XSD file locally
		AWSCredentials creds = null;
		try {
			creds = new PropertiesCredentials(
				DataHandler.class.getResourceAsStream(AWS_CREDENTIALS));
		}
		catch (Exception ex) {
			logger.error("Could not read AWS credentials");
			throw new Exception();
		}
		AmazonS3 s3 = new AmazonS3Client(creds);
		S3Object schemaObject = s3.getObject(schemaBucket, schemaKey);
		BufferedReader reader = new BufferedReader(new InputStreamReader(schemaObject.getObjectContent()));
		File localSchemaFile = new File(LOCAL_SCHEMA);
		BufferedWriter out = new BufferedWriter(new FileWriter(localSchemaFile));
		String line = null;
		while ((line = reader.readLine()) != null) {
			out.write(line + newline);
			out.flush();
		}
		reader.close();
		out.close();
		
		
		// Use the local XSD file to marshal the catalog 
		Marshaller marshaller = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance("com.cleargist.data.jaxb");
    		marshaller = jaxbContext.createMarshaller();
    		SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    		Schema schema = null;
    		try {
    			schema = sf.newSchema(localSchemaFile);
    		}
    		catch (Exception e){
    			logger.warn("Cannot create schema, check schema location " + localSchemaFile.getAbsolutePath());
    			System.exit(-1);
    		}
    		marshaller.setSchema(schema);
    		marshaller.setEventHandler(new MyValidationEventHandler());
    		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
    	}
    	catch (JAXBException ex) {
    		logger.error("Setting up marshalling failed");
    		throw new Exception();
    	}
    	
    	File localFile = new File(LOCAL_FILE);
    	try {
			marshaller.marshal(collection, new FileOutputStream(localFile));
		}
		catch (JAXBException ex) {
			logger.error("Could not marshal the catalog");
			throw new Exception();
		}
		catch (FileNotFoundException ex2) {
			logger.error("Could not write to " + localFile.getAbsolutePath());
			throw new Exception();
		}
		
		// Now copy the marshaled local file to S3
		if (!s3.doesBucketExist(bucket)) {
			s3.createBucket(bucket);
		}
    	PutObjectRequest r = new PutObjectRequest(bucket, key, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
    	localSchemaFile.delete();
    	localFile.delete();
	}
	
	public Collection unmarshallData(String bucket, String key) throws JAXBException, IOException, Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				DataHandler.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
        S3Object catalogFile = s3.getObject(new GetObjectRequest(bucket, key));
		BufferedReader reader = new BufferedReader(new InputStreamReader(catalogFile.getObjectContent()));
		
		Collection collection = null;
		try {
			collection = unmarshallData(reader);
		}
		catch (JAXBException ex) {
			String errorMessage = "Error while unmarshalling from bucket : " + bucket + " and key : " + key;
			logger.error(errorMessage);
			throw new JAXBException(errorMessage);
		}
		catch (IOException ex) {
			String errorMessage = "Error while reading from bucket : " + bucket + " and key : " + key;
			logger.error(errorMessage);
			throw new IOException(errorMessage);
		}
		
		return collection;
	}
	
	public Collection unmarshallData(File file) throws JAXBException, IOException, Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Collection collection = null;
		try {
			collection = unmarshallData(reader);
		}
		catch (JAXBException ex) {
			String errorMessage = "Error while unmarshalling from " + file.getAbsolutePath();
			logger.error(errorMessage);
			throw new JAXBException(errorMessage);
		}
		catch (IOException ex) {
			String errorMessage = "Error while reading from : " + file.getAbsolutePath();
			logger.error(errorMessage);
			throw new IOException(errorMessage);
		}
		
		return collection;
	}
	
	private Collection unmarshallData(BufferedReader reader) throws JAXBException, IOException, Exception {
		Unmarshaller unmarshaller = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance("com.cleargist.data.jaxb");
    		unmarshaller = jaxbContext.createUnmarshaller();
    	}
    	catch (JAXBException ex) {
    		String errorMessage = "Setting up unmarshalling failed";
    		logger.error(errorMessage);
    		throw new JAXBException(errorMessage);
    	}
    	
    	Collection catalog = (Collection)unmarshaller.unmarshal(reader);
        
        reader.close();
        logger.info("Catalog unmarshalled");
        
        return catalog;
	}
	
	public void insertInSimpleDB(Collection collection, String tenantID) throws AmazonServiceException, AmazonClientException, IOException {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				DataHandler.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.setEndpoint(SIMPLEDB_ENDPOINT);
    	
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
    	formatter.setTimeZone(TIME_ZONE);
		String rawDataDomain = ACTIVITY_DOMAIN + tenantID;
		List<ReplaceableItem> newData = new ArrayList<ReplaceableItem>();
		for (DataType data : collection.getDataList().getData()) {
			
			List<ReplaceableAttribute> attributeList = new LinkedList<ReplaceableAttribute>();
			String itemName = UUID.randomUUID().toString();
			String productID = data.getItemId();
			attributeList.add(new ReplaceableAttribute(ITEM_STRING, productID, true));
			
			StringBuffer sb = new StringBuffer();
			int rating = -1;
			boolean ratingFound = false;
			if (data.getEvent().getName() == null) {
				String ratingEvent = data.getEvent().getRatingAction().getName().toString();
				sb.append(ratingEvent); 
				rating = data.getEvent().getRatingAction().getRating().intValue();
				ratingFound = true;
			}
			else {
				String event = data.getEvent().getName().toString();
				sb.append(event);
			}
			String event = sb.toString();
			attributeList.add(new ReplaceableAttribute(EVENT_STRING, event, true));
			
			if (ratingFound) {
				ReplaceableAttribute ratingAttribute = new ReplaceableAttribute(RATING_STRING, Integer.toString(rating), true);
				attributeList.add(ratingAttribute);
			}
			
			
			String sessionID = data.getSession();
			attributeList.add(new ReplaceableAttribute(SESSION_STRING, sessionID, true));
			
			String dateString = formatter.format(data.getTimeStamp().toGregorianCalendar().getTime());
			attributeList.add(new ReplaceableAttribute(DATE_STRING, dateString, true));
			
			String userID = data.getUserId();
			if (userID != null && !userID.isEmpty() && !userID.equals("0")) {
				ReplaceableAttribute userAttribute = new ReplaceableAttribute(USER_STRING, userID, true);
				attributeList.add(userAttribute);
			}
			
			ReplaceableItem item = new ReplaceableItem(itemName);
			item.setAttributes(attributeList);
			newData.add(item);
			
			if (newData.size() >= 25) {
				sdb.batchPutAttributes(new BatchPutAttributesRequest(rawDataDomain, newData));
				newData = new ArrayList<ReplaceableItem>();
			}
		}
		
		if (newData.size() > 0) {
			sdb.batchPutAttributes(new BatchPutAttributesRequest(rawDataDomain, newData));
			newData = new ArrayList<ReplaceableItem>();
		}
		
	}
}
