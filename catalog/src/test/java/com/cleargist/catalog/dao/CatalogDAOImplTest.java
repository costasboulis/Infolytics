package com.cleargist.catalog.dao;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.cleargist.catalog.dao.CatalogDAOImpl;
import com.cleargist.catalog.entity.jaxb.Catalog;

public class CatalogDAOImplTest {
    
    @Test
    public void createCatalogTest() {
    	// You need to first upload file sampleCatalog.xml to S3 under bucket "cleargist"
        CatalogDAOImpl catalog = new CatalogDAOImpl();
     
        try {
        	catalog.insertCatalog("cleargist", "sampleCatalog.xml", "", "TEST");
        }
        catch (Exception ex) {
        	assertTrue(false);
        }
        
        assertTrue(true);
    }
    
    @Test
    public void retrieveProductTest() {
    	CatalogDAOImpl catalog = new CatalogDAOImpl();
        
        try {
        	catalog.insertCatalog("cleargist", "sampleCatalog.xml", "", "TEST");
        }
        catch (Exception ex) {
        	assertTrue(false);
        }
        
        Catalog.Products.Product product = null;
        try {
        	product = catalog.getProductByID("322233", "", "TEST");
        }
        catch (Exception ex) {
        	assertTrue(false);
        }
        
        assertTrue(product.getUid().equals("322233"));
        assertTrue(product.getCategory().equals("ÁèëçôéêÜ > Extreme Sports"));
    }
    
    @Test
    public void deleteProductTest() {
    	CatalogDAOImpl catalog = new CatalogDAOImpl();
        
        try {
        	catalog.insertCatalog("cleargist", "sampleCatalog.xml", "", "TEST");
        }
        catch (Exception ex) {
        	assertTrue(false);
        }
        
        try {
        	catalog.deleteProduct("322233", "", "TEST");
        }
        catch (Exception ex) {
        	assertTrue(false);
        }
        
        Catalog.Products.Product product = null;
        try {
        	product = catalog.getProductByID("322233", "", "TEST");
        }
        catch (Exception ex) {
        	assertTrue(true);
        }
    }
    
}
