package com.cleargist.recommendations.util;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.cleargist.recommendations.entity.Catalog2;

public interface Modelable {
	public void createModel(String tenantID) throws AmazonServiceException, AmazonClientException, Exception;
	
	public List<Catalog2.Products.Product> getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public List<Catalog2.Products.Product> getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception;
}
