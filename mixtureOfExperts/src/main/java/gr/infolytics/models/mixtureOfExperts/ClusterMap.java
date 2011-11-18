package gr.infolytics.models.mixtureOfExperts;


import java.io.File;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Estimates and writes to disk sufficient statistics of the provided data
 * Used for distributed estimation of clustering models, when data cannot 
 * fit on a single machine's memory
 * 
 * @author Kostas Boulis
 *
 */
// TODO: Do you really need the prior parameters in ClusterMap?


public class ClusterMap {
	private static final Logger logger = LoggerFactory.getLogger(ClusterMap.class);
	
	public static void main(String[] argv) {
		CommandLineParser parser = new CommandLineParser(argv);
		if (parser.hasErrors(new String[]{"writeHardMemberships", "hardAssignments"}, new String[]{
				"numberOfExperts",
				"data",
				"stats",
				"prior",
				"priorResources",
				"alpha",
				"outMemberships",
				"inMemberships",
				"referencePartition",
				"models"})) {
			System.err.println("Usage: ClusterMap\n" +
					"-numberOfExperts\tInteger\n" +
					"-data\tString\n" +
					"-stats\tString\n" +
					"[-prior]\tString Choices are: \"uniform\"\n" +
					"[-priorResources]\tString\n" +
					"[-alpha]\tDouble\n" +
					"[-outMemberships]\tString\n" +
					"[-inMemberships]\tString\n" +
					"[-hardAssignments]\tBoolean\n" + 
					"[-models]\tString\n" +
					"[-referencePartition]\tString\n" );
			System.exit(1);
		}
		
		HashMap<String, Object> arguments;
		arguments = parser.getArguments();
		
		String data = CommandLineParser.getStringParameter(arguments, "data", true);
		String stats = CommandLineParser.getStringParameter(arguments, "stats", true);
		String models = CommandLineParser.getStringParameter(arguments, "models", false);
		String outMembershipsFilename = CommandLineParser.getStringParameter(arguments, "outMemberships", false);
		String inMembershipsFilename = CommandLineParser.getStringParameter(arguments, "inMemberships", false);
		boolean hardAssignments = CommandLineParser.getBooleanParameter(arguments, "hardAssignments", false, false);
		int nExperts = CommandLineParser.getIntegerParameter(arguments, "numberOfExperts", true, 10); 
    	String referencePartitionFilename = CommandLineParser.getStringParameter(arguments, "referencePartition", false);
    	String priorName = CommandLineParser.getStringParameter(arguments, "prior", false);
    	String priorResourcesFilename = CommandLineParser.getStringParameter(arguments, "priorResources", false);
    	double alpha = CommandLineParser.getDoubleParameter(arguments, "alpha", false, 1.0);
    	
    	MixtureOfMultinomials mom = new MixtureOfMultinomials(nExperts, false, alpha, priorName, priorResourcesFilename);
  	    mom.setWriteThreshold(0.001);
		mom.setEstimationThreshold(0.0);
    	
		
  	    logger.info("Running ClusterMap..."); 
		
		
		
		if (models != null)
			mom.readModels(new File(models));
		else {
			if (inMembershipsFilename == null) {
				logger.info("Random initialization");
				mom.randomInitialization(new File(data));
			}
			else {
				logger.info("Reading cluster memberships from " + inMembershipsFilename);
				mom.readClusterMemberships(new File(inMembershipsFilename));
			}
		}
		
		if (outMembershipsFilename != null)
			mom.calculateSufficientStatistics(new File(data), new File(outMembershipsFilename));
		else
			mom.calculateSufficientStatistics(new File(data));
		
		mom.writeSufficientStatistics(new File(stats));
		
		
		if (referencePartitionFilename != null){
			File refPartition = new File(referencePartitionFilename);
			File hypPartition = new File(outMembershipsFilename);
			double adjRandIndex = mom.computeAdjustedRandIndex(refPartition, hypPartition);					
			logger.info("\nAdjusted Rand Index : " + adjRandIndex);
		}
		
		logger.info("ClusterMap finished\n"); 	
	}

}

