package gr.infolytics.models.mixtureOfExperts;


public class DirichletPriorFactory {
	public static DirichletPrior getPrior(String priorName, int nExperts, float alpha, String resourcesFilename){
		if (priorName.equalsIgnoreCase("uniform")){
			return new UniformDirichletPrior(alpha, resourcesFilename);
		}
		else if (priorName.equalsIgnoreCase("nonUniform")){
			return new NonUniformDirichletPrior(alpha, resourcesFilename);
		}
		else {
			return null;
		}
	}
}
