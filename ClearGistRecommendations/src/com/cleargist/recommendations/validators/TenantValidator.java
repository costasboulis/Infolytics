package com.cleargist.recommendations.validators;

import org.springframework.validation.Errors;

import com.cleargist.recommendations.entity.Tenant;

public class TenantValidator {

	public void validate(Tenant tenant, Errors errors) {

		if (tenant.getFirstName() != null && tenant.getFirstName().equals(""))
			errors.rejectValue("first name", "is required");

		if (tenant.getLastName() != null && tenant.getLastName().equals(""))
			errors.rejectValue("last name", "is required");
		
		if (tenant.getEmail() != null && tenant.getEmail().equals(""))
			errors.rejectValue("email", "is required");
		
		if (tenant.getUrl() != null && tenant.getUrl().equals(""))
			errors.rejectValue("url", "is required");
		
		if (tenant.getUsername() != null && tenant.getUsername().equals(""))
			errors.rejectValue("username", "is required");
		
		if (tenant.getPassword() != null && tenant.getPassword().equals(""))
			errors.rejectValue("password", "is required");

	}
	
}
