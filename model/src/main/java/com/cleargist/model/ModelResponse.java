package com.cleargist.model;

import java.io.Serializable;
import java.util.List;

import com.cleargist.catalog.entity.jaxb.Catalog;

public class ModelResponse implements Serializable {
	private static final long serialVersionUID = 1L;
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
