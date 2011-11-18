package gr.infolytics.models.mixtureOfExperts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.Math;
import java.lang.Float;
import java.lang.Integer;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Fits a mixture of multinomial distributions on the provided data. 
 * 
 * @author Kostas Boulis
 */



public class MixtureOfMultinomials extends MixtureOfExperts{
	private ArrayList<HashMap<Integer, Float>> logProbAttributeGivenExpert;
	private HashMap<Integer, Float> logProbAttribute;
	private double alpha;
	private float[] logConstant;
	protected DirichletPrior prior;                          
	private final Logger logger = LoggerFactory.getLogger(getClass());
	/** Sufficient statistics counts */
	private float[] SS0;
	private List<HashMap<Integer, Float>> SS1;
	private HashMap<Integer, Float> globalSS1;
	
	// Main constructor
	MixtureOfMultinomials(int nExperts, boolean hardAssignments,
							double _alpha, String priorName, String priorResourcesName){
		super(nExperts, hardAssignments);
		
		alpha = _alpha;
		allocateParameters();
		SS0 = new float[numberOfExperts];
		SS1 = new ArrayList<HashMap<Integer, Float>>(numberOfExperts);
		for (int expert=0; expert<numberOfExperts; expert ++)
			SS1.add(expert, new HashMap<Integer, Float>());
		globalSS1 = new HashMap<Integer, Float>();
		
		if ((prior = DirichletPriorFactory.getPrior(priorName, numberOfExperts, (float)alpha, priorResourcesName)) == null){
			logger.error("Unknown prior " + priorName);
			System.exit(1);
		}
	}
	
	// Constructor used in ClusterReduce
	MixtureOfMultinomials(int nExperts, double _alpha, String priorName, String priorResourcesName){
		super(nExperts);
		
		alpha = _alpha;
		allocateParameters();
		
		if ((prior = DirichletPriorFactory.getPrior(priorName, numberOfExperts, (float)alpha, priorResourcesName)) == null){
			logger.error("Unknown prior " + priorName);
			System.exit(1);
		}
	}
	
	private void allocateParameters(){
		logProbAttribute = new HashMap<Integer, Float>();
		logConstant = new float[numberOfExperts];
		logProbAttributeGivenExpert = new ArrayList<HashMap<Integer, Float>>(numberOfExperts);
		for (int expert=0; expert<numberOfExperts; expert ++)
			logProbAttributeGivenExpert.add(expert, new HashMap<Integer, Float>());
	}
	
	
//TODO: Make the writes more efficient
	public void readModels(File modelsFile){
		logger.info("Reading models from " + modelsFile.getAbsolutePath());
	    try { 
			if (!modelsFile.exists()) {
				throw new FileNotFoundException();
		    }
		    if (!modelsFile.isFile()) {
		    	throw new FileNotFoundException();
			}
			if (!modelsFile.canRead()) {
				logger.error("Cannot read " + modelsFile.getAbsolutePath());
				throw new IOException();
			}
	    	FileInputStream file_input = new FileInputStream (modelsFile);
		    DataInputStream data_in    = new DataInputStream (file_input);
		    for (int k=0; k<numberOfExperts; k++)
		    	logProbOfCluster[k] = data_in.readFloat();
		    for (int expert=0; expert<numberOfExperts; expert ++){
		    	int numEntries = data_in.readInt();
		    	for (int k=0; k<numEntries-1; k=k+2){
		    		
		    		Integer attribute = data_in.readInt();
		    		Float logProb = data_in.readFloat();
	    		
		    		logProbAttributeGivenExpert.get(expert).put(new Integer(attribute) , new Float(logProb));
		    	}
		    }
		    for (int expert=0; expert<numberOfExperts; expert ++)
		    	logConstant[expert] = data_in.readFloat();
		    
		    file_input.close();
		} 
	    catch (FileNotFoundException e) { 
			logger.error("Cannot find file " + modelsFile.getAbsolutePath());
			e.printStackTrace();
		    System.exit(1);
		}
		catch (IOException e) { 
		    e.printStackTrace();
		    System.exit(1);
		 }	
		
	}
	
	// TODO: Make the writes more efficient
	public void writeModels(File modelsFile) {
		logger.info("Writing models to " + modelsFile.getAbsolutePath());
		try {	
			FileOutputStream file_output = new FileOutputStream (modelsFile);
		    DataOutputStream data_out = new DataOutputStream (file_output);
		    for (int expert=0; expert<numberOfExperts; expert ++){
		    	data_out.writeFloat(logProbOfCluster[expert]);
		    }
		    
		    for (int expert=0; expert<numberOfExperts; expert ++){
		    	int numEntries = 2 * logProbAttributeGivenExpert.get(expert).size();
		    	data_out.writeInt(numEntries);
		    	for ( Iterator<Map.Entry<Integer, Float>> it = logProbAttributeGivenExpert.get(expert).entrySet().iterator(); it.hasNext(); ) {
		    	    Map.Entry<Integer, Float> e = it.next();
		    	    Integer key = e.getKey();
		    		Float value = e.getValue();
		    		data_out.writeInt(key.intValue());	
		    		data_out.writeFloat(value.floatValue());
		    	}
		    }
		    for (int expert=0; expert<numberOfExperts; expert ++){
		    	data_out.writeFloat(logConstant[expert]);
		    }
		    file_output.close();
		 }
		 catch (IOException e) {
			 logger.error("Error while writing to " + modelsFile.getAbsolutePath());
			 e.printStackTrace();
			 System.exit(1);
		 }
	}
	
		
	public void getAttributeClusterLogProbs(int attribute, float [] logProbVector){
		Integer key = new Integer(attribute);
		for (int expert=0; expert<numberOfExperts; expert ++){
			Float dlogProb = (Float)logProbAttributeGivenExpert.get(expert).get(key);
			if (dlogProb != null){
				logProbVector[expert] = dlogProb.floatValue();
			}
			else {
				logProbVector[expert] = (prior.getAttributeLogHyperparameter(expert,attribute)
										- logConstant[expert]);
			}
		}
	}
	
	protected void calculateLogProbOfUserGivenCluster(List<AttributeValue<Integer>> profile, 
			float[] logUserProb){	
		for (int expert=0; expert<numberOfExperts; expert ++)
			logUserProb[expert] = 0.0f;
		for (AttributeValue<Integer> att : profile){
			Integer attribute = Integer.valueOf(att.getIndex());
			float count = att.getValue();
			
			for (int expert=0; expert<numberOfExperts; expert ++){
				Float logProb = logProbAttributeGivenExpert.get(expert).get(attribute);
				if (logProb != null)
					logUserProb[expert] += count * logProb.floatValue();
				else {
					float logh = prior.getAttributeLogHyperparameter(expert, attribute.intValue());
					logUserProb[expert] += count * (logh - logConstant[expert]);
				}
					
			}
		}
	}
	
	private void zeroSufficientStatistics(){
		for (int c=0; c<numberOfExperts; c++){
			SS0[c] = 0.0f;
			SS1.get(c).clear();
		}
		globalSS1.clear();
	}
	
	private void randomInitialization(BufferedReader br) throws IOException{
		zeroSufficientStatistics();
		Random generator = new Random();
		String lineStr;
		while ((lineStr = br.readLine()) != null) {
			int c = generator.nextInt(numberOfExperts);
			
			String[] st = lineStr.split("\\s+");
			if (st.length % 2 == 0){
				logger.error("Incorrect number of arguments for profile " + lineStr);
				throw new IOException();
			}
			for (int i=1; i<st.length-1; i=i+2){
				Integer att = Integer.parseInt(st[i]);
				float value = Float.parseFloat(st[i+1]);
				
				Float prevCnt = SS1.get(c).get(att);
				if (prevCnt == null)
					SS1.get(c).put(new Integer(att), new Float(value));
				else
					SS1.get(c).put(new Integer(att), new Float(prevCnt.floatValue() + value));
			}
			SS0[c] ++;
		}
	}
	
	private void setInitialCluster(int c, List<AttributeValue<Integer>> profile){
		float tot=0.0f;
		for (AttributeValue<Integer> av: profile)
			tot += 2000.0f * av.getValue();
		HashMap<Integer, Float> hm = logProbAttributeGivenExpert.get(c);
		for (AttributeValue<Integer> av: profile){
			float n = prior.getAttributeHyperparameter(c,av.getIndex().intValue());
			float v = (float)Math.log((2000.0f * av.getValue() + n)/ (tot + prior.getSumOfHyperparameters(c)));
			hm.put(new Integer(av.getIndex()), new Float(v));
		}
		logProbOfCluster[c] = (float)Math.log(1.0f / (float)numberOfExperts);
		logConstant[c] = (float)Math.log(tot + prior.getSumOfHyperparameters(c));
	}
	
	public void initializationWithSeeds(File data){
		try {
        	if (!data.exists()) {
    			logger.error("File " +  data.getAbsolutePath() + " does not exist");
        		throw new FileNotFoundException();
    	    }
    	    if (!data.isFile()) {
    	    	logger.error("File " +  data.getAbsolutePath() + " is not a file");
    	    	throw new FileNotFoundException();
    		}
    		if (!data.canRead()) {
    			logger.error("File " +  data.getAbsolutePath() + " cannot be read");
    			throw new IOException();
    		}
    		logger.info("Seed nitialization, reading from " + data.getAbsolutePath());
    		HashMap<String, Float> minDistances = new HashMap<String, Float>();
        	BufferedReader br = new BufferedReader(new FileReader(data));
        	// First cluster, choose uniformly from the data
    		String lineStr;
    		int numProfiles=1;
    		String currentChoice=null;
    		Random generator = new Random();
    		while ((lineStr = br.readLine()) != null) {
    			int c = generator.nextInt(numProfiles);
    			
    			if (c == 0)
    				currentChoice = lineStr;
    			
    			numProfiles ++;
    		}
        	br.close();
			
			List<AttributeValue<Integer>> profile = createUserProfile(currentChoice.split("\\s+"));
			logger.info("Initializing cluster 0 with user " + currentChoice.split("\\s+")[0]);
			setInitialCluster(0, profile);
			
        	// For clusters = 2..numberOfExperts sample according to min distance from existing clusters
			for (int c=1; c<numberOfExperts; c++){
				float totDist=0.0f;
				br = new BufferedReader(new FileReader(data));
				while ((lineStr = br.readLine()) != null) {
					String[] st = lineStr.split("\\s+");
					// Get the distance of the current profile from the previous cluster
					profile = createUserProfile(st);
					float logProb=0.0f;
					for (AttributeValue<Integer> att : profile){
						Integer attribute = Integer.valueOf(att.getIndex());
						float count = att.getValue();
						
						Float logProbAtt = logProbAttributeGivenExpert.get(c-1).get(attribute);
						if (logProbAtt != null)
							logProb += count * logProbAtt.floatValue();
						else {
							float logh = prior.getAttributeLogHyperparameter(c-1, attribute.intValue());
							logProb += count * (logh - logConstant[c-1]);
						}
					}
					
					String user = st[0];
					Float d = minDistances.get(user);
					float dist = -1.0f * logProb;
					if (d == null)
						minDistances.put(user, new Float(dist));
					else {
						float v = d.floatValue();
						if (v > dist )
							minDistances.put(user, new Float(dist));
					}
					
					float v = minDistances.get(user).floatValue();
					totDist += v;
					
					float sampleProb = v / totDist;
					
					float p = generator.nextFloat();
					
					if (p <= sampleProb)
						currentChoice = lineStr;
					
				}
				br.close();
				
				profile = createUserProfile(currentChoice.split("\\s+"));
				logger.info("Initializing cluster " + c + " with user " + currentChoice.split("\\s+")[0]);
				setInitialCluster(c, profile);
			}
			
        	// Now that all clusters are seeded, generate suff. stats
			this.calculateSufficientStatistics(data);
			
        }
        catch (FileNotFoundException e) { 
        	logger.error("Cannot find file " + data.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }	
        catch (IOException e) { 
        	logger.error("Error while reading file " + data.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }		
		
		
	}
	
	public void randomInitialization(File data){
		try {
        	if (!data.exists()) {
    			logger.error("File " +  data.getAbsolutePath() + " does not exist");
        		throw new FileNotFoundException();
    	    }
    	    if (!data.isFile()) {
    	    	logger.error("File " +  data.getAbsolutePath() + " is not a file");
    	    	throw new FileNotFoundException();
    		}
    		if (!data.canRead()) {
    			logger.error("File " +  data.getAbsolutePath() + " cannot be read");
    			throw new IOException();
    		}
    		logger.info("Random initialization, reading from " + data.getAbsolutePath());
        	BufferedReader br = new BufferedReader(new FileReader(data));
        	this.randomInitialization(br);
        	br.close();
        }
        catch (FileNotFoundException e) { 
        	logger.error("Cannot find file " + data.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }	
        catch (IOException e) { 
        	logger.error("Error while reading file " + data.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }		
	}
	
	public void randomInitialization(InputStream is){
		try {
			if (is == null){
				logger.error("Null input stream");
				throw new IOException();
			}
			logger.info("Random initialization");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			this.randomInitialization(br);
			br.close();
		}
		catch (IOException e) { 
        	logger.error("Error while reading data from stream");
        	e.printStackTrace();
        	System.exit(1);
        }
	}
	
	private List<AttributeValue<Integer>> createUserProfile(String[] tokenizedProfile){
		try{
			if (tokenizedProfile.length % 2 == 0){
				logger.error("Incorrect number of arguments for profile od user " + tokenizedProfile[0]);
				throw new IOException();
			}
			int numberOfEntries = (tokenizedProfile.length - 1) / 2;
			List<AttributeValue<Integer>> profile = new ArrayList<AttributeValue<Integer>>(numberOfEntries);
			for (int i=1; i<tokenizedProfile.length-1; i=i+2) {
				int att = Integer.parseInt(tokenizedProfile[i]);
	    		float cnt = Float.parseFloat(tokenizedProfile[i+1]);
	    		
	    		if (att < 0){
	    			logger.error("Incorrect attribute value for profile of user " + tokenizedProfile[0]);
	    			throw new IOException();
	    		}
	    		if (cnt <= 0.0f){
	    			logger.error("Incorrect count value for attribute " + att + " in profile of user " + tokenizedProfile[0]);
	    			throw new IOException();
	    		}
	    		// Estimate global prob. of attributes
	    		Float gcount = globalSS1.get(att);
	    		if (gcount == null)
	    			globalSS1.put(new Integer(att), new Float(cnt));
	    		else
	    			globalSS1.put(new Integer(att), new Float(cnt + gcount.floatValue()));
	    		
	    		profile.add(new AttributeValue<Integer>(new Integer(att), new Float(cnt))); 
			}	
			return profile;
		}
		catch (IOException e) { 
        	logger.error("Error while reading profile of user " + tokenizedProfile[0]);
        	e.printStackTrace();
        	return null;
        }	
	}
	
	private void incrementSufficientStatistics(List<AttributeValue<Integer>> userMemberships, 
											   List<AttributeValue<Integer>> profile){
		Collections.sort(userMemberships);
		for (AttributeValue<Integer> att : userMemberships){
			float memb = att.getValue();
			if (memb < estimationPosteriorThreshold)
				break;
			
			
			int expert = att.getIndex().intValue();
			
			for (AttributeValue<Integer> profileAtt : profile){
				float increment = memb * profileAtt.getValue();
				HashMap<Integer, Float> hm = SS1.get(expert);
				if (hm == null){
					hm = new HashMap<Integer, Float>();
					hm.put(profileAtt.getIndex(), new Float(increment));
					SS1.add(expert, hm);
				}
				else {
					Float existingCount = hm.get(profileAtt.getIndex());
					if (existingCount == null)
						hm.put(new Integer(profileAtt.getIndex()), new Float(increment));
					else {
						float updatedCount = increment + existingCount.floatValue();
						hm.put(new Integer(profileAtt.getIndex()), new Float(updatedCount));
					}
				}
			}
			SS0[expert] += memb;
		}
	}
	
	private void calculateSufficientStatisticsFromInitMemberships(BufferedReader profileReader, File memberships){
		zeroSufficientStatistics();
		try {
        	if (!memberships.exists()) {
    			logger.error("File " +  memberships.getAbsolutePath() + " does not exist");
        		throw new FileNotFoundException();
    	    }
    	    if (!memberships.isFile()) {
    	    	logger.error("File " +  memberships.getAbsolutePath() + " is not a file");
    	    	throw new FileNotFoundException();
    		}
    		if (!memberships.canRead()) {
    			logger.error("File " +  memberships.getAbsolutePath() + " cannot be read");
    			throw new IOException();
    		}
        	BufferedReader membershipsReader = new BufferedReader(new FileReader(memberships));
        	String lineStr;
    		logProbOfAllUsers=0.0;
    		numberOfUsers=0;
    		while ((lineStr = profileReader.readLine()) != null) {
    			String[] st = lineStr.split("\\s+");
    			numberOfUsers ++;
    			
    			List<AttributeValue<Integer>> profile = createUserProfile(st);
    			
    			// Read memberships
    			lineStr = membershipsReader.readLine();
    			String[] mst = lineStr.split("\\s+");
    			
    			if (! mst[0].equals(st[0])){
    				logger.error("Expecting profile for user " + st[0] + " but found user " + mst[0]);
    				throw new IOException();
    			}
    			List<AttributeValue<Integer>> userMemberships = new ArrayList<AttributeValue<Integer>>();
    			for (int k=1; k<mst.length; k++) {
		    		String[] st2 = mst[k].split(":");
		    		userMemberships.add(new AttributeValue<Integer>(new Integer(st2[0]), Float.parseFloat(st2[1])));
    			}
    			incrementSufficientStatistics(userMemberships, profile);
    		}
    		profileReader.close();
    		membershipsReader.close();
        }
        catch (FileNotFoundException e) { 
        	logger.error("Cannot find filename " + memberships.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }	
        catch (IOException e) { 
        	logger.error("Error while reading filename " + memberships.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }		
		
	}
	
	public void calculateSufficientStatisticsFromInitMemberships(File profiles, File memberships){
		try {
        	if (!profiles.exists()) {
    			logger.error("File " +  profiles.getAbsolutePath() + " does not exist");
        		throw new FileNotFoundException();
    	    }
    	    if (!profiles.isFile()) {
    	    	logger.error("File " +  profiles.getAbsolutePath() + " is not a file");
    	    	throw new FileNotFoundException();
    		}
    		if (!profiles.canRead()) {
    			logger.error("File " +  profiles.getAbsolutePath() + " cannot be read");
    			throw new IOException();
    		}
    		logger.info("Using initial memberships from " + memberships.getAbsolutePath());
        	BufferedReader br = new BufferedReader(new FileReader(profiles));
        	this.calculateSufficientStatisticsFromInitMemberships(br, memberships);
        	br.close();
        }
        catch (FileNotFoundException e) { 
        	logger.error("Cannot find filename " + profiles.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }	
        catch (IOException e) { 
        	logger.error("Error while reading filename " + profiles.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }		
	}
	private void calculateSufficientStatistics(BufferedReader br, File writeUserMemberships) throws IOException{
		zeroSufficientStatistics();
		
		
		Writer output = writeUserMemberships != null ? new BufferedWriter(new FileWriter(writeUserMemberships)) 
														: null;
		float[] logUserProb = new float[numberOfExperts];
		String lineStr;
		logProbOfAllUsers=0.0;
		numberOfUsers=0;
		while ((lineStr = br.readLine()) != null) {
			String[] st = lineStr.split("\\s+");
			numberOfUsers ++;
			
			List<AttributeValue<Integer>> profile = createUserProfile(st);
			
			// Calculate p(cluster | user)
			List<AttributeValue<Integer>> userMemberships = new ArrayList<AttributeValue<Integer>>();
			calculateLogProbOfUserGivenCluster(profile, logUserProb);
			float maxLogProb = Float.NEGATIVE_INFINITY;
			int maxIndex=0;
			for (int c=0; c<numberOfExperts; c++){	
				logUserProb[c] += logProbOfCluster[c];
				
				if (logUserProb[c] > maxLogProb){
					maxLogProb = logUserProb[c];
					maxIndex = c;
				}
			}
			
			if (hardAssignments){
				userMemberships.add(new AttributeValue<Integer>(new Integer(maxIndex), new Float(1.0f)));
				
				logProbOfAllUsers += maxLogProb; 
			}
			else {		
				for (int c=0; c<numberOfExperts; c++){
					if (c == maxIndex)
						continue;
					if (maxLogProb-logUserProb[c] > 15.0)
						continue;
				
					double tmpSum=0.0;
					for (int k=0; k<numberOfExperts; k++){
					
						if (k == c){
							tmpSum += 1.0;
							continue;
						}
					
						double tmp = logUserProb[k]-logUserProb[c];
						if (tmp < - 15.0)
							continue;
						
						tmpSum += Math.exp(tmp);	
					}
					double memb = 1.0 / tmpSum;
					userMemberships.add(new AttributeValue<Integer>(new Integer(c), new Float(memb)));
				}
				// Prob for the cluster with highest membership
				double tmpSum=0.0;
				for (int k=0; k<numberOfExperts; k++){
					if (k == maxIndex){
						tmpSum += 1.0;
						continue;
					}
				
					double tmp = logUserProb[k]-logUserProb[maxIndex];
					if (tmp < - 15.0)
						continue;
					
					tmpSum += Math.exp(tmp);	
				}
				logProbOfAllUsers += (double)maxLogProb;
				if (tmpSum != 1.0){
					double memb = 1.0 / tmpSum;
					userMemberships.add(new AttributeValue<Integer>(new Integer(maxIndex), (float)memb));
					logProbOfAllUsers += Math.log(tmpSum);
				}
				else 
					userMemberships.add(new AttributeValue<Integer>(new Integer(maxIndex), 1.0f));
				
			}
			
			// Write user memberships, if desired
			if (output != null){
				Collections.sort(userMemberships);
				StringBuffer sb = new StringBuffer();
				sb.append(st[0]);
				for (AttributeValue<Integer> att: userMemberships){
					float roundedProb = (float)Math.round(att.getValue() * 10000.0f) / 10000.0f;
					if (roundedProb < writePosteriorThreshold)
						break;
					sb.append(" ");
					sb.append(att.getIndex().intValue());
					sb.append(":");
					sb.append(roundedProb);
				}
				sb.append("\n");
				output.write(sb.toString());
			}
			
			// Now add to the suff. stats
			incrementSufficientStatistics(userMemberships, profile);
		}
		br.close();
		if (output != null)
			output.close();
	}
	
	public void calculateSufficientStatistics(File data){
		this.calculateSufficientStatistics(data, null);
	}
	
	public void calculateSufficientStatistics(File data, File writeUserMemberships){
		try {
        	if (!data.exists()) {
    			logger.error("File " +  data.getAbsolutePath() + " does not exist");
        		throw new FileNotFoundException();
    	    }
    	    if (!data.isFile()) {
    	    	logger.error("File " +  data.getAbsolutePath() + " is not a file");
    	    	throw new FileNotFoundException();
    		}
    		if (!data.canRead()) {
    			logger.error("File " +  data.getAbsolutePath() + " cannot be read");
    			throw new IOException();
    		}
    		logger.info("Computing sufficient statistics on-the-fly from file " + data.getAbsolutePath());
        	BufferedReader br = new BufferedReader(new FileReader(data));
        	this.calculateSufficientStatistics(br, writeUserMemberships);
        	br.close();
        }
        catch (FileNotFoundException e) { 
        	logger.error("Cannot find filename " + data.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }	
        catch (IOException e) { 
        	logger.error("Error while reading filename " + data.getAbsolutePath());
        	e.printStackTrace();
        	System.exit(1);
        }		
	}
	
	public void calculateSufficientStatistics(InputStream is){
		this.calculateSufficientStatistics(is);
	}
	
	public void calculateSufficientStatistics(InputStream is, File writeUserMemberships){
		try {
			if (is == null){
				logger.error("Null input stream");
				throw new IOException();
			}
			logger.info("Computing sufficient statistics on-the-fly from stream");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			this.calculateSufficientStatistics(br, writeUserMemberships);
			br.close();
		}
		catch (IOException e) { 
        	logger.error("Error while reading data from stream");
        	e.printStackTrace();
        	System.exit(1);
        }
	}
	
	public void calculateSufficientStatistics(){
		for (int expert=0; expert<numberOfExperts; expert ++)
			logProbAttributeGivenExpert.get(expert).clear();
			
		
		for (int expert=0; expert<numberOfExperts; expert ++)
			logProbOfCluster[expert] = 0.0f;
		
		float[] posterior = new float[numberOfExperts];
		for (Iterator<Map.Entry<String, ArrayList<AttributeValue<Integer>>>> it = 
			users.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, ArrayList<AttributeValue<Integer>>> e = it.next();
			
			for (int expert=0; expert<numberOfExperts; expert ++)
				posterior[expert]=0.0f;
			
			for (Iterator<Map.Entry<Integer, Float>> it2 = probOfClusterGivenUser.get(e.getKey()).entrySet().iterator(); it2.hasNext();){
				Map.Entry<Integer, Float> e2 = it2.next();
				
				int expert = e2.getKey().intValue();
				float p = e2.getValue().floatValue();
				posterior[expert] = p;
				if (p < (float)estimationPosteriorThreshold) 
					continue;
				logProbOfCluster[expert] += p;
			}
			
			for (Iterator<AttributeValue<Integer>> it3 = e.getValue().iterator(); it3.hasNext();){
				AttributeValue<Integer> av = it3.next();
				int indx = av.getIndex();
				Integer _indx = Integer.valueOf(indx);
				float count = av.getValue();
				

				for (int expert=0; expert<numberOfExperts; expert ++){
					if (posterior[expert] < (float)estimationPosteriorThreshold) 
						continue;
					
					Float dcount = logProbAttributeGivenExpert.get(expert).get(_indx);
					if (dcount != null){
						float updatedCount = dcount.floatValue();
						updatedCount += count * posterior[expert];
						dcount = Float.valueOf(updatedCount);
						logProbAttributeGivenExpert.get(expert).put(new Integer(indx) , new Float(dcount));
					}
					else {
						Integer newKey = new Integer(indx);
						float value = count * posterior[expert];
						logProbAttributeGivenExpert.get(expert).put(newKey , new Float(value));
					}
					 
				}
				
			}
		}
	}
	
	// TODO: Write these more efficiently - see SVD code
	public void writeSufficientStatistics(File statsFile){
		logger.info("Writing sufficient stats in file " + statsFile.getAbsolutePath());
		try {
			 FileOutputStream file_output = new FileOutputStream (statsFile);
		     DataOutputStream data_out = new DataOutputStream (file_output);
		     int numEntries = 2 * globalSS1.size();
		     data_out.writeInt(numEntries);
		     for (Iterator<Map.Entry<Integer, Float>> it = globalSS1.entrySet().iterator(); it.hasNext(); ) {
	    		 Map.Entry<Integer, Float> e = it.next();
	    		 data_out.writeInt(e.getKey().intValue()); 
	    		 data_out.writeFloat(e.getValue().floatValue());
		     }
		     for (int expert=0; expert<numberOfExperts; expert ++)
		    	 data_out.writeFloat(SS0[expert]);
		     
		     
		     for (int expert=0; expert<numberOfExperts; expert ++){
		    	 numEntries = 2 * SS1.get(expert).size();
			     data_out.writeInt(numEntries);
		    	 for (Iterator<Map.Entry<Integer, Float>> it = SS1.get(expert).entrySet().iterator(); it.hasNext(); ) {
		    		 Map.Entry<Integer, Float> e = it.next();
		    		 data_out.writeInt(e.getKey().intValue()); 
		    		 data_out.writeFloat(e.getValue().floatValue());
		    	 }
		     }
             data_out.writeInt(numberOfUsers);
             data_out.writeFloat((float)logProbOfAllUsers);

		     file_output.close();
		}
		catch (IOException e){
			logger.error("Error while writing to " + statsFile.getAbsolutePath());
			System.exit(1);
		}
	}
	
	public double mergeSufficientStatistics(File statsMetaFile) {
	    try { 
	    	if (!statsMetaFile.exists()) {
			      throw new FileNotFoundException("File does not exist: ");
		    }
		    if (!statsMetaFile.isFile()) {
			      throw new FileNotFoundException("Should not be a directory: ");
			}
			if (!statsMetaFile.canRead()) {
			      throw new IOException("File cannot be read: ");
			}
			FileReader fr     = new FileReader(statsMetaFile);
		    BufferedReader br = new BufferedReader(fr);
		    String statsFilename;
		    Float dcount = new Float(0);
		    float count;
		    Integer attribute = new Integer(0);
		    logProbOfAllUsers=0.0;
		   
		    for (int expert=0; expert<numberOfExperts; expert++)
		    	logProbOfCluster[expert] = (float)0.0;
		 
	    	while ((statsFilename = br.readLine()) != null){
               
                logger.info("Merging stats " + statsFilename);

	    		File aFile2 = new File(statsFilename);
	    		if (!aFile2.exists()) {
	    		      throw new FileNotFoundException();
	    	    }
	    	    if (!aFile2.isFile()) {
	    		      throw new FileNotFoundException();
	    		}
	    		if (!aFile2.canRead()) {
	    		      throw new IOException();
	    		}
	    	
	    	    FileInputStream file_input = new FileInputStream (aFile2);
			    DataInputStream data_in    = new DataInputStream (file_input);
	    	    try {
	    	    	int numEntries = data_in.readInt();
	    	    	for (int k=0; k<numEntries-1; k=k+2){
	    	    		attribute = Integer.valueOf(data_in.readInt());
			    		count = data_in.readFloat();
			    		
			    		Float gcount = logProbAttribute.get(attribute);
			    		if (gcount == null)
			    			logProbAttribute.put(attribute, new Float(count));
			    		else
			    			logProbAttribute.put(attribute, new Float(gcount.floatValue()+count));
	    	    	}
	    	    	for (int expert=0; expert<numberOfExperts; expert++)
			    		logProbOfCluster[expert] += data_in.readFloat();
			    	
	    	    	
			    	for (int expert=0; expert<numberOfExperts; expert ++){
			    		numEntries = data_in.readInt();
		    	    	for (int k=0; k<numEntries-1; k=k+2){
		    	    		attribute = Integer.valueOf(data_in.readInt());
				    		count = data_in.readFloat();
		    					
				    		dcount = (Float)logProbAttributeGivenExpert.get(expert).get(attribute);
		    				if (dcount != null){
		    					float updatedCount = dcount.floatValue();
		    					updatedCount += count;
		    					dcount = logProbAttributeGivenExpert.get(expert).put(attribute , new Float(updatedCount));
		   					}
		   					else {
		   						Integer newAttribute = new Integer(attribute.intValue());
	    						logProbAttributeGivenExpert.get(expert).put(newAttribute , new Float(count));
	    					}
		    	    	}
			    	}
                    numberOfUsers += data_in.readInt();
                    logProbOfAllUsers += (double)data_in.readFloat();
	    
                    file_input.close();
	    	    }
	    	    catch( FileNotFoundException e){
	    	    	logger.error("Cannot find file " + statsFilename);
	    	    	e.printStackTrace();
	    	    	System.exit(1);
	    	    }
	    	    catch (IOException e) { 
	    	    	logger.error("Error while reading stats filename " + statsFilename);
	    	    	e.printStackTrace();
	    	    	System.exit(1);
	    	    }
	    	}
	    	br.close();
	    }
	    catch( FileNotFoundException e){
	    	logger.error("Cannot find file " + statsMetaFile.getAbsolutePath());
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	    catch (IOException e) { 
	    	logger.error("Error while reading stats meta filename " + statsMetaFile.getAbsolutePath());
	    	e.printStackTrace();
	    	System.exit(1);
	    }

	    
	    return(logProbOfAllUsers / (double)numberOfUsers);
	}
	
	
	protected void estimateAttributeParameters(){
		float totSum = 0.0f;
		for (Iterator<Map.Entry<Integer,Float>> it = logProbAttribute.entrySet().iterator(); it.hasNext(); ) {
		    Map.Entry<Integer, Float> e = it.next();
		    totSum += e.getValue().floatValue();
		}
		for (Iterator<Map.Entry<Integer,Float>> it= logProbAttribute.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer, Float> e = it.next();
			float logProb = (float)Math.log(e.getValue() / totSum);
			e.setValue(logProb);
		}
		for (int expert=0; expert<numberOfExperts; expert++){
			totSum = 0.0f;
			for ( Iterator<Map.Entry<Integer, Float>> it = logProbAttributeGivenExpert.get(expert).entrySet().iterator(); it.hasNext(); ) {
			    Map.Entry<Integer, Float> e = it.next();
			    totSum += e.getValue().floatValue();
			}
			
			
			logConstant[expert] = (float)Math.log(totSum + prior.getSumOfHyperparameters(expert));
			for (Iterator<Map.Entry<Integer,Float>> it = logProbAttributeGivenExpert.get(expert).entrySet().iterator(); it.hasNext(); ) {
			    Map.Entry<Integer, Float> e = it.next();
			    int att = e.getKey().intValue();
			    float value = e.getValue().floatValue();
			    
			    float n = prior.getAttributeHyperparameter(expert,att);
			    value = (float)Math.log((double) (value + n )) - logConstant[expert];
			    
			    
			    Float dvalue = Float.valueOf(value);
			    e.setValue(dvalue);
			}
		}
	}
	
	/*
	 * Returns a sorted list of attributes according to distinctiveness score for a given cluster
	 */
	public ArrayList<AttributeValue<Integer>> calculateDistinctiveScore(int expert){
		float threshold = (float)Math.log(1.5);
		ArrayList<AttributeValue<Integer>> scores = new ArrayList<AttributeValue<Integer>>();
		HashMap<Integer, Float> hm = logProbAttributeGivenExpert.get(new Integer(expert));
		for (Iterator<Map.Entry<Integer, Float>> it = hm.entrySet().iterator(); it.hasNext();){
			Map.Entry<Integer, Float> me = it.next();
			float logAttExpert = me.getValue().floatValue();
			float logAtt = logProbAttribute.get(me.getKey()).floatValue();
			float s = logAttExpert - logAtt;
			if (s >= threshold)
				scores.add(new AttributeValue<Integer>(me.getKey(), 1000.0f * (float)Math.exp(logAttExpert+s)));
		}
		Collections.sort(scores);
		return scores;
	}
	
	/*
	 * Returns a sorted list of attributes according to global frequency
	 */
	public ArrayList<AttributeValue<Integer>> calculateDistinctiveScore(){
		ArrayList<AttributeValue<Integer>> scores = new ArrayList<AttributeValue<Integer>>();
		for (Iterator<Map.Entry<Integer, Float>> it = logProbAttribute.entrySet().iterator(); it.hasNext();){
			Map.Entry<Integer, Float> me = it.next();
			float v = 1000.0f * (float)Math.exp(me.getValue().floatValue());
			scores.add(new AttributeValue<Integer>(me.getKey(), v));
			
		}
		Collections.sort(scores);
		return scores;
	}
}
