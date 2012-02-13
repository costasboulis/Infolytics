package com.cleargist.model;

import java.util.List;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.cleargist.catalog.entity.jaxb.Catalog;

public interface Modelable {
	public void createModel(String tenantID) throws AmazonServiceException, AmazonClientException, Exception;
	
	public List<Catalog.Products.Product> getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public List<Catalog.Products.Product> getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception;
}
