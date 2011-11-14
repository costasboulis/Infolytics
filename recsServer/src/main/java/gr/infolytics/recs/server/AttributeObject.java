package gr.infolytics.recs.server;

import gr.infolytics.recs.server.jaxb.ItemType;

public class AttributeObject implements Comparable<AttributeObject>{
	private ItemType item;
	private double score;
	
	public AttributeObject(ItemType item, double s) {
		this.item = item;
		score = s;
	}
	
	public double getScore() {
		return score;
	}
	
	public ItemType getItem() {
		return this.item;
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

