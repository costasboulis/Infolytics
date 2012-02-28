package com.cleargist.recommendations.dao;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.cleargist.recommendations.entity.Catalog2;

public class DummyCatalog implements CatalogDAO {
	
	public Catalog2.Products.Product getProductByID(String productID, String catalogID, String tenantID) throws Exception {
		Catalog2.Products.Product product = new Catalog2.Products.Product();
		product.setUid(productID);
		
		return product;
	}
	
	public boolean doesProductExist(String productID, String catalogID, String tenantID)  throws Exception {
		return true;
	}
	
	public void deleteProduct(String productID, String catalogID, String tenantID) throws Exception {
		
	}
	
	public void deleteCatalog(String catalogID, String tenantID) throws Exception {
		
	}
	
	public void insertCatalog(String bucket, String catalogName, String catalogID, String tenantID) throws Exception {
		
	}
	
	public void appendCatalog(String bucket, String catalogName, String catalogID, String tenantID) throws Exception {
		
	}
	
	public List<Catalog2.Products.Product> getAllProducts(String catalogID, String tenantID) throws Exception {
		return new LinkedList<Catalog2.Products.Product>();
	}
	
	public void marshallCatalog(Catalog2 catalog, String schemaBucketName, String schemaFilename, String bucketName, String filename, String tenantID) throws JAXBException, IOException, Exception {
		
	}
}
