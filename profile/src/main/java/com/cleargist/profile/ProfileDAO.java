package com.cleargist.profile;

import java.util.List;

public interface ProfileDAO {

	public List<Profile> getAllProfiles(String bucket, String key) throws Exception;
	public void writeProfile(Profile profile) throws Exception;
	
	// Sequential access
	public void initSequentialProfileRead(String bucket, String key) throws Exception;
	
	public void closeSequentialProfileRead() throws Exception;
	
	public Profile getNextProfile() throws Exception;
	
	// Random-access
	public void loadAllProfiles(String bucket, String key) throws Exception;
	
	public Profile getProfile(String profileID) throws Exception;
	
}
