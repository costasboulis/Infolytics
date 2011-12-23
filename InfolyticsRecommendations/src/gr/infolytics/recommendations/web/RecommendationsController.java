package gr.infolytics.recommendations.web;

import gr.infolytics.recommendations.dao.RecommendationsDAO;
import gr.infolytics.recommendations.entity.Activity;
import gr.infolytics.recommendations.entity.ActivityEvent;
import gr.infolytics.recommendations.entity.ErrorType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;


/**
 * This is the core of the Recommendations functionality.  It's a Spring controller implemented
 * using annotations.  Most methods for loading and storing journals, entries, comments and photos
 * are initiated in this class.
 */
@Controller
public class RecommendationsController {

	private RecommendationsDAO dao;
	private static final Logger logger=Logger.getLogger(RecommendationsController.class.getName());
	private String TENANT_TOKEN = "NONE";

	/**
	 * AWS Elastic Beanstalk checks your application's health by periodically
	 * sending an HTTP HEAD request to a resource in your application. By
	 * default, this is the root or default resource in your application,
	 * but can be configured for each environment.
	 *
	 * Here, we report success as long as the app server is up, but skip
	 * generating the whole page since this is a HEAD request only. You
	 * can employ more sophisticated health checks in your application.
	 *
	 * @param model the spring model for the request
	 */
	@RequestMapping(value="/home.do", method=RequestMethod.HEAD)
	public void doHealthCheck(HttpServletResponse response) {
		response.setContentLength(0);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * The main request handler that builds out the home page
	 * @param model the spring model for the request
	 */
	@RequestMapping(value="/home.do", method={RequestMethod.GET, RequestMethod.POST})
	public void doHome (HttpServletRequest request) {

		

	}

	/**
	 * The activity controller, which records the user's activities
	 * @param attributes that hold the activity data 
	 */
	@RequestMapping(value="/activity.do", method={RequestMethod.GET})
	public @ResponseBody String recordActivity (@RequestParam(value = "userId") String userId, @RequestParam(value="event") String event,
			@RequestParam(value = "session") String session, @RequestParam(value = "itemId") String itemId, @RequestParam(value = "customer") String customer) {

		//initialize activity data
		Date activityDateTime = Calendar.getInstance().getTime();
		Integer tenant = 0;
		String activityEvent = ActivityEvent.ITEM_PAGE.toString();
		//initialize failure data
		List<String> failures = new ArrayList<String>();
		//initialize list of simple db attributes
		List<ReplaceableItem> simpleDBItems = new ArrayList<ReplaceableItem>();

		
		//validate requests	- tenant
		try {
			tenant = Integer.parseInt(customer);
		} catch(NumberFormatException e) {
			failures.add("Tenant posted is not an int.");
		}
		
		//validate requests	- item
		if(itemId==null || itemId.equals(""))
			failures.add("Item not posted.");
				
		//validate requests	- event

		if(event.equals("PURCHASE")){
			activityEvent = ActivityEvent.PURCHASE.toString();
		}else if(event.equals("ITEM_PAGE")){
			activityEvent = ActivityEvent.ITEM_PAGE.toString();
		}else if(event.equals("ADD_TO_CART")){
			activityEvent = ActivityEvent.ADD_TO_CART.toString();
		}else if(event.equals("REC_SERVED")){
			activityEvent = ActivityEvent.REC_SERVED.toString();
		}else if(event.equals("REC_SEEN")){
			activityEvent = ActivityEvent.REC_SEEN.toString();
		}else if(event.equals("REC_CLICK")){
			activityEvent = ActivityEvent.REC_CLICK.toString();
		}else if(event.equals("HOME_PAGE_VIEW")){
			activityEvent = ActivityEvent.HOME_PAGE_VIEW.toString();
		}else if(event.equals("CATEGORY_PAGE_VIEW")){
			activityEvent = ActivityEvent.CATEGORY_PAGE_VIEW.toString();
		}else if(event.equals("SEARCH")){
			activityEvent = ActivityEvent.SEARCH.toString();
		}else if(event.equals("RATING")){
			activityEvent = ActivityEvent.ITEM_PAGE.toString();
		}else{
			failures.add("Event not posted");
		}

		//record activity or failures
		if(failures.size()==0){
			//initialize uuid and add to list of list of simple db activity attributes	
			UUID uuId = UUID.randomUUID();
			simpleDBItems.add(new ReplaceableItem().withName("act"+uuId).withAttributes(
					new ReplaceableAttribute().withName("EVENT").withValue(activityEvent),
					new ReplaceableAttribute().withName("ITEM").withValue(itemId),
					new ReplaceableAttribute().withName("USER").withValue(userId),
					new ReplaceableAttribute().withName("SESSION").withValue(session),
					new ReplaceableAttribute().withName("ACTDATE").withValue(activityDateTime.toString()))
					);
			dao.saveActivity(simpleDBItems, tenant.toString());
			//logger.log(Level.INFO,"--------------Activity Saved----------------");
		}else{
			//add to list of list of simple db error attributes
			for(String mes:failures){
				//initialize uuid and add to list of list of simple db error attributes
				UUID uuId = UUID.randomUUID();
				simpleDBItems.add(new ReplaceableItem().withName("error"+uuId).withAttributes(
						new ReplaceableAttribute().withName("ERRDATE").withValue(activityDateTime.toString()),
						new ReplaceableAttribute().withName("MESSAGE").withValue(mes),
						new ReplaceableAttribute().withName("TYPE").withValue(ErrorType.RECORD_ACTIVITY.toString()),
						new ReplaceableAttribute().withName("TENANT").withValue(tenant.toString()))
						);
			}

			dao.saveErrorLog(simpleDBItems);
			//logger.log(Level.INFO,"--------------Error Log Saved----------------");
		}
		return null;
	}

	@Autowired
	public void setRecommendationsDAO (RecommendationsDAO dao) {
		this.dao = dao;
	}

	/**
	 * Method establishes the transformation of incoming date strings into Date objects
	 * @param binder the spring databinder object that we connect to the date editor
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat.setLenient(true);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}


}
