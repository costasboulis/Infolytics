package com.cleargist.recommendations.util;


public class AttributeObject implements Comparable<AttributeObject>{
	private String uid;
	private double score;
	
	public AttributeObject(String uid, double s) {
		this.uid = uid;
		score = s;
	}
	
	public double getScore() {
		return score;
	}
	
	public String getUID() {
		return this.uid;
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

