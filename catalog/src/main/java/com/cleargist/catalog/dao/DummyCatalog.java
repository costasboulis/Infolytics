package com.cleargist.catalog.dao;


import com.cleargist.catalog.entity.jaxb.Catalog;

public class DummyCatalog implements CatalogDAO {
	
	public Catalog.Products.Product getProductByID(String productID, String catalogID, String tenantID) throws Exception {
		Catalog.Products.Product product = new Catalog.Products.Product();
		product.setUid(productID);
		
		return product;
	}
	
	public void deleteProduct(String productID, String catalogID, String tenantID) throws Exception {
		
	}
	
	public void deleteCatalog(String catalogID, String tenantID) throws Exception {
		
	}
	
	public void insertCatalog(String bucket, String catalogName, String catalogID, String tenantID) throws Exception {
		
	}
	
	public void appendCatalog(String bucket, String catalogName, String catalogID, String tenantID) throws Exception {
		
	}
}
