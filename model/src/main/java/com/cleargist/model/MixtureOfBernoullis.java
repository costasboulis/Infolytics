package com.cleargist.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
	private int numberOfIterations; 
	private static double ALPHA = 1.0;
	private static double BETA = 1.0;
	private static double UPPER_LOG_THRESHOLD = 10.0;
	private static double LOWER_LOG_THRESHOLD = -10.0;
	private Random random;

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
	
	public void setNumberOfClusters(int numClusters) {
		if (logProb.size() != 0) {
			logger.warn("Ignoring setting of number of clusters to " + numClusters + " . Models are alredy loaded will read numClusters from there");
			return;
		}
		this.C = numClusters;
		this.ss0 = new double[numClusters];
		for (int m = 0; m < numClusters; m ++) {
			this.ss1.add(new HashMap<Integer, Double>());
			this.ss0[m] = 0.0;
		}
	}
	
	public void setNumberOfIterations(int r) {
		this.numberOfIterations = r;
	}
	
	public void trainInMemory(String dataBucketName, String dataKey, String tenantID) throws Exception {
		
		String statsBucketName = getStatsBucketName(tenantID);
		String statsKey = "merged.txt";
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		
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
		
		
		logger.info("Initializing models");
		calculateSufficientStatistics(dataBucketName, dataKey, statsBucketName, statsKey, tenantID, true);
		estimateModelParameters(statsBucketName, statsKey, tenantID);
		for (int iter = 1 ; iter <= this.numberOfIterations; iter ++) {
			logger.info("Iteration " + iter);
			
			calculateSufficientStatistics(dataBucketName, dataKey, statsBucketName, statsKey, tenantID, false);
			estimateModelParameters(statsBucketName, statsKey, tenantID);
		}
		
		logger.info("Writing models to file");
		String outputBucketName = "cleargist";
		String outputKey = getDomainBasename() + tenantID;
		writeModelsToFile(outputBucketName, outputKey, tenantID);
		logger.info("Training done");
	}
	
	private void readModels() {
		
	}
	
	public void calculateSufficientStatistics(String bucketName, String key, String tenantID) {
		// Create the S3 file with all the data
		
		// Distribute to many files
	}
	
	private void calculateSufficientStatistics(String dataBucketName, String dataKey, 
											   String statsBucketName, String statsKey, String tenantID, boolean isInitial) throws Exception {
		
		
		this.ss1 = new ArrayList<HashMap<Integer, Double>>();
		for (int m = 0; m < this.C; m ++) {
			this.ss1.add(new HashMap<Integer, Double>());
		}
		this.ss0 = new double[this.C];
		this.N = 0;
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullis.class.getResourceAsStream(AWS_CREDENTIALS)));
		
		S3Object dataObject = s3.getObject(dataBucketName, dataKey);
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataObject.getObjectContent()));
		String lineStr = null;
		while ((lineStr = reader.readLine()) != null) {
			String[] fields = lineStr.split(" ");
			
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
			for (String f : fields) {
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
					ss0[m] += probs[m];
				}
			}
			
			this.N += 1.0;
		}
		reader.close();
		
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
				bw.write(m);
				for (Map.Entry<Integer, Double> entry : ss1.get(m).entrySet()) {
					sb = new StringBuffer();
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
		PutObjectRequest r = new PutObjectRequest(statsBucketName, statsKey, localFile);
    	r.setStorageClass(StorageClass.ReducedRedundancy);
    	s3.putObject(r);
    	
		// cleanup
		localFile.delete();
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
	
	public void mergeSufficientStatistics(String bucketName, String key, String tenantID) {
		
	}
	
	private void mergeSufficientStatistics(String mergedBuckeName, String mergedKey, 
										   String statsBucketName, String statsKey, String tenantID) {
		
	}
	
	public void estimateModelParameters(String bucketName, String key, String tenantID) 
	throws AmazonServiceException, AmazonClientException, IOException, Exception {
		
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
				sb.append(" "); sb.append(logProbUnseen.get(m));
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
}
