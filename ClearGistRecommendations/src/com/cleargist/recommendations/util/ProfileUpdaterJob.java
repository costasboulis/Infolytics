	package com.cleargist.recommendations.util;
	
	import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.cleargist.recommendations.dao.RecommendationsDAO;
import com.cleargist.recommendations.entity.Tenant;
import com.cleargist.recommendations.entity.Widget;
	
	
	/**
	 * profile updater quartz job
	 */
	public class ProfileUpdaterJob extends QuartzJobBean {
	
		private int timeout;
		private RecommendationsDAO recDao;
	
		public int getTimeout() {
			return timeout;
		}
	
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}

		public RecommendationsDAO getRecDao() {
			return recDao;
		}
	
		public void setRecDao(RecommendationsDAO recDao) {
			this.recDao = recDao;
		}
	
		@Override
		protected void executeInternal(JobExecutionContext arg0)
				throws JobExecutionException {
			// do the actual work
			List<Tenant> tenants = recDao.getTenantsforProfileUpd();
			for (Tenant t : tenants) {
				List<Widget> widgets = recDao.getWidgets(t);
				for (Widget w : widgets) {
					recDao.runModelProfilerByWidget(w, t);
				}
			}
		}
	}
