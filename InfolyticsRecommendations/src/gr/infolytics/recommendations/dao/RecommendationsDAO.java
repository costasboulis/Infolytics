package gr.infolytics.recommendations.dao;

import gr.infolytics.recommendations.entity.Activity;
import gr.infolytics.recommendations.entity.ErrorLog;

import java.util.List;

import com.amazonaws.services.simpledb.model.ReplaceableItem;

public interface RecommendationsDAO {
	
	//Activity related methods
	public void saveActivity (List<ReplaceableItem> attributes, String tenantId);
	public List<Activity> getActivities (String itemId, String tenantId);
	
	//Error Log related methods
	public void saveErrorLog (List<ReplaceableItem> attributes);
	public List<ErrorLog> getErrorLog (String tenantId);

}
