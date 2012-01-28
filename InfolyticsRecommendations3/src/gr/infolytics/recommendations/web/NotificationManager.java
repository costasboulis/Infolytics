package gr.infolytics.recommendations.web;

import gr.infolytics.recommendations.entity.Tenant;

public interface NotificationManager {

	public void sendConfirmationEmail(Tenant tenant);
	public void sendResetPassEmail(Tenant tenant);
	
}
