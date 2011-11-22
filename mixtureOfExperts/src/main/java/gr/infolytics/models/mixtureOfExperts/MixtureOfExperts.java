package gr.infolytics.models.mixtureOfExperts;


import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;
import java.lang.Integer;
import java.lang.StringBuffer;
import java.lang.Double;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.Writer;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * Base class for estimating a mixture of experts. 
 * 
 * @author Kostas Boulis
 *
 */

public abstract class MixtureOfExperts {
	public static String newline = System.getProperty("line.separator");
	protected int numberOfExperts;
	protected int numberOfAttributes;
	protected int numberOfUsers;
	protected HashMap<String, HashMap<Integer, Float>> probOfClusterGivenUser;
	protected boolean hardAssignments;              // Makes a hard assignment of item to an expert, much cheaper to compute 
	protected double logProbOfAllUsers;             // log-likelihood of data given the current model
	protected double estimationPosteriorThreshold;  // Items with p(cluster|item) <= posteriorThreshold are not contributing to new parameter estimates 
	protected double writePosteriorThreshold;       // Write p(cluster|item) to output only if > writePosteriorThreshold 
    protected HashMap<String, ArrayList<AttributeValue<Integer>>> users;        // user profiles
	protected float [] logProbOfCluster;            // Prob of each cluster
	protected HashMap<String,Integer> attribute2Index;
	protected HashMap<Integer,String> attributeName;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	
	// Main constructor
	MixtureOfExperts(int nExperts, boolean _hardAssignments){	
		numberOfExperts = nExperts;
		hardAssignments = _hardAssignments;
		writePosteriorThreshold = 0.0;
		if (hardAssignments)
			estimationPosteriorThreshold = 0.99;
		else
			estimationPosteriorThreshold = 0.0;
		
			
			
		logProbOfCluster = new float[numberOfExperts];
	}	
	
	// Constructor used in ClusterReduce
	MixtureOfExperts(int nExperts){
		writePosteriorThreshold = 0.0;
		estimationPosteriorThreshold = 0.0;
		numberOfExperts = nExperts;
		logProbOfCluster = new float[numberOfExperts];
        numberOfUsers = 0;
        
	}
	
	public void setWriteThreshold(double t){
		writePosteriorThreshold = t;
	}
	
	public void setEstimationThreshold(double t){
		estimationPosteriorThreshold = t;
	}
	
	
	private void estimateLogProbOfCluster(){
		float tmpSum = 0.0f;
		for (int c=0; c<numberOfExperts; c++){
			tmpSum += logProbOfCluster[c];
		}
		
		for (int c=0; c<numberOfExperts; c++){
			if (logProbOfCluster[c] <= 0.0f)
				logProbOfCluster[c] = -100.0f;
			else
				logProbOfCluster[c] = (float)Math.log((double)(logProbOfCluster[c] / tmpSum));
		}
	}
	
	
	private void readData(File dataFile) {
		try{
			if (!dataFile.exists()) {
				throw new FileNotFoundException();
			}
			if (!dataFile.isFile()) {
				throw new IOException();
			}
			if (!dataFile.canRead()) {
				throw new IOException();
			}
			String lineStr;
			FileReader fr     = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr);
			numberOfUsers=0;
		 
			users = new HashMap<String, ArrayList<AttributeValue<Integer>>>();
		    while ((lineStr = br.readLine()) != null) {
		    	String[] st = lineStr.split("\\s+");
		    	
		    	if (users.containsKey(st[0])){
					logger.error("User " + st[0] + " has been encountered more than once");
					continue;
				}
		    	
				int numberOfNonZeros = (int)((st.length-1)/2);
					
				ArrayList<AttributeValue<Integer>> userProfile = 
					new ArrayList<AttributeValue<Integer>>(numberOfNonZeros);
				
				for (int i=1; i<st.length-1; i=i+2) {
					int att = Integer.parseInt(st[i]);
		    		float cnt = Float.parseFloat(st[i+1]);
		    		
					userProfile.add(new AttributeValue<Integer>(att, cnt)); 
				}
				
		    	users.put(new String(st[0]), userProfile);
		    	numberOfUsers ++;
		    }
		    br.close();
		    logger.info("Read " + users.size() + " profiles");
		}
		catch (FileNotFoundException e) { 
			logger.error("File not found: " + dataFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e) { 
			logger.error("Cannot read " + dataFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public abstract void getAttributeClusterLogProbs(int attribute, float [] logProbVector);
		
	protected abstract void calculateLogProbOfUserGivenCluster(List<AttributeValue<Integer>> profile, 
			float[] logProbOfUserGivenCluster);
	
	public void performExpectationStep(){
		float tmpSum;
		float [] logProbOfUserCluster = new float[numberOfExperts];
		
		logProbOfAllUsers = 0.0;
		Iterator<Map.Entry<String, ArrayList<AttributeValue<Integer>>>> it = users.entrySet().iterator();
		int maxIndex=0;
		while (it.hasNext()){
			float maxLogProb = Float.NEGATIVE_INFINITY;
			Map.Entry<String, ArrayList<AttributeValue<Integer>>> e = it.next();
			String userId = e.getKey();
			calculateLogProbOfUserGivenCluster(e.getValue(), logProbOfUserCluster);
			for (int c=0; c<numberOfExperts; c++){	
				logProbOfUserCluster[c] += logProbOfCluster[c];
				
				if (logProbOfUserCluster[c] > maxLogProb){
					maxLogProb = logProbOfUserCluster[c];
					maxIndex = c;
				}
			}
			
			if (hardAssignments){
				HashMap<Integer, Float> ht = new HashMap<Integer, Float>(1);
				ht.put(new Integer(maxIndex), new Float(1.0f));
				probOfClusterGivenUser.put(userId, ht);
				
				logProbOfAllUsers += maxLogProb; 
				continue;
			}
			
			tmpSum=(float)0.0;
			for (int c=0; c<numberOfExperts; c++){
				tmpSum += (float)Math.exp((double)(logProbOfUserCluster[c] - maxLogProb));
			}
			logProbOfAllUsers += (double)maxLogProb + Math.log((double)tmpSum);
			
			probOfClusterGivenUser.put(userId, new HashMap<Integer, Float>());
			for (int c=0; c<numberOfExperts; c++){
				if (maxLogProb-logProbOfUserCluster[c] > 15.0)
					continue;
				
				tmpSum=0.0f;
				for (int k=0; k<numberOfExperts; k++){
					
					if (k == c){
						tmpSum += 1.0f;
						continue;
					}
					
					double tmp = logProbOfUserCluster[k]-logProbOfUserCluster[c];
					
					tmpSum += (float)Math.exp(tmp);	
				}
				
				HashMap<Integer, Float> ht = probOfClusterGivenUser.get(userId);
				ht.put(new Integer(c), new Float(1.0f / tmpSum));
			}
		}
	}
	
	public abstract void readModels(File modelsFile) ;
	public abstract void writeModels(File modelsFile);
	/** Calculates suff.stats after data has been read in memory */
	public abstract void calculateSufficientStatistics();
	public abstract void writeSufficientStatistics(File statsFilename);
	/** Calculates suff. stats on-the-fly, without reading the data in memory */
	public abstract void calculateSufficientStatistics(File profiles);

	public abstract double mergeSufficientStatistics(File statsMetaFilename);
	protected abstract void estimateAttributeParameters(); 
	


	public void performMaximizationStep(){
		estimateLogProbOfCluster();
		estimateAttributeParameters();
	}
	
	
	public void readClusterMemberships(File inMembFile) {
		try {
			if (!inMembFile.exists()) {
				throw new FileNotFoundException();
			}
			if (!inMembFile.isFile()) {
				throw new FileNotFoundException();
			}
			if (!inMembFile.canRead()) {
				throw new IOException();
			}
			String lineStr;
			FileReader fr     = new FileReader(inMembFile);
		    BufferedReader br = new BufferedReader(fr);
		    while ((lineStr = br.readLine()) != null) {
		    	String[] st = lineStr.split("\\s+");
		    	HashMap<Integer, Float> ht = new HashMap<Integer, Float>(st.length-1);
		    	for (int k=1; k<st.length; k++) {
		    		String[] st2 = st[k].split(":");
		    		ht.put(new Integer(st2[0]), new Float(st2[1]));
		    	}
		    	probOfClusterGivenUser.put(new String(st[0]), ht);
		    }
		    br.close();
		} 
		catch (FileNotFoundException e) { 
			logger.error("Cannot find file " + inMembFile.getAbsolutePath());
			e.printStackTrace();
		    System.exit(1);
		}
		catch (IOException e) { 
			logger.error("ERROR: While reading " + inMembFile.getAbsolutePath());
			e.printStackTrace();
		    System.exit(1);
		}
	}
	
	
	public double trainSingleTrial(int numberOfIterations){
		calculateSufficientStatistics();
		performMaximizationStep();
		
		for (int iter=0; iter<numberOfIterations; iter ++){
			performExpectationStep();
			
			logger.info("Log-likelihood of data: " + Double.toString(logProbOfAllUsers));
			
			calculateSufficientStatistics();
			performMaximizationStep();
		}
		
		return logProbOfAllUsers;
	}
	
	/*
	public double train(){	
		double maxLogProb = Double.NEGATIVE_INFINITY;
		for (int trial=0; trial<numberOfTrials; trial ++){
			
			logger.info("Run " + Integer.toString(trial));
			
			if (trial > 0)
				initializeRandomly();
			
			double logProb = trainSingleTrial();
			if (logProb > maxLogProb){
				maxLogProb = logProb;
				if (numberOfTrials <= 1)
					continue;
				
				writeClusterMemberships(new File(outputFilename), false);  // writes the currently best model to disk	
				
			}
		}
		if (numberOfTrials > 1){
			readClusterMemberships(new File(outputFilename));
			performMaximizationStep();
		}	
		
		return maxLogProb;
		
	}
	*/
	
	
	public double computeAdjustedRandIndex(File referencePartitionFile, File hypPartitionFile) {
		double adjRandIndex=0.0;
		int numberOfClasses = 0;
		HashMap<String, Integer> referencePartition = new HashMap<String, Integer>();
		try {
			if (!referencePartitionFile.exists()) {
				throw new FileNotFoundException();
			}
			if (!referencePartitionFile.isFile()) {
				throw new FileNotFoundException();
			}
			if (!referencePartitionFile.canRead()) {
				throw new IOException();
			}
			
			String lineStr;
			FileReader fr     = new FileReader(referencePartitionFile);
			BufferedReader br = new BufferedReader(fr);
			while ((lineStr = br.readLine()) != null) {
				String[] st = lineStr.split("\\s+");
				String user = st[0];
				int c = Integer.parseInt(st[1]);
				referencePartition.put(user, new Integer(c));
			    	
				if (c > numberOfClasses){
					numberOfClasses = c;
				}
			} 
			br.close();
		}
		catch (FileNotFoundException e) {
			logger.error("Could not find filename " + referencePartitionFile.getAbsolutePath());
		    e.printStackTrace();
		    System.exit(1);
		}
		catch (IOException e) {
			logger.error("Error while reading filename " + referencePartitionFile.getAbsolutePath());
		    e.printStackTrace();
		    System.exit(1);
		}	
		
		// Since class ids start from 0, add one 
		numberOfClasses ++;
			
		// Compute n_{i,j} = number of items that belong in cluster i of partition A and cluster j of partition B 
		double [][] crossCounts = new double[numberOfClasses][];
		for (int n=0; n<numberOfClasses; n ++){
			crossCounts[n] = new double[numberOfExperts];
			for (int j=0; j<numberOfExperts; j++){
				crossCounts[n][j] = 0.0;
			}
		}
		
		try {
			if (!hypPartitionFile.exists()) {
				throw new FileNotFoundException();
			}
			if (!hypPartitionFile.isFile()) {
				throw new FileNotFoundException();
			}
			if (!hypPartitionFile.canRead()) {
				throw new IOException();
			}
			
			numberOfUsers = 0;
			String lineStr;
			FileReader fr     = new FileReader(hypPartitionFile);
			BufferedReader br = new BufferedReader(fr);
			while ((lineStr = br.readLine()) != null) {
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
			br.close();
		}
		catch (FileNotFoundException e) {
			logger.error("Could not find filename " + hypPartitionFile.getAbsolutePath());
		    e.printStackTrace();
		    System.exit(1);
		}
		catch (IOException e) {
			logger.error("Error while reading filename " + hypPartitionFile.getAbsolutePath());
		    e.printStackTrace();
		    System.exit(1);
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
	
	
	public void readAttributeNames(File attributeNamesFile){
		attributeName = new HashMap<Integer,String>();
		attribute2Index = new HashMap<String,Integer>();
		String lineStr;
		try {
			if (!attributeNamesFile.exists()) {
			      throw new FileNotFoundException();
		    }
		    if (!attributeNamesFile.isFile()) {
			      throw new FileNotFoundException();
			}
			if (!attributeNamesFile.canRead()) {
			      throw new IOException();
			}
			
			FileReader fr     = new FileReader(attributeNamesFile);
		    BufferedReader br = new BufferedReader(fr);
		    String[] st;
	
		    while ((lineStr = br.readLine()) != null){
		    	st = lineStr.split("\\t");
		    	int indx = Integer.parseInt(st[0]);
		    	attributeName.put(new Integer(indx), st[1]);
		    	attribute2Index.put(st[1], new Integer(indx));
		    } 
		    br.close();
		} 
		catch (FileNotFoundException e) { 
		    logger.error("Cannot find filename " + attributeNamesFile.getAbsolutePath());
		    e.printStackTrace();
		    System.exit(1);
		}	
		catch (IOException e) { 
		    logger.error("Error while reading filename " + attributeNamesFile.getAbsolutePath());
		    e.printStackTrace();
		    System.exit(1);
		}	
	}
    
	
	protected float[][] calculateProbClusterGivenAttribute(){
		float tmpSum=0.0f;
		float probOfClusterGivenAttribute;
		float[][] posteriorProb = new float[numberOfExperts][];
		for (int expert=0; expert<numberOfExperts; expert ++)
			posteriorProb[expert] = new float[numberOfAttributes];
		float [] generativeProbability = new float[numberOfExperts];
		
		
		for (int att=0; att<numberOfAttributes; att ++){
			getAttributeClusterLogProbs(att,generativeProbability);
				                      
			for (int c=0; c<numberOfExperts; c++)
				generativeProbability[c] += logProbOfCluster[c];
				
			for (int c=0; c<numberOfExperts; c++){
				boolean zeroProb=false;
				probOfClusterGivenAttribute = 0.0f;
				tmpSum=0.0f;
				for (int k=0; k<numberOfExperts; k++){
					float tmp = generativeProbability[k]-generativeProbability[c];
					
					if (tmp > 15.0){
						zeroProb = true;
						break;
					}
					else {
						tmpSum += Math.exp(tmp);	
					}
				}
				if (!zeroProb) 
					probOfClusterGivenAttribute = 1.0f / tmpSum;
					
				posteriorProb[c][att] = probOfClusterGivenAttribute;
			}
			
		}
		return(posteriorProb);
	}
	



	
	// for each item, rank attributes according to popularity, i.e. \sum_{c} p(attribute|c)p(c|item)
	public void calculateMostPopularAttributesPerUser(String outFilename, int maxAttributes) {
		try {
			int maxStoredAttributes = numberOfAttributes / 10;
			double[] userScores = new double[numberOfAttributes];
			ArrayList<AttributeValue<String>> tokens = new ArrayList<AttributeValue<String>>();
			ArrayList<ArrayList<AttributeValue<String>>> rankedProbs = 
				new ArrayList<ArrayList<AttributeValue<String>>>(numberOfExperts);
			File awFile = new File(outFilename);
			float[][] probAttributeGivenCluster = new float[numberOfAttributes][];
			for (int att=0; att<numberOfAttributes; att++)
				probAttributeGivenCluster[att] = new float[numberOfExperts];
			for (int att=0; att<numberOfAttributes; att ++){
				getAttributeClusterLogProbs(att , probAttributeGivenCluster[att]);
				for (int c=0; c<numberOfExperts; c++)
					if (probAttributeGivenCluster[att][c] > -15.0)
						probAttributeGivenCluster[att][c] = (float)Math.exp(probAttributeGivenCluster[att][c]);
					else
						probAttributeGivenCluster[att][c] = (float)0.0;
			}
			
			// Create the most popular attributes per cluster
			String[] mostProbableAttributesPerCluster = new String[numberOfExperts];
			for (int c=0; c<numberOfExperts; c++){
				tokens = new ArrayList<AttributeValue<String>>(numberOfAttributes);
				for (int att=0; att<numberOfAttributes; att ++){
	//				tokens[att] = new AttributeValue<String>(attributeName[att], probAttributeGivenCluster[att][c]);
					tokens.add(att, new AttributeValue<String>(Integer.toString(att), probAttributeGivenCluster[att][c]));
				}
				Collections.sort(tokens);
				
				// Store the top-N attributes per cluster
				int attributesStored=0;
				rankedProbs.add(c, new ArrayList<AttributeValue<String>>(maxStoredAttributes));
				for (AttributeValue<String> attObj : tokens){
					
					if (attributesStored >= maxStoredAttributes)
		    			break;
					
					rankedProbs.get(c).add(new AttributeValue<String>(attObj.getIndex(), attObj.getValue()));
					attributesStored ++;
				}
				int attributesShown=0;
				StringBuffer sb= new StringBuffer();
		    	for (AttributeValue<String> attObj : tokens){
		    		float score = attObj.getValue();
		    		String attribute = attObj.getIndex();
		    		
		    		if (attributesShown >= maxAttributes)
		    			break;
		    		
		    		sb.append(" "); sb.append(attribute); sb.append(" "); sb.append(score);
		    		attributesShown ++;
		    	}
		    	sb.append("\n");
		    	mostProbableAttributesPerCluster[c] = sb.toString();
			}
			
			
		
			// Go through the users and output the most probable attributes
		    Writer output = new BufferedWriter( new FileWriter(awFile) );
		    for (Iterator<Map.Entry<String, ArrayList<AttributeValue<Integer>>>> it=
		    	users.entrySet().iterator(); it.hasNext();){
		    	// If a user belongs to a single cluster then use cluster's most probable attributes
		    	boolean belongsToSingleCluster = false;
		    	Map.Entry<String, ArrayList<AttributeValue<Integer>>> e = it.next();
		    	HashMap<Integer, Float> ht = probOfClusterGivenUser.get(e.getKey());
		    	for (Iterator<Map.Entry<Integer,Float>> it2 = ht.entrySet().iterator(); it2.hasNext();){
		    		 Map.Entry<Integer, Float> e2 = it2.next();
		    		 if (e2.getValue().floatValue() >= 0.99){
		    			 StringBuffer sb= new StringBuffer();
		    			 sb.append(e.getKey());
		    			 sb.append(mostProbableAttributesPerCluster[e2.getKey().intValue()]);
		    			 output.write(sb.toString());
		    			 belongsToSingleCluster = true;
		    			 break;
		    		 }
		    	}
		    	
		    	if (belongsToSingleCluster){
		    		continue;
		    	}
		    	
		    	// User belongs to multiple clusters, need to combine most probable attributes from multiple clusters
		    	for (int att=0; att<numberOfAttributes; att ++){
		    		userScores[att] = 0.0;
		    	}
				for (Iterator<Map.Entry<Integer,Float>> it2 = ht.entrySet().iterator(); it2.hasNext();){
					Map.Entry<Integer, Float> e2 = it2.next();
					if (e2.getValue().floatValue() < 0.001){
						continue;
					}
					for (AttributeValue<String> attObj : rankedProbs.get(e2.getKey().intValue())){
						
						int att = Integer.parseInt(attObj.getIndex());
						float score =  e2.getValue().floatValue() * attObj.getValue();
						
						userScores[att] += score;
					}
				}
				int nnz=0;
				for (int att=0; att<numberOfAttributes; att ++){
					if (userScores[att] != 0.0)
						nnz ++;
				}
				tokens = new ArrayList<AttributeValue<String>>(nnz);
				int cnt=0;
				for (int att=0; att<numberOfAttributes; att ++){
					if (userScores[att] == 0.0)
						continue;
	//				tokens.add(new AttributeValue<String>(attributeName[att], userScores[att]));
					tokens.add(new AttributeValue<String>(Integer.toString(att), (float)userScores[att]));
					cnt ++;
				}
				Collections.sort(tokens);
				int attributesShown=0;
				StringBuffer sb= new StringBuffer();
		    	sb.append(e.getKey()); 
		    	for (AttributeValue<String> attObj : tokens){
		    		float score = attObj.getValue();
		    		String attribute = attObj.getIndex();
		    		
		    		if (attributesShown >= maxAttributes) 
		    			break;
		    		
		    		sb.append(" "); sb.append(attribute); sb.append(" "); sb.append(score);
		    		attributesShown ++;
		    	}
		    	sb.append("\n");
		    	output.write(sb.toString());
			}
			output.close();
		}
		catch (IOException e){
			logger.error("ERROR: Cannot write to " + outFilename);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public void writeAttributeAssociations(String associationsFilename, int maxAssociations){
		try {
		    File awFile = new File(associationsFilename);
		    float[][] attributePosteriorProb = calculateProbClusterGivenAttribute();
		    float[] probOfCluster = new float[numberOfExperts];
			for (int c=0; c<numberOfExperts; c++)
				probOfCluster[c] = (float)Math.exp((double)logProbOfCluster[c]);
		    float[][] attributeDistinctiveness = new float[numberOfExperts][];
		    for (int c=0; c<numberOfExperts; c++)
		    	attributeDistinctiveness[c] = new float[numberOfAttributes];
		    for (int c=0; c<numberOfExperts; c++)
		    	for (int i=0; i<numberOfAttributes; i ++){
		    		if (attributePosteriorProb[c][i] > 0.01 * probOfCluster[c])
		    			attributeDistinctiveness[c][i] = attributePosteriorProb[c][i] / probOfCluster[c];
		    		else
		    			attributeDistinctiveness[c][i] = (float)0.0;
		    	}
		    
		    Writer output = new BufferedWriter( new FileWriter(awFile) );
		    for (int att=0; att<numberOfAttributes; att ++){
		    	ArrayList<AttributeValue<String>> tokensList = new ArrayList<AttributeValue<String>>(numberOfAttributes);
		    	tokensList.clear();
		    	for (int i=0; i<numberOfAttributes; i ++){
		    		if (att == i){
		    			continue;
		    		}
		    		float score = 0.0f;
		    		for (int c=0; c<numberOfExperts; c ++){
		    			if ((attributeDistinctiveness[c][i] <= 0.001) || (attributePosteriorProb[c][att] <= 0.001)){
		    				continue;
		    			}
		    			score +=  attributeDistinctiveness[c][i] * attributePosteriorProb[c][att];
		    		}
		    		if (score > 0.0f)
		    			tokensList.add(new AttributeValue<String>(attributeName.get(i), score));
		    	}
		    	Collections.sort(tokensList);
		    	int associationsShown=0;
		    	StringBuffer sb = new StringBuffer();
		    	for (AttributeValue<String> attObj : tokensList){
		    		float score = attObj.getValue();
		    		String attribute = attObj.getIndex();
	
		    		if  (associationsShown >= maxAssociations)
		    			break;
	
		    		sb.append("\"" + attributeName.get(att) + "\"");
		    		sb.append(",\"");  sb.append(attribute); sb.append("\",\""); sb.append(score); sb.append("\"\n");
	
		    		associationsShown ++;
		    	}
		    	output.write(sb.toString());
			}
		    output.close();
		}
		catch (IOException e){
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	}
	
	
// Associations can be calculated for a list of attributes rather than an individual attribute
// This code is optimized for the case where the inputFilename contains only a small portion of all attributes
	public void writeAttributeAssociations(String inputFilename, String outputFilename, int maxAssociations){
		try {
			File aFile = new File(inputFilename);
			
			if (!aFile.exists()) {
				throw new IOException();
			}
			if (!aFile.isFile()) {
				throw new IOException();
			}
			if (!aFile.canRead()) {
				throw new IOException();
			}
	
		    File awFile = new File(outputFilename);
		    Writer output = new BufferedWriter( new FileWriter(awFile) );
		    ArrayList<AttributeValue<String>> tokensList = new ArrayList<AttributeValue<String>>(300);
		    float[] logProbOfAttributeGivenCluster = new float[numberOfExperts];
		    double[][] logQuant = new double[numberOfExperts][];
		    for (int c=0; c<numberOfExperts; c++)
		    	logQuant[c] = new double[numberOfExperts];
		    
		    // Calculate the distinctiveness of each attribute, i.e p(c|att)/p(c) for each cluster
		    ArrayList<HashMap<String,Float>> attributeScores = null;
	    
	    	FileReader fr     = new FileReader(aFile);
	    	BufferedReader br = new BufferedReader(fr);
	    	String lineStr;
	    	while ((lineStr = br.readLine()) != null) {
	    		String[] st = lineStr.split(",");
	    		String category = st[0];
	    		StringBuffer sb2 = new StringBuffer();
	    		sb2.append("\"" + category + "\"");
	    		
	    		boolean found=false;

	    		// Compute p(c|list_of_seed_terms)
	    		for (int c=0; c<numberOfExperts; c++)
	    			for (int r=0; r<numberOfExperts; r++)
	    				logQuant[r][c] = logProbOfCluster[r] - logProbOfCluster[c];
	    		for (int i=1; i<st.length; i++){
	    			Integer indx = attribute2Index.get(st[i]);
	    			if (indx == null)
	    				continue;
                		
	    			found=true;
//                		System.out.print(" " + st[i]);
	    			sb2.append(",\"");  sb2.append(st[i]); sb2.append("\"");
	    			int attribute = indx.intValue();
	    			getAttributeClusterLogProbs(attribute,logProbOfAttributeGivenCluster);
	    			for (int c=0; c<numberOfExperts; c++)
	    				for (int r=0; r<numberOfExperts; r++){
	    					logQuant[r][c] += logProbOfAttributeGivenCluster[r] - logProbOfAttributeGivenCluster[c];
	    				}
	    		}
	    		sb2.append("\n");
                	
                	
	    		if (! found){
	    			StringBuffer sb = new StringBuffer();
	    			sb.append("\"" + category + "\"");
	    			sb.append(",\"NULL\"\n"); 
//	    			output.write(sb2.toString());
                		output.write(sb.toString());
	    			continue;
	    		}

                	
	    		double[] conditionalProb = new double[numberOfExperts];
	    		for (int c=0; c<numberOfExperts; c++){
	    			boolean zeroProb = false;
	    			for (int r=0; r<numberOfExperts; r++){
	    				if (logQuant[r][c] > 15.0){
	    					conditionalProb[c] = 0.0;
	    					zeroProb = true;
	    					break;
	    				}
	    				else {
	    					conditionalProb[c] += Math.exp(logQuant[r][c]);
	    				}
	    			}
	    			if (! zeroProb)
	    				conditionalProb[c] = 1.0 / conditionalProb[c];
	    		}

	    		tokensList.clear();
	    		for (int i=0; i<numberOfAttributes; i ++){
	    			float score = 0.0f;
	    			for (int c=0; c<numberOfExperts; c ++){
	    				if (! attributeScores.get(c).containsKey(attributeName.get(i))){
	    					continue;
	    				}
	    				Float dprob=attributeScores.get(c).get(attributeName.get(i));
	    				score += dprob.floatValue() * conditionalProb[c];
	    			}
	    			if (score > 0.0f)
	    				tokensList.add(new AttributeValue<String>(attributeName.get(i), score));
	    		}
	    		Collections.sort(tokensList);
	    		int associationsShown=0;
	    		StringBuffer sb = new StringBuffer();
	    		for (AttributeValue<String> attObj : tokensList){
	    			float score = attObj.getValue();
	    			String attribute = attObj.getIndex();

	    			if  (associationsShown >= maxAssociations)
	    				break;

	    			sb.append("\"" + category + "\"");
	    			sb.append(",\"");  sb.append(attribute); sb.append("\","); sb.append(score); sb.append("\n");

	    			associationsShown ++;
	    		}
 //               	output.write(sb2.toString());
	    		output.write(sb.toString());
	    	}
	    	output.close();
	    }
	    catch (IOException e){
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	}
	
	
	/** 
	 * Generate a description for each cluster by showing the most distinctive attributes per cluster
	 * <p>
	 * Attributes are ranked according to [p(attribute | cluster) / p(attribute)] 
	 * 
	 * @param descriptionFilename  Output; contains ranked list of distinctive attributes for each cluster
	 * @param maxAttributes Output; maximum number of attributes per cluster displayed 
	 */
	public void writeClusterDescriptions(File descriptionFile, int maxAttributes) {
		try {
		    Writer output = new BufferedWriter( new FileWriter(descriptionFile) );
			for (int c=0; c<numberOfExperts; c++){
				ArrayList<AttributeValue<Integer>> rankedList = calculateDistinctiveScore(c);
				int attributesShown=0;
				StringBuffer  sb = new StringBuffer();
		    	for (AttributeValue<Integer> attObj : rankedList){
		    		float score = attObj.getValue();
		    		String attribute = attributeName.get(attObj.getIndex());
		    		
		    		if (maxAttributes != 0 && attributesShown >= maxAttributes)
		    			break;
		    		
	                sb.append(c); sb.append("\t");
		    		sb.append(attribute); sb.append("\t"); sb.append(score); sb.append("\n");
		    		attributesShown ++;
		    	}
		    	output.write(sb.toString());
		    	output.flush();
			}
			output.close();
	    }
		catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public abstract ArrayList<AttributeValue<Integer>> calculateDistinctiveScore(int expert);
	public abstract ArrayList<AttributeValue<Integer>> calculateDistinctiveScore();
	
	public void writeClusterModels(File descriptionFile, int maxAttributes) {
		try {
		    Writer output = new BufferedWriter( new FileWriter(descriptionFile) );
		    // Write the global attribute probs first
		    ArrayList<AttributeValue<Integer>> rankedList = calculateDistinctiveScore();
			int attributesShown=0;
			StringBuffer  sbIndx = new StringBuffer();
			StringBuffer  sbScore = new StringBuffer();
			boolean firstTime=true;
	    	for (AttributeValue<Integer> attObj : rankedList){
	    		
	    		if (maxAttributes != 0 && attributesShown >= maxAttributes)
	    			break;
	    		
	    		if (! firstTime){
	    			sbScore.append(" "); 
		    		sbIndx.append(" "); 
	    		}
	    		sbScore.append(attObj.getValue());
	    		sbIndx.append(attObj.getIndex().intValue());
	    		
	    		firstTime = false;
	    		attributesShown ++;
	    	}
	    	sbIndx.append("\n");
	    	sbScore.append("\n");
	    	output.write(sbIndx.toString());
	    	output.write(sbScore.toString());
	    	output.flush();
	    	
	    	// Write the distinctive attribute for each cluster
			for (int c=0; c<numberOfExperts; c++){
				rankedList = calculateDistinctiveScore(c);
				attributesShown=0;
				sbIndx = new StringBuffer();
				sbScore = new StringBuffer();
				firstTime=true;
		    	for (AttributeValue<Integer> attObj : rankedList){
		    		
		    		if (maxAttributes != 0 && attributesShown >= maxAttributes)
		    			break;
		    		
		    		if (! firstTime){
		    			sbScore.append(" "); 
		    			sbIndx.append(" ");
		    		}
		    		sbScore.append(attObj.getValue());
		    		sbIndx.append(attObj.getIndex().intValue());
		    		
		    		firstTime = false;
		    		attributesShown ++;
		    	}
		    	sbIndx.append("\n");
		    	sbScore.append("\n");
		    	output.write(sbIndx.toString());
		    	output.write(sbScore.toString());
		    	output.flush();
			}
			output.close();
	    }
		catch (IOException e){
			logger.error("Could not write to " + descriptionFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void calculateTopN(File expertMembershipsFile, File outAssociations, int topN) {
		calculateTopN(expertMembershipsFile, outAssociations, 0.3f, topN);
	}
	
	public void calculateTopN(File expertMembershipsFile, File outAssociations, float similarityThreshold, int topN) {
		
		// Setup writing
		Writer output = null;
		try {
			output = new BufferedWriter( new FileWriter(outAssociations) );
		}
		catch (IOException e){
			logger.error("Could not write to " + outAssociations.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
		
		// Read the expert memberships
		HashMap<String, HashMap<Integer, Float>> expertMemberships = new HashMap<String, HashMap<Integer, Float>>();
		if (!expertMembershipsFile.exists()) {
			logger.error("File does not exist " + expertMembershipsFile.getAbsolutePath());
			System.exit(-1);
		}
		if (!expertMembershipsFile.isFile()) {
			logger.error("Not a file " + expertMembershipsFile.getAbsolutePath());
			System.exit(-1);
		}
		if (!expertMembershipsFile.canRead()) {
			logger.error("No read rights on " + expertMembershipsFile.getAbsolutePath());
			System.exit(-1);
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(expertMembershipsFile));
			numberOfUsers=0;
			String lineStr;
			
		    while ((lineStr = br.readLine()) != null) {
		    	String[] fields = lineStr.split(" ");
		    	String id = fields[0];
		    	
		    	if (expertMemberships.containsKey(id)) {
		    		logger.error("Id \"" + id + "\" already exists");
		    		continue;
		    	}
		    	
		    	HashMap<Integer, Float> hm = new HashMap<Integer, Float>();
		    	expertMemberships.put(id, hm);
		    	for (int i = 1; i < fields.length; i ++) {
		    		String[] expertFields = fields[i].split(":");
		    		if (expertFields.length != 2){
		    			continue;
		    		}
		    		
		    		int expert = -1;
		    		try {
		    			expert = Integer.parseInt(expertFields[0]);
		    		}
		    		catch (NumberFormatException ex) {
		    			logger.error("Cannot parse expert index " + expertFields[0]);
		    			System.exit(-1);
		    		}
		    		
		    		float membership = 0.0f;
		    		try {
		    			membership = Float.parseFloat(expertFields[1]);
		    		}
		    		catch (NumberFormatException ex) {
		    			logger.error("Cannot parse expert membership " + expertFields[1]);
		    			System.exit(-1);
		    		}
		    		
		    		hm.put(expert, membership);
		    	}
		    }
		    br.close();
		}
		catch (IOException e){
			logger.error("Could not read from " + expertMembershipsFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
		
		// Now calculate the top N
		// First calculate the norm for each id
		HashMap<String, Float> norms = new HashMap<String, Float>();
		for (Map.Entry<String, HashMap<Integer, Float>> me : expertMemberships.entrySet()) {
			String id = me.getKey();
			HashMap<Integer, Float> hm = me.getValue();
			float norm = 0.0f;
			for (Map.Entry<Integer, Float> experts : hm.entrySet()) {
				float m = experts.getValue();
				norm += m * m;
			}
			norm = norm > 0.0f ? (float)Math.sqrt(norm) : 0.0f;
			
			norms.put(id, norm);
		}
		
		// Now do pairwise calculations
		for (Map.Entry<String, HashMap<Integer, Float>> meA : expertMemberships.entrySet()) {
			String idA = meA.getKey();
			HashMap<Integer, Float> hmA = meA.getValue();
			float normA = norms.get(idA);
			
			List<AttributeValue<String>> topNList = new ArrayList<AttributeValue<String>>();
			for (Map.Entry<String, HashMap<Integer, Float>> meB : expertMemberships.entrySet()) {
				String idB = meB.getKey();
				if (idA.equals(idB)) {
					continue;
				}
				HashMap<Integer, Float> hmB = meB.getValue();
				
				float similarity = 0.0f;
				for (Map.Entry<Integer, Float> expertsA : hmA.entrySet()) {
					Float m = hmB.get(expertsA.getKey());
					if (m == null) {
						continue;
					}
					similarity += m * expertsA.getValue();
				}
				float normB = norms.get(idB);
				similarity /= (normA * normB);
				
				
				if (similarity < similarityThreshold) {
					continue;
				}
				
				topNList.add(new AttributeValue<String>(idB, similarity));
			}
			Collections.sort(topNList);
			
			topNList = topNList.size() < topN ? topNList : topNList.subList(0, topN);
			if (topNList.size() == 0) {
				continue;
			}
			
			StringBuffer sb = new StringBuffer();
		    sb.append(idA); 
		    for (AttributeValue<String> av : topNList) {
		    	sb.append(";"); sb.append(av.getIndex()); sb.append(";"); sb.append((float)Math.round(av.getValue() * 1000)/10.0f);
		    }
		    sb.append(newline);
		    
		    try {
			    output.write(sb.toString());
			}
			catch (IOException e){
				logger.error("Could not write to " + outAssociations.getAbsolutePath());
				e.printStackTrace();
				System.exit(1);
			}
			
		}
		try {
			output.close();
		}
		catch (IOException e){
			logger.error("Could not write to " + outAssociations.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
	}
}


