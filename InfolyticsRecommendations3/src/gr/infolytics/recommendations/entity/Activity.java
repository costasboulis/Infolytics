package gr.infolytics.recommendations.entity;


import java.util.Calendar;
import java.util.Date;


/**
 * Maps to user activity. 
 */
public class Activity {
	
	private String id;
	private ActivityEvent activityEvent;
    private String itemId;
    private String userId;
    private String session;
    private Date activityDate = Calendar.getInstance().getTime();
    
    
	public Activity() {
		super();
	}

	public Activity(ActivityEvent activityEvent, String itemId,
			String userId, String session, Date activityDate) {
		super();
		this.activityEvent = activityEvent;
		this.itemId = itemId;
		this.userId = userId;
		this.session = session;
		this.activityDate = activityDate;
	}
	
	public ActivityEvent getActivityEvent() {
		return activityEvent;
	}
	public void setActivityEvent(ActivityEvent activityEvent) {
		this.activityEvent = activityEvent;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getItemId() {
		return itemId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}
	public Date getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}

}
