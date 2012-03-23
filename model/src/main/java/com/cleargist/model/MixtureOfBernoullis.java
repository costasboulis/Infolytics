package com.cleargist.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.cleargist.catalog.entity.jaxb.Catalog.Products.Product;

/**
 * Mixture of multivariate Bernoulli distributions with missing values
 * 
 * @author kboulis
 *
 */
public class MixtureOfBernoullis extends BaseModel {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private Logger logger = Logger.getLogger(getClass());
	private List<HashMap<Integer, List<Double>>> logProb;
	private List<Double> logPriors;
	private List<List<Double>> logProbUnseen;
	private List<HashMap<Integer, Double>> ss1;
	private double[] ss0;
	private double N;  // Number of data points
	private int C;   // Number of clusters
	private int numberOfIterations = 10; 
	private static double ALPHA = 1.0;
	private static double BETA = 1.0;
	private static double UPPER_LOG_THRESHOLD = 10.0;
	private static double LOWER_LOG_THRESHOLD = -10.0;
	private Random random;
	private String dataBucketName;
	private String dataKey;
	private long maxDataSize = 1000000000;

	
	public MixtureOfBernoullis() {
		this.logProb = new ArrayList<HashMap<Integer, List<Double>>>();
		this.logPriors = new ArrayList<Double>();
		this.logProbUnseen = new ArrayList<List<Double>>();
		this.random = new Random();
	}
	
	protected  String getDomainBasename() {
		return "MODEL_MIXBERNOULLIS_";
	}
	
	protected String getStatsBucketName(String tenantID) {
		return STATS_BASE_BUCKETNAME + "mixbernoullis" + tenantID;
	}
	
	public void setMaxDataSize(long l) {
		this.maxDataSize = l;
	}
	
	public void setNumberOfClusters(int numClusters) {
		this.C = numClusters;
	}
	
	public void setNumberOfIterations(int r) {
		this.numberOfIterations = r;
	}
	
	public void setDataBucketNameAndKey(String bucketname, String key) {
		this.dataBucketName = bucketname;
		this.dataKey = key;
	}
	
	public String getDataBucketName() {
		return this.dataBucketName;
	}
	
	public String getDataKey() {
		return this.dataKey;
	}
	
	public void createModel(String tenantID) throws Exception {
		String dataBucketName = getDataBucketName();
		String dataKey = getDataKey();
		logger.info("Initializing models");
		calculateInitialSufficientStatistics(dataBucketName, dataKey, tenantID);
		mergeSufficientStatistics(tenantID);
		estimateModelParameters(tenantID);
		for (int iter = 1 ; iter <= this.numberOfIterations; iter ++) {
			logger.info("Iteration " + iter);
			
			calculateSufficientStatistics(dataBucketName, dataKey, tenantID);
			mergeSufficientStatistics(tenantID);
			estimateModelParameters(tenantID);
		}
		
		logger.info("Writing models to file");
		String outputBucketName = "cleargist";
		String outputKey = getDomainBasename() + tenantID;
		writeModelsToFile(outputBucketName, outputKey, tenantID);
		logger.info("Training done");
	}
	
	public void createModel(String tenantID, File dataFile) throws Exception {
		logger.info("Initializing models");
		calculateInitialSufficientStatistics(dataFile, tenantID);
		mergeSufficientStatistics(tenantID);
		estimateModelParameters(tenantID);
		for (int iter = 1 ; iter <= this.numberOfIterations; iter ++) {
			logger.info("Iteration " + iter);
			
			calculateSufficientStatistics(dataFile, tenantID);
			mergeSufficientStatistics(tenantID);
			estimateModelParameters(tenantID);
		}
		
		logger.info("Writing models to file");
		String outputBucketName = "cleargist";
		String outputKey = getDomainBasename() + tenantID;
		writeModelsToFile(outputBucketName, outputKey, tenantID);
		logger.info("Training done");
	}
	
	
	public void calculateInitialSufficientStatistics(String bucketName, String key, String tenantID) throws Exception {
		calculateSufficientStatistics(bucketName, key, tenantID, true);
	}
	
	public void calculateInitialSufficientStatistics(File dataFile, String tenantID) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		calculateSufficientStatistics(br, tenantID, true);
		br.close();
	}
	
	public void calculateSufficientStatistics(File dataFile, String tenantID) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		calculateSufficientStatistics(br, tenantID, false);
		br.close();
	}
	
	public void calculateSufficientStatistics(String bucketName, String key, String tenantID) throws Exception {
		calculateSufficientStatistics(bucketName, key, tenantID, false);
	}
	
	private void calculateSufficientStatistics(String bucketName, String key, String tenantID, boolean isInitial) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		S3Object dataObject = s3.getObject(dataBucketName, dataKey);
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataObject.getObjectContent()));
		calculateSufficientStatistics(reader, tenantID, isInitial);
		reader.close();
	}
	
	private void calculateSufficientStatistics(BufferedReader reader, String tenantID, boolean isInitial) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		String statsBucketName = getStatsBucketName(tenantID);
		if (!s3.doesBucketExist(statsBucketName)) {
			s3.createBucket(statsBucketName, Region.EU_Ireland);
		}
		else {
			logger.info("Deleting contents of bucket " + statsBucketName);
			ObjectListing objListing = s3.listObjects(statsBucketName);
			if (objListing.getObjectSummaries().size() > 0) {
				for (S3ObjectSummary objSummary : objListing.getObjectSummaries()) {
					s3.deleteObject(statsBucketName, objSummary.getKey());
				}
			}
		}
		
		// Create the S3 file with all the data
//		long numBytes = s3.getObjectMetadata(bucketName, key).getContentLength();
		long numBytes = 1000;
		
		if (numBytes < maxDataSize) {
			calculateSufficientStatistics(reader, 
					   					  statsBucketName, STATS_BASE_FILENAME + "_1.txt", 
					   					  tenantID, isInitial);
		}
		else {
			// Distribute to many files
			logger.error("Mutliple file splitting not yet implemented");
			System.exit(-1);
		}
		
	}
	
	
	private void calculateSufficientStatistics(BufferedReader reader, 
											   String statsBucketName, String statsKey, String tenantID, boolean isInitial) throws Exception {
		
		
		this.ss1 = new ArrayList<HashMap<Integer, Double>>();
		for (int m = 0; m < this.C; m ++) {
			this.ss1.add(new HashMap<Integer, Double>());
		}
		this.ss0 = new double[this.C];
		this.N = 0;
		
		String lineStr = null;
		while ((lineStr = reader.readLine()) != null) {
			String[] fields = lineStr.split(" ");
			String user = fields[0];
			
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
			for (int k = 1; k < fields.length; k ++) {
				String f = fields[k];
				String[] values = f.split(":");
				
				int indx = -1;
				try {
					indx = Integer.parseInt(values[0]);
				}
				catch (NumberFormatException ex) {
					logger.warn("Cannot parse index \"" + values[0] + "\" ...skipping");
					continue;
				}
				
				int value = -1;
				try {
					value = Integer.parseInt(values[1]);
				}
				catch (NumberFormatException ex) {
					logger.warn("Cannot parse value \"" + values[1] + "\" ...skipping");
					continue;
				}
				if (value != 0 && value != 1) {
					logger.warn("Illegal value " + value + " ... skipping");
					continue;
				}
				hm.put(indx, value);
			}
			if (hm.size() == 0) {
				logger.warn("Could not parse profile from " + lineStr);
				continue;
			}
			
			
			// Now update the sufficient statistics
			double[] probs = isInitial ? calculateInitialClusterPosteriors(hm) : calculateClusterPosteriors(hm);
			for (int m = 0 ; m < ss1.size(); m ++) {
				if (probs[m] == 0.0) {
					continue;
				}
				for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
					int indx = entry.getKey();
					int value = entry.getValue();
					
					if (value == 1) {
						Double v = ss1.get(m).get(indx);
						if (v == null) {
							ss1.get(m).put(indx, probs[m]);
						}
						else {
							ss1.get(m).put(indx, probs[m] + v.doubleValue());
						}
					}
				}
				ss0[m] += probs[m];
			}
			
			this.N += 1.0;
		}
		
		// Now write the sufficient statistics
		File localFile = new File(tenantID + "_" + statsBucketName + "_" + statsKey);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(localFile));
			bw.write(this.N + newline);
			StringBuffer sb = new StringBuffer();
			sb.append(ss0[0]);
			for (int m = 1; m < this.C; m ++) {
				sb.append(" "); sb.append(ss0[m]);
			}
			sb.append(newline);
			bw.write(sb.toString());
			for (int m = 0; m < this.C; m ++) {
				sb = new StringBuffer();
				sb.append(m);
				for (Map.Entry<Integer, Double> entry : ss1.get(m).entrySet()) {
					sb.append(" "); sb.append(entry.getKey()); sb.append(":"); sb.append(entry.getValue());
				}
				sb.append(newline);
				bw.write(sb.toString());
				bw.flush();
			}
			bw.flush();
			bw.close();
		}
		catch (Exception ex) {
			logger.error("Could not write to local file " + localFile.getAbsolutePath());
			throw new Exception();
		}
		
		// Now copy over to S3
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		PutObjectRequest r = new PutObjectRequest(statsBucketName, statsKey, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localFile.delete();
	}

	public void writeClusterMemberships(String dataBucketName, String dataKey, 
				 String membershipsBucketName, String membershipsKey, String tenantID) throws Exception {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		s3.deleteObject(membershipsBucketName, membershipsKey);
		S3Object dataObject = s3.getObject(dataBucketName, dataKey);
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataObject.getObjectContent()));
		File localFile = new File(membershipsBucketName + "_" + membershipsKey +"_" + tenantID);
		BufferedWriter writer = new BufferedWriter(new FileWriter(localFile));
		writeClusterMemberships(reader, writer, tenantID);
		reader.close();
		writer.close();
		
		// Now copy over to S3
		PutObjectRequest r = new PutObjectRequest(membershipsBucketName, membershipsKey, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localFile.delete();
	}
	
	public void writeClusterMemberships(File dataFile, 
			 							File clusterMembershipFile, String tenantID) throws Exception {
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		clusterMembershipFile.delete();
		BufferedWriter writer = new BufferedWriter(new FileWriter(clusterMembershipFile));
		writeClusterMemberships(reader, writer, tenantID);
		reader.close();
		writer.close();
	}

	private void writeClusterMemberships(BufferedReader reader, 
			   							 BufferedWriter writer, String tenantID) throws Exception {
			
		String lineStr = null;
		while ((lineStr = reader.readLine()) != null) {
			String[] fields = lineStr.split(" ");
			String user = fields[0];
			
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
			for (int k = 1; k < fields.length; k ++) {
				String f = fields[k];
				String[] values = f.split(":");
				
				int indx = -1;
				try {
					indx = Integer.parseInt(values[0]);
				}
				catch (NumberFormatException ex) {
					logger.warn("Cannot parse index \"" + values[0] + "\" ...skipping");
					continue;
				}
				
				int value = -1;
				try {
					value = Integer.parseInt(values[1]);
				}
				catch (NumberFormatException ex) {
					logger.warn("Cannot parse value \"" + values[1] + "\" ...skipping");
					continue;
				}
				if (value != 0 && value != 1) {
					logger.warn("Illegal value " + value + " ... skipping");
					continue;
				}
				hm.put(indx, value);
			}
			if (hm.size() == 0) {
				logger.warn("Could not parse profile from " + lineStr);
				continue;
			}
			
			
			double[] probs = calculateClusterPosteriors(hm);
			
			StringBuffer sb = new StringBuffer();
			sb.append(user); 
			for (int m = 0; m < probs.length; m ++) {
				if (probs[m] == 0.0) {
					continue;
				}
				sb.append(" "); sb.append(m); sb.append(":"); sb.append(probs[m]);
			}
			sb.append(newline);
			
			writer.write(sb.toString());
			writer.flush();
		}
	}
	
	/*
	 * Random assignment of cluster to data point. Used in first EM iteration
	 */
	private double[] calculateInitialClusterPosteriors(HashMap<Integer, Integer> hm) {
		double[] probs = new double[this.C];
		
		int m = this.random.nextInt(this.C);
		
		for (int v = 0; v < this.C; v ++) {
			probs[v] = m == v ? 1.0 : 0.0;
		}
		
		return probs;
	}
	/*
	 * Calculate p(cluster = m | t_n ; \theta)
	 * 
	 * Does some smart normalization so that it never overflows / underflows
	 */
	private double[] calculateClusterPosteriors(HashMap<Integer, Integer> hm) {
		int numClusters = logProb.size();
		double[] logProbs = new double[numClusters];
		
		for (int m = 0; m < numClusters; m ++) {
			logProbs[m] = logPriors.get(m);
			for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
				int indx = entry.getKey();
				int value = entry.getValue();
				
				Double v = logProb.get(m).get(indx).get(value);
				if (v == null) {
					v = logProbUnseen.get(m).get(value);
				}
				
				logProbs[m] += v;
			}
		}
		
		// Now that the logProbs are estimated calculate p[m]
		double[] probs = new double[numClusters];
		double totProbs = 0.0;
		for (int m = 0; m < numClusters; m ++) {
			boolean probsFound = false;
			List<Double> diffs = new ArrayList<Double>();
			for (int j = 0; j < numClusters; j ++) {
				if (m == j) {
					continue;
				}
				
				double diff = logProbs[j] - logProbs[m];
				if (diff > UPPER_LOG_THRESHOLD) {
					probs[m] = 0.0;
					
					probsFound = true;
					break;
				}
				else if (diff < LOWER_LOG_THRESHOLD) {
					continue;
				}
				diffs.add(diff);
			}
			if (!probsFound) {
				double sum = 0.0;
				for (Double d : diffs) {
					sum += Math.exp(d);
				}
				probs[m] = 1.0 / (1.0 + sum);
				
				totProbs += probs[m];
			}
		}
		
		// Do a final normalization
		for (int m = 0; m < numClusters; m ++) {
			probs[m] /= totProbs;
		}
		
		return probs;
	}
	
	public void mergeSufficientStatistics(String tenantID) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
		
		String statsBucketName = getStatsBucketName(tenantID);
		String statsKey = MERGED_STATS_FILENAME;
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
    	
    	ObjectListing objectListing = s3.listObjects(statsBucketName);
    	List<S3ObjectSummary> objSummaries = objectListing.getObjectSummaries();
    	
    	if (objSummaries.size() == 0) {
    		logger.warn("No stats files found for tenant " + tenantID + " in bucket " + statsBucketName);
    		return;
    	}
    	
    	
    	String statsFilename = objSummaries.get(0).getKey();
    	s3.copyObject(statsBucketName, statsFilename, statsBucketName, statsKey);
    	s3.deleteObject(statsBucketName, statsFilename);
    	int i = 1;
    	while (i < objSummaries.size()) {
    		statsFilename = objSummaries.get(i).getKey();
        	
    		mergeSufficientStatistics(statsBucketName, statsKey, 
    								  statsBucketName, statsFilename, tenantID);
    		
    		s3.deleteObject(statsBucketName, statsFilename);
    		i ++;
    	}
	}
	
	private void mergeSufficientStatistics(String mergedBucketName, String mergedKey, 
										   String statsBucketName, String statsKey, String tenantID) throws Exception {
		
		// Load in memory the statsKey
		this.ss1 = new ArrayList<HashMap<Integer, Double>>();
		for (int m = 0; m < this.C; m ++) {
			this.ss1.add(new HashMap<Integer, Double>());
		}
		this.ss0 = new double[this.C];
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		S3Object statsObject = s3.getObject(statsBucketName, statsKey);
		BufferedReader reader = new BufferedReader(new InputStreamReader(statsObject.getObjectContent()));
		String lineStr = reader.readLine();
		this.N = Double.parseDouble(lineStr);
		lineStr = reader.readLine();
		String[] fields = lineStr.split(" ");
		for (int m = 0; m < fields.length; m ++) {
			this.ss0[m] = Double.parseDouble(fields[m]);
		}
		while ((lineStr = reader.readLine()) != null) {
			fields = lineStr.split(" ");
			int m = Integer.parseInt(fields[0]);
			
			for (int k = 1; k < fields.length; k ++) {
				String[] f = fields[k].split(":");
				int indx = Integer.parseInt(f[0]);
				double value = Double.parseDouble(f[1]);
				
				ss1.get(m).put(indx, value);
			}
		}
		HashSet<Integer> hsA = new HashSet<Integer>();
		for (int m = 0; m < this.ss1.size(); m ++) {
			if (this.ss1.get(m).size() != 0) {
				hsA.add(m);
			}
		}
		
		
		// Read line-by-line the mergedKey, merge with statsKey and write to new local file
		String localFilename = mergedBucketName + "_" + mergedKey + "_" + tenantID;
		File localFile = new File(localFilename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(localFile));
		S3Object mergedObject = s3.getObject(mergedBucketName, mergedKey);
		reader = new BufferedReader(new InputStreamReader(mergedObject.getObjectContent()));
		lineStr = reader.readLine();
		double mergedN = Double.parseDouble(lineStr) + this.N;
		StringBuffer sb = new StringBuffer();
		sb.append(mergedN); sb.append(newline);
		
		lineStr = reader.readLine();
		fields = lineStr.split(" ");
		for (int m = 0; m < fields.length; m ++) {
			double v = Double.parseDouble(fields[m]) + ss0[m];
			sb.append(v);
		}
		sb.append(newline);
		bw.write(sb.toString());
		bw.flush();
		
		HashSet<Integer> hsB = new HashSet<Integer>();
		while ((lineStr = reader.readLine()) != null) {
			fields = lineStr.split(" ");
			int m = Integer.parseInt(fields[0]);
			
			hsB.add(m);
			sb = new StringBuffer();
			sb.append(m);
			HashMap<Integer, Double> hm = this.ss1.get(m);
			for (int k = 1; k < fields.length; k ++) {
				String[] f = fields[k].split(":");
				int indx = Integer.parseInt(f[0]);
				double value = Double.parseDouble(f[1]);
				
				Double v = hm.get(indx);
				if (v == null) {
					sb.append(" "); sb.append(indx); sb.append(":"); sb.append(value);
				}
				else {
					sb.append(" "); sb.append(indx); sb.append(":"); sb.append(value + v);
				}
			}
			sb.append(newline);
			bw.write(sb.toString());
			bw.flush();
		}
		reader.close();
		
		for (Integer m : hsA) {
			if (!hsB.contains(m)) {
				continue;
			}
			sb = new StringBuffer();
			sb.append(m); 
			for (Map.Entry<Integer, Double> entry : this.ss1.get(m).entrySet()) {
				sb.append(" "); sb.append(entry.getKey()); sb.append(":"); sb.append(entry.getValue());
			}
			sb.append(newline);
			bw.write(sb.toString());
			bw.flush();
		}
		bw.close();
		
		// Now copy over to S3
		PutObjectRequest r = new PutObjectRequest(mergedBucketName, mergedKey, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localFile.delete();
	}
	
	public void estimateModelParameters(String tenantID) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
		
		String bucketName = getStatsBucketName(tenantID);
		String key = MERGED_STATS_FILENAME;
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		S3Object statsObject = s3.getObject(bucketName, key);
		BufferedReader reader = new BufferedReader(new InputStreamReader(statsObject.getObjectContent()));
		String lineStr = reader.readLine();
		this.N = Double.parseDouble(lineStr);
		
		// Estimate logPriors and logProbUnseen
		lineStr = reader.readLine();
		String[] fields = lineStr.split(" ");
		this.C = fields.length;
		this.logPriors = new ArrayList<Double>();
		this.logProbUnseen = new ArrayList<List<Double>>();
		double[] ss0 = new double[this.C];
		for (int m = 0; m < this.C; m ++) {
			double v = Double.parseDouble(fields[m]);
			ss0[m] = v;
			
			double u = Math.log(v / this.N);
			logPriors.add(u);
			
			double v1 = Math.log(ALPHA / (v + ALPHA + BETA));
			double v0 = Math.log((v + BETA) / (v + ALPHA + BETA));
			List<Double> l = new ArrayList<Double>();
			l.add(v0); l.add(v1);
			
			logProbUnseen.add(l);
		}
		
		// Estimate logProb
		this.logProb = new ArrayList<HashMap<Integer, List<Double>>>();
		for (int m = 0; m < this.C; m ++) {
			this.logProb.add(new HashMap<Integer, List<Double>>());
		}
		while ((lineStr = reader.readLine()) != null) {
			fields = lineStr.split(" ");
			int m = Integer.parseInt(fields[0]);
			
			for (int i = 1; i < fields.length; i ++) {
				String[] f = fields[i].split(":");
				int indx = Integer.parseInt(f[0]);
				double v = Double.parseDouble(f[1]);
				
				double logv1 = Math.log((v + ALPHA) / (ss0[m] + ALPHA + BETA));
				double logv0 = Math.log(1.0 - ( (v + ALPHA) / (ss0[m] + ALPHA + BETA) ));
				List<Double> l = new ArrayList<Double>();
				l.add(logv0); l.add(logv1);
				
				logProb.get(m).put(indx, l);
			}
		}
		reader.close();
		
	}
	
	private void writeModelsToFile(String outputBucketName, String outputKey, String tenantID) throws Exception {
		
		File localFile = new File(tenantID + "_" + outputBucketName + "_" + outputKey);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(localFile));
			StringBuffer sb = new StringBuffer();
			
			// Write log priors
			sb.append(logPriors.get(0));
			for (int m = 1; m < this.C; m ++) {
				sb.append(" "); sb.append(logPriors.get(m));
			}
			sb.append(newline);
			bw.write(sb.toString());
			bw.flush();
			
			// Write log of unseen events
			sb = new StringBuffer();
			sb.append(logProbUnseen.get(0));
			for (int m = 1; m < logPriors.size(); m ++) {
				sb.append(" "); sb.append(logProbUnseen.get(m).get(0)); sb.append(":"); sb.append(logProbUnseen.get(m).get(1));
			}
			sb.append(newline);
			bw.write(sb.toString());
			bw.flush();
			
			// Write log of p(attribute | cluster)
			for (int m = 0; m < this.C; m ++) {
				sb = new StringBuffer();
				sb.append(m);
				for (Map.Entry<Integer, List<Double>> entry : logProb.get(m).entrySet()) {
					int indx = entry.getKey();
					List<Double> values = entry.getValue();
					
					sb.append(" "); sb.append(indx); sb.append(":"); sb.append(values.get(0)); 
					sb.append(":"); sb.append(values.get(1));
				}
				sb.append(newline);
				bw.write(sb.toString());
				bw.flush();
			}
			bw.flush();
			bw.close();
		}
		catch (IOException ex) {
			logger.error("Could not write models to local file " + localFile.getAbsolutePath());
			throw new Exception();
		}
		
		// Copy local file to S3
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		PutObjectRequest r = new PutObjectRequest(outputBucketName, outputKey, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localFile.delete();
	}

	@Override
	public List<Product> getRecommendedProductsInternal(
			List<String> productIds, String tenantID, Filter filter)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Product> getPersonalizedRecommendedProductsInternal(
			String userId, String tenantID, Filter filter) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
