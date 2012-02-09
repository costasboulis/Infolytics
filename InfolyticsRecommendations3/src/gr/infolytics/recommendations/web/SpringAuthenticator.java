package gr.infolytics.recommendations.web;


import gr.infolytics.recommendations.dao.RecommendationsDAO;
import gr.infolytics.recommendations.entity.Tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


/**
 * This is the authentication implementation for the Recommendations app.  It implements the
 * Spring UserDetails service to retrieve user information from db.
 */
public class SpringAuthenticator implements UserDetailsService {


	public SpringAuthenticator() throws Exception {
		super();
	}

	private RecommendationsDAO dao;

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

		UserDetails user = null; 

		System.out.println("I AM IN ----------------");
		Tenant tenant = dao.getTenantByUsername(username);
		if (tenant == null) {
			throw new UsernameNotFoundException("Unknown user: "+username);
		}

		//user =  new User(tenant.getUsername(),tenant.getPassword().toLowerCase(),true,true,true,true,tenant.getAuthorities()); 
		return user;
	}

	@Autowired
	public void setRecommendationsDAO (RecommendationsDAO dao) {
		this.dao = dao;
	}

	/**   
	 ** Retrieves the correct ROLE type depending on the access level, where access level is an Integer.   
	 ** Basically, this interprets the access value whether it's for a regular user or admin.   
	 *** @param access an integer value representing the access of the user   
	 ** @return collection of granted authorities   */ 
	
	/*public Collection<GrantedAuthority> getAuthorities(Integer access) {
		// Create a list of grants for this user
		List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>(2);
		// All users are granted with ROLE_USER access
		// Therefore this user gets a ROLE_USER by default
		authList.add(new GrantedAuthorityImpl("ROLE_USER"));
		// Check if this user has admin access
		// We interpret Integer(1) as an admin user
		if ( access.compareTo(1) == 0) {
			// User has admin access
			authList.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
			}
		// Return list of granted authorities
		return authList; 
		} */
	}
