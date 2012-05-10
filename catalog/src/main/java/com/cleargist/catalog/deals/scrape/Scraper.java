package com.cleargist.catalog.deals.scrape;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import com.cleargist.catalog.dao.MyValidationEventHandler;
import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;

public abstract class Scraper {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	
	public abstract DealType scrape(String url) throws Exception;
	
	public static String html2text(String html) {
	    return Jsoup.parse(html).text();
	}
	
	public Collection scrape(List<String> urlList) {
		Collection collection = new Collection();
		Collection.Deals deals = new Collection.Deals();
		collection.setDeals(deals);
		List<DealType> dealsList = deals.getDeal();
		for (String url : urlList) {
			DealType deal = null;
			try {
				deal = scrape(url);
			}
			catch (Exception ex) {
				logger.warn("Could not scrape deal " + url);
				continue;
			}
			dealsList.add(deal);
			
			logger.info("Processed deal " + url);
		}
		return collection;
	}
	
	public void marshall(Collection collection, String XSDbucket, String XSDkey, String bucket, String key) throws Exception {
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Scraper.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		File localXSDfile = new File(XSDbucket + "_" + XSDkey);
		S3Object xsdObject = s3.getObject(XSDbucket, XSDkey);
		InputStream reader = new BufferedInputStream(xsdObject.getObjectContent());   
		OutputStream writer = new BufferedOutputStream(new FileOutputStream(localXSDfile));
		int read = -1;
		while ( ( read = reader.read() ) != -1 ) {
			writer.write(read);
		}
		writer.flush();
		writer.close();
		reader.close();
		
		File localFile = new File(bucket + "_" + key);
		marshall(collection, localXSDfile, localFile);
		
		PutObjectRequest r = new PutObjectRequest(bucket, key, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
    	localFile.delete();
    	localXSDfile.delete();
	}
	
	public void marshall(Collection collection, File XSDFile, File outputFile) throws Exception {
		
		Marshaller marshaller = null;
    	try {
    		String contextPath = com.cleargist.catalog.deals.entity.jaxb.Collection.class.getCanonicalName().replaceAll("\\.Collection", "");
    		JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
    		marshaller = jaxbContext.createMarshaller();
    		SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    		Schema schema = null;
    		try {
    			schema = sf.newSchema(XSDFile);
    		}
    		catch (Exception e){
    			logger.warn("Cannot create schema, check schema location " + XSDFile.getAbsolutePath());
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
    	
    	try {
			marshaller.marshal(collection, new FileOutputStream(outputFile));
		}
		catch (JAXBException ex) {
			logger.error("Could not marshal the catalog");
			throw new Exception();
		}
		catch (FileNotFoundException ex2) {
			logger.error("Could not write to " + outputFile.getAbsolutePath());
			throw new Exception();
		}
	}
	
	public Collection unmarshall(String bucket, String filename) throws JAXBException, IOException, Exception {
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				Scraper.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
        S3Object catalogFile = s3.getObject(new GetObjectRequest(bucket, filename));
		BufferedReader reader = new BufferedReader(new InputStreamReader(catalogFile.getObjectContent()));
		
		Unmarshaller unmarshaller = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance("com.cleargist.catalog.deals.entity.jaxb");
    		unmarshaller = jaxbContext.createUnmarshaller();
    	}
    	catch (JAXBException ex) {
    		String errorMessage = "Setting up unmarshalling failed";
    		logger.error(errorMessage);
    		throw new JAXBException(errorMessage);
    	}
    	
        
    	Collection catalog = null;
        try {
        	catalog = (Collection)unmarshaller.unmarshal(reader);
        }
        catch (JAXBException ex) {
        	String errorMessage = "Error while unmarshalling catalog BUCKET : " + bucket + " FILE : " + filename;
    		logger.error(errorMessage);
    		throw new JAXBException(errorMessage);
    	}
        reader.close();
        logger.info("Deals unmarshalled");
        
        return catalog;
	}
}
