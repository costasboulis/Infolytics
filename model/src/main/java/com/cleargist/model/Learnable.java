package com.cleargist.model;

import java.util.List;

import com.amazonaws.services.simpledb.model.Item;

public interface Learnable {
	
	public void updateModel(String tenantID) throws Exception;
	
	public void calculateSufficientStatistics(String tenantID, List<Item> items, String name) throws Exception;
	
	public void mergeSufficientStatistics(String tenantID) throws Exception;
	
	public List<String> getRecommendedProducts(List<String> productIds, String tenantID, Filter filter) throws Exception;
	
	public List<String> getPersonalizedRecommendedProducts(String userId, String tenantID, Filter filter) throws Exception;
}
