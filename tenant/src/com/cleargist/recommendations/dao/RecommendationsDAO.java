package com.cleargist.recommendations.dao;



import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.cleargist.recommendations.entity.Activity;
import com.cleargist.recommendations.entity.Catalog;
import com.cleargist.recommendations.entity.ErrorLog;
import com.cleargist.recommendations.entity.Tenant;
import com.cleargist.recommendations.entity.Widget;

public interface RecommendationsDAO {
	
	//Activity related methods
	public void saveActivity (List<ReplaceableItem> attributes, String tenantId);
	public List<Activity> getActivities (String itemId, String tenantId);
	
	//Error Log related methods
	public void saveErrorLog (List<ReplaceableItem> attributes);
	public List<ErrorLog> getErrorLog (String tenantId);
	
	//Tenant related methods
	/*public void saveTenant (List<ReplaceableItem> attributes);*/
	public List<Tenant> getTenants ();
	public List<Tenant> getTenantsToUploadCatalog ();
	public Tenant getTenantByUsername (String username);
	public Tenant getTenantByUsernameAndEmail (String username, String email);
	public Tenant getNextTenant ();
	public void saveTenant (Tenant tenant);
	public void updateTenant (Tenant tenant);
	public void updateTenantPassword (Tenant tenant);
	public Tenant getTenantById (String id);
	public void generateTenant (Tenant tenant);
	public Tenant verifyTenant (String username, String password);
	public void resetPass (Tenant tenant);
	
	//catalog related methods
	public long countItems(String tenantId);
	public long countItems(Map<String,String> condition, String tenantId);
	public List<Catalog> getCatalog(Map<String,String> condition, String sorting, String ordering, int start, int page, String tenantId);
	public List<Catalog> getCatalog(String sorting, String ordering, int start, int page, String tenantId);
	public List<Catalog> getCatalog(int start, int page, String tenantId);
	public void saveCatalog (List<ReplaceableItem> attributes, String tenantId);
	public void uploadCatalog (MultipartFile f, String token);
	public List<Catalog> getSampleItems(int limit, String token);
	
	//widget related methods
	public Widget getWidget (Tenant tenant, String id);
	public List<Widget> getWidgets (Tenant tenant);
	public Number countWidgets(Tenant tenant);
	public void addWidget(Widget widget);
	public void deleteWidget(Tenant tenant, String id);
	public void updateWidget(Widget widget);
}
