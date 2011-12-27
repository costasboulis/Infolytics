package gr.infolytics.catalog.dao;

import static org.junit.Assert.assertTrue;
import gr.infolytics.catalog.entity.jaxb.Catalog;

import org.junit.Test;

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
