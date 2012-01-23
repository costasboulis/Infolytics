package com.cleargist.model;

import java.util.List;

import com.cleargist.catalog.entity.jaxb.Catalog;

public interface Filter {
	public List<Catalog.Products.Product> applyFiltering(List<String> unfilteredIds, String tenantID);
}
