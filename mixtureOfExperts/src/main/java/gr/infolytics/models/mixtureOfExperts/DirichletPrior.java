package gr.infolytics.models.mixtureOfExperts;


/**
 * Abstract class for calculating the prior of attributes.  
 * <p>
 * When estimating the probabilities of attributes given clusters we use a Maximum-A-Posteriori approach
 * and assign a prior to the attribute parameters. The prior can be made as specific as we choose to. 
 * For example, we can assign a different prior for each cluster & attribute or we can assign the same value for all 
 * attributes and clusters (add-one prior).
 * 
 * 
 * @author kboulis
 *
 */

public abstract class DirichletPrior {
	
	String priorName;
	
	DirichletPrior(String name){
		priorName = name;
	}
	
	
	/** Returns the name of the prior object
	 * 
	 * @return
	 */
	public String getName(){
		return(priorName);
	}
	
	
	/**
	 * Returns the parameter for \"attribute\" of the "\cluster\" dirichlet distribution
	 * 
	 * @param cluster
	 * @param item
	 * @return
	 */
	public abstract float getAttributeHyperparameter(int cluster, int attribute);
	
	
	/**
	 * Returns the sum of all hyperparameters of the "\cluster\" dirichlet distribution
	 * @return
	 */
	public abstract float getSumOfHyperparameters(int cluster);
	
	
	/**
	 * Returns the log parameter for \"attribute\" of the "\cluster\" dirichlet distribution
	 * @return
	 */
	public abstract float getAttributeLogHyperparameter(int cluster, int attribute);
		
	
	
}