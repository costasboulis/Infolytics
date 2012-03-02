package com.cleargist.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.catalog.dao.CatalogDAOImpl;
//import com.cleargist.catalog.dao.DummyCatalog;
import com.cleargist.catalog.entity.jaxb.Catalog;

public class PassThroughFilter implements Filter {
	private Logger logger = Logger.getLogger(getClass());
	private CatalogDAO catalog;
	
	public PassThroughFilter() {
		catalog = new CatalogDAOImpl();
	}
	
	public String getName() {
		return "PassThrough";
	}
	
	public List<Catalog.Products.Product> applyFiltering(List<String> unfilteredIds, String tenantID) {
		List<Catalog.Products.Product> filteredProducts = new ArrayList<Catalog.Products.Product>();
		Catalog.Products.Product product = null;
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
