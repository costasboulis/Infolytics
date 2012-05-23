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
	public void loadAllProfiles(String tenantID, String bucket, String key) throws Exception;
	
	public Profile getProfile(String tenantID, String profileID) throws Exception;
	
	/**
	 * * Merges the new profiles (incremental / decremental) with the existing ones
	 * 
	 * @param tenantID
	 * @param incrementalProfiles
	 * @param decrementalProfiles
	 * @throws Exception
	 */
	public void updateProfiles(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) throws Exception;
	
	public void initProfiles(String tenantID) throws Exception;
}
