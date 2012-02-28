package com.cleargist.recommendations.entity;

/**
 * activity event enumeration. 
 */
public enum StatisticalPeriod {

	LAST_30DAYS(0),
	LAST_6MONTHS(1),
	LAST_YEAR(2);

	private int id;

	private StatisticalPeriod(int id) {
		this.id = id;
	}   
	
	public int getId() {
		return this.id;
	}
	

	public static StatisticalPeriod valueOf(int id) {
		switch (id) {
		case 0: return LAST_30DAYS;
		case 1: return LAST_6MONTHS;
		case 2: return LAST_YEAR;
		default: return LAST_30DAYS;
		}
	}
	
	public String getName() {
		switch(this.id) {
		case 0: return "LAST_30DAYS";
		case 1: return "LAST_6MONTHS";
		case 2: return "LAST_YEAR";
		default: return "LAST_30DAYS";
		}
	}
	
	@Override
	public String toString() {
		switch(this.id) {
		case 0: return "LAST_30DAYS";
		case 1: return "LAST_6MONTHS";
		case 2: return "LAST_YEAR";
		default: return "LAST_30DAYS";
		}
	}
}