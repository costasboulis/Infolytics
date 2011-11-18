package gr.infolytics.models.mixtureOfExperts;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Uniform prior, i.e. the same hyperparameters for all attributes & clusters
 * 
 * @author kboulis
 *
 */

public class UniformDirichletPrior extends DirichletPrior {
	private int numberOfAttributes;
	private float hyperparameter;
	private float logHyperparameter;
	private float alpha;
	private final Logger logger = LoggerFactory.getLogger(UniformDirichletPrior.class);
	
	UniformDirichletPrior(float _alpha, String resources){
		super("uniform");
		alpha = _alpha;
		
		if (alpha <= 0.0f){
			logger.error("ERROR: Alpha must be positive but is set to " + alpha);
			System.exit(1);
		}
		hyperparameter = alpha;
		logHyperparameter = (float)Math.log((double)alpha);
		readFromFile(resources);
	}
	private void readFromFile(String resourcesFilename){
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
			numberOfAttributes=0;
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				numberOfAttributes ++;
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
	}
	
	public float getAttributeLogHyperparameter(int cluster, int attribute){
		return(logHyperparameter);
	}
	
	public float getAttributeHyperparameter(int cluster, int attribute){
		return(hyperparameter);
	}
	
	public float getSumOfHyperparameters(int cluster){
		return(alpha * (float)numberOfAttributes);
	}
}
