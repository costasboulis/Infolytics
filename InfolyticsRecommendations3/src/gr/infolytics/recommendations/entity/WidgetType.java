package gr.infolytics.recommendations.entity;

/**
 * activity event enumeration. 
 */
public enum WidgetType {

	Most_popular_overall(0),
	Most_popular_in_category(1),
	Customers_who_viewed_this_also_viewed_that(2),
	Customers_who_bought_this_also_bought_that(3),
	Recommended_For_You(4),
	Packaged_for_you(5);

	private int id;

	private WidgetType(int id) {
		this.id = id;
	}   
	
	public int getId() {
		return this.id;
	}
	

	public static WidgetType valueOf(int id) {
		switch (id) {
		case 0: return Most_popular_overall;
		case 1: return Most_popular_in_category;
		case 2: return Customers_who_viewed_this_also_viewed_that;
		case 3: return Customers_who_bought_this_also_bought_that;
		case 4: return Recommended_For_You;
		case 5: return Packaged_for_you;
		default: return Most_popular_overall;
		}
	}
	
	public String getName() {
		switch(this.id) {
		case 0: return "Most_popular_overall";
		case 1: return "Most_popular_in_category";
		case 2: return "Customers_who_viewed_this_also_viewed_that";
		case 3: return "Customers_who_bought_this_also_bought_that";
		case 4: return "Recommended_For_You";
		case 5: return "Packaged_for_you";
		default: return "Most_popular_overall";
		}
	}
	
	@Override
	public String toString() {
		switch(this.id) {
		case 0: return "Most_popular_overall";
		case 1: return "Most_popular_in_category";
		case 2: return "Customers_who_viewed_this_also_viewed_that";
		case 3: return "Customers_who_bought_this_also_bought_that";
		case 4: return "Recommended_For_You";
		case 5: return "Packaged_for_you";
		default: return "Most_popular_overall";
		}
	}
}