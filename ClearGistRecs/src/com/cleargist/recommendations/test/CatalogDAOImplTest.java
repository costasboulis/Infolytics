package com.cleargist.recommendations.test;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.cleargist.recommendations.dao.CatalogDAOImpl;
import com.cleargist.recommendations.entity.Catalog2;
import com.cleargist.recommendations.dao.CatalogDAO;

public class CatalogDAOImplTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	
	@After
	public void cleanUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImplTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.deleteDomain(new DeleteDomainRequest("CATALOG_test"));
	}
	
	@Test
	public void loadCatalog() throws Exception {
		createXMLCatalog();
		CatalogDAO catalog = new CatalogDAOImpl();
		catalog.insertCatalog("cleargist", "recipesTextsSample.xml", "", "test");
		
		Assert.assertTrue(true);
	}
	
	public void createXMLCatalog() throws Exception {
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				CatalogDAOImplTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		S3Object rawProfilesFile = s3.getObject("cleargist", "recipesTextsSample.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(rawProfilesFile.getObjectContent()));
		String line = null;
		
		CatalogDAO catalogWriter = new CatalogDAOImpl();
		Catalog2 catalog = new Catalog2();
		// Calendar conversions
		GregorianCalendar gc = new GregorianCalendar();
		Date date = new Date();
        gc.setTimeInMillis(date.getTime());
        DatatypeFactory df = DatatypeFactory.newInstance();
        
		catalog.setCreatedAt(df.newXMLGregorianCalendar(gc));
		catalog.setProducts(new Catalog2.Products());
		Catalog2.Products products = catalog.getProducts();
		List<Catalog2.Products.Product> productList = products.getProduct();
		while ((line = reader.readLine()) != null) {
			String[] topFields = line.split("\";\"");
			if (topFields.length != 2) {
				logger.warn("Skipping line " + line);
				continue;
			}
			topFields[0] = topFields[0].replaceAll("\"", "");
			topFields[1] = topFields[1].replaceAll("\"", "");
			
			Catalog2.Products.Product product = new Catalog2.Products.Product();
			product.setUid(topFields[0]);
			product.setName(topFields[0]);
			product.setLink(topFields[0]);
			product.setImage(topFields[0]);
			product.setPrice(new BigDecimal(0.0f));
			product.setCategory("FOOD");
			product.setDescription(topFields[1]);
			
			productList.add(product);
		}
		reader.close();
		
		catalogWriter.marshallCatalog(catalog, "cleargist", "catalog.xsd", "cleargist", "recipesTextsSample.xml", "test");
	}

    @Test
    public void createCatalogTest() {
        CatalogDAOImpl catalog = new CatalogDAOImpl();
     
        try {
        	catalog.insertCatalog("cleargist", "sampleCatalog.xml", "", "TEST");
        }
        catch (Exception ex) {
        	Assert.assertTrue(false);
        }
        
        Assert.assertTrue(true);
    }
    
    @Test
    public void retrieveProductTest() {
    	CatalogDAOImpl catalog = new CatalogDAOImpl();
        
        try {
        	catalog.insertCatalog("cleargist", "sampleCatalog.xml", "", "TEST");
        }
        catch (Exception ex) {
        	Assert.assertTrue(false);
        }
        
        Catalog2.Products.Product product = null;
        try {
        	product = catalog.getProductByID("322233", "", "TEST");
        }
        catch (Exception ex) {
        	Assert.assertTrue(false);
        }
        
        Assert.assertTrue(product.getUid().equals("322233"));
        String category = product.getCategory();
        String referenceCategory = "Αθλητικά > Extreme Sports";
        Assert.assertTrue(category.equals(referenceCategory));
    }
    
    @Test
    public void deleteProductTest() {
    	CatalogDAOImpl catalog = new CatalogDAOImpl();
        
        try {
        	catalog.insertCatalog("cleargist", "sampleCatalog.xml", "", "TEST");
        }
        catch (Exception ex) {
        	Assert.assertTrue(false);
        }
        
        try {
        	catalog.deleteProduct("322233", "", "TEST");
        }
        catch (Exception ex) {
        	Assert.assertTrue(false);
        }
        
        Catalog2.Products.Product product = null;
        try {
        	product = catalog.getProductByID("322233", "", "TEST");
        }
        catch (Exception ex) {
        	Assert.assertTrue(true);
        }
        
        Assert.assertTrue(true);
    }
    
}
