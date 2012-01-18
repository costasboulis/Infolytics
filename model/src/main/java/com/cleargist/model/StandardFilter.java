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
	
	public List<String> applyFiltering(List<String> unfilteredIds, String tenantID) {
		List<String> finalList = new ArrayList<String>();
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
    			if (product.getInstock().equals("N") && !this.allowOutOfStock) {
					continue;
				}
    			if (finalList.size() != 0 && !product.getCategory().equals(category) && this.showOnlyFromSameCategory) {
    				continue;
    			}
    			finalList.add(uid);
    			if (finalList.size() == 1) {
    				category = product.getCategory();
    			}
    			
    			if (finalList.size() >= this.getNumRecs()) {
    				break;
    			}
    		}
    	}
    	
    	return finalList;
	}
}
