package com.cleargist.recommendations.entity;


import java.util.Calendar;
import java.util.Date;


/**
 * Maps to error log. 
 */
public class ErrorLog {

    private ErrorType errorType;
    private String message;
    private String tenant;
    private Date errorDate = Calendar.getInstance().getTime();
    
    
	public ErrorLog() {
		super();
	}


	public ErrorLog(ErrorType errorType, String message, String tenant,
			Date errorDate) {
		super();
		this.errorType = errorType;
		this.message = message;
		this.tenant = tenant;
		this.errorDate = errorDate;
	}


	public ErrorType getErrorType() {
		return errorType;
	}


	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getTenant() {
		return tenant;
	}


	public void setTenant(String tenant) {
		this.tenant = tenant;
	}


	public Date getErrorDate() {
		return errorDate;
	}


	public void setErrorDate(Date errorDate) {
		this.errorDate = errorDate;
	}


}
