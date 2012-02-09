package gr.infolytics.recommendations.entity;

/**
 * activity event enumeration. 
 */
public enum ErrorType {

	UKNKNOWN(0),
	RECORD_ACTIVITY(1);

	private int id;

	private ErrorType(int id) {
		this.id = id;
	}   
	
	public int getId() {
		return this.id;
	}

	public static ErrorType valueOf(int id) {
		switch (id) {
		case 0: return UKNKNOWN;
		case 1: return RECORD_ACTIVITY;
		default: return UKNKNOWN;
		}
	}
	
	public String getName() {
		switch(this.id) {
		case 0: return "UKNKNOWN";
		case 1: return "RECORD_ACTIVITY";
		default: return "UKNKNOWN";
		}
	}
	
	@Override
	public String toString() {
		switch(this.id) {
		case 0: return "UKNKNOWN";
		case 1: return "RECORD_ACTIVITY";
		default: return "UKNKNOWN";
		}
	}
}