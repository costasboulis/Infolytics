package com.cleargist.recommendations.util;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.cleargist.recommendations.entity.Catalog2.Products.Product;

public interface Modelable {
	public void createModel(String tenantID) throws AmazonServiceException, AmazonClientException, Exception;
	
	public List<Product> getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public List<Product> getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception;
}
