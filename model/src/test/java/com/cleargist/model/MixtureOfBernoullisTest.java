package com.cleargist.model;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

public class MixtureOfBernoullisTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	public static String newline = System.getProperty("line.separator");
	private Logger logger = Logger.getLogger(getClass());
	
	private int sample(double[] pdf) {
		Random random = new Random();
		double[] cdf = new double[pdf.length];
		cdf[0] = pdf[0];
		for (int i = 1; i < pdf.length; i ++) {
			cdf[i] = cdf[i - 1] + pdf[i];
		}
		double v = random.nextDouble();
		for (int i = 0; i < cdf.length; i ++) {
			if (v <= cdf[i]) {
				return i;
			}
		}
		return -1;
	}
	
	private void convertToBernoulliFormat(String filename) {
		File inputFile = new File(filename);
		File outputFile = new File("c:\\recs\\20Newsgroups_data_Bernoullis.txt");
		int numAttributes = 21147;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				String[] fields = lineStr.split(" ");
				
				String user = fields[0];
				HashSet<Integer> hs = new HashSet<Integer>();
				for (int f = 1; f < fields.length; f = f + 2) {
					int indx = Integer.parseInt(fields[f]);
					hs.add(indx);
				}
				
				StringBuffer sb = new StringBuffer();
				sb.append(user); 
				for (int att = 0; att < numAttributes; att ++ ) {
					sb.append(" "); sb.append(att); sb.append(":"); 
					int value = hs.contains(att) ? 1 : 0;
					sb.append(value);
				}
				sb.append(newline);
				bw.write(sb.toString());
				bw.flush();
			}
			br.close();
			bw.close();
		}
		catch (Exception ex) {
			logger.error("Could not read from " + inputFile.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	private void createSample() {
		int numberOfSamples = 10000;
		/*
		double[] priors = {8.0/36.0, 7.0/36.0, 6.0/36.0, 5.0/36, 4.0/36.0, 3.0/36.0, 2.0/36.0, 1.0/36.0};
		double[][] probs = { {0.8, 0.8, 0.8, 0.8, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2},
							 {0.2, 0.2, 0.2, 0.2, 0.8, 0.8, 0.8, 0.8, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2}, 
							 {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.8, 0.8, 0.8, 0.8, 0.2, 0.2, 0.2, 0.2},
							 {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.8, 0.8, 0.8, 0.8},
							 {0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2},
							 {0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2},
							 {0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2},
							 {0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8}  };
		*/
		double[] priors = {0.5, 0.5};
		double[][] probs = { {0.9, 0.9, 0.9, 0.9, 0.1, 0.1, 0.1, 0.1},
							 {0.1, 0.1, 0.1, 0.1, 0.9, 0.9, 0.9, 0.9}  };
		
		int[] categories = new int[numberOfSamples];
		String filename = "c:\\recs\\mixBernoullisSample.txt";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < numberOfSamples; i ++) {
				int m = sample(priors);
				categories[i] = m;
				StringBuffer sb = new StringBuffer();
				sb.append(i);
				for (int att = 0; att < probs[m].length; att ++) {
					double[] p = new double[2];
					p[0] = 1.0 - probs[m][att];
					p[1] = probs[m][att];
					int indx = sample(p);
					
					sb.append(" "); sb.append(att); sb.append(":"); sb.append(indx);
				}
				sb.append(newline);
				bw.write(sb.toString());
				bw.flush();
			}
			bw.flush();
			bw.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + filename);
			System.exit(-1);
		}
		
		
		filename = "c:\\recs\\mixBernoullisSampleCategories.txt";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < numberOfSamples; i ++) {
				StringBuffer sb = new StringBuffer();
				sb.append(i); sb.append(" "); sb.append(categories[i]); sb.append(newline);
				
				bw.write(sb.toString());
				bw.flush();
			}
			bw.flush();
			bw.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + filename);
			System.exit(-1);
		}
	}
	
	/* 
	 * TODO : Can easily add combining multiple systems by using List<File> hypPartitionFileList 
	 * and having crossCounts[reference][c] += (memb) / (float)hypPartitionFileList.size();
	 */
	
	private double computeAdjustedRandIndex(File referencePartitionFile,
											File hypPartitionFile      , int numberOfExperts) throws Exception {
		double adjRandIndex=0.0;
		int numberOfClasses = 0;
		HashMap<String, Integer> referencePartition = new HashMap<String, Integer>();
		
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				MixtureOfBernoullisTest.class.getResourceAsStream(AWS_CREDENTIALS)));
		String lineStr = null;
		BufferedReader reader = new BufferedReader(new FileReader(referencePartitionFile));
		while ((lineStr = reader.readLine()) != null) {
			String[] st = lineStr.split("\\s+");
			String user = st[0];
			int c = Integer.parseInt(st[1]);
			referencePartition.put(user, new Integer(c));
		    	
			if (c > numberOfClasses){
				numberOfClasses = c;
			}
		} 
		
		// Since class ids start from 0, add one 
		numberOfClasses ++;
			
		// Compute n_{i,j} = number of items that belong in cluster i of partition A and cluster j of partition B 
		double [][] crossCounts = new double[numberOfClasses][];
		for (int n = 0; n < numberOfClasses; n ++){
			crossCounts[n] = new double[numberOfExperts];
			for (int j=0; j<numberOfExperts; j++){
				crossCounts[n][j] = 0.0;
			}
		}
		
		int numberOfUsers = 0;
		reader = new BufferedReader(new FileReader(hypPartitionFile));
		while ((lineStr = reader.readLine()) != null) {
			String[] st = lineStr.split("\\s+");
			String user = st[0];
			int reference = referencePartition.get(user).intValue();
	    	for (int k=1; k<st.length; k++) {
	    		String[] st2 = st[k].split(":");
	    		int c = Integer.parseInt(st2[0]);
	    		float memb = Float.parseFloat(st2[1]);
	    		
	    		crossCounts[reference][c] += memb;
	    	}
	    	numberOfUsers ++;
		}
			
			
			
		
		double [] crossCountsOverClusters = new double[numberOfClasses];
		for (int i=0; i<numberOfClasses; i++){
			crossCountsOverClusters[i] = 0.0;
			for (int j=0; j<numberOfExperts; j++)
				crossCountsOverClusters[i] += crossCounts[i][j];	
		}
			
		double [] crossCountsOverClasses = new double[numberOfExperts];
		for (int j=0; j<numberOfExperts; j++){
			crossCountsOverClasses[j] = 0.0;
			for (int i=0; i<numberOfClasses; i++)
				crossCountsOverClasses[j] += crossCounts[i][j];	
		}
			
		double A = 0.0;
		for (int i=0; i<numberOfClasses; i++){
			for (int j=0; j<numberOfExperts; j++)
				A += crossCounts[i][j] * (crossCounts[i][j] - 1.0) / 2.0 ;
		}
		double B = 0.0;
		for (int i=0; i<numberOfClasses; i++)
			B += crossCountsOverClusters[i] * (crossCountsOverClusters[i] - 1.0) / 2.0;
		
		double C = 0.0;
		for (int j=0; j<numberOfExperts; j++)
			C += crossCountsOverClasses[j] * (crossCountsOverClasses[j] - 1.0) / 2.0;
		
		double D = (double)numberOfUsers * ((double)numberOfUsers - 1.0) / 2.0;
			
		adjRandIndex = ( A - ((B*C)/D) ) / (0.5 * (B + C) - ((B*C)/D) );
			
		
		return adjRandIndex;
	}
	
	@Test
	public void trainInMemoryTest() {
	//	createSample();
		
//		convertToBernoulliFormat("C:\\Users\\kboulis\\ClearGist\\Infolytics\\mixtureOfExperts\\src\\test\\resources\\gr\\infolytics\\models\\mixtureOfExperts\\ClusteringSmokeTest\\20Newsgroups.data.vocab.10.txt");
		
		File dataFile = new File("c:\\recs\\20Newsgroups_data_Bernoullis.txt");
		File refCategoriesFile = new File("c:\\recs\\20Newsgroups_labels.txt");
		int numClusters = 20;
		
		
//		File dataFile = new File("c:\\recs\\mixBernoullisSample.txt");
//		File refCategoriesFile = new File("c:\\recs\\mixBernoullisSampleCategories.txt");
//		int numClusters = 2;
		
		File hypCategoriesFile = new File("c:\\recs\\mixBernoullisSampleHyp.txt");
		MixtureOfBernoullis mixBernoullis = new MixtureOfBernoullis();
		mixBernoullis.setNumberOfClusters(numClusters);
		mixBernoullis.setNumberOfIterations(10);
		try {
			mixBernoullis.createModel("test", dataFile);
			mixBernoullis.writeClusterMemberships(dataFile, hypCategoriesFile, "test"); 
			double adjRank = computeAdjustedRandIndex(refCategoriesFile, hypCategoriesFile, numClusters);
			System.out.println("AdjustedRank : " + adjRank);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		
		hypCategoriesFile.delete();
		
		assertTrue(true);
	}
}