package com.cleargist.recommendations.web;

import com.cleargist.recommendations.entity.Tenant;

public interface NotificationManager {

	public void sendConfirmationEmail(Tenant tenant);
	public void sendResetPassEmail(Tenant tenant);
	
}
