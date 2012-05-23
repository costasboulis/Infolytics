package com.cleargist.profile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.spy.memcached.MemcachedClient;

public class ProfileDAOMemcached implements ProfileDAO {
	private String memcachedServer;
	private String domain;
    private int memcachedPort = 11211;
    private int TTL_CACHE = 60 * 60 * 24; 
    private MemcachedClient client;
    private Logger logger = Logger.getLogger(getClass());
    
    public ProfileDAOMemcached(String domain, String memcachedServer, int memcachedPort) {
    	this.domain = domain;
    	this.memcachedServer = memcachedServer;
    	this.memcachedPort = memcachedPort;
    	this.client = null;
    	try {
        	this.client = new MemcachedClient(new InetSocketAddress(this.memcachedServer, this.memcachedPort));
    	}
    	catch (IOException ex) {
        	logger.error("Cannot insantiate memcached client");
        }
    }
    
    private String getKey(String id) {
    	return "profile_" + this.domain + "_" + id;
    }
    
    public void writeProfile(Profile profile) throws Exception {
    	String key = getKey(profile.getUserID());
    	this.client.set(key, TTL_CACHE, (Object)profile);
    }
    
    public Profile getProfile(String tenantID, String profileID) throws Exception {
    	String key = getKey(profileID);
    	Object obj = this.client.get(key);
    	if (obj == null) {
    		logger.warn("No profile found for id " + profileID);
    		return null;
    	}
		Profile profile = (Profile)obj;
		
		return profile;
	}
    
    public List<Profile> getAllProfiles(String bucket, String key) throws Exception {
    	return new ArrayList<Profile>();
    }
    
    public Profile getNextProfile() throws Exception {
		return new Profile();
	}
	
	public void initSequentialProfileRead(String bucket, String key) throws Exception {
		
	}
	
	public void closeSequentialProfileRead() throws Exception {
		
	}
	
	public void loadAllProfiles(String tenantID, String bucket, String key) throws Exception {
		
	}
	
	public void resetCache() {
		this.client.flush();
	}
	
	public void updateProfiles(String tenantID, List<Profile> incrementalProfiles, List<Profile> decrementalProfiles) throws Exception {
		
	}
	
	public void initProfiles(String tenantID) throws Exception {
		
	}
}
