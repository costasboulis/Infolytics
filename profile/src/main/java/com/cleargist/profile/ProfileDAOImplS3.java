package com.cleargist.profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

public class ProfileDAOImplS3 implements ProfileDAO {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	private BufferedReader reader;
	private S3Object profilesObject;
	
	private String getProfileBucketName(String tenantID) {
		return "profile" + tenantID;
	}
	
	private String getProfileKey(String tenantID) {
		return "profiles.txt";
	}
	
	public List<Profile> getAllProfiles(String tenantID) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileDAOImplS3.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String bucket = getProfileBucketName(tenantID);
		String key = getProfileKey(tenantID);
		S3Object profilesObject = s3.getObject(bucket, key);
		BufferedReader reader = new BufferedReader(new InputStreamReader(profilesObject.getObjectContent()));
		
		List<Profile> profiles = new LinkedList<Profile>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			Profile profile = new Profile(line);
			
			profiles.add(profile);
		}
		reader.close();
		
		return profiles;
	}
	
	public void initSequentialProfileRead(String tenantID) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileDAOImplS3.class.getResourceAsStream(AWS_CREDENTIALS)));
		String bucket = getProfileBucketName(tenantID);
		String key = getProfileKey(tenantID);
		this.profilesObject = s3.getObject(bucket, key);
		this.reader = new BufferedReader(new InputStreamReader(profilesObject.getObjectContent()));
	}
	
	
	public Profile getNextProfile() throws Exception {
		String line = this.reader.readLine();
		
		return line != null ? new Profile(line) : null;
		
	}
	
	public void closeSequentialProfileRead() throws Exception {
		this.reader.close();
	}
}
