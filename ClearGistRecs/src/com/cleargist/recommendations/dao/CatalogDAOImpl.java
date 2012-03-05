package com.cleargist.recommendations.dao;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.AttributeDoesNotExistException;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
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
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.cleargist.recommendations.entity.Catalog2;
import com.cleargist.recommendations.util.MyValidationEventHandler;




public class CatalogDAOImpl implements CatalogDAO {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	private Locale locale = new Locale("el", "GR");
	public static String newline = System.getProperty("line.separator");
	private static final String UID_STRING = "UID";
	private static final String NAME_STRING = "NAME";
	private static final String LINK_STRING = "LINK";
	private static final String IMAGE_STRING = "IMAGE";
	private static final String PRICE_STRING = "PRICE";
	private static final String CATEGORY_STRING = "CATEGORY";
	private static final String CATEGORYID_STRING = "CATEGORY_ID";
	private static final String DESCRIPTION_STRING = "DESCRIPTION";
	private static final String WEIGHT_STRING = "WEIGHT";
	private static final String MANUFACTURER_STRING = "MANUFACTURER";
	private static final String MPN_STRING = "MPN";
	private static final String SHIPPING_STRING = "SHIPPING";
	private static final String AVAILABILITY_STRING = "AVAILABILITY";
	private static final String INSTOCK_STRING = "INSTOCK";
	private static final String ISBN_STRING = "ISBN";
	

	public CatalogDAOImpl() {
		Locale.setDefault(this.locale);
	}
	
	public static String getCatalogName(String catalogID, String tenantID) {
		return catalogID == null || catalogID.isEmpty() ? "CATALOG_" + tenantID : "CATALOG_" + catalogID + "_" + tenantID;
	}
	
	public void deleteCatalog(String catalogID, String tenantID) throws Exception {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		logger.error("Cannot initiate SimpleDB client");
    		throw new IOException();
    	}
        String catalogDomain = getCatalogName(catalogID, tenantID);
        try {
    		for (String domain : sdb.listDomains().getDomainNames()) {
    			if (domain.equals(catalogDomain)) {
    				sdb.deleteDomain(new DeleteDomainRequest(catalogDomain));
    				break;
    			}
    		}
    	}
    	catch (MissingParameterException ex) {
            logger.error("Cannot delete SimpleDB domain " + catalogDomain);
            throw new Exception();
        }
    	catch (AmazonServiceException ase) {
    		logger.error("Amazon Service Exception while deleting SimpleDB domain " + catalogDomain);
    		throw new Exception();
        }
    	catch (AmazonClientException ace) {
    		logger.error("Amazon Client Exception while deleting SimpleDB domain " + catalogDomain);
    		throw new Exception();
        }
	}
	
	public void marshallCatalog(Catalog2 catalog, String schemaBucketName, String schemaFilename, String bucketName, String filename, String tenantID) 
	throws JAXBException, IOException, Exception {
		
		// Copy the XSD file locally
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
		S3Object s3CarSchemaFile = s3.getObject(schemaBucketName, schemaFilename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(s3CarSchemaFile.getObjectContent()));
		File localSchemaFile = new File("catalog.xsd");
		BufferedWriter out = new BufferedWriter(new FileWriter(localSchemaFile));
		String line = null;
		while ((line = reader.readLine()) != null) {
			out.write(line + newline);
			out.flush();
		}
		reader.close();
		out.close();
		
		
		// Use the local XSD file to marshall the catalog 
		Marshaller marshaller = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance("com.cleargist.catalog.entity.jaxb");
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
    	
    	File localCatalogFile = new File(getCatalogName(null, tenantID));
    	try {
			marshaller.marshal(catalog, new FileOutputStream(localCatalogFile));
		}
		catch (JAXBException ex) {
			logger.error("Could not marshal the catalog");
			throw new Exception();
		}
		catch (FileNotFoundException ex2) {
			logger.error("Could not write to " + localCatalogFile.getAbsolutePath());
			throw new Exception();
		}
		
		// Now copy the marshalled local file to S3
		if (!s3.doesBucketExist(bucketName)) {
			s3.createBucket(bucketName);
		}
    	PutObjectRequest r = new PutObjectRequest(bucketName, filename, localCatalogFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
    	localSchemaFile.delete();
    	localCatalogFile.delete();
	}
	
	private Catalog2 unmarshallCatalog(String bucket, String filename) throws JAXBException, IOException, Exception {
	
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
        S3Object catalogFile = s3.getObject(new GetObjectRequest(bucket, filename));
		BufferedReader reader = new BufferedReader(new InputStreamReader(catalogFile.getObjectContent()));
		
		Unmarshaller unmarshaller = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance("com.cleargist.catalog.entity.jaxb");
    		unmarshaller = jaxbContext.createUnmarshaller();
    	}
    	catch (JAXBException ex) {
    		String errorMessage = "Setting up unmarshalling failed";
    		logger.error(errorMessage);
    		throw new JAXBException(errorMessage);
    	}
    	
        
        Catalog2 catalog = null;
        try {
        	catalog = (Catalog2)unmarshaller.unmarshal(reader);
        }
        catch (JAXBException ex) {
        	String errorMessage = "Error while unmarshalling catalog BUCKET : " + bucket + " FILE : " + filename;
    		logger.error(errorMessage);
    		throw new JAXBException(errorMessage);
    	}
        reader.close();
        logger.info("Catalog2 unmarshalled");
        
        return catalog;
	}
	
	private boolean isValidURL(String url) {
		try {
			URL u = new URL ( url ); 
			HttpURLConnection huc =  ( HttpURLConnection )  u.openConnection (); 
			huc.setRequestMethod ("HEAD"); 
			huc.connect () ; 
			return (huc.getResponseCode() == HttpURLConnection.HTTP_OK);
		}
		catch (Exception ex) {
			return false;
		}
		
	}
	
	private void insertItems(AmazonSimpleDB sdb, Catalog2 catalog, String catalogDomain) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
		// Persist the items in the SimpleDB
    	List<ReplaceableItem> recsPairs = new ArrayList<ReplaceableItem>();
    	HashSet<String> uids = new HashSet<String>();
    	for (Catalog2.Products.Product product : catalog.getProducts().getProduct()) {
    		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
    		
    		String uid = product.getUid();
    		if (uid != null && !uid.isEmpty()) {
    			if (uids.contains(uid)) {
    				logger.warn("UID " + uid + " already encountered...skipping");
    				continue;
    			}
    			attributes.add(new ReplaceableAttribute(UID_STRING, uid, true));
    			uids.add(uid);
    		}
    		
    		String name = product.getName();
    		if (name != null && !name.isEmpty() && name.getBytes("UTF-8").length < 1024) {
    			attributes.add(new ReplaceableAttribute(NAME_STRING, name, true));
    		}
    		
    		String link = product.getLink();
    		if (link != null && !link.isEmpty() && link.getBytes("UTF-8").length < 1024 && isValidURL(link)) {
    			attributes.add(new ReplaceableAttribute(LINK_STRING, link, true));
    		}
    		
    		String image = product.getImage();
    		if (image != null && !image.isEmpty() && image.getBytes("UTF-8").length < 1024 && isValidURL(image)) {
    			attributes.add(new ReplaceableAttribute(IMAGE_STRING, image, true));
    		}
    		
    		String price = product.getPrice() != null ? product.getPrice().toPlainString() : null;
    		if (price != null && !price.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(PRICE_STRING, price, true));
    		}
    		
    		String category = product.getCategory();
    		if (category != null && !category.isEmpty() && category.getBytes("UTF-8").length < 1024) {
    			attributes.add(new ReplaceableAttribute(CATEGORY_STRING, category, true));
    		}
    		
    		String categoryId = product.getCategoryId() != null ? product.getCategoryId().toString() : null;
    		if (categoryId != null && !categoryId.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(CATEGORYID_STRING, categoryId, true));
    		}
    		
    		String description = product.getDescription();
    		if (description != null && !description.isEmpty() && description.getBytes("UTF-8").length < 1024) {
    			attributes.add(new ReplaceableAttribute(DESCRIPTION_STRING, description, true));
    		}
    		
    		String weight = product.getWeight() != null ? product.getWeight().toString() : null;
    		if (weight != null && !weight.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(WEIGHT_STRING, weight, true));
    		}
    		
    		String manufacturer = product.getManufacturer();
    		if (manufacturer != null && !manufacturer.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(MANUFACTURER_STRING, manufacturer, true));
    		}
    		
    		String mpn = product.getMpn();
    		if (mpn != null && !mpn.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(MPN_STRING, mpn, true));
    		}
    		
    		String shipping = product.getShipping() != null ? product.getShipping().toPlainString() : null;
    		if (shipping != null && !shipping.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(SHIPPING_STRING, shipping, true));
    		}
    		
    		String availability = product.getAvailability();
    		if (availability != null && !availability.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(AVAILABILITY_STRING, availability, true));
    		}
    		
    		String instock = product.getInstock();
    		if (instock != null && !instock.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(INSTOCK_STRING, instock, true));
    		}
    		
    		String isbn = product.getIsbn();
    		if (isbn != null && !isbn.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(ISBN_STRING, isbn, true));
    		}
    		
    		
    		recsPairs.add(new ReplaceableItem(uid).withAttributes(attributes));
    		
    		if (recsPairs.size() == 25) {
        		writeSimpleDB(sdb, catalogDomain, recsPairs);
            	recsPairs = new ArrayList<ReplaceableItem>();
        	}
    	}
    	if (recsPairs.size() > 0) {
    		writeSimpleDB(sdb, catalogDomain, recsPairs);
        }
	}
	
	public void insertCatalog(String bucket, String filename, String catalogID, String tenantID) 
	throws JAXBException, DuplicateItemNameException, InvalidParameterValueException, 
	NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, NumberSubmittedAttributesExceededException, 
	NumberDomainAttributesExceededException, NumberItemAttributesExceededException, NoSuchDomainException, 
	AmazonServiceException, AmazonClientException, IOException, Exception {
		
		// First, unmarshall the catalog
		Catalog2 catalog = unmarshallCatalog(bucket, filename);
        
		// Delete existing Amazon SimpleDB domain, if it exists
        AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
        String catalogDomain = getCatalogName(catalogID, tenantID);
        try {
    		for (String domain : sdb.listDomains().getDomainNames()) {
    			if (domain.equals(catalogDomain)) {
    				sdb.deleteDomain(new DeleteDomainRequest(catalogDomain));
    				break;
    			}
    		}
    	}
    	catch (MissingParameterException ex) {
    		String errorMessage = "Cannot delete SimpleDB domain " + catalogDomain + " " + ex.getStackTrace();
            logger.error(errorMessage);
            throw new MissingParameterException(errorMessage);
        }
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Amazon Service Exception while deleting SimpleDB domain " + catalogDomain + " " + ase.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonServiceException(errorMessage);
        }
    	catch (AmazonClientException ace) {
    		String errorMessage = "Amazon Client Exception while deleting SimpleDB domain " + catalogDomain + " " + ace.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonClientException(errorMessage);
        }
    	
    	
		// Create new Amazon SimpleDB domain
        try {
    		sdb.createDomain(new CreateDomainRequest(catalogDomain));
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Amazon Service Exception while creating new SimpleDB domain " + catalogDomain + " " + ase.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonServiceException(errorMessage);
        }
    	catch (AmazonClientException ace) {
    		String errorMessage = "Amazon Client Exception while creating new SimpleDB domain " + catalogDomain + " " + ace.getStackTrace();
    		logger.error(errorMessage);
    		throw new AmazonClientException(errorMessage);
        }
    	
    	// Insert catalog items
    	insertItems(sdb, catalog, catalogDomain);
	}
	
	public void appendCatalog(String bucket, String filename, String catalogID, String tenantID) 
	throws JAXBException, DuplicateItemNameException, InvalidParameterValueException, 
	NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, NumberSubmittedAttributesExceededException, 
	NumberDomainAttributesExceededException, NumberItemAttributesExceededException, NoSuchDomainException, 
	AmazonServiceException, AmazonClientException, IOException, Exception {
		// First, unmarshall the catalog
		Catalog2 catalog = unmarshallCatalog(bucket, filename);
		
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
        String catalogDomain = getCatalogName(catalogID, tenantID);
        
		// Insert catalog items
        insertItems(sdb, catalog, catalogDomain);
	}
	
	private void writeSimpleDB(AmazonSimpleDB sdb, String SimpleDBDomain, List<ReplaceableItem> recsPairs) 
	throws DuplicateItemNameException, InvalidParameterValueException, NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, 
	NumberSubmittedAttributesExceededException, NumberDomainAttributesExceededException, NumberItemAttributesExceededException, 
	NoSuchDomainException, AmazonServiceException, AmazonClientException, Exception {
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(SimpleDBDomain, recsPairs));
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
	
	public void deleteProduct(String productID, String catalogID, String tenantID) throws Exception {
		AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		logger.error("Cannot initiate SimpleDB client");
    		throw new Exception();
    	}
		String catalogDomain = getCatalogName(catalogID, tenantID);
    	String selectExpression = "select * from `" + catalogDomain + "` where UID = '" + productID + "'";
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        List<Item> items = sdb.select(selectRequest).getItems();
        if (items.size() > 1) {
        	logger.error("Expecting to find a single product but found " + items.size());
        	throw new Exception();
        	
        }
        if (items.size() == 0) {
        	logger.error("Expecting to find a single product but found none");
        	throw new Exception();
        	
        }
        
        DeleteAttributesRequest deleteRequest = new DeleteAttributesRequest();
        StringBuffer sb = new StringBuffer();
		sb.append(productID); sb.append("_"); sb.append(catalogID); sb.append("_"); sb.append(tenantID);
		String itemName = sb.toString();
		
        deleteRequest.setItemName(itemName);
        deleteRequest.setDomainName(catalogDomain);
        try {
        	sdb.deleteAttributes(deleteRequest);
        }
        catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot delete productID " + productID + " from SimpleDB domain " + catalogDomain + " because of invalid parameter value";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
        catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + catalogDomain;
    		logger.error(errorMessage);
    		throw new Exception();
    	}
        catch (AttributeDoesNotExistException ex) {
    		String errorMessage = "Cannot delete one or more attributes from SimpleDB domain " + catalogDomain + " , productID " + productID;
    		logger.error(errorMessage);
    		throw new Exception();
    	}
        catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot write in SimpleDB, Amazon Service error (" + ase.getErrorType().toString() + ")";
    		logger.error(errorMessage);
    		throw new Exception();
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network.";
            logger.error(errorMessage);
    		throw new Exception();
        }
    	catch (Exception ex) {
    		String errorMessage = "Cannot delete from SimpleDB domain " + catalogDomain;
    		logger.error(errorMessage);
    		throw new Exception();
    	}
	}
	
	public boolean doesProductExist(String productID, String catalogID, String tenantID)  throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String catalogDomain = getCatalogName(catalogID, tenantID);
		GetAttributesRequest request = new GetAttributesRequest();
		request.setDomainName(catalogDomain);
		request.setItemName(productID);
		GetAttributesResult result = sdb.getAttributes(request);
		
		return result.getAttributes().size() == 0 ? false : true;
	}
	
	public Catalog2.Products.Product getProductByID(String productID, String catalogID, String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String catalogDomain = getCatalogName(catalogID, tenantID);
		GetAttributesRequest request = new GetAttributesRequest();
		request.setDomainName(catalogDomain);
		request.setItemName(productID);
		GetAttributesResult result = sdb.getAttributes(request);
		if (result.getAttributes().size() == 0) {
			return null;
		}
		
        Catalog2.Products.Product product = new Catalog2.Products.Product();
        product.setUid(productID);
        for (Attribute attribute : result.getAttributes()) {
        	if (attribute.getName().equals(NAME_STRING)) {
        		product.setName(attribute.getValue());
        	}
        	else if (attribute.getName().equals(LINK_STRING)) {
        		product.setLink(attribute.getValue());
        	}
        	else if (attribute.getName().equals(IMAGE_STRING)) {
        		product.setImage(attribute.getValue());
        	}
        	else if (attribute.getName().equals(PRICE_STRING)) {
        		if (attribute.getValue() != null) {
        			product.setPrice(BigDecimal.valueOf(Double.parseDouble(attribute.getValue())));
        		}
        	}
        	else if (attribute.getName().equals(CATEGORY_STRING)) {
        		product.setCategory(attribute.getValue());
        	}
        	else if (attribute.getName().equals(CATEGORYID_STRING)) {
        		if (attribute.getValue() != null) {
        			product.setCategoryId(BigInteger.valueOf(Integer.parseInt(attribute.getValue())));
        		}
        	}
        	else if (attribute.getName().equals(DESCRIPTION_STRING)) {
        		product.setDescription(attribute.getValue());
        	}
        	else if (attribute.getName().equals(WEIGHT_STRING)) {
        		if (attribute.getValue() != null) {
        			product.setWeight(BigInteger.valueOf(Integer.parseInt(attribute.getValue())));
        		}
        	}
        	else if (attribute.getName().equals(MANUFACTURER_STRING)) {
        		product.setManufacturer(attribute.getValue());
        	}
        	else if (attribute.getName().equals(MPN_STRING)) {
        		product.setMpn(attribute.getValue());
        	}
        	else if (attribute.getName().equals(SHIPPING_STRING)) {
        		if (attribute.getValue() != null) {
        			product.setShipping(BigDecimal.valueOf(Double.parseDouble(attribute.getValue())));
        		}
        	}
        	else if (attribute.getName().equals(AVAILABILITY_STRING)) {
        		product.setAvailability(attribute.getValue());
        	}
        	else if (attribute.getName().equals(INSTOCK_STRING)) {
        		product.setInstock(attribute.getValue());
        	}
        	else if (attribute.getName().equals(ISBN_STRING)) {
        		product.setIsbn(attribute.getValue());
        	}
        }
        return product;
	}
	
	public List<Catalog2.Products.Product> getAllProducts(String catalogID, String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
		String catalogDomain = getCatalogName(catalogID, tenantID);
    	String selectExpression = "select * from `" + catalogDomain + "` limit 2500";
    	String resultNextToken = null;
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        List<Catalog2.Products.Product> products = new LinkedList<Catalog2.Products.Product>();
        
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
		    
		    List<Item> items = selectResult.getItems();
	        for (Item item : items) {
	        	Catalog2.Products.Product product = new Catalog2.Products.Product();
	            for (Attribute attribute : item.getAttributes()) {
	            	if (attribute.getName().equals(UID_STRING)) {
	            		product.setUid(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(NAME_STRING)) {
	            		product.setName(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(LINK_STRING)) {
	            		product.setLink(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(IMAGE_STRING)) {
	            		product.setImage(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(PRICE_STRING)) {
	            		if (attribute.getValue() != null) {
	            			product.setPrice(BigDecimal.valueOf(Double.parseDouble(attribute.getValue())));
	            		}
	            	}
	            	else if (attribute.getName().equals(CATEGORY_STRING)) {
	            		product.setCategory(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(CATEGORYID_STRING)) {
	            		if (attribute.getValue() != null) {
	            			product.setCategoryId(BigInteger.valueOf(Integer.parseInt(attribute.getValue())));
	            		}
	            	}
	            	else if (attribute.getName().equals(DESCRIPTION_STRING)) {
	            		product.setDescription(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(WEIGHT_STRING)) {
	            		if (attribute.getValue() != null) {
	            			product.setWeight(BigInteger.valueOf(Integer.parseInt(attribute.getValue())));
	            		}
	            	}
	            	else if (attribute.getName().equals(MANUFACTURER_STRING)) {
	            		product.setManufacturer(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(MPN_STRING)) {
	            		product.setMpn(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(SHIPPING_STRING)) {
	            		if (attribute.getValue() != null) {
	            			product.setShipping(BigDecimal.valueOf(Double.parseDouble(attribute.getValue())));
	            		}
	            	}
	            	else if (attribute.getName().equals(AVAILABILITY_STRING)) {
	            		product.setAvailability(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(INSTOCK_STRING)) {
	            		product.setInstock(attribute.getValue());
	            	}
	            	else if (attribute.getName().equals(ISBN_STRING)) {
	            		product.setIsbn(attribute.getValue());
	            	}
	            }
	            
	            products.add(product);
	        }
	        
		    
        } while (resultNextToken != null);
        
        
        
        return products;
	}
	
	public void addProduct(Catalog2.Products.Product product, String catalogID, String tenantID) throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImpl.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
		ReplaceableAttribute attributeUID = new ReplaceableAttribute(UID_STRING, product.getUid(), true);
		attributes.add(attributeUID);
		
		String name = product.getName();
		if (name != null && !name.isEmpty() && name.getBytes("UTF-8").length < 1024) {
			ReplaceableAttribute attributeName = new ReplaceableAttribute(NAME_STRING, name, true);
			attributes.add(attributeName);
		}
		
		String link = product.getLink();
		if (link != null && !link.isEmpty() && link.getBytes("UTF-8").length < 1024 && isValidURL(link)) {
			ReplaceableAttribute attributeLink = new ReplaceableAttribute(LINK_STRING, link, true);
			attributes.add(attributeLink);
		}
		
		String image = product.getImage();
		if (image != null && !image.isEmpty() && image.getBytes("UTF-8").length < 1024 && isValidURL(image)) {
			ReplaceableAttribute attributeImage = new ReplaceableAttribute(IMAGE_STRING, image, true);
			attributes.add(attributeImage);
		}
		
		if (product.getPrice() != null) {
			ReplaceableAttribute attributePrice = new ReplaceableAttribute(PRICE_STRING, product.getPrice().toString(), true);
			attributes.add(attributePrice);
		}
		if (product.getCategory() != null) {
			ReplaceableAttribute attributeCategory = new ReplaceableAttribute(CATEGORY_STRING, product.getCategory(), true);
			attributes.add(attributeCategory);
		}
		if (product.getCategoryId() != null) {
			ReplaceableAttribute attributeCategoryId = new ReplaceableAttribute(CATEGORYID_STRING, product.getCategoryId().toString(), true);
			attributes.add(attributeCategoryId);
		}
		
		String description = product.getDescription();
		if (description != null && !description.isEmpty() && description.getBytes("UTF-8").length < 1024) {
			ReplaceableAttribute attributeDescription = new ReplaceableAttribute(DESCRIPTION_STRING, description, true);
			attributes.add(attributeDescription);
		}
		
		if (product.getWeight() != null) {
			ReplaceableAttribute attributeWeight = new ReplaceableAttribute(WEIGHT_STRING, product.getWeight().toString(), true);
			attributes.add(attributeWeight);
		}
		if (product.getManufacturer() != null) {
			ReplaceableAttribute attributeManufacturer = new ReplaceableAttribute(MANUFACTURER_STRING, product.getManufacturer(), true);
			attributes.add(attributeManufacturer);
		}
		if (product.getMpn() != null) {
			ReplaceableAttribute attributeMpn = new ReplaceableAttribute(MPN_STRING, product.getMpn(), true);
			attributes.add(attributeMpn);
		}
		if (product.getShipping() != null) {
			ReplaceableAttribute attributeShipping = new ReplaceableAttribute(SHIPPING_STRING, product.getShipping().toString(), true);
			attributes.add(attributeShipping);
		}
		if (product.getAvailability() != null) {
			ReplaceableAttribute attributeAvailability = new ReplaceableAttribute(AVAILABILITY_STRING, product.getAvailability(), true);
			attributes.add(attributeAvailability);
		}
		if (product.getInstock() != null) {
			ReplaceableAttribute attributeInstock = new ReplaceableAttribute(INSTOCK_STRING, product.getInstock(), true);
			attributes.add(attributeInstock);
		}
		if (product.getIsbn() != null) {
			ReplaceableAttribute attributeIsbn = new ReplaceableAttribute(ISBN_STRING, product.getIsbn(), true);
			attributes.add(attributeIsbn);
		}
		
		
		ReplaceableItem item = new ReplaceableItem();
		item.setName(product.getUid());
		item.setAttributes(attributes);
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		items.add(item);
		
		writeSimpleDB(sdb, getCatalogName(catalogID, tenantID), items);
	}
}
