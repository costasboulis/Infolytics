package com.cleargist.profile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;

/*
 * S3 store to be used for sequential access
 */
public class ProfileDAOImplS3 implements ProfileDAO {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	public static String newline = System.getProperty("line.separator");
	private final String PROFILE_BUCKETNAME = "profiles";
	private int maxNumberOfProfilesPerFile;
	private Logger logger = Logger.getLogger(getClass());
	private BufferedReader reader;
	private S3Object profilesObject;
	
	public void setMaxNumberOfProfilesPerFile(int maxNmbr) {
		this.maxNumberOfProfilesPerFile = maxNmbr;
	}
	
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
	
	public Profile getProfile(String tenantID, String profileID) throws Exception {
		return new Profile();
	}
	
	public void writeProfile(Profile profile) throws Exception {
		
	}
	
	public void loadAllProfiles(String tenantID, String bucket, String key) throws Exception {
		
	}
	
	private String getProfilesBucketName(String tenantID) {
		return PROFILE_BUCKETNAME + tenantID;
	}
	
	public void updateProfiles(String tenantID, List<Profile> incrementalProfilesList, List<Profile> decrementalProfilesList) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileDAOImplS3.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String profilesBucket = getProfilesBucketName(tenantID);
		if (!s3.doesBucketExist(profilesBucket)) {
			CreateBucketRequest createBucketRequest = new CreateBucketRequest(profilesBucket, Region.EU_Ireland);
			s3.createBucket(createBucketRequest);
		}
		
		HashMap<String, Profile> incrementalProfiles = new HashMap<String, Profile>();
		for (Profile profile : incrementalProfilesList) {
			String userID = profile.getUserID();
			incrementalProfiles.put(userID, profile);
		}
		
		HashMap<String, Profile> decrementalProfiles = new HashMap<String, Profile>();
		for (Profile profile : decrementalProfilesList) {
			String userID = profile.getUserID();
			decrementalProfiles.put(userID, profile);
		}
		
		int maxProfilesPerFile = maxNumberOfProfilesPerFile <= 0 ? Integer.MAX_VALUE : maxNumberOfProfilesPerFile;
		List<String> profileKeys = new LinkedList<String>();
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
		listObjectsRequest.setBucketName(profilesBucket);
		
		String marker = null;
		do {
			listObjectsRequest.setMarker(marker);
			ObjectListing listing = s3.listObjects(listObjectsRequest);
			for (S3ObjectSummary summary : listing.getObjectSummaries() ) {
				profileKeys.add(summary.getKey());
			}
			marker = listing.getNextMarker();
			
		} while (marker != null);
		
		String filename = "PROFILES_" + UUID.randomUUID().toString();
		File localFile = new File(filename);
		BufferedWriter writer = new BufferedWriter(new FileWriter(localFile));
		int linesWritten = 0;
		
		for (String key : profileKeys) {
			
			S3Object profile = s3.getObject(profilesBucket, key);
			BufferedReader reader = new BufferedReader(new InputStreamReader(profile.getObjectContent()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(";");
				String userId = fields[0];
				
				Profile incr = incrementalProfiles.get(userId);
				Profile decr = decrementalProfiles.get(userId);
				if (incr == null && decr == null) {                      // Profiles that haven't changed
					StringBuffer sb = new StringBuffer();
					sb.append(line); sb.append(newline);
					writer.write(line);
					writer.flush();
					linesWritten ++;
					if (linesWritten > maxProfilesPerFile) {
						// Copy the local file to S3
				    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);  
				    	r.setStorageClass(StorageClass.ReducedRedundancy);
				    	s3.putObject(r);
						linesWritten = 0;
						writer.close();
						boolean localFileDeleted = localFile.delete();
				    	if (!localFileDeleted) {
				    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
				    	}
						filename = "PROFILES_" + UUID.randomUUID().toString();
						localFile = new File(filename);
						writer = new BufferedWriter(new FileWriter(localFile));
					}
					
					continue;
				}
				
				Profile existing = new Profile(line);
				if (incr != null) {
					existing.merge(incr);
					incrementalProfiles.remove(userId);
				}
				if (decr != null) {
					existing.reduce(decr);
					if (existing.getAttributes().size() == 0) {  // Profile needs to be deleted
						continue;
					}
				}
				
				StringBuffer sb = new StringBuffer();
				sb.append(existing.toString()); sb.append(newline);
				writer.write(line);
				writer.flush();
				linesWritten ++;
				if (linesWritten > maxProfilesPerFile) {
					// Copy the local file to S3
					writer.close();
			    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);
			    	r.setStorageClass(StorageClass.ReducedRedundancy);
			    	s3.putObject(r);
					linesWritten = 0;
					boolean localFileDeleted = localFile.delete();
			    	if (!localFileDeleted) {
			    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
			    	}
					filename = "PROFILES_" + UUID.randomUUID().toString();
					localFile = new File(filename);
					writer = new BufferedWriter(new FileWriter(localFile));
				}
				
			}
			reader.close();
			
			s3.deleteObject(profilesBucket, key);
		}
		
		// Write the new users
		for (Profile newUserProfile : incrementalProfiles.values()) {
			StringBuffer sb = new StringBuffer();
			sb.append(newUserProfile.toString()); sb.append(newline);
			writer.write(sb.toString());
			writer.flush();
			linesWritten ++;
			if (linesWritten > maxProfilesPerFile) {
				// Copy the local file to S3
				writer.close();
		    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);
		    	r.setStorageClass(StorageClass.ReducedRedundancy);
		    	s3.putObject(r);
				linesWritten = 0;
				boolean localFileDeleted = localFile.delete();
		    	if (!localFileDeleted) {
		    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
		    	}
				filename = "PROFILES_" + UUID.randomUUID().toString();
				localFile = new File(filename);
				writer = new BufferedWriter(new FileWriter(localFile));
			}
		}
		writer.close();
		
		// Copy the local file to S3
    	PutObjectRequest r = new PutObjectRequest(profilesBucket, filename, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	boolean localFileDeleted = localFile.delete();
    	if (!localFileDeleted) {
    		logger.error("Could not delete local file " + localFile.getAbsolutePath());
    	}
	}
	
	public void initProfiles(String tenantID) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				ProfileDAOImplS3.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String bucketName = getProfilesBucketName(tenantID);
		if (!s3.doesBucketExist(bucketName)) {
			s3.createBucket(bucketName, Region.EU_Ireland);
		}
		else {
			ObjectListing objListing = s3.listObjects(bucketName);
			if (objListing.getObjectSummaries().size() > 0) {
				for (S3ObjectSummary objSummary : objListing.getObjectSummaries()) {
					s3.deleteObject(bucketName, objSummary.getKey());
				}
			}
		}
	}
}
