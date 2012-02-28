package com.cleargist.recommendations.entity;

/**
 * activity event enumeration. 
 */
public enum Metric {

	REC_SERVED(0),
	REC_CLICK(1),
	PURCHASE(2),
	ADD_TO_CART(3),
	ITEM_PAGE(4),
	CTR(5);

	private int id;

	private Metric(int id) {
		this.id = id;
	}   
	
	public int getId() {
		return this.id;
	}
	

	public static Metric valueOf(int id) {
		switch (id) {
		case 0: return REC_SERVED;
		case 1: return REC_CLICK;
		case 2: return PURCHASE;
		case 3: return ADD_TO_CART;
		case 4: return ITEM_PAGE;
		case 5: return CTR;
		default: return REC_SERVED;
		}
	}
	
	public String getName() {
		switch(this.id) {
		case 0: return "Number of requests that returned one or more results";
		case 1: return "Number of times any recommendation in the widget was clicked";
		case 2: return "Total number of purchases from items that were clicked from widgets";
		case 5: return "Click-through rate";
		case 4: return "Number of item pages requests";
		case 3: return "Number of add to cart requests";
		default: return "Number of requests that returned one or more results";
		}
	}
	
	public String getShortDesc() {
		switch(this.id) {
		case 0: return "served";
		case 1: return "clicks";
		case 2: return "purchases";
		case 5: return "ctr";
		case 4: return "item pages";
		case 3: return "add to cart";
		default: return "served";
		}
	}
	
	public String getColor() {
		switch(this.id) {
		case 0: return "#0099FF";
		case 1: return "#ADFF2F";
		case 2: return "#97FFFF";
		case 5: return "#FFFF00";
		case 4: return "#DDDF0D";
		case 3: return "#DF5353";
		default: return "#0099FF";
		}
	}
	
	public String getMetricType() {
		switch(this.id) {
		case 0: return "spline";
		case 1: return "spline";
		case 2: return "spline";
		case 5: return "spline";
		case 4: return "spline";
		case 3: return "spline";
		default: return "spline";
		}
	}
	
	public String getYDesc() {
		switch(this.id) {
		case 0: return "Number Of Requests";
		case 1: return "Number of Clicks";
		case 2: return "Number of Purchases";
		case 5: return "Rate %";
		case 4: return "Item Pages Requests";
		case 3: return "Add To Cart Requests";
		default: return "Number Of Requests";
		}
	}
	
	@Override
	public String toString() {
		switch(this.id) {
		case 0: return "REC_SERVED";
		case 1: return "REC_CLICK";
		case 2: return "PURCHASE";
		case 5: return "CTR";
		case 4: return "ITEM_PAGE";
		case 3: return "ADD_TO_CART";
		default: return "REC_SERVED";
		}
	}
}