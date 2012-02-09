package gr.infolytics.recommendations.entity;

/**
 * activity event enumeration. 
 */
public enum CatalogStatus {

	SYNCING(0),
	INSYNC(1),
	FAILED(2),
	WAITING(3);

	private int id;

	private CatalogStatus(int id) {
		this.id = id;
	}   
	
	public int getId() {
		return this.id;
	}
	

	public static CatalogStatus valueOf(int id) {
		switch (id) {
		case 0: return SYNCING;
		case 1: return INSYNC;
		case 2: return FAILED;
		case 3: return WAITING;
		default: return INSYNC;
		}
	}
	
	public String getName() {
		switch(this.id) {
		case 0: return "SYNCING";
		case 1: return "INSYNC";
		case 2: return "FAILED";
		case 3: return "WAITING";
		default: return "INSYNC";
		}
	}
	
	@Override
	public String toString() {
		switch(this.id) {
		case 0: return "SYNCING";
		case 1: return "INSYNC";
		case 2: return "FAILED";
		case 3: return "WAITING";
		default: return "INSYNC";
		}
	}
}