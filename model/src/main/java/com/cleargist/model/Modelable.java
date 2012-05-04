package com.cleargist.model;

import java.util.List;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public interface Modelable {
	public void createModel(String tenantID) throws AmazonServiceException, AmazonClientException, Exception;
	
	public ModelResponse getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public ModelResponse getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception;
}
