package com.cleargist.model;

import static org.junit.Assert.assertTrue;


import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.catalog.dao.CatalogDAOImpl;
import com.cleargist.catalog.entity.jaxb.Catalog;

public class SemanticModelTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	
/*	
	@Before
	public void loadCatalog() throws Exception {
//		createXMLCatalog();
		CatalogDAO catalog = new CatalogDAOImpl();
//		catalog.insertCatalog("cleargist", "recipesTextsSample.xml", "", "test");
		catalog.insertCatalog("cleargist", "103.xml", "", "test");
	}
	*/
	/*
	public void createXMLCatalog() throws Exception {
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModelTest.class.getResourceAsStream(AWS_CREDENTIALS)));
//		S3Object rawProfilesFile = s3.getObject("sintagespareas", "recipesTexts.txt");
		S3Object rawProfilesFile = s3.getObject("cleargist", "recipesTextsSample.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(rawProfilesFile.getObjectContent()));
		String line = null;
		
		CatalogDAO catalogWriter = new CatalogDAOImpl();
		Catalog catalog = new Catalog();
		catalog.setProducts(new Catalog.Products());
		Catalog.Products products = catalog.getProducts();
		List<Catalog.Products.Product> productList = products.getProduct();
		while ((line = reader.readLine()) != null) {
			String[] topFields = line.split("\";\"");
			if (topFields.length != 2) {
				logger.warn("Skipping line " + line);
				continue;
			}
			topFields[0] = topFields[0].replaceAll("\"", "");
			topFields[1] = topFields[1].replaceAll("\"", "");
			
			Catalog.Products.Product product = new Catalog.Products.Product();
			product.setUid(topFields[0]);
			product.setName(topFields[0]);
			product.setLink(topFields[0]);
			product.setImage(topFields[0]);
			product.setPrice(new BigDecimal(0.0f));
			product.setCategory("FOOD");
			String choppedDescription = topFields[1].length() > 200 ? topFields[1].substring(0, 200) : topFields[1];
			product.setDescription(choppedDescription);
			
			productList.add(product);
		}
		reader.close();
		
		catalogWriter.marshallCatalog(catalog, "cleargist", "catalog.xsd", "cleargist", "recipesTextsSample.xml", "test");
	}
	*/
	
	
	@After
	public void cleanUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				SemanticModelTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		sdb.deleteDomain(new DeleteDomainRequest("MODEL_SEMANTIC_test_A"));
		sdb.deleteDomain(new DeleteDomainRequest("MODEL_SEMANTIC_test_B"));
		sdb.deleteDomain(new DeleteDomainRequest("MODEL_SEMANTIC_test"));
		sdb.deleteDomain(new DeleteDomainRequest("CATALOG_test"));
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				SemanticModelTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String bucketName = "profilessemanticmodeltest";
		if (s3.doesBucketExist(bucketName)) {
			List<S3ObjectSummary> objSummaries = s3.listObjects(bucketName).getObjectSummaries();
			int i = 0;
	    	while (i < objSummaries.size()) {
	    		s3.deleteObject(bucketName, objSummaries.get(i).getKey());
	    		i ++;
	    	}
	    	s3.deleteBucket(bucketName);
		}
		
		bucketName = "tmpstatssemantictest";
		if (s3.doesBucketExist(bucketName)) {
			List<S3ObjectSummary> objSummaries = s3.listObjects(bucketName).getObjectSummaries();
			int i = 0;
	    	while (i < objSummaries.size()) {
	    		s3.deleteObject(bucketName, objSummaries.get(i).getKey());
	    		i ++;
	    	}
	    	s3.deleteBucket(bucketName);
		}
		
	}
	
	@Test
	public void testCorrelations() {
		SemanticModel model = new SemanticModel();
		/*
		try {
			model.createModel("test");
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		*/
		StandardFilter filter = new StandardFilter();
		List<String> productIDs = new LinkedList<String>();
		productIDs.add("3156");
		try {
			List<Catalog.Products.Product> recommendedProducts = model.getRecommendedProductsInternal(productIDs, "103", filter);
			assertTrue(recommendedProducts.size() > 0);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		assertTrue(true);
	}
}
