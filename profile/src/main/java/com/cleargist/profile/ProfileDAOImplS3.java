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

/*
 * S3 store to be used for sequential access
 */
public class ProfileDAOImplS3 implements ProfileDAO {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	private BufferedReader reader;
	private S3Object profilesObject;
	
	
	public List<Profile> getAllProfiles(String bucket, String key) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileDAOImplS3.class.getResourceAsStream(AWS_CREDENTIALS)));
		
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
	
	public void initSequentialProfileRead(String bucket, String key) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileDAOImplS3.class.getResourceAsStream(AWS_CREDENTIALS)));
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
	
	public Profile getProfile(String profileID) throws Exception {
		return new Profile();
	}
	
	public void writeProfile(Profile profile) throws Exception {
		
	}
	
	public void loadAllProfiles(String bucket, String key) throws Exception {
		
	}
}
