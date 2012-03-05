package com.cleargist.recommendations.util;

import java.util.List;

import com.cleargist.recommendations.entity.Catalog2.Products.Product;

public interface Filter {
	public String getName();
	public List<Product> applyFiltering(List<String> unfilteredIds, String tenantID);
}
