package com.cleargist.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.catalog.dao.CatalogDAOImpl;
import com.cleargist.catalog.dao.CatalogDataThread;
import com.cleargist.catalog.entity.jaxb.Catalog;

public class StandardFilter implements Filter {
	private Logger logger = Logger.getLogger(getClass());
	private CatalogDAO catalog;
	private boolean showOnlyFromSameCategory;
	private int numRecs;
	private boolean allowOutOfStock;
	
	public StandardFilter() {
		this.catalog = new CatalogDAOImpl();
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
		List<Future<Catalog.Products.Product>> unfilteredList = new ArrayList<Future<Catalog.Products.Product>>();
		List<Catalog.Products.Product> finalList = new ArrayList<Catalog.Products.Product>();
    	String category = null;
    	int noOfThreads = unfilteredIds.size(); 

		ExecutorService pool = Executors.newFixedThreadPool(noOfThreads);
		
		for (String uid : unfilteredIds) {
			unfilteredList.add(pool.submit(new CatalogDataThread(tenantID, uid, catalog)));
		}
		
    	for (Future<Catalog.Products.Product> prodFuture : unfilteredList) {
    		
    		if (prodFuture != null) {
    			try {
    				String instock;

    				instock = prodFuture.get().getInstock() == null ? "Y" : prodFuture.get().getInstock();
    				if (instock.equals("N") && !this.allowOutOfStock) {
    					continue;
    				}

    				String productCategory = prodFuture.get().getCategory() == null ? "Category" : prodFuture.get().getCategory();
    				if (finalList.size() != 0 && !productCategory.equals(category) && this.showOnlyFromSameCategory) {
    					continue;
    				}
    				finalList.add(prodFuture.get());
    				if (finalList.size() == 1) {
    					category = productCategory;
    				}

    				if (finalList.size() >= this.getNumRecs()) {
    					break;
    				}
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (ExecutionException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	return finalList;
	}
}
