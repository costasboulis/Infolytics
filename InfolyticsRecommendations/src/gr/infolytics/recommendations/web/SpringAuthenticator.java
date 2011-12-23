package gr.infolytics.recommendations.web;


import gr.infolytics.recommendations.dao.RecommendationsDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


/**
 * This is the authentication implementation for the Recommendations app.  It implements the
 * Spring UserDetails service to retrieve user information from SimpleDB.
 */
public class SpringAuthenticator implements UserDetailsService {


	public SpringAuthenticator() throws Exception {
		super();
	}

	private RecommendationsDAO dao;
	
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		/*UserDetails details = dao.getUser(username);
		if (details == null) {
			throw new UsernameNotFoundException("Unknown user: "+username);
		}
		return details;*/
		return null;
	}

    @Autowired
    public void setRecommendationsLogDAO (RecommendationsDAO dao) {
    	this.dao = dao;
    }

}
