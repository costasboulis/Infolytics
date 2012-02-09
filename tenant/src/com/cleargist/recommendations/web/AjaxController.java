package com.cleargist.recommendations.web;

import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Service;


/**
 * This class is a controller to handle the Ajax requests from the Recommendations UI.  
 * It leverages Direct Web Remoting (DWR) to simplify the Ajax coding.
 */
@Service
@RemoteProxy(name="AjaxController")
public class AjaxController {
	
	/*private RecommendationsDAO dao;
	
	@RemoteMethod
	public Entry getEntry (String entryId) {
		Entry entry = dao.getEntry(entryId);
		
		return entry;
	}*/
	
}
