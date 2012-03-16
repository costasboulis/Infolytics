package com.cleargist.catalog.deals.scrape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import com.cleargist.catalog.dao.MyValidationEventHandler;
import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;

public abstract class Scraper {
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
}
