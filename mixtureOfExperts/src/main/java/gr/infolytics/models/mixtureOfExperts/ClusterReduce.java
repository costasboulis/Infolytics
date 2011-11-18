package gr.infolytics.models.mixtureOfExperts;


import java.io.File;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Merge the suffficient statistics from the ClusterMap jobs and estimate the new parameters
 * 
 * @author Kostas Boulis
 */

public class ClusterReduce {
	private static final Logger logger = LoggerFactory.getLogger(ClusterReduce.class);
	public static void main(String[] argv) {
		CommandLineParser parser = new CommandLineParser(argv);
		if (parser.hasErrors(new String[]{}, new String[]{
				"numberOfExperts",
				"masterStats",
				"writeModels",
				"outExpertDescription",
				"prior",
				"priorResources",
				"maxAttributes",
				"attributes",
				"alpha"})) {
			System.err.println("Usage: ClusterReduce\n" +
					"-numberOfExperts\tInteger\n" +
					"-masterStats\tString\n" +
					"-writeModels\tString\n" +
					"[-outExpertDescription]\tString\n" +
					"[-maxAttributes]\tInteger\n" +
					"[-attributes]\tString\n" +
					"[-alpha]\tDouble\n" +
					"[-prior]\tString Choices are: \"uniform\" , \"nonUniform\"\n" +
					"[-priorResources]\tString\n");
			System.exit(1);
		}
		HashMap<String, Object> arguments;
		arguments = parser.getArguments();
		String masterStats = CommandLineParser.getStringParameter(arguments, "masterStats", true);
		String writeModels = CommandLineParser.getStringParameter(arguments, "writeModels", true);
		int nExperts = CommandLineParser.getIntegerParameter(arguments, "numberOfExperts", true, 10); 
    	String outExpertDescriptionFilename = CommandLineParser.getStringParameter(arguments, "outExpertDescription", false);
    	int maxAttributes = CommandLineParser.getIntegerParameter(arguments, "maxAttributes", false, 0);
    	String attributesFilename = CommandLineParser.getStringParameter(arguments, "attributes", false);
    	double alpha = CommandLineParser.getDoubleParameter(arguments, "alpha", false, 1.0);
    	String priorName = CommandLineParser.getStringParameter(arguments, "prior", false);
    	String priorResourcesFilename = CommandLineParser.getStringParameter(arguments, "priorResources", false);
    	
    	
    	
    	MixtureOfMultinomials mom = new MixtureOfMultinomials(nExperts, alpha, priorName, priorResourcesFilename);
 
    	
		logger.info("ClusterReduce started"); 
		double logProbOfAllItems = mom.mergeSufficientStatistics(new File(masterStats));
		logger.info("Log likelihood: " + logProbOfAllItems);
		
		mom.performMaximizationStep();	
		mom.writeModels(new File(writeModels));

		if (attributesFilename != null)
			mom.readAttributeNames(new File(attributesFilename));
		
		if ((outExpertDescriptionFilename != null) && (attributesFilename != null))
			mom.writeClusterModels(new File(outExpertDescriptionFilename), maxAttributes);
			
		logger.info("ClusterReduce finished\n"); 
	}


}

