package com.cleargist.catalog.dao;

import java.util.concurrent.Callable;

import com.cleargist.catalog.entity.jaxb.Catalog;



public class CatalogDataThread implements Callable<Catalog.Products.Product> {
	
	private CatalogDAO catalog;
	private String tenantID;
	private String uid;

	public CatalogDataThread(String tenantID, String uid, CatalogDAO catalog) {
		this.tenantID = tenantID;
		this.uid = uid;
		this.catalog = catalog;
	}
	

	@Override
	public Catalog.Products.Product call() throws Exception {
		Catalog.Products.Product product = catalog.getProductByID(uid, "", tenantID);
		return product;
	}
	

	public String getTenantID() {
		return tenantID;
	}


	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}
	
	
}
