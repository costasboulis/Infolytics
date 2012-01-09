package com.cleargist.catalog.dao;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpledb.model.DuplicateItemNameException;
import com.amazonaws.services.simpledb.model.InvalidParameterValueException;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.cleargist.catalog.entity.jaxb.Catalog;




public interface CatalogDAO {
	public void insertCatalog(String bucket, String catalogName, String catalogID, String tenantID) 
	throws JAXBException, DuplicateItemNameException, InvalidParameterValueException, 
	NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, NumberSubmittedAttributesExceededException, 
	NumberDomainAttributesExceededException, NumberItemAttributesExceededException, NoSuchDomainException, 
	AmazonServiceException, AmazonClientException, IOException, Exception;
	
	public void appendCatalog(String bucket, String catalogName, String catalogID, String tenantID) throws JAXBException, DuplicateItemNameException, InvalidParameterValueException, 
	NumberDomainBytesExceededException, NumberSubmittedItemsExceededException, NumberSubmittedAttributesExceededException, 
	NumberDomainAttributesExceededException, NumberItemAttributesExceededException, NoSuchDomainException, 
	AmazonServiceException, AmazonClientException, IOException, Exception;
	
	public Catalog.Products.Product getProductByID(String productID, String catalogID, String tenantID) throws Exception;
	
	public void deleteProduct(String productID, String catalogID, String tenantID) throws Exception;
	
	public void deleteCatalog(String catalogID, String tenantID) throws Exception;
	
}
