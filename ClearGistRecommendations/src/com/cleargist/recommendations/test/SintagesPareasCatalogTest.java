package com.cleargist.recommendations.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.cleargist.recommendations.dao.SintagesPareasCatalog;

public class SintagesPareasCatalogTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	
	@Before
	public void setUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImplTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.deleteDomain(new DeleteDomainRequest("CATALOG_test"));
		sdb.createDomain(new CreateDomainRequest("CATALOG_test"));
	}
	
	@After
	public void cleanUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				CatalogDAOImplTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.deleteDomain(new DeleteDomainRequest("CATALOG_test"));
	}
	
	@Test
	public void test() throws Exception {
		SintagesPareasCatalog catalog = new SintagesPareasCatalog();
		catalog.addProduct("http://sintagespareas.gr/sintages/tourta-jeans-pantelonaki.html", "test");
		
		Assert.assertTrue(true);
	}
}
