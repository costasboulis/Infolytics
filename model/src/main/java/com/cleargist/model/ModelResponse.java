package com.cleargist.model;

import java.util.List;

import com.cleargist.catalog.entity.jaxb.Catalog;

public class ModelResponse {
	private boolean isPersonalized;
	private List<Catalog.Products.Product> products;
	
	public ModelResponse(List<Catalog.Products.Product> products, boolean isPersonalized) {
		this.isPersonalized = isPersonalized;
		this.products = products;
	}
	
	public List<Catalog.Products.Product> getProducts() {
		return this.products;
	}
	
	public boolean isPersonalized() {
		return this.isPersonalized;
	}
}
