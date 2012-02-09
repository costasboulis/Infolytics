package com.cleargist.recommendations.util;


import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpledb.model.DuplicateItemNameException;
import com.amazonaws.services.simpledb.model.InvalidParameterValueException;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.recommendations.dao.RecommendationsDAO;
import com.cleargist.recommendations.entity.CatalogStatus;
import com.cleargist.recommendations.entity.Tenant;


/**
 * 
 */
public class ProcessCatalogJob extends QuartzJobBean {

	private int timeout;
	private RecommendationsDAO recDao;
	private CatalogDAO catDao;
	private static final String bucketName = "cleargist";
	private static final String FULLSIZE_SUFFIX = ".xml";
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setCatalogDAO(CatalogDAO catDao) {
		this.catDao = catDao;
	}

	public RecommendationsDAO getRecDao() {
		return recDao;
	}

	public void setRecDao(RecommendationsDAO recDao) {
		this.recDao = recDao;
	}

	public CatalogDAO getCatDao() {
		return catDao;
	}

	public void setCatDao(CatalogDAO catDao) {
		this.catDao = catDao;
	}
	
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		// do the actual work
		List<Tenant> tenants = recDao.getTenantsToUploadCatalog();
		/*System.out.println("SIZE OF TENANTS IS "+ tenants.size());*/
		for (Tenant t : tenants) {
			try {
				catDao.insertCatalog(bucketName, t.getToken()+FULLSIZE_SUFFIX, null , Integer.toString(t.getToken()));
				t.setCatalogStatus(CatalogStatus.INSYNC);
				recDao.updateTenant(t);
			} catch (DuplicateItemNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (InvalidParameterValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (NumberDomainBytesExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (NumberSubmittedItemsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (NumberSubmittedAttributesExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (NumberDomainAttributesExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (NumberItemAttributesExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (NoSuchDomainException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (AmazonServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			} catch (AmazonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			/*} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("IOException");
				recDao.updateTenant(t);*/
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				t.setCatalogStatus(CatalogStatus.FAILED);
				t.setCatalogStatusMessage("We have encountered an internal error. Please try again later or contact us");
				recDao.updateTenant(t);
			}
		}
	}

}
