package com.cleargist.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.catalog.entity.jaxb.Catalog;

public class StandardFilter implements Filter {
	private Logger logger = Logger.getLogger(getClass());
	private CatalogDAO catalog;
	private boolean showOnlyFromSameCategory;
	private int numRecs;
	private boolean allowOutOfStock;
	
	public StandardFilter() {
		this.showOnlyFromSameCategory = false;
		this.numRecs = 10;
		this.allowOutOfStock = false;
	}
	
	public String getName() {
		return "Standard";
	}
	
	public void setShowOnlyFromSameCategory(boolean b) {
		this.showOnlyFromSameCategory = b;
	}
	
	public void setNumRecs(int r) {
		this.numRecs = r;
	}
	
	public void setAllowOutOfStock(boolean b) {
		this.allowOutOfStock = b;
	}
	
	public boolean showOnlyForSameCategory() {
		return this.showOnlyFromSameCategory;
	}
	
	public int getNumRecs() {
		return this.numRecs;
	}
	
	public boolean allowOutOfStock() {
		return this.allowOutOfStock;
	}
	
	public List<Catalog.Products.Product> applyFiltering(List<String> unfilteredIds, String tenantID) {
		List<Catalog.Products.Product> finalList = new ArrayList<Catalog.Products.Product>();
    	String category = null;
    	for (String uid : unfilteredIds) {
    		Catalog.Products.Product product = null;
    		try {
    			product = catalog.getProductByID(uid, "", tenantID);
    		}
    		catch (Exception ex) {
    			logger.error("Could not lookup product " + uid + " in tenant " + tenantID);
    			continue;
    		}
    		
    		if (product != null) {
    			String instock = product.getInstock() == null ? "Y" : product.getInstock();
    			if (instock.equals("N") && !this.allowOutOfStock) {
					continue;
				}
    			
    			String productCategory = product.getCategory() == null ? "Category" : product.getCategory();
    			if (finalList.size() != 0 && !productCategory.equals(category) && this.showOnlyFromSameCategory) {
    				continue;
    			}
    			finalList.add(product);
    			if (finalList.size() == 1) {
    				category = productCategory;
    			}
    			
    			if (finalList.size() >= this.getNumRecs()) {
    				break;
    			}
    		}
    	}
    	
    	return finalList;
	}
}
