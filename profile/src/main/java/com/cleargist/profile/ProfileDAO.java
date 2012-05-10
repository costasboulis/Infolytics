package com.cleargist.profile;

import java.util.List;

public interface ProfileDAO {

	public List<Profile> getAllProfiles(String tenantID) throws Exception;
	
	public void initSequentialProfileRead(String tenantID) throws Exception;
	
	public void closeSequentialProfileRead() throws Exception;
	
	public Profile getNextProfile() throws Exception;
}
