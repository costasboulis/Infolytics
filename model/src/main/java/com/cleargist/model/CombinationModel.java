package com.cleargist.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.cleargist.catalog.entity.jaxb.Catalog;

public class CombinationModel extends BaseModel {
	private int TOP_N_SEMANTIC = 100;
	private int TOP_N_CORRELATIONS = 30;
	private SemanticModel semanticModel;
	private CorrelationsModel correlationsModel;

	
	public CombinationModel() {
		this.correlationsModel = new CorrelationsModel();
		this.semanticModel = new SemanticModel();
		this.semanticModel.setTopCorrelations(TOP_N_SEMANTIC);
		this.correlationsModel.setTopCorrelations(TOP_N_CORRELATIONS);
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
	
	public List<Catalog.Products.Product> getRecommendedProductsInternal(List<String> productIDs, String tenantID, Filter filter) throws Exception {
		
		Filter dummyFilter = new DummyFilter();
		List<Catalog.Products.Product> semanticProducts = this.semanticModel.getRecommendedProductsInternal(productIDs, tenantID, dummyFilter);
		HashSet<String> semantics = new HashSet<String>();
		for (Catalog.Products.Product product : semanticProducts) {
			semantics.add(product.getUid());
		}
		
		List<String> unfilteredProductIDs = new LinkedList<String>();
		List<Catalog.Products.Product> correlationProducts = this.correlationsModel.getRecommendedProductsInternal(productIDs, tenantID, dummyFilter);
		for (Catalog.Products.Product product : correlationProducts) {
			if (semantics.contains(product.getUid())) {
				unfilteredProductIDs.add(product.getUid());
			}
		}
		
		return filter.applyFiltering(unfilteredProductIDs, tenantID);
	}
	
	public List<Catalog.Products.Product> getPersonalizedRecommendedProductsInternal(String userID, String tenantID, Filter filter) throws Exception {
		
		Filter dummyFilter = new DummyFilter();
		List<Catalog.Products.Product> semanticProducts = this.semanticModel.getPersonalizedRecommendedProductsInternal(userID, tenantID, dummyFilter);
		HashSet<String> semantics = new HashSet<String>();
		for (Catalog.Products.Product product : semanticProducts) {
			semantics.add(product.getUid());
		}
		
		List<String> unfilteredProductIDs = new LinkedList<String>();
		List<Catalog.Products.Product> correlationProducts = this.correlationsModel.getPersonalizedRecommendedProductsInternal(userID, tenantID, dummyFilter);
		for (Catalog.Products.Product product : correlationProducts) {
			if (semantics.contains(product.getUid())) {
				unfilteredProductIDs.add(product.getUid());
			}
			
		}
		
		return filter.applyFiltering(unfilteredProductIDs, tenantID);
	}
}
