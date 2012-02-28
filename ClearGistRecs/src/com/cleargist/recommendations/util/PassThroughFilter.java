package com.cleargist.recommendations.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cleargist.recommendations.dao.CatalogDAO;
import com.cleargist.recommendations.dao.DummyCatalog;
import com.cleargist.recommendations.entity.Catalog;
import com.cleargist.recommendations.entity.Catalog2;


public class PassThroughFilter implements Filter {
	private Logger logger = Logger.getLogger(getClass());
	private CatalogDAO catalog;
	
	public PassThroughFilter() {
		catalog = new DummyCatalog();
	}
	
	public String getName() {
		return "PassThrough";
	}
	
	public List<Catalog2.Products.Product> applyFiltering(List<String> unfilteredIds, String tenantID) {
		List<Catalog2.Products.Product> filteredProducts = new ArrayList<Catalog2.Products.Product>();
		Catalog2.Products.Product product = null;
		for (String uid : unfilteredIds) {
			try {
				product = catalog.getProductByID(uid, "", tenantID);
				filteredProducts.add(product);
			}
			catch (Exception ex) {
				logger.error("Could not lookup product " + uid + " in tenant " + tenantID);
				continue;
			}
		}
		
		return filteredProducts;
	}
}
