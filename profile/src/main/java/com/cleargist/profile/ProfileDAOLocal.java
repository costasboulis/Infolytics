package com.cleargist.profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

/*
 * Uses the local memory to implement the profile DAO operations
 */

public class ProfileDAOLocal implements ProfileDAO {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private HashMap<String, Profile> profiles;
	
	public ProfileDAOLocal() {
		this.profiles = new HashMap<String, Profile>();
	}
	
	
	public void loadAllProfiles(String tenantID, String bucket, String key) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileDAOImplS3.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		S3Object profilesObject = s3.getObject(bucket, key);
		BufferedReader reader = new BufferedReader(new InputStreamReader(profilesObject.getObjectContent()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			Profile profile = new Profile(line);
			
			profiles.put(profile.getUserID(), profile);
		}
		reader.close();
	}
	
	public Profile getProfile(String tenantID, String profileID) throws Exception {
		return profiles.get(profileID);
	}
	
	public void writeProfile(Profile profile) throws Exception {
		this.profiles.put(profile.getUserID(), profile);
	}
	
	public List<Profile> getAllProfiles(String bucket, String key) throws Exception {
		List<Profile> profileList = new ArrayList<Profile>();
		for (Profile pr : profiles.values()) {
			profileList.add(pr);
		}
		return profileList;
	}
	
	public Profile getNextProfile() throws Exception {
		return new Profile();
	}
	
	public void initSequentialProfileRead(String bucket, String key) throws Exception {
		
	}
	
	public void closeSequentialProfileRead() throws Exception {
		
	}
	
	public void updateProfiles(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) throws Exception {
		
	}
	
	public void initProfiles(String tenantID) throws Exception {
		
	}
	
}
