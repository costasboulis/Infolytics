package gr.infolytics.trainer;

public class AttributeObject implements Comparable<AttributeObject>{
	private String id;
	private double score;
	
	public AttributeObject(String id, double s) {
		this.id = id;
		score = s;
	}
	
	public double getScore() {
		return score;
	}
	
	public String getId() {
		return id;
	}
	
	public int compareTo(AttributeObject us) {
		double diff = this.score - us.getScore();
		if (diff < 0.0) {
			return 1;
		}
		else if (diff > 0.0) {
			return -1;
		}
		return 0;
	}
}

