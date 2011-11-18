package gr.infolytics.models.mixtureOfExperts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class NonUniformDirichletPrior extends DirichletPrior{
	private int numberOfAttributes;
	private float[] hyperparameter;
	private float[] logHyperparameter;
	private float sumOfHyperparameters;
	private float alpha;
	private final Logger logger = LoggerFactory.getLogger(NonUniformDirichletPrior.class);
	
	NonUniformDirichletPrior(float _alpha, String resources){
		super("nonUniform");
		alpha = _alpha;
		
		
		readFromFile(resources);
		
		for (int att=0; att<numberOfAttributes; att++){
			float hp = alpha * hyperparameter[att];
			if (hp < 1.0f)
				hp = 1.0f;
			hyperparameter[att] = hp;
		}
		
		for (int att=0; att<numberOfAttributes; att++){
			logHyperparameter[att] = (float)Math.log((double)hyperparameter[att]);
		}
		sumOfHyperparameters = (float)0.0;
		for (int att=0; att<numberOfAttributes; att++)
			sumOfHyperparameters += hyperparameter[att];
		
	}
	
	private void readFromFile(String resourcesFilename){
		List<AttributeValue<Integer>> hyperList = new ArrayList<AttributeValue<Integer>>();
		try {
			File aFile = new File(resourcesFilename);
			if (!aFile.exists()) {
				throw new FileNotFoundException();
			}
			if (!aFile.isFile()) {
				throw new FileNotFoundException();
			}
			if (!aFile.canRead()) {
				throw new IOException();
			}
			FileReader fr     = new FileReader(aFile);
			BufferedReader br = new BufferedReader(fr);
			String lineStr;
			while ((lineStr = br.readLine()) != null) {
				String[] st = lineStr.split("\\s+");
				hyperList.add(new AttributeValue<Integer>(new Integer(st[0]), Float.parseFloat(st[1])));
			}
			br.close();
		}
		catch (FileNotFoundException e){
			logger.error("Could not find file " + resourcesFilename);
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e){
			logger.error("Could not read file " + resourcesFilename);
			e.printStackTrace();
			System.exit(1);
		}
		numberOfAttributes = hyperList.size();
		hyperparameter= new float[numberOfAttributes];
		logHyperparameter = new float[numberOfAttributes];
		for (AttributeValue<Integer> att : hyperList)
			hyperparameter[att.getIndex().intValue()] = att.getValue();
		
		
		float tot = (float)0.0;
		for (int i=0; i<numberOfAttributes; i++){
			if (hyperparameter[i] == 0.0f){
				logger.error("ERROR: Prior for attribure " + i + " is zero");
				System.exit(1);
			}
			tot += hyperparameter[i];
		}
		if ((tot < 0.99f) || (tot > 1.01f)){
			logger.error("ERROR: Sum of prior probabilities is " + tot);
			System.exit(1);
		}
		
	}
	
	public float getAttributeLogHyperparameter(int cluster, int attribute){
		return (logHyperparameter[attribute]);
	}
	
	public float getAttributeHyperparameter(int cluster, int attribute){
		return(hyperparameter[attribute]);
	}
	
	public float getSumOfHyperparameters(int cluster){
		return(sumOfHyperparameters);
	}
}
