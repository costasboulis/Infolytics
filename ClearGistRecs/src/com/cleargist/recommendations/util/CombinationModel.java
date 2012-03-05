package com.cleargist.recommendations.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.cleargist.recommendations.entity.Catalog2.Products.Product;

public class CombinationModel extends BaseModel {
	private int TOP_N_SEMANTIC = 300;
	private int TOP_N_CORRELATIONS = 30;
	private SemanticModel semanticModel;
	private CorrelationsModel correlationsModel;
	private int topCorrelations;
	
	public CombinationModel() {
		this.correlationsModel = new CorrelationsModel();
		this.semanticModel = new SemanticModel();
		this.semanticModel.setTopCorrelations(TOP_N_SEMANTIC);
		this.correlationsModel.setTopCorrelations(TOP_N_CORRELATIONS);
		this.topCorrelations = 10;
	}
	
	public void setTopCorrelations(int n) {
		this.topCorrelations = n > 10 ? n : 10;
	}
	
	protected void calculateSufficientStatistics(String bucketName, String baseFilename, String tenantID) throws Exception {
		this.correlationsModel.calculateSufficientStatistics(bucketName, baseFilename, tenantID);
		this.semanticModel.calculateSufficientStatistics(bucketName, baseFilename, tenantID);
	}
	
	protected void mergeSufficientStatistics(String bucketName, String mergedStatsFilename, String tenantID) throws Exception {
		this.correlationsModel.mergeSufficientStatistics(bucketName, mergedStatsFilename, tenantID);
		this.semanticModel.mergeSufficientStatistics(bucketName, mergedStatsFilename, tenantID);
	}
	
	protected void estimateModelParameters(String bucketName, String filename, String tenantID) throws Exception {
		this.correlationsModel.estimateModelParameters(bucketName, filename, tenantID);
		this.semanticModel.estimateModelParameters(bucketName, filename, tenantID);
		
		swapModelDomainNames(this.correlationsModel.getDomainBasename(), tenantID);
		swapModelDomainNames(this.semanticModel.getDomainBasename(), tenantID);
	}
	
	protected String getDomainBasename() {
		return "MODEL_COMBINED_";
	}
	
	protected String getStatsBucketName(String tenantID) {
		return STATS_BASE_BUCKETNAME + "combined" + tenantID;
	}
	
	public List<Product> getRecommendedProductsInternal(List<String> productIDs, String tenantID, Filter filter) throws Exception {
		
		List<Product> semanticProducts = this.semanticModel.getRecommendedProductsInternal(productIDs, tenantID, filter);
		HashSet<String> semantics = new HashSet<String>();
		for (Product product : semanticProducts) {
			semantics.add(product.getUid());
		}
		
		List<Product> products = new LinkedList<Product>();
		List<Product> correlationProducts = this.correlationsModel.getRecommendedProductsInternal(productIDs, tenantID, filter);
		for (Product product : correlationProducts) {
			if (semantics.contains(product.getUid())) {
				products.add(product);
			}
			
			if (products.size() == this.topCorrelations) {
				break;
			}
		}
		
		return products;
	}
	
	public List<Product> getPersonalizedRecommendedProductsInternal(String userID, String tenantID, Filter filter) throws Exception {
		List<Product> semanticProducts = this.semanticModel.getPersonalizedRecommendedProductsInternal(userID, tenantID, filter);
		HashSet<String> semantics = new HashSet<String>();
		for (Product product : semanticProducts) {
			semantics.add(product.getUid());
		}
		
		List<Product> products = new LinkedList<Product>();
		List<Product> correlationProducts = this.correlationsModel.getPersonalizedRecommendedProductsInternal(userID, tenantID, filter);
		for (Product product : correlationProducts) {
			if (semantics.contains(product.getUid())) {
				products.add(product);
			}
			
			if (products.size() == this.topCorrelations) {
				break;
			}
		}
		
		return products;
	}
}
