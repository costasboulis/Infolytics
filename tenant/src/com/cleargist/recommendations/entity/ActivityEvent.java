package com.cleargist.recommendations.entity;

/**
 * activity event enumeration. 
 */
public enum ActivityEvent {

	PURCHASE(0),
	ITEM_PAGE(1),
	ADD_TO_CART(2),
	REC_SERVED(3),
	REC_SEEN(4),
	REC_CLICK(5),
	HOME_PAGE_VIEW(6),
	CATEGORY_PAGE_VIEW(7),
	SEARCH(8),
	RATING(9),
	ERROR(10);

	private int id;

	private ActivityEvent(int id) {
		this.id = id;
	}   
	
	public int getId() {
		return this.id;
	}

	public static ActivityEvent valueOf(int id) {
		switch (id) {
		case 0: return PURCHASE;
		case 1: return ITEM_PAGE;
		case 2: return ADD_TO_CART;
		case 3: return REC_SERVED;
		case 4: return REC_SEEN;
		case 5: return REC_CLICK;
		case 6: return HOME_PAGE_VIEW;
		case 7: return CATEGORY_PAGE_VIEW;
		case 8: return SEARCH;
		case 9: return RATING;
		case 10: return ERROR;
		default: return ITEM_PAGE;
		}
	}
	
	public String getName() {
		switch(this.id) {
		case 0: return "PURCHASE";
		case 1: return "ITEM_PAGE";
		case 2: return "ADD_TO_CART";
		case 3: return "REC_SERVED";
		case 4: return "REC_SEEN";
		case 5: return "REC_CLICK";
		case 6: return "HOME_PAGE_VIEW";
		case 7: return "CATEGORY_PAGE_VIEW";
		case 8: return "SEARCH";
		case 9: return "RATING";
		case 10: return "ERROR";
		default: return "ITEM_PAGE";
		}
	}
	
	@Override
	public String toString() {
		switch(this.id) {
		case 0: return "PURCHASE";
		case 1: return "ITEM_PAGE";
		case 2: return "ADD_TO_CART";
		case 3: return "REC_SERVED";
		case 4: return "REC_SEEN";
		case 5: return "REC_CLICK";
		case 6: return "HOME_PAGE_VIEW";
		case 7: return "CATEGORY_PAGE_VIEW";
		case 8: return "SEARCH";
		case 9: return "RATING";
		case 10: return "ERROR";
		default: return "ITEM_PAGE";
		}
	}
}