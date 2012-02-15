package com.cleargist.catalog.dao;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;

public class FashionPlusCatalogTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	
	
	@Before
	public void setUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				FashionPlusCatalogTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.deleteDomain(new DeleteDomainRequest("CATALOG_test"));
		sdb.createDomain(new CreateDomainRequest("CATALOG_test"));
	}
	
	@After
	public void cleanUp() throws Exception {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				FashionPlusCatalogTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		sdb.deleteDomain(new DeleteDomainRequest("CATALOG_test"));
	}
	
	@Test
	public void test() throws Exception {
		FashionPlusCatalog catalog = new FashionPlusCatalog();
		catalog.addProduct("http://www.fashionplus.gr/proion/25932/La_Redoute__poukamisa__Tirante_toynik.html", "test");
		
		assertTrue(true);
	}
}
