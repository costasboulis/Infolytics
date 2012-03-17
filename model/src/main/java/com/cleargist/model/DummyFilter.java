package com.cleargist.model;

import java.util.ArrayList;
import java.util.List;

import com.cleargist.catalog.entity.jaxb.Catalog;

/**
 * DummyFilter is used in combination models where the intermediate models do not need to do catalog lookups. It is basically a pass-through model
 * without any catalog lookup and it is present to help comply with the Model interfaces
 * 
 * @author kboulis
 *
 */
public class DummyFilter implements Filter {
	
	public String getName() {
		return "DummyFilter";
	}
	
	public List<Catalog.Products.Product> applyFiltering(List<String> unfilteredIds, String tenantID) {
		List<Catalog.Products.Product> filteredProducts = new ArrayList<Catalog.Products.Product>();
		for (String uid : unfilteredIds) {
			Catalog.Products.Product product = new Catalog.Products.Product();
			product.setUid(uid);
			
			filteredProducts.add(product);
		}
		return filteredProducts;
	}

}
