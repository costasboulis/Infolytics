package gr.infolytics.recommendations.entity;

/**
 * activity event enumeration. 
 */
public enum Authority {

	ROLE_USER(0),
	ROLE_ADMIN(1);

	private int id;

	private Authority(int id) {
		this.id = id;
	}   
	
	public int getId() {
		return this.id;
	}

	public static Authority valueOf(int id) {
		switch (id) {
		case 0: return ROLE_USER;
		default: return ROLE_ADMIN;
		}
	}
	
	public String getName() {
		switch(this.id) {
		case 0: return "ROLE_USER";
		default: return "ROLE_ADMIN";
		}
	}
	
	@Override
	public String toString() {
		switch(this.id) {
		case 0: return "ROLE_USER";
		default: return "ROLE_ADMIN";
		}
	}
}