package gr.infolytics.recommendations.util;

/**
 * Utilities for determining what logical "stage" our application is currently
 * running in.
 *
 * This code relies on the Java system property <code>application.stage</code>
 * to determine if the application is running in "beta", "gamma", "prod", etc.
 *
 * By including the application stage in any AWS resources you create (SQS
 * queues, SNS topics, S3 buckets, etc.), you can easily run multiple versions
 * of the same application in one AWS account without each instance of your
 * application stomping on other instances' AWS resources.
 */
public class StageUtils {

	/**
	 * The Java system property declaring the stage in which the application
	 * should run (ex: 'beta', 'gamma', 'prod').
	 */
	public static final String STAGE_PROPERTY_NAME = "application.stage";

	/**
	 * Returns a resource suffix that can be added to the end of AWS resource
	 * names to isolate AWS resources by application stage.
	 *
	 * For example, if the <code>application.stage</code> Java system property
	 * is set to "prod", this method would return "-prod". If no value has been
	 * specified for <code>application.stage</code>, this method will return the
	 * empty string, "".
	 *
	 * @return A suffix suitable for appending to an AWS resource's unique name
	 *         (ex: an Amazon S3 bucket, or SQS queue) to indicate in what
	 *         application stage the resource was created. If no application
	 *         stage has been set through the <code>application.stage</code>
	 *         Java system property, this method returns the empty string, "".
	 */
	public static String getResourceSuffixForCurrentStage() {
		String currentStage = getCurrentStage();
		if (currentStage == null) return "";
		return "-" + currentStage;
	}

	/**
	 * Returns the current stage in which our application is running, which is
	 * determined by looking at the <code>application.stage</code> Java system
	 * property.
	 *
	 * @return the current stage in which our application is running, or null if
	 *         no stage has been specified.
	 */
	public static String getCurrentStage() {
		return System.getProperty(STAGE_PROPERTY_NAME);
	}
}