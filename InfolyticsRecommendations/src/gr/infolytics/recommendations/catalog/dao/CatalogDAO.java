package gr.infolytics.recommendations.catalog.dao;

import gr.infolytics.recommendations.catalog.entity.jaxb.Catalog;


import com.amazonaws.services.s3.model.S3Object;

public interface CatalogDAO {
	public void createCatalog(S3Object c, String catalogID, String tenantID) throws Exception;
	public Catalog.Products.Product getProductByID(String productID, String catalogID, String tenantID) throws Exception;
	public void deleteProduct(String productID, String catalogID, String tenantID) throws Exception;
	public void deleteCatalog(String catalogID, String tenantID) throws Exception;
}
