package com.cleargist.recommendations.dao;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.cleargist.recommendations.aws.S3StorageManager;
import com.cleargist.recommendations.aws.StorageObject;
import com.cleargist.recommendations.entity.Activity;
import com.cleargist.recommendations.entity.ActivityEvent;
import com.cleargist.recommendations.entity.Authority;
import com.cleargist.recommendations.entity.Catalog;
import com.cleargist.recommendations.entity.Catalog2.Products.Product;
import com.cleargist.recommendations.entity.CatalogStatus;
import com.cleargist.recommendations.entity.ErrorLog;
import com.cleargist.recommendations.entity.ErrorType;
import com.cleargist.recommendations.entity.Metric;
import com.cleargist.recommendations.entity.StatisticalPeriod;
import com.cleargist.recommendations.entity.Tenant;
import com.cleargist.recommendations.entity.User;
import com.cleargist.recommendations.entity.UserRole;
import com.cleargist.recommendations.entity.Widget;
import com.cleargist.recommendations.entity.WidgetType;
import com.cleargist.recommendations.util.CombinationModel;
import com.cleargist.recommendations.util.Configuration;
import com.cleargist.recommendations.util.CorrelationsModel;
import com.cleargist.recommendations.util.Filter;
import com.cleargist.recommendations.util.PassThroughFilter;
import com.cleargist.recommendations.util.PurchaseProfileProcessor;
import com.cleargist.recommendations.util.SemanticModel;


@Repository("RecommendationsDAO")
public class RecommendationsDAOImpl implements RecommendationsDAO {

	/*
	 * The Simple DB client class is thread safe so we only ever need one static instance.
	 * While you can have multiple instances it is better to only have one because it's
	 * a relatively heavy weight class.
	 */
	private static AmazonSimpleDB sdb;
	static {
		AWSCredentials creds = new BasicAWSCredentials(getKey(), getSecret());
		sdb = new AmazonSimpleDBClient(creds);
	}
	
	private static final String bucketName = "cleargist";
	private static final String FULLSIZE_SUFFIX = ".xml";
	
	public RecommendationsDAOImpl(){}
	
	/*private static Map<String,String> propers = new HashMap<String,String>();
	static {
		propers.put("accessKey",getKey());
		propers.put("secretKey",getSecret());
	}
	private static EntityManagerFactoryImpl factory = new EntityManagerFactoryImpl("ClearGistRecommender", propers);*/

	@PersistenceContext
	private EntityManager entityManager;

	public EntityManager getEntityManager(){
		return entityManager;
	} 

	private DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	String simpleDBDomain;

	/**
	 * Catalog DAO Implementation
	 */
	
	public void uploadCatalog (MultipartFile f, String token) {
		
		StorageObject obj = new StorageObject();
		byte[] bytes = null;
		S3StorageManager mgr = new S3StorageManager();
		
		try {
			bytes = f.getBytes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		obj.setBucketName(bucketName);
		obj.setData(bytes);
		obj.setStoragePath(token+FULLSIZE_SUFFIX);
		mgr.storePublicRead(obj, true);
		
	}
	
	public long countItems(String tenantId) {
		long count = 0;
		simpleDBDomain = "CATALOG_" + tenantId;
		String selectExpression = "select count(*) from " + simpleDBDomain;
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		for (Item item : sdb.select(selectRequest).getItems()) {
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("Count"))
					count = Long.parseLong(attribute.getValue());
			}
		}

		return count;
	}

	public long countItems(Map<String, String> condition, String tenantId) {
		long count = 0;
		simpleDBDomain = "CATALOG_" + tenantId;
		String key = "NAME";
		String value = "";
		for (Map.Entry<String, String> entry : condition.entrySet()) { 
			key = entry.getKey();
			value = entry.getValue(); 
		} 

		String selectExpression = "select count(*) from " + simpleDBDomain + " where " + key + " like '%" + value + "%'";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		for (Item item : sdb.select(selectRequest).getItems()) {
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("Count"))
					count = Long.parseLong(attribute.getValue());
			}
		}

		return count;
	}

	public List<Catalog> getCatalog(Map<String, String> condition, String sorting,
			String ordering, int start, int page, String tenantId) {
		List<Catalog> catalogs = new ArrayList<Catalog>();
		simpleDBDomain = "CATALOG_" + tenantId;
		String nextToken = null;
		String key = "NAME";
		String value = "";
		for (Map.Entry<String, String> entry : condition.entrySet()) { 
			key = entry.getKey();
			value = entry.getValue(); 
		}

		if (start>1) {
			SelectRequest req = new SelectRequest("select count(*) from " + simpleDBDomain + " where " + sorting + " is not null and " + key + " like '%" + value + "%' order by " + sorting + " " + ordering + " limit "+start);
			SelectResult res = sdb.select(req);
			//List<Item> itemsList = res.getItems();
			nextToken = res.getNextToken();
		}
		String selectExpression = "select * from " + simpleDBDomain + " where " + sorting + " is not null and " + key + " like '%" + value + "%' order by " + sorting + " " + ordering + " limit 20";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		selectRequest.setNextToken(nextToken);

		for (Item item : sdb.select(selectRequest).getItems()) {
			Catalog catalog = new Catalog();

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("ID"))
					catalog.setId(attribute.getValue());
				if (attribute.getName().equals("UID"))
					catalog.setCustId(attribute.getValue());
				if (attribute.getName().equals("NAME"))
					catalog.setItem(attribute.getValue());
				if (attribute.getName().equals("CATEGORY"))
					catalog.setCategory(attribute.getValue());
				if (attribute.getName().equals("IMAGE"))
					catalog.setImage(attribute.getValue());
				if (attribute.getName().equals("INSTOCK")) {
					int stock = attribute.getValue().equals("N") ? 0 : 1; 
					catalog.setStock(stock);
				}
				if (attribute.getName().equals("LINK"))
					catalog.setUrl(attribute.getValue());
				if (attribute.getName().equals("DESCRIPTION"))
					catalog.setDescription(attribute.getValue());
				if (attribute.getName().equals("PRICE"))
					catalog.setPrice(new Double(attribute.getValue()));
				/*if (attribute.getName().equals("INSDATE")) {
					try {
						catalog.setDateInserted(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}*/
			}
			catalogs.add(catalog);
		}

		return catalogs;
	}

	public List<Catalog> getCatalog(String sorting, String ordering, int start,
			int page, String tenantId) {
		List<Catalog> catalogs = new ArrayList<Catalog>();
		simpleDBDomain = "CATALOG_" + tenantId;
		String nextToken = null;

		if (start>1) {
			SelectRequest req = new SelectRequest("select count(*) from " + simpleDBDomain + " where " + sorting + " is not null order by " + sorting + " " + ordering + " limit "+start);
			SelectResult res = sdb.select(req);
			//List<Item> itemsList = res.getItems();
			nextToken = res.getNextToken();
		}
		String selectExpression = "select * from " + simpleDBDomain + " where " + sorting + " is not null order by " + sorting + " " + ordering + " limit 20";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		selectRequest.setNextToken(nextToken);

		for (Item item : sdb.select(selectRequest).getItems()) {
			Catalog catalog = new Catalog();

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("ID"))
					catalog.setId(attribute.getValue());
				if (attribute.getName().equals("UID"))
					catalog.setCustId(attribute.getValue());
				if (attribute.getName().equals("NAME"))
					catalog.setItem(attribute.getValue());
				if (attribute.getName().equals("CATEGORY"))
					catalog.setCategory(attribute.getValue());
				if (attribute.getName().equals("IMAGE"))
					catalog.setImage(attribute.getValue());
				if (attribute.getName().equals("INSTOCK")) {
					int stock = attribute.getValue().equals("N") ? 0 : 1; 
					catalog.setStock(stock);
				}
				if (attribute.getName().equals("LINK"))
					catalog.setUrl(attribute.getValue());
				if (attribute.getName().equals("DESCRIPTION"))
					catalog.setDescription(attribute.getValue());
				if (attribute.getName().equals("PRICE"))
					catalog.setPrice(new Double(attribute.getValue()));
				/*if (attribute.getName().equals("INSDATE")) {
					try {
						catalog.setDateInserted(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}*/
			}
			catalogs.add(catalog);
		}

		return catalogs;
	}

	public List<Catalog> getCatalog(int start, int page, String tenantId) {
		List<Catalog> catalogs = new ArrayList<Catalog>();
		simpleDBDomain = "CATALOG_" + tenantId;
		String nextToken = null;
		if (start>1) {
			SelectRequest req = new SelectRequest("select count(*) from " + simpleDBDomain + " limit "+start);
			SelectResult res = sdb.select(req);
			//List<Item> itemsList = res.getItems();
			nextToken = res.getNextToken();
		}

		String selectExpression = "select * from " + simpleDBDomain+ " limit 20";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		selectRequest.setNextToken(nextToken);

		for (Item item : sdb.select(selectRequest).getItems()) {
			Catalog catalog = new Catalog();

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("ID"))
					catalog.setId(attribute.getValue());
				if (attribute.getName().equals("UID"))
					catalog.setCustId(attribute.getValue());
				if (attribute.getName().equals("NAME"))
					catalog.setItem(attribute.getValue());
				if (attribute.getName().equals("CATEGORY"))
					catalog.setCategory(attribute.getValue());
				if (attribute.getName().equals("IMAGE"))
					catalog.setImage(attribute.getValue());
				if (attribute.getName().equals("INSTOCK")) {
					int stock = attribute.getValue().equals("N") ? 0 : 1; 
					catalog.setStock(stock);
				}
				if (attribute.getName().equals("LINK"))
					catalog.setUrl(attribute.getValue());
				if (attribute.getName().equals("DESCRIPTION"))
					catalog.setDescription(attribute.getValue());
				if (attribute.getName().equals("PRICE"))
					catalog.setPrice(new Double(attribute.getValue()));
				/*if (attribute.getName().equals("INSDATE")) {
					try {
						catalog.setDateInserted(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}*/
			}
			catalogs.add(catalog);
		}

		return catalogs;
	}
	
	public List<Catalog> getSampleItems(int limit, String tenantId) {
		List<Catalog> catalogs = new ArrayList<Catalog>();
		simpleDBDomain = "CATALOG_" + tenantId;

		String selectExpression = "select * from " + simpleDBDomain + " limit " + limit;
		SelectRequest selectRequest = new SelectRequest(selectExpression);

		for (Item item : sdb.select(selectRequest).getItems()) {
			Catalog catalog = new Catalog();

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("ID"))
					catalog.setId(attribute.getValue());
				if (attribute.getName().equals("UID"))
					catalog.setCustId(attribute.getValue());
				if (attribute.getName().equals("NAME"))
					catalog.setItem(attribute.getValue());
				if (attribute.getName().equals("CATEGORY"))
					catalog.setCategory(attribute.getValue());
				if (attribute.getName().equals("IMAGE"))
					catalog.setImage(attribute.getValue());
				if (attribute.getName().equals("INSTOCK")) {
					int stock = attribute.getValue().equals("N") ? 0 : 1; 
					catalog.setStock(stock);
				}
				if (attribute.getName().equals("LINK"))
					catalog.setUrl(attribute.getValue());
				if (attribute.getName().equals("DESCRIPTION"))
					catalog.setDescription(attribute.getValue());
				if (attribute.getName().equals("PRICE"))
					catalog.setPrice(new Double(attribute.getValue()));
				/*if (attribute.getName().equals("INSDATE")) {
					try {
						catalog.setDateInserted(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}*/
			}
			catalogs.add(catalog);
		}

		return catalogs;
	}
	
	/**
	 * Tenant DAO Implementation
	 */
	@Transactional 
	public void saveTenant(Tenant tenant) {
		getEntityManager().persist(tenant);
		//persist spring security user
		User user = new User(tenant.getToken(), tenant.getUsername(), tenant.getPassword(), 0);
		UserRole userRole = new UserRole(tenant.getToken(), tenant.getToken(), Authority.ROLE_USER.toString());
		getEntityManager().persist(user);
		getEntityManager().persist(userRole);

	}

	@Transactional 
	public void updateTenantPassword(Tenant tenant) {
		getEntityManager().merge(tenant);
		//merge spring security user
		User user = getUserById(tenant.getToken());
		user.setPassword(tenant.getPassword());
		getEntityManager().merge(user);
	}

	@Transactional 
	public void updateTenant(Tenant tenant) {
		getEntityManager().merge(tenant);
	}

	/*public void saveTenant(List<ReplaceableItem> attributes) {
		simpleDBDomain = "TENANT";
		sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));
	}*/

	@Transactional(readOnly = true)
	public Tenant getNextTenant() {
		Tenant tenant = null;
		Query q = getEntityManager().createQuery("SELECT t FROM Tenant t ORDER BY t.token DESC").setMaxResults(1);
		try {
			return (Tenant) q.getSingleResult();
		} catch (NoResultException e) {
			return tenant;
		}
	}

	/*public Boolean checkTenantUsername(String username) {
		Boolean usernameExists = false;
		simpleDBDomain = "TENANT";
		String qry = "select * from " + simpleDBDomain + " where USER = '" + username + "'";
		SelectRequest selectRequest = new SelectRequest(qry);
		for (@SuppressWarnings("unused") Item item : sdb.select(selectRequest).getItems()) {
			//System.out.println("tenant Name: " + item.getName());
			usernameExists = true;
		}
		return usernameExists;
	}*/

	@Transactional(readOnly = true)
	public Tenant getTenantById (String tenantId) {
		Tenant tenant = null;
		Query q = getEntityManager().createQuery("SELECT t FROM Tenant t WHERE t.id = ?1");
		q.setParameter(1, tenantId);
		try {
			return (Tenant) q.getSingleResult();
		} catch (NoResultException e) {
			return tenant;
		}

	}

	@Transactional(readOnly = true)
	public User getUserById (int userId) {
		User user = null;
		Query q = getEntityManager().createQuery("SELECT u FROM User u WHERE u.id = ?1");
		q.setParameter(1, userId);
		try {
			return (User) q.getSingleResult();
		} catch (NoResultException e) {
			return user;
		}

	}

	@Transactional(readOnly = true)
	public Tenant getTenantByUsername(String username) {
		Tenant tenant = null;
		Query q = getEntityManager().createQuery("SELECT t FROM Tenant t WHERE t.username = ?1");
		q.setParameter(1, username);
		try {
			return (Tenant) q.getSingleResult();
		} catch (NoResultException e) {
			return tenant;
		}
	}

	@Transactional(readOnly = true)
	public Tenant getTenantByUsernameAndEmail(String username, String email) {
		Tenant tenant = null;
		Query q = getEntityManager().createQuery("SELECT t FROM Tenant t WHERE t.username = ?1 and t.email = ?2");
		q.setParameter(1, username);
		q.setParameter(2, email);
		try {
			return (Tenant) q.getSingleResult();
		} catch (NoResultException e) {
			return tenant;
		}
	}

	@Transactional(readOnly = true)
	public Tenant verifyTenant(String username, String password) {
		Tenant tenant = null;
		Query q = getEntityManager().createQuery("SELECT t FROM Tenant t WHERE t.username = ?1 and t.password = ?2");
		q.setParameter(1, username);
		q.setParameter(2, password);
		try {
			return (Tenant) q.getSingleResult();
		} catch (NoResultException e) {
			return tenant;
		}
	}

	@Transactional 
	public void generateTenant (Tenant tenant) {
		//create simpledb domains
		sdb.createDomain(new CreateDomainRequest("CATALOG_"+tenant.getToken()));
		sdb.createDomain(new CreateDomainRequest("ACTIVITY_"+tenant.getToken()));
		sdb.createDomain(new CreateDomainRequest("PROFILE_"+tenant.getToken()));
		/*sdb.createDomain(new CreateDomainRequest("WIDGET_"+tenant.getToken()));*/
		//initialize widget
		/*simpleDBDomain = "WIDGET_" + tenant.getToken();
		sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));*/
		
		//activate user
		tenant.setActive(1);
		getEntityManager().merge(tenant);

		// enable user - spring security
		Query q = getEntityManager().createQuery("SELECT u FROM User u WHERE u.id = ?1");
		q.setParameter(1, tenant.getToken());
		User user = (User) q.getSingleResult();
		user.setActive(1);
		getEntityManager().merge(user);

	}

	@Transactional 
	public void resetPass (Tenant tenant) {
		getEntityManager().merge(tenant);

		// update user - spring security
		Query q = getEntityManager().createQuery("SELECT u FROM User u WHERE u.id = ?1");
		q.setParameter(1, tenant.getToken());
		User user = (User) q.getSingleResult();
		user.setPassword(tenant.getPassword());
		getEntityManager().merge(user);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<Tenant> getTenantsToUploadCatalog() {

		Query q = getEntityManager().createQuery("SELECT t FROM Tenant t WHERE t.catalogStatus = ?1");
		q.setParameter(1, CatalogStatus.SYNCING);
		List<Tenant> tenants = q.getResultList();
		
		return tenants;
	}
	
	public List<Tenant> getTenants() {
		
		return null;
	}

	/**
	 * Catalog DAO Implementation
	 */
	public void saveCatalog(List<ReplaceableItem> attributes, String tenantId) {
		simpleDBDomain = "CATALOG_"+tenantId;
		sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));
	}
	
	/**
	 * User Widget DAO Implementation
	 */
	
	/*public void addWidget(List<ReplaceableItem> attributes, String token) {
		simpleDBDomain = "WIDGET_" + token;
		sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));
	}*/
	
	@Transactional 
	public void updateWidget(Widget widget) {
		getEntityManager().merge(widget);
	}
	
	@Transactional
	public void addWidget(Widget widget) {
		getEntityManager().persist(widget);
	}
	
	@Transactional
	public void deleteWidget(Tenant tenant, String id) {
		Query q= getEntityManager().createQuery("DELETE FROM Widget w WHERE w.tenant = ?1 and w.id = ?2");
		q.setParameter(1, tenant);
		q.setParameter(2, id);
		q.executeUpdate();
	}
	
	/*public void deleteWidget(String token, String id) {
		simpleDBDomain = "WIDGET_" + token;
		sdb.deleteAttributes(new DeleteAttributesRequest(simpleDBDomain, "wid"+id));
	}*/
	
	@Transactional(readOnly = true)
	public Number countWidgets(Tenant tenant) {
		Number count = 0;
		Query q= getEntityManager().createQuery("SELECT COUNT(w.id) FROM Widget w WHERE w.tenant = ?1");
		q.setParameter(1, tenant);
		count = (Number)q.getSingleResult();

		return count;
	}
	
	/*public long countWidgets(String tenantId) {
		long count = 0;
		simpleDBDomain = "WIDGET_" + tenantId;
		String selectExpression = "select count(*) from " + simpleDBDomain;
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		for (Item item : sdb.select(selectRequest).getItems()) {
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("Count"))
					count = Long.parseLong(attribute.getValue());
			}
		}

		return count;
	}*/
	
	/*public List<Widget> getWidgets(String token) {
		
		List<Widget> widgets = new ArrayList<Widget>();
		simpleDBDomain = "WIDGET_" + token;
		String selectExpression = "select * from " + simpleDBDomain;
		
		SelectRequest selectRequest = new SelectRequest(selectExpression);

		for (Item item : sdb.select(selectRequest).getItems()) {
			
			Widget widget = new Widget();
			
			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("ID"))
					widget.setId(attribute.getValue());
				if (attribute.getName().equals("NAME"))
					widget.setName(attribute.getValue());
				if (attribute.getName().equals("DESCRIPTION"))
					widget.setDescription(attribute.getValue());
				if (attribute.getName().equals("LAYOUTTYPE"))
					widget.setLayoutType(attribute.getValue());
				if (attribute.getName().equals("NOOFITEMS"))
					widget.setNoOfItems(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("IMAGESIZEWIDTH"))
					widget.setImageSizeWidth(attribute.getValue());
				if (attribute.getName().equals("IMAGESIZEHEIGHT"))
					widget.setImageSizeHeight(attribute.getValue());
				if (attribute.getName().equals("TEXTAREAWIDTH"))
					widget.setTextAreaWidth(attribute.getValue());
				if (attribute.getName().equals("SHOWHEADER"))
					widget.setShowHeader(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWIMAGES"))
					widget.setShowImages(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWCLEARGISTLOGO"))
					widget.setShowClearGistLogo(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("BORDERCOLOR"))
					widget.setBorderColor(attribute.getValue());
				if (attribute.getName().equals("BORDERWIDTH"))
					widget.setBorderWidth(attribute.getValue());
				if (attribute.getName().equals("HEADERBACK"))
					widget.setHeaderBack(attribute.getValue());
				if (attribute.getName().equals("HEADERBACKTRANS"))
					widget.setHeaderBackTrans(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("MAINBACK"))
					widget.setMainBack(attribute.getValue());
				if (attribute.getName().equals("MAINBACKTRANS"))
					widget.setMainBackTrans(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("FOOTERBACK"))
					widget.setFooterBack(attribute.getValue());
				if (attribute.getName().equals("FOOTERBACKTRANS"))
					widget.setFooterBackTrans(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("FONTFAMILY"))
					widget.setFontFamily(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTCOLOR"))
					widget.setHeaderTextColor(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXT"))
					widget.setHeaderText(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTSIZE"))
					widget.setHeaderTextSize(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTWEIGHT"))
					widget.setHeaderTextWeight(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTALIGN"))
					widget.setHeaderTextAlign(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTCOLOR"))
					widget.setNameTextColor(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTSIZE"))
					widget.setNameTextSize(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTWEIGHT"))
					widget.setNameTextWeight(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTALIGN"))
					widget.setNameTextAlign(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTCOLOR"))
					widget.setPriceTextColor(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTSIZE"))
					widget.setPriceTextSize(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTWEIGHT"))
					widget.setPriceTextWeight(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTALIGN"))
					widget.setPriceTextAlign(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTCOLOR"))
					widget.setCategoryTextColor(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTSIZE"))
					widget.setCategoryTextSize(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTWEIGHT"))
					widget.setCategoryTextWeight(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTALIGN"))
					widget.setCategoryTextAlign(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTCOLOR"))
					widget.setStockTextColor(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTSIZE"))
					widget.setStockTextSize(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTWEIGHT"))
					widget.setStockTextWeight(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTALIGN"))
					widget.setStockTextAlign(attribute.getValue());
				if (attribute.getName().equals("SHOWNAME"))
					widget.setShowName(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWPRICE"))
					widget.setShowPrice(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWCATEGORY"))
					widget.setShowCategory(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWSTOCK"))
					widget.setShowStock(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("DATEUPDATED"))
					try {
						widget.setDateUpdated(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				
			}
			widgets.add(widget);
		}
		return widgets;
	}*/
	
	@Transactional(readOnly = true)
	public List<Widget> getWidgets(Tenant tenant) {
		
		Query q = getEntityManager().createQuery("SELECT w FROM Widget w WHERE w.tenant = ?1 ORDER BY w.name");
		q.setParameter(1, tenant);
		List<Widget> widgets = q.getResultList();
		
		return widgets;
	}
	
	@Transactional(readOnly = true)
	public Widget getWidget(Tenant tenant, String id) {
		Widget widget = null;
		Query q = getEntityManager().createQuery("SELECT w FROM Widget w WHERE w.id = ?1 AND w.tenant = ?2");
		q.setParameter(1, id);
		q.setParameter(2, tenant);
		try {
			return (Widget) q.getSingleResult();
		} catch (NoResultException e) {
			return widget;
		}
	}
	

	/*public Widget getWidget(String token, String id) {
		
		Widget widget = new Widget();
		simpleDBDomain = "WIDGET_" + token;
		String selectExpression = "select * from " + simpleDBDomain + " where ID=" + id;

		SelectRequest selectRequest = new SelectRequest(selectExpression);

		for (Item item : sdb.select(selectRequest).getItems()) {

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("LAYOUTTYPE"))
					widget.setLayoutType(attribute.getValue());
				if (attribute.getName().equals("NOOFITEMS"))
					widget.setNoOfItems(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("IMAGESIZEWIDTH"))
					widget.setImageSizeWidth(attribute.getValue());
				if (attribute.getName().equals("IMAGESIZEHEIGHT"))
					widget.setImageSizeHeight(attribute.getValue());
				if (attribute.getName().equals("TEXTAREAWIDTH"))
					widget.setTextAreaWidth(attribute.getValue());
				if (attribute.getName().equals("SHOWHEADER"))
					widget.setShowHeader(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWIMAGES"))
					widget.setShowImages(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWCLEARGISTLOGO"))
					widget.setShowClearGistLogo(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("BORDERCOLOR"))
					widget.setBorderColor(attribute.getValue());
				if (attribute.getName().equals("BORDERWIDTH"))
					widget.setBorderWidth(attribute.getValue());
				if (attribute.getName().equals("HEADERBACK"))
					widget.setHeaderBack(attribute.getValue());
				if (attribute.getName().equals("HEADERBACKTRANS"))
					widget.setHeaderBackTrans(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("MAINBACK"))
					widget.setMainBack(attribute.getValue());
				if (attribute.getName().equals("MAINBACKTRANS"))
					widget.setMainBackTrans(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("FOOTERBACK"))
					widget.setFooterBack(attribute.getValue());
				if (attribute.getName().equals("FOOTERBACKTRANS"))
					widget.setFooterBackTrans(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("FONTFAMILY"))
					widget.setFontFamily(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTCOLOR"))
					widget.setHeaderTextColor(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXT"))
					widget.setHeaderText(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTSIZE"))
					widget.setHeaderTextSize(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTWEIGHT"))
					widget.setHeaderTextWeight(attribute.getValue());
				if (attribute.getName().equals("HEADERTEXTALIGN"))
					widget.setHeaderTextAlign(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTCOLOR"))
					widget.setNameTextColor(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTSIZE"))
					widget.setNameTextSize(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTWEIGHT"))
					widget.setNameTextWeight(attribute.getValue());
				if (attribute.getName().equals("NAMETEXTALIGN"))
					widget.setNameTextAlign(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTCOLOR"))
					widget.setPriceTextColor(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTSIZE"))
					widget.setPriceTextSize(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTWEIGHT"))
					widget.setPriceTextWeight(attribute.getValue());
				if (attribute.getName().equals("PRICETEXTALIGN"))
					widget.setPriceTextAlign(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTCOLOR"))
					widget.setCategoryTextColor(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTSIZE"))
					widget.setCategoryTextSize(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTWEIGHT"))
					widget.setCategoryTextWeight(attribute.getValue());
				if (attribute.getName().equals("CATEGORYTEXTALIGN"))
					widget.setCategoryTextAlign(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTCOLOR"))
					widget.setStockTextColor(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTSIZE"))
					widget.setStockTextSize(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTWEIGHT"))
					widget.setStockTextWeight(attribute.getValue());
				if (attribute.getName().equals("STOCKTEXTALIGN"))
					widget.setStockTextAlign(attribute.getValue());
				if (attribute.getName().equals("SHOWNAME"))
					widget.setShowName(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWPRICE"))
					widget.setShowPrice(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWCATEGORY"))
					widget.setShowCategory(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("SHOWSTOCK"))
					widget.setShowStock(Integer.valueOf(attribute.getValue()));
				if (attribute.getName().equals("DATEUPDATED"))
					try {
						widget.setDateUpdated(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
			}
		}
		return widget;
	}*/


	/**
	 * User Activity DAO Implementation
	 */
	public void saveActivity(List<ReplaceableItem> attributes, String tenantId) {
		simpleDBDomain = "ACTIVITY_"+tenantId;
		sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));
	}

	public List<Activity> getActivities(String itemId, String tenantId) {

		List<Activity> activities = new ArrayList<Activity>();
		simpleDBDomain = "ACTIVITY_"+tenantId;
		String selectExpression = itemId==null ? "select * from `" + simpleDBDomain : "select * from `" + simpleDBDomain + "` where PRODUCT = '" + itemId + "'";

		SelectRequest selectRequest = new SelectRequest(selectExpression);

		for (Item item : sdb.select(selectRequest).getItems()) {
			Activity activity = new Activity();

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("ID"))
					activity.setId(attribute.getValue());
				if (attribute.getName().equals("ITEM"))
					activity.setItemId(attribute.getValue());
				if (attribute.getName().equals("EVENT"))
					activity.setActivityEvent(ActivityEvent.valueOf(attribute.getValue()));
				if (attribute.getName().equals("USER"))
					activity.setUserId(attribute.getValue());
				if (attribute.getName().equals("SESSION"))
					activity.setSession(attribute.getValue());
				if (attribute.getName().equals("ACTDATE"))
					try {
						activity.setActivityDate(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}

				activities.add(activity);

			}
		}
		return activities;
	}

	/**
	 * Error Log DAO Implementation
	 */
	public void saveErrorLog(List<ReplaceableItem> attributes) {
		simpleDBDomain = "ERROR_LOG";
		sdb.batchPutAttributes(new BatchPutAttributesRequest(simpleDBDomain, attributes));
	}

	public List<ErrorLog> getErrorLog(String tenantId) {

		List<ErrorLog> errorLogs = new ArrayList<ErrorLog>();
		simpleDBDomain = "ERROR_LOG";
		String selectExpression = "select * from `" + simpleDBDomain;

		SelectRequest selectRequest = new SelectRequest(selectExpression);

		for (Item item : sdb.select(selectRequest).getItems()) {
			ErrorLog errorLog = new ErrorLog();

			for (Attribute attribute : item.getAttributes()) {
				if (attribute.getName().equals("TENANT"))
					errorLog.setTenant(attribute.getValue());
				if (attribute.getName().equals("TYPE"))
					errorLog.setErrorType(ErrorType.valueOf(attribute.getValue()));
				if (attribute.getName().equals("MESSAGE"))
					errorLog.setMessage(attribute.getValue());
				if (attribute.getName().equals("ERRDATE")) {
					try {
						errorLog.setErrorDate(df.parse(attribute.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}

				errorLogs.add(errorLog);

			}
		}
		return errorLogs;
	}


	public static String getKey () {
		Configuration config = Configuration.getInstance();
		return config.getProperty("accessKey");
	}

	public static String getSecret () {
		Configuration config = Configuration.getInstance();
		return config.getProperty("secretKey");
	}

	@Override
	public JSONArray getLast30Days() {
		SimpleDateFormat df = new SimpleDateFormat("dd");
		Date today = new Date();
		JSONArray ar1 = new JSONArray();
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		
		cal.add(Calendar.DAY_OF_MONTH, -30);
		Date today30 = cal.getTime();
		
		DateTime endDate = new DateTime(today30);

		for (int i=0; i < 30; i++) {
			DateTime d = endDate.withFieldAdded(DurationFieldType.days(), i);
			Date curDate = d.toDateMidnight().toDate();
		    ar1.put(df.format(curDate));
		}
		
		return ar1;
	}
	
	@Override
	public JSONArray getLastMonths(int noOfMonths) {
		SimpleDateFormat df = new SimpleDateFormat("MM/yy");
		Date today = new Date();
		JSONArray ar1 = new JSONArray();
		
		Calendar cal = new GregorianCalendar();
		//while (i<1) {
		//for (int i=noOfMonths; i < 30; i--) {
		for (int i=1; i < noOfMonths+1; i++) {
			cal.setTime(today);
			cal.add(Calendar.MONTH, -i);
			Date month = cal.getTime();
		    ar1.put(df.format(month));
		}
		
		return ar1;
	}

	@Override
	public JSONArray getActivityStats(Metric metric, StatisticalPeriod period, int token) {
		JSONArray ar1 = new JSONArray();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		simpleDBDomain = "ACTIVITY_" + token;
		double value = 0;
		double value1 = 0;
		double value2 = 0;
		Date today = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		
		if (period.toString().equals("LAST_30DAYS")) {
			
			cal.add(Calendar.DAY_OF_MONTH, -30);
			Date today30 = cal.getTime();
			DateTime endDate = new DateTime(today30);

			for (int i=0; i < 30; i++) {
				
				DateTime d = endDate.withFieldAdded(DurationFieldType.days(), i);
				Date curDate = d.toDateMidnight().toDate();
				cal.setTime(curDate);
				//get date before
				cal.add(Calendar.DAY_OF_MONTH, -1);
				Date oneDayBefore = cal.getTime();
				//get date after
				cal.add(Calendar.DAY_OF_MONTH, 2);
				Date oneDayAfter = cal.getTime();
				
				if (metric.toString()=="CTR") {
					
					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='REC_CLICK' AND ACTDATE > '" + df.format(oneDayBefore) + "' AND ACTDATE < '" + df.format(oneDayAfter) + "'";
					SelectRequest selectRequest = new SelectRequest(selectExpression);
					
					String selectExpression2 = "select count(*) from " + simpleDBDomain + " where EVENT='REC_SERVED' AND ACTDATE > '" + df.format(oneDayBefore) + "' AND ACTDATE < '" + df.format(oneDayAfter) + "'";
					SelectRequest selectRequest2 = new SelectRequest(selectExpression2);
			
					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							
							if (attribute.getName().equals("Count"))
								value1 = Double.parseDouble(attribute.getValue());
						}
					}
					
					for (Item item2 : sdb.select(selectRequest2).getItems()) {
						for (Attribute attribute2 : item2.getAttributes()) {
							
							if (attribute2.getName().equals("Count"))
								value2 = Double.parseDouble(attribute2.getValue());
						}
					}

					value = value2!=0.0 ? Math.round((value1 / value2)*100.0)/100.0 : 0;
					
				} else {

					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='" + metric.toString() + "' AND ACTDATE > '" + df.format(oneDayBefore) + "' AND ACTDATE < '" + df.format(oneDayAfter) + "'";
					SelectRequest selectRequest = new SelectRequest(selectExpression);
			
					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							if (attribute.getName().equals("Count"))
								value = Double.parseDouble(attribute.getValue());
						}
					}
				
				}
				
				try {
					ar1.put(value);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		
		} else if (period.toString().equals("LAST_6MONTHS") || period.toString().equals("LAST_YEAR")) {

			List<Integer> monthWithZeroAL = new ArrayList<Integer>();
			for (int i=1; i<10; i++) monthWithZeroAL.add(i);

			int noOfMonths = period.toString().equals("LAST_6MONTHS") ? 6 : 12;
			for (int i=1; i < noOfMonths+1; i++) {
				/*cal.add(Calendar.MONTH, -i);
				int month = cal.get(Calendar.MONTH)+1;
				boolean bMonth = monthWithZeroAL.contains(month);
				String strMonth = bMonth==true ? "0"+month : Integer.toString(month);
				int year = cal.get(Calendar.YEAR);
				int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				cal.setTime(today);
				
				String monthFromStr = year + "-" + strMonth + "-01";
				String monthToStr = year + "-" + strMonth + "-" + lastDay;*/
				
				//GET FROM MONTH
				int y = i+1;
				cal.add(Calendar.MONTH, -y);
				int month = cal.get(Calendar.MONTH)+1;
				boolean bMonth = monthWithZeroAL.contains(month);
				String strMonth = bMonth==true ? "0"+month : Integer.toString(month);
				int year = cal.get(Calendar.YEAR);
				int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				//GET TO MONTH
				cal.setTime(today);
				y = i-1;
				cal.add(Calendar.MONTH, -y);
				int month2 = cal.get(Calendar.MONTH)+1;
				boolean bMonth2 = monthWithZeroAL.contains(month2);
				String strMonth2 = bMonth2==true ? "0"+month2 : Integer.toString(month2);
				int year2 = cal.get(Calendar.YEAR);
				cal.setTime(today);
				
				String monthFromStr = year + "-" + strMonth + "-" + lastDay;
				String monthToStr = year2 + "-" + strMonth2 + "-01";
				
				
				if (metric.toString()=="CTR") {
					
					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='REC_CLICK' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
					SelectRequest selectRequest = new SelectRequest(selectExpression);
					
					String selectExpression2 = "select count(*) from " + simpleDBDomain + " where EVENT='REC_SERVED' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
					SelectRequest selectRequest2 = new SelectRequest(selectExpression2);
			
					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							
							if (attribute.getName().equals("Count"))
								value1 = Double.parseDouble(attribute.getValue());
						}
					}
					
					for (Item item2 : sdb.select(selectRequest2).getItems()) {
						for (Attribute attribute2 : item2.getAttributes()) {
							
							if (attribute2.getName().equals("Count"))
								value2 = Double.parseDouble(attribute2.getValue());
						}
					}
					
					value = value2!=0.0 ? Math.round((value1 / value2)*100.0)/100.0 : 0;
					
				} else {
				
					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='" + metric.toString() + "' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
			
					SelectRequest selectRequest = new SelectRequest(selectExpression);
			
					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							if (attribute.getName().equals("Count"))
								value = Double.parseDouble(attribute.getValue());
						}
					}

				}

				try {
					ar1.put(value);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		return ar1;
	}

	@Override
	public String getPerfOfMetrics(StatisticalPeriod period, int token) {
		
		String html = "<table width='100%'><tr><td width='60%'><table width='100%'><tr>";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		simpleDBDomain = "ACTIVITY_" + token;
		double value = 0;
		int intValue = 0;
		double totalVal = 0;
		double totalValComp = 0;
		double valueComp = 0;
		double value1 = 0;
		double value2 = 0;
		Metric[] metrics = Metric.values();
		Date today = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		int count = 0;
		List<Double> resListTotal = new ArrayList<Double>();
		List<Double> resListTotalComp = new ArrayList<Double>();
		String periodDesc = "";
		
		if (period.toString().equals("LAST_30DAYS")) {
			
			cal.add(Calendar.DAY_OF_MONTH, -30);
			Date today30 = cal.getTime();
			DateTime endDate = new DateTime(today30);

			for(Metric metric : metrics){
				
				List<Double> resList = new ArrayList<Double>();
				
				for (int i=0; i < 30; i++) {
					
					DateTime d = endDate.withFieldAdded(DurationFieldType.days(), i);
					Date curDate = d.toDateMidnight().toDate();
					cal.setTime(curDate);
					//get date before
					cal.add(Calendar.DAY_OF_MONTH, -1);
					Date oneDayBefore = cal.getTime();
					//get date after
					cal.add(Calendar.DAY_OF_MONTH, 2);
					Date oneDayAfter = cal.getTime();

					if (metric.toString()=="CTR") {
						
						String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='REC_CLICK' AND ACTDATE > '" + df.format(oneDayBefore) + "' AND ACTDATE < '" + df.format(oneDayAfter) + "'";
						SelectRequest selectRequest = new SelectRequest(selectExpression);
						
						String selectExpression2 = "select count(*) from " + simpleDBDomain + " where EVENT='REC_SERVED' AND ACTDATE > '" + df.format(oneDayBefore) + "' AND ACTDATE < '" + df.format(oneDayAfter) + "'";
						SelectRequest selectRequest2 = new SelectRequest(selectExpression2);
				
						for (Item item : sdb.select(selectRequest).getItems()) {
							for (Attribute attribute : item.getAttributes()) {
								
								if (attribute.getName().equals("Count"))
									value1 = Double.parseDouble(attribute.getValue());
							}
						}
						
						for (Item item2 : sdb.select(selectRequest2).getItems()) {
							for (Attribute attribute2 : item2.getAttributes()) {
								
								if (attribute2.getName().equals("Count"))
									value2 = Double.parseDouble(attribute2.getValue());
							}
						}
						
						value = value2!=0.0 ? Math.round((value1 / value2)*100.0)/100.0 : 0;
						
						resList.add(value);
						
					} else {
						
						String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='" + metric.toString() + "' AND ACTDATE > '" + df.format(oneDayBefore) + "' AND ACTDATE < '" + df.format(oneDayAfter) + "'";
						SelectRequest selectRequest = new SelectRequest(selectExpression);
	
						for (Item item : sdb.select(selectRequest).getItems()) {
							for (Attribute attribute : item.getAttributes()) {
								if (attribute.getName().equals("Count"))
									value = Double.parseDouble(attribute.getValue());
							}
						} 
	
						resList.add(value);
						resListTotal.add(value);
					}

				}
				
//				comparison
		
				cal.setTime(today);
				cal.add(Calendar.DAY_OF_MONTH, -60);
				Date fromDate60 = cal.getTime();
				cal.setTime(today);
				cal.add(Calendar.DAY_OF_MONTH, -30);
				Date toDate60 = cal.getTime();

				if (metric.toString()=="CTR") {
					
					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='REC_CLICK' AND ACTDATE > '" + df.format(fromDate60) + "' AND ACTDATE < '" + df.format(toDate60) + "'";
					SelectRequest selectRequest = new SelectRequest(selectExpression);
					
					String selectExpression2 = "select count(*) from " + simpleDBDomain + " where EVENT='REC_SERVED' AND ACTDATE > '" + df.format(fromDate60) + "' AND ACTDATE < '" + df.format(toDate60) + "'";
					SelectRequest selectRequest2 = new SelectRequest(selectExpression2);
			
					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							
							if (attribute.getName().equals("Count"))
								value1 = Double.parseDouble(attribute.getValue());
						}
					}
					
					for (Item item2 : sdb.select(selectRequest2).getItems()) {
						for (Attribute attribute2 : item2.getAttributes()) {
							
							if (attribute2.getName().equals("Count"))
								value2 = Double.parseDouble(attribute2.getValue());
						}
					}
					
					valueComp = value2!=0.0 ? Math.round((value1 / value2)*100.0)/100.0 : 0;
					
					
				} else {
					
					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='" + metric.toString() + "' AND ACTDATE > '" + df.format(fromDate60) + "' AND ACTDATE < '" + df.format(toDate60) + "'";
					SelectRequest selectRequest = new SelectRequest(selectExpression);

					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							if (attribute.getName().equals("Count"))
								valueComp = Double.parseDouble(attribute.getValue());
						}
					}
					
					resListTotalComp.add(valueComp);
					
				}
				
				
				value = metric.toString()=="CTR" ? avgOfList(resList) : sumOfList(resList);
				value = Math.round(value*100.0)/100.0;
				if (metric.toString()!="CTR") intValue = (int)value;
				String perc = metric.toString()=="CTR" ? "%" : "";
				periodDesc = "last month";
				String tr = count == 2 || count == 4 ? "</tr><tr>" : "";

				html += tr+
						"<td>" +
						"<table cellpadding='3' cellspacing='2'>" +
						"<tr>" +
						"<td width='20%' align='left'><span class='spanKeyValue'>";
				
				if (metric.toString()!="CTR")
					html += intValue;
				else
					html += value;
				
				html += perc+"</span></td>" +
						"<td><span class='spanKeyValueDesc'>"+metric.getShortDesc()+" "+periodDesc+"<br />";
						
				valueComp = Math.round(valueComp*100.0)/100.0;
				valueComp = valueComp!=0 ? Math.round(((value/valueComp)*100)-100) : value;
				
				String valueCompStr = valueComp<0 ? "<font color='#e42e21'>"+valueComp+"%</font>" : "<font color='#3cdd03'>+"+valueComp+"%</font>";
						
				html += valueCompStr+" this period</span></td>" +
						"</tr>" +
						"</table>" +
						"</td>";
				
				count ++;
			}
		
		} else if (period.toString().equals("LAST_6MONTHS") || period.toString().equals("LAST_YEAR")) {

			List<Integer> monthWithZeroAL = new ArrayList<Integer>();
			for (int i=1; i<10; i++) monthWithZeroAL.add(i);

			int noOfMonths = period.toString().equals("LAST_6MONTHS") ? 6 : 12;

			for(Metric metric : metrics){
				
				List<Double> resList = new ArrayList<Double>();
				
				for (int i=1; i < noOfMonths+1; i++) {
					//GET FROM MONTH
					int y = i+1;
					cal.add(Calendar.MONTH, -y);
					int month = cal.get(Calendar.MONTH)+1;
					boolean bMonth = monthWithZeroAL.contains(month);
					String strMonth = bMonth==true ? "0"+month : Integer.toString(month);
					int year = cal.get(Calendar.YEAR);
					int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
					//GET TO MONTH
					cal.setTime(today);
					y = i-1;
					cal.add(Calendar.MONTH, -y);
					int month2 = cal.get(Calendar.MONTH)+1;
					boolean bMonth2 = monthWithZeroAL.contains(month2);
					String strMonth2 = bMonth2==true ? "0"+month2 : Integer.toString(month2);
					int year2 = cal.get(Calendar.YEAR);
					cal.setTime(today);
					
					String monthFromStr = year + "-" + strMonth + "-" + lastDay;
					String monthToStr = year2 + "-" + strMonth2 + "-01";

					if (metric.toString()=="CTR") {
						
						String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='REC_CLICK' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
						SelectRequest selectRequest = new SelectRequest(selectExpression);
						
						String selectExpression2 = "select count(*) from " + simpleDBDomain + " where EVENT='REC_SERVED' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
						SelectRequest selectRequest2 = new SelectRequest(selectExpression2);
				
						for (Item item : sdb.select(selectRequest).getItems()) {
							for (Attribute attribute : item.getAttributes()) {
								
								if (attribute.getName().equals("Count"))
									value1 = Double.parseDouble(attribute.getValue());
							}
						}
						
						for (Item item2 : sdb.select(selectRequest2).getItems()) {
							for (Attribute attribute2 : item2.getAttributes()) {
								
								if (attribute2.getName().equals("Count"))
									value2 = Double.parseDouble(attribute2.getValue());
							}
						}
						
						value = value2!=0.0 ? Math.round((value1 / value2)*100.0)/100.0 : 0;
						
						resList.add(value);
						
					} else {
						
						String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='" + metric.toString() + "' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
						SelectRequest selectRequest = new SelectRequest(selectExpression);
	
						for (Item item : sdb.select(selectRequest).getItems()) {
							for (Attribute attribute : item.getAttributes()) {
								if (attribute.getName().equals("Count"))
									value = Double.parseDouble(attribute.getValue());
							}
						}
	
						resList.add(value);
						resListTotal.add(value);
					}

				}
				
				
//				comparison
				
				//get date from
				cal.setTime(today);
				if (period.toString().equals("LAST_6MONTHS")) cal.add(Calendar.YEAR, -1); else cal.add(Calendar.YEAR, -2);
				int month = cal.get(Calendar.MONTH);
				boolean bMonth = monthWithZeroAL.contains(month);
				String strMonth = bMonth==true ? "0"+month : Integer.toString(month);
				int year = cal.get(Calendar.YEAR);
				cal.add(Calendar.MONTH, -1);
				int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				
				//get date to
				cal.setTime(today);
				if (period.toString().equals("LAST_6MONTHS")) cal.add(Calendar.MONTH, -6); else cal.add(Calendar.YEAR, -1);
				int month2 = cal.get(Calendar.MONTH)+1;
				boolean bMonth2 = monthWithZeroAL.contains(month2);
				String strMonth2 = bMonth2==true ? "0"+month2 : Integer.toString(month2);
				int year2 = cal.get(Calendar.YEAR);
				/*int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);*/
				
				String monthFromStr = year + "-" + strMonth + "-" + lastDay;
				String monthToStr = year2 + "-" + strMonth2 + "-01";
				
				if (metric.toString()=="CTR") {
					
					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='REC_CLICK' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
					SelectRequest selectRequest = new SelectRequest(selectExpression);
					
					String selectExpression2 = "select count(*) from " + simpleDBDomain + " where EVENT='REC_SERVED' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
					SelectRequest selectRequest2 = new SelectRequest(selectExpression2);
			
					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							
							if (attribute.getName().equals("Count"))
								value1 = Double.parseDouble(attribute.getValue());
						}
					}
					
					for (Item item2 : sdb.select(selectRequest2).getItems()) {
						for (Attribute attribute2 : item2.getAttributes()) {
							
							if (attribute2.getName().equals("Count"))
								value2 = Double.parseDouble(attribute2.getValue());
						}
					}
					
					valueComp = value2!=0.0 ? Math.round((value1 / value2)*100.0)/100.0 : 0;
					
				} else {
					
					String selectExpression = "select count(*) from " + simpleDBDomain + " where EVENT='" + metric.toString() + "' AND ACTDATE > '" + monthFromStr + "' AND ACTDATE < '" + monthToStr + "'";
					SelectRequest selectRequest = new SelectRequest(selectExpression);

					for (Item item : sdb.select(selectRequest).getItems()) {
						for (Attribute attribute : item.getAttributes()) {
							if (attribute.getName().equals("Count"))
								valueComp = Double.parseDouble(attribute.getValue());
						}
					}
					
					resListTotalComp.add(valueComp);
					
				}
				
				value = metric.toString()=="CTR" ? avgOfList(resList) : sumOfList(resList);
				value = Math.round(value*100.0)/100.0;
				if (metric.toString()!="CTR") intValue = (int)value;
				String perc = metric.toString()=="CTR" ? "%" : "";
				periodDesc = period.toString().equals("LAST_6MONTHS") ? "last 6 months" : "last year";
				String tr = count == 2 || count == 4 ? "</tr><tr>" : "";
				
				html += tr+
						"<td>" +
						"<table cellpadding='3' cellspacing='2'>" +
						"<tr>" +
						"<td width='20%' align='left'><span class='spanKeyValue'>";
				
				if (metric.toString()!="CTR")
					html += intValue;
				else
					html += value;
				
				html += perc+"</span></td>" +
						"<td><span class='spanKeyValueDesc'>"+metric.getShortDesc()+" "+periodDesc+"<br />";
				
				valueComp = Math.round(valueComp*100.0)/100.0;
				valueComp = valueComp!=0 ? Math.round(((value/valueComp)*100)-100) : value;
				
				String valueCompStr = valueComp<0 ? "<font color='#e42e21'>"+valueComp+"%</font>" : "<font color='#3cdd03'>+"+valueComp+"%</font>";
						
				html += valueCompStr+" this period</span></td>" +
						"</tr>" +
						"</table>" +
						"</td>";
				
				count ++;
			}
		}
		
		//get totals and finish html edition
		totalVal = Math.round(sumOfList(resListTotal)*100.0)/100.0;
		totalValComp = Math.round(sumOfList(resListTotalComp)*100.0)/100.0;
		totalValComp = totalValComp!=0 ? Math.round(((totalVal/totalValComp)*100)-100) : totalVal;
		
		String totalValueCompStr = totalValComp<0 ? "<font color='#e42e21'>"+totalValComp+"%</font>" : "<font color='#3cdd03'>+"+totalValComp+"%</font>";
		String totalValueStr = totalValComp<0 ? "<font color='#e42e21'>"+totalVal+"</font>" : "<font color='#3cdd03'>"+totalVal+"</font>";
		
		html += "</tr></table>" +
				"<td width='40%'><table width='100%'><tr>";
		html +="<td valign='middle'>" +
				"<table cellpadding='3' cellspacing='2'>" +
				"<tr>" +
				"<td width='20%' align='left'><span class='spanKeyValueTotal'>"+totalValueStr+"</span></td>" +
				"<td><span class='spanKeyValueDescTotal'>total requests "+periodDesc+"<br />";
		html += totalValueCompStr+" this period</span></td>" +
				"</tr>" +
				"</table>" +
				"</td>";
		html += "</tr></table>";
				
		return html;
	}
	
	private Double sumOfList (List<Double> list) { 
		double sum= 0;  
		for (Double i:list) 
			sum = sum + i; 
		return sum; 
	} 

	private Double avgOfList (List<Double> list) { 
		double sum= 0;  
		double avg= 0;
		
		for (Double i:list) 
			sum = sum + i; 

		avg = sum/list.size();

		return avg; 
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<Tenant> getTenantsforProfileUpd() {

		Query q = getEntityManager().createQuery("SELECT t FROM Tenant t WHERE t.catalogStatus = ?1");
		q.setParameter(1, CatalogStatus.INSYNC);
		List<Tenant> tenants = q.getResultList();
		
		return tenants;
	}

	@Override
	public void runModelProfilerByWidget(Widget w, Tenant t) {
		CorrelationsModel corrModel = new CorrelationsModel();
		SemanticModel semaModel = new SemanticModel();
		CombinationModel combModel = new CombinationModel();
		PurchaseProfileProcessor profProc = new PurchaseProfileProcessor();
		
		try {
			if (w.getType() == WidgetType.Customers_who_bought_this_also_bought_that) {
				profProc.updateProfiles(Integer.toString(t.getToken()));
				semaModel.createModel(Integer.toString(t.getToken()));
			} else if (w.getType() == WidgetType.Customers_who_viewed_this_also_viewed_that) { 
				profProc.updateProfiles(Integer.toString(t.getToken()));
				semaModel.createModel(Integer.toString(t.getToken()));
			} else if (w.getType() == WidgetType.Most_popular_in_category) { 
				profProc.updateProfiles(Integer.toString(t.getToken()));
				semaModel.createModel(Integer.toString(t.getToken()));
			} else if (w.getType() == WidgetType.Most_popular_overall) { 
				profProc.updateProfiles(Integer.toString(t.getToken()));
				semaModel.createModel(Integer.toString(t.getToken()));
			} else if (w.getType() == WidgetType.Packaged_for_you) {
				profProc.updateProfiles(Integer.toString(t.getToken()));
				semaModel.createModel(Integer.toString(t.getToken()));
			} else if (w.getType() == WidgetType.Recommended_For_You) {
				profProc.updateProfiles(Integer.toString(t.getToken()));
				semaModel.createModel(Integer.toString(t.getToken()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Catalog> getRecommendationsByWidget(Widget w, Tenant t, List<String> itemIds) {
		CorrelationsModel corrModel = new CorrelationsModel();
		SemanticModel semaModel = new SemanticModel();
		CombinationModel combModel = new CombinationModel();
		List<Product> products = new ArrayList<Product>();
		List<Catalog> items = new ArrayList<Catalog>();
		Filter filter = new PassThroughFilter();
		try {
			if (w.getType() == WidgetType.Customers_who_bought_this_also_bought_that)
				products = semaModel.getRecommendedProducts(itemIds, Integer.toString(t.getToken()), filter);
			else if (w.getType() == WidgetType.Customers_who_viewed_this_also_viewed_that) 
				products = semaModel.getRecommendedProducts(itemIds, Integer.toString(t.getToken()), filter);
			else if (w.getType() == WidgetType.Most_popular_in_category) 
				products = semaModel.getRecommendedProducts(itemIds, Integer.toString(t.getToken()), filter);
			else if (w.getType() == WidgetType.Most_popular_overall)
				products = semaModel.getRecommendedProducts(itemIds, Integer.toString(t.getToken()), filter);
			else if (w.getType() == WidgetType.Packaged_for_you)
				products = semaModel.getRecommendedProducts(itemIds, Integer.toString(t.getToken()), filter);
			else if (w.getType() == WidgetType.Recommended_For_You) 
				products = semaModel.getRecommendedProducts(itemIds, Integer.toString(t.getToken()), filter);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Product p : products) {
			Catalog c = new Catalog();
			double price = new Double (p.getPrice().toString());
			
			c.setItem(p.getName());
			c.setUrl(p.getLink());
			c.setImage(p.getImage());
			c.setCategory(p.getCategory());
			c.setPrice(price);
			
			items.add(c);
		}
		
		
		return items;
	}
	
	@Transactional(readOnly = true)
	public void deleteTenant(Tenant tenant) {
		//delete tenant
		Query q1 = getEntityManager().createQuery("DELETE W FROM Widget w WHERE w.tenant = ?1");
		q1.setParameter(1, tenant).executeUpdate();
		
		//delete user roles
		Query q2 = getEntityManager().createQuery("DELETE ur FROM UserRole ur WHERE ur.userId = ?1");
		q2.setParameter(1, tenant.getToken()).executeUpdate();
		
		//delete user
		Query q3 = getEntityManager().createQuery("DELETE u FROM User u WHERE u.id = ?1");
		q3.setParameter(1, tenant.getToken()).executeUpdate();
				
		//delete tenant
		Query q4 = getEntityManager().createQuery("DELETE t FROM Tenant t WHERE t.id = ?1");
		q4.setParameter(1, tenant.getId()).executeUpdate();
		
		
		 String actDomain = "ACTIVITY_"+tenant.getToken();
		 String catDomain = "CATALOG_"+tenant.getToken();
		 String profDomain = "PROFILE_"+tenant.getToken();
		 String modelSemDomainA = "MODEL_SEMANTIC_"+tenant.getToken()+"_A";
		 String modelSemDomainB = "MODEL_SEMANTIC_"+tenant.getToken()+"_B";
		 String modelCorDomainA = "MODEL_CORRELATIONS_"+tenant.getToken()+"_A";
		 String modelCorDomainB = "MODEL_CORRELATIONS_"+tenant.getToken()+"_B";
		 
		for (String domain : sdb.listDomains().getDomainNames()) {
			if (domain.equals(actDomain))
				sdb.deleteDomain(new DeleteDomainRequest(actDomain));
			else if (domain.equals(catDomain))
				sdb.deleteDomain(new DeleteDomainRequest(catDomain));
			else if (domain.equals(profDomain))
				sdb.deleteDomain(new DeleteDomainRequest(profDomain));
			else if (domain.equals(modelSemDomainA))
				sdb.deleteDomain(new DeleteDomainRequest(modelSemDomainA));
			else if (domain.equals(modelSemDomainB))
				sdb.deleteDomain(new DeleteDomainRequest(modelSemDomainB));
			else if (domain.equals(modelCorDomainA))
				sdb.deleteDomain(new DeleteDomainRequest(modelCorDomainA));
			else if (domain.equals(modelCorDomainB))
				sdb.deleteDomain(new DeleteDomainRequest(modelCorDomainB));
		}
	    
	}

}
