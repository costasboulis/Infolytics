package com.cleargist.model;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.cleargist.catalog.entity.jaxb.Catalog;

public class CombinationsModelTest {

	@Test
	public void testModelCreation() throws Exception {
		CombinationModel model = new CombinationModel();
		
		model.createModel("104");
		
		assertTrue(true);
	}
	
	@Ignore
	@Test
	public void testRecommendations() {
		CombinationModel model = new CombinationModel();
		
		StandardFilter filter = new StandardFilter();
		List<String> productIDs = new LinkedList<String>();
		productIDs.add("11811");
		try {
			List<Catalog.Products.Product> recommendedProducts = model.getRecommendedProductsInternal(productIDs, "104", filter);
			assertTrue(recommendedProducts.size() > 0);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
	}
}
