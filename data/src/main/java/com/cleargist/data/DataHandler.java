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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import com.cleargist.data.jaxb.Collection;

public class DataHandler {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private static String LOCAL_FILE = "c:\\recs\\data.xml";
	private static String LOCAL_SCHEMA = "c:\\recs\\dataTmp.xsd";
	
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
}
