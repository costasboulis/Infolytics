package com.cleargist.recommendations.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cleargist.recommendations.dao.CatalogDAO;
import com.cleargist.recommendations.dao.CatalogDAOImpl;
import com.cleargist.recommendations.entity.Catalog2.Products.Product;


public class PassThroughFilter implements Filter {
	private Logger logger = Logger.getLogger(getClass());
	private CatalogDAO catalog;
	
	public PassThroughFilter() {
		catalog = new CatalogDAOImpl();
	}
	
	public String getName() {
		return "PassThrough";
	}
	
	public List<Product> applyFiltering(List<String> unfilteredIds, String tenantID) {
		List<Product> filteredProducts = new ArrayList<Product>();
		Product product = null;
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
