package gr.infolytics.models.mixtureOfExperts;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ClusteringSmokeTest extends TestCase {
	private String dataDir;
	private String outDir;
	private String className;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public ClusteringSmokeTest(String name) {
		super(name);
		className = getClass().toString().replaceFirst("class ","");
		String fs = File.separatorChar == '\\' ? "\\\\" : File.separator;
		className = className.replaceAll("\\.", fs);
		dataDir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator 
		+ "resources" + File.separator + className + File.separator ;
		outDir = System.getProperty("user.dir") + File.separator + "target" + File.separator + 
		"test-classes" + File.separator + className + File.separator ;
	}
	
	/**
	 * Sufficient statistics are calculated on the fly without storing data into memory
	 */
	public void testOnTheFlyCalculationsSingleChunkZoo(){
		File data = new File(dataDir + "zoo.txt");
		File outStats = new File(outDir + "zoo_stats.txt");
		File metaStats = new File(outDir + "zoo_metaStats.txt");
		String priorResources = dataDir + "zoo.vocabulary.txt";
		try{
			Writer output = new BufferedWriter(new FileWriter(metaStats));
			output.write(outStats.getAbsolutePath());
			output.close();
		}
		catch (IOException e){
			System.exit(-1);
		}
		File models = new File(outDir + "zoo_models.txt");
		File referencePartition = new File(dataDir + "zoo.class.txt");
		File hypPartition = new File(outDir + "zoo_memberships.txt");
		
		int nClusters = 7;
		double alpha = 0.1;
		MixtureOfMultinomials mom = new MixtureOfMultinomials(nClusters, false, alpha, "uniform", priorResources);
		mom.randomInitialization(data);
		mom.writeSufficientStatistics(outStats);
		
		mom = new MixtureOfMultinomials(nClusters, alpha, "uniform", priorResources);
		mom.mergeSufficientStatistics(metaStats);
		mom.performMaximizationStep();
		mom.writeModels(models);
		double prevLogLikelihood = Double.NEGATIVE_INFINITY;
		
		double adjRandIndex=0.0;
		for (int iter=1; iter<=15; iter ++){
			mom = new MixtureOfMultinomials(nClusters, false, alpha, "uniform", priorResources);
			mom.setWriteThreshold(0.001);
			mom.setEstimationThreshold(0.0);
			mom.readModels(models);
			mom.calculateSufficientStatistics(data, hypPartition);
			mom.writeSufficientStatistics(outStats);
			
			mom = new MixtureOfMultinomials(nClusters, alpha, "uniform", priorResources);
			double logLikelihood = mom.mergeSufficientStatistics(metaStats);
			logger.info("Log likelihood: " + logLikelihood);
			assertTrue(logLikelihood > prevLogLikelihood);
			mom.performMaximizationStep();
			mom.writeModels(models);
			
			adjRandIndex = mom.computeAdjustedRandIndex(referencePartition, hypPartition);
			logger.info("ADJUSTED RAND INDEX : " + adjRandIndex);
		}
		outStats.delete();
		metaStats.delete();
		models.delete();
		hypPartition.delete();
		
		assertTrue(adjRandIndex > 0.30);
	}
	
	
	public void testOnTheFlyCalculationsSingleChunk20News(){
		File data = new File(dataDir + "20Newsgroups.data.vocab.10.txt");
		File outStats = new File(outDir + "20Newsgroups_stats.txt");
		File metaStats = new File(outDir + "20Newsgroups_metaStats.txt");
		String priorResources = dataDir + "20Newsgroups.vocabulary.10.txt";
		try{
			Writer output = new BufferedWriter(new FileWriter(metaStats));
			output.write(outStats.getAbsolutePath());
			output.close();
		}
		catch (IOException e){
			System.exit(-1);
		}
		File models = new File(outDir + "20Newsgroups_models.txt");
		File referencePartition = new File(dataDir + "20Newsgroups_labels.txt");
		File hypPartition = new File(outDir + "20Newsgroups_memberships.txt");
		int nClusters = 20;
		double alpha = 1.0;
		
		MixtureOfMultinomials mom = new MixtureOfMultinomials(nClusters, false, alpha, "uniform", priorResources);
		mom.randomInitialization(data);
//		mom.initializationWithSeeds(data);
		mom.writeSufficientStatistics(outStats);
		
		mom = new MixtureOfMultinomials(nClusters, alpha, "uniform", priorResources);
		mom.mergeSufficientStatistics(metaStats);
		mom.performMaximizationStep();
		mom.writeModels(models);
		double prevLogLikelihood = Double.NEGATIVE_INFINITY;
		
		double adjRandIndex=0.0;
		for (int iter=1; iter<=20 && adjRandIndex <= 0.25; iter ++){
//		for (int iter=1; iter<=20 ; iter ++){
			mom = new MixtureOfMultinomials(nClusters, false, alpha, "uniform", priorResources);
			mom.setWriteThreshold(0.001);
			mom.setEstimationThreshold(0.0);
			mom.readModels(models);
			mom.calculateSufficientStatistics(data, hypPartition);
			mom.writeSufficientStatistics(outStats);
			
			mom = new MixtureOfMultinomials(nClusters, alpha, "uniform", priorResources);
			double logLikelihood = mom.mergeSufficientStatistics(metaStats);
			logger.info("Log likelihood: " + logLikelihood);
			assertTrue(logLikelihood > prevLogLikelihood);
			prevLogLikelihood = logLikelihood;
			mom.performMaximizationStep();
			mom.writeModels(models);
			
			adjRandIndex = mom.computeAdjustedRandIndex(referencePartition, hypPartition);
			logger.info("ADJUSTED RAND INDEX : " + adjRandIndex);
		}
		outStats.delete();
		metaStats.delete();
		models.delete();
		hypPartition.delete();
		
		assertTrue(adjRandIndex > 0.25);
	}
	
	
}
