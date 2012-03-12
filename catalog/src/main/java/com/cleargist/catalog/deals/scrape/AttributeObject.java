package com.cleargist.catalog.deals.scrape;


public class AttributeObject implements Comparable<AttributeObject>{
	private String name;
	private int indx;
	
	public AttributeObject(String name, int s) {
		this.name = name;
		indx = s;
	}
	
	public int getIndx() {
		return indx;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int compareTo(AttributeObject us) {
		int diff = this.indx - us.getIndx();
		if (diff > 0) {
			return 1;
		}
		else if (diff < 0) {
			return -1;
		}
		return 0;
	}
}

