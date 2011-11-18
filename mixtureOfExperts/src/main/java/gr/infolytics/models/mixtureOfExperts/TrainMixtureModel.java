package gr.infolytics.models.mixtureOfExperts;


import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Estimates a mixture of Experts (e.g. Multinomials, Bernoullis or Gaussians) on the provided data
 * 
 * @author Kostas Boulis
 */

public class TrainMixtureModel {
	private static final Logger logger = LoggerFactory.getLogger(TrainMixtureModel.class);
	public static void main(String[] argv) {
		CommandLineParser parser = new CommandLineParser(argv);
		if (parser.hasErrors(new String[]{}, new String[]{
				"data",
				"tempDir",
				"outUserMemberships",
				"inUserMemberships",
				"outExpertDescription",
				"maxAttributes",
				"attributes",
				"numberOfExperts",
				"numberOfIterations",
				"referencePartition",
				"alpha",
				"prior",
				"priorResources"})) {
			logger.error("Usage: TrainMixtureModel\n" +
					"-data\tString\n" +
					"-outUserMemberships\tString\n" +
					"-numberOfExperts\tInteger\n" +
					"-tempDir\tString: Directory where temporary files are written\n" +
					"[-numberOfIterations]\tInteger\n" +
					"[-alpha]\tDouble\n" +
					"[-prior]\tString Choices are: \"uniform\", \"nonUniform\"\n" +
					"[-priorResources]\tString\n" +
					"[-hardAssignments]\tBoolean\n" +
					"[-inUserMemberships]\tString\n" +
					"[-outExpertDescription]\tString\n" +
					"[-maxAttributes]\tInteger\n" +
					"[-attributes]\tString\n" + 
					"[-referencePartition]\tString\n");
			System.exit(1);
		}
		HashMap<String, Object> arguments;
		arguments = parser.getArguments();
		
		String dataFilename = CommandLineParser.getStringParameter(arguments, "data", true);
    	String outUserMembershipsFilename = CommandLineParser.getStringParameter(arguments, "outUserMemberships", true);
    	int numberOfExperts = CommandLineParser.getIntegerParameter(arguments, "numberOfExperts", true, 0); 
    	int numberOfIterations = CommandLineParser.getIntegerParameter(arguments, "numberOfIterations", false, 15); 
    	String referencePartitionFilename = CommandLineParser.getStringParameter(arguments, "referencePartition", false);
    	String inUserMembershipsFilename = CommandLineParser.getStringParameter(arguments, "inUserMemberships", false);
    	String outExpertDescriptionFilename = CommandLineParser.getStringParameter(arguments, "outExpertDescription", true);
    	int maxAttributes = CommandLineParser.getIntegerParameter(arguments, "maxAttributes", false, 50); 
    	String attributesFilename = CommandLineParser.getStringParameter(arguments, "attributes", false);
    	double alpha = CommandLineParser.getDoubleParameter(arguments, "alpha", false, 1.0);
    	String priorName = CommandLineParser.getStringParameter(arguments, "prior", false);
    	String priorResourcesFilename = CommandLineParser.getStringParameter(arguments, "priorResources", false);
    	String tmpDir = CommandLineParser.getStringParameter(arguments, "tempDir", true);
    	boolean hardAssignments = false;
    	
    	String fs = File.separatorChar == '\\' ? "\\\\" : File.separator;
    	File data = new File(dataFilename);
    	File models = new File(tmpDir + fs + "models.txt");
    	File stats = new File(tmpDir + fs + "stats.txt");
    	File metaStats = new File(tmpDir + fs + "metaStats.txt");
		try{
			Writer output = new BufferedWriter(new FileWriter(metaStats));
			output.write(stats.getAbsolutePath());
			output.close();
		}
		catch (IOException e){
			System.exit(-1);
		}
    	MixtureOfMultinomials mom = 
    		new MixtureOfMultinomials(numberOfExperts, hardAssignments, alpha, priorName, priorResourcesFilename);
		if (inUserMembershipsFilename != null){
			File inMembs = new File(inUserMembershipsFilename);
			mom.calculateSufficientStatisticsFromInitMemberships(data, inMembs);
		}	
		else
			mom.randomInitialization(data);
		mom.writeSufficientStatistics(stats);
		
		mom = new MixtureOfMultinomials(numberOfExperts, hardAssignments, alpha, priorName, priorResourcesFilename);
		mom.mergeSufficientStatistics(metaStats);
		mom.performMaximizationStep();
		mom.writeModels(models);
		
		if (numberOfIterations == 0){
			if (referencePartitionFilename != null && inUserMembershipsFilename != null){
				File refPartition = new File(referencePartitionFilename);
				double adjRandIndex = mom.computeAdjustedRandIndex(refPartition, new File(inUserMembershipsFilename));
				logger.info("ADJUSTED RAND INDEX : " + adjRandIndex);
			}
			mom.writeClusterModels(new File(outExpertDescriptionFilename), maxAttributes);
			if (attributesFilename != null){
	    		mom.readAttributeNames(new File(attributesFilename));
	    		File descFile = new File(outExpertDescriptionFilename + "_descriptions.txt");
	    		mom.writeClusterDescriptions(descFile, maxAttributes);
			}
			metaStats.delete();
			stats.delete();
			models.delete();
			return;
		}
		for (int iter=1; iter<numberOfIterations; iter ++){
			mom = 
				new MixtureOfMultinomials(numberOfExperts, hardAssignments, alpha, priorName, priorResourcesFilename);
			mom.setEstimationThreshold(0.0);
			mom.readModels(models);
			mom.calculateSufficientStatistics(data);
			mom.writeSufficientStatistics(stats);
			
			mom = new MixtureOfMultinomials(numberOfExperts, hardAssignments, alpha, priorName, priorResourcesFilename);
			double logLikelihood = mom.mergeSufficientStatistics(metaStats);
			logger.info("Iteration: " + iter + " , Log likelihood: " + logLikelihood);
			mom.performMaximizationStep();
			mom.writeModels(models);
		}
		// Last iteration, use some different arguments
		mom = 
			new MixtureOfMultinomials(numberOfExperts, hardAssignments, alpha, priorName, priorResourcesFilename);
		double threshold = 1.0/((double)numberOfExperts*10.0);
		mom.setWriteThreshold(threshold);
		mom.setEstimationThreshold(0.0);
		mom.readModels(models);
		mom.calculateSufficientStatistics(data, new File(outUserMembershipsFilename));
		if (referencePartitionFilename != null){
			File refPartition = new File(referencePartitionFilename);
			double adjRandIndex = mom.computeAdjustedRandIndex(refPartition, new File(outUserMembershipsFilename));
			logger.info("ADJUSTED RAND INDEX : " + adjRandIndex);
		}
		mom.writeSufficientStatistics(stats);
		
		mom = new MixtureOfMultinomials(numberOfExperts, hardAssignments, alpha, priorName, priorResourcesFilename);
		mom.mergeSufficientStatistics(metaStats);
		mom.performMaximizationStep();
		mom.writeClusterModels(new File(outExpertDescriptionFilename), maxAttributes);
//		mom.writeModels(models);
		if (attributesFilename != null){
    		mom.readAttributeNames(new File(attributesFilename));
    		File descFile = new File(outExpertDescriptionFilename + "_descriptions.txt");
    		mom.writeClusterDescriptions(descFile, maxAttributes);
		}
		metaStats.delete();
		stats.delete();
		models.delete();
	}
	
}


