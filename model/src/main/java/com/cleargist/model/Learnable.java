package com.cleargist.model;

import java.util.List;

public interface Learnable {
	
	public void updateModel(String tenantID) throws Exception;
	
	public void calculateSufficientStatistics(String tenantID, String token) throws Exception;
	
	public void mergeSufficientStatistics(String tenantID) throws Exception;
	
	public List<String> getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public List<String> getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception;
}
