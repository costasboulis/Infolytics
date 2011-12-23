package gr.infolytics.recommendations.catalog.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.AttributeDoesNotExistException;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.DuplicateItemNameException;
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

import gr.infolytics.recommendations.catalog.entity.jaxb.Catalog;
import gr.infolytics.recommendations.util.Configuration;


public class CatalogDAOImpl implements CatalogDAO {
	private Logger logger = Logger.getLogger(getClass());
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
	private static AmazonSimpleDB sdb;
	static {
		AWSCredentials creds = new BasicAWSCredentials(getKey(), getSecret());
		sdb = new AmazonSimpleDBClient(creds);
	}

	
	private String getCatalogName(String catalogID, String tenantID) {
		return "CATALOG_" + catalogID + "_" + tenantID;
	}
	
	public void deleteCatalog(String catalogID, String tenantID) throws Exception {
        if (sdb == null) {
        	logger.error("Cannot crate SimpleDB client");
        	throw new Exception();
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
	
	public void createCatalog(S3Object catalogFile, String catalogID, String tenantID) throws Exception {
		// Validate that the S3Object exists and that you can unmarshall it
		BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(new InputStreamReader(catalogFile.getObjectContent()));
    	}
    	catch (Exception ex) {
    		logger.error("Cannot read catalog from S3 object");
    		throw new Exception();
    	}
		
		Unmarshaller unmarshaller = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance("gr.infolytics.recommendations.catalog.entity.jaxb");
    		unmarshaller = jaxbContext.createUnmarshaller();
    	}
    	catch (JAXBException ex) {
    		logger.error("Setting up unmarshalling failed");
    		throw new Exception();
    	}
    	
        
        Catalog catalog = null;
        try {
        	catalog = (Catalog)unmarshaller.unmarshal(reader);
        }
        catch (JAXBException ex) {
    		logger.error("Error while unmarshalling catalog " + catalogID + " for tenant " + tenantID);
    		throw new Exception();
    	}
        reader.close();
        logger.info("Catalog unmarshalled");
        
		// Delete existing Amazon SimpleDB domain, if it exists
        if (sdb == null) {
        	logger.error("Cannot crate SimpleDB client");
        	throw new Exception();
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
    	
    	
		// Create new Amazon SimpleDB domain
        try {
    		sdb.createDomain(new CreateDomainRequest(catalogDomain));
    	}
    	catch (AmazonServiceException ase) {
    		logger.error("Amazon Service Exception while creating new SimpleDB domain");
    		throw new Exception();
        }
    	catch (AmazonClientException ace) {
    		logger.error("Amazon Client Exception while creating new SimpleDB domain");
    		throw new Exception();
        }
    	
		// Persist the items in the SimpleDB
    	List<ReplaceableItem> recsPairs = new ArrayList<ReplaceableItem>();
    	for (Catalog.Products.Product product : catalog.getProducts().getProduct()) {
    		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
    		
    		String uid = product.getUid();
    		if (uid != null && !uid.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(UID_STRING, uid, true));
    		}
    		
    		String name = product.getName();
    		if (name != null && !name.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(NAME_STRING, name, true));
    		}
    		
    		String link = product.getLink();
    		if (link != null && !link.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(LINK_STRING, link, true));
    		}
    		
    		String image = product.getImage();
    		if (image != null && !image.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(IMAGE_STRING, image, true));
    		}
    		
    		String price = product.getPrice() != null ? product.getPrice().toPlainString() : null;
    		if (price != null && !price.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(PRICE_STRING, price, true));
    		}
    		
    		String category = product.getCategory();
    		if (category != null && !category.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(CATEGORY_STRING, category, true));
    		}
    		
    		String categoryId = product.getCategoryId() != null ? product.getCategoryId().toString() : null;
    		if (categoryId != null && !categoryId.isEmpty()) {
    			attributes.add(new ReplaceableAttribute(CATEGORYID_STRING, categoryId, true));
    		}
    		
    		String description = product.getDescription();
    		if (description != null && !description.isEmpty()) {
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
    		
    		
    		StringBuffer sb = new StringBuffer();
    		sb.append(uid); sb.append("_"); sb.append(catalogID); sb.append("_"); sb.append(tenantID);
    		String itemName = sb.toString();
    		
    		recsPairs.add(new ReplaceableItem(itemName).withAttributes(attributes));
    		
    		if (recsPairs.size() == 25) {
        		writeSimpleDB(sdb, catalogDomain, recsPairs);
            	recsPairs = new ArrayList<ReplaceableItem>();
        	}
    	}
    	if (recsPairs.size() > 0) {
    		writeSimpleDB(sdb, catalogDomain, recsPairs);
        }
	}
	
	
	private void writeSimpleDB(AmazonSimpleDB sdb, String SimpleDBDomain, List<ReplaceableItem> recsPairs) throws Exception {
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(SimpleDBDomain, recsPairs));
    	}
		catch (DuplicateItemNameException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because of duplicate item names";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
		catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because of invalid parameter value";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
		catch (NumberDomainBytesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of domain bytes exceeded";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
		catch (NumberSubmittedItemsExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of submitted items exceeded";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
		catch (NumberSubmittedAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of submitted attributes exceeded";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
		catch (NumberDomainAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of domain attributes exceeded";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
		catch (NumberItemAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SimpleDBDomain + " because max number of item attributes exceeded";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    	catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + SimpleDBDomain;
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
    		String errorMessage = "Cannot write to SimpleDB";
    		logger.error(errorMessage);
    		throw new Exception();
    	}
    }
	
	public void deleteProduct(String productID, String catalogID, String tenantID) throws Exception {
		if (sdb == null) {
        	logger.error("Cannot crate SimpleDB client");
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
	
	public Catalog.Products.Product getProductByID(String productID, String catalogID, String tenantID) throws Exception {
		if (sdb == null) {
        	logger.error("Cannot crate SimpleDB client");
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
        
        Item item = items.get(0);
        Catalog.Products.Product product = new Catalog.Products.Product();
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
        return product;
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
