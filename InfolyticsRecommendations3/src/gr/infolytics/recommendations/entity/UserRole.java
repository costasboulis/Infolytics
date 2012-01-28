package gr.infolytics.recommendations.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Maps to tenant. 
 */
@Entity
@Table(name = "user_roles")
public class UserRole {
	
	@Id
	@Column(name="USER_ROLE_ID")
	private int id;
	/**
	 * we did not create a relationship @OneToMany with User as 
	 * in our case user and user roles is a one to one relationship 
	 */
	@Column(name="USER_ID", nullable=false)
	private int userId;
	@Column(name="AUTHORITY", nullable=false)
	private String authority;

	public UserRole() {
		super();
	}

	
	public UserRole(int id, int userId, String authority) {
		super();
		this.id = id;
		this.userId = userId;
		this.authority = authority;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	@Override
	public String toString() {
		return "UserRole [id=" + id + ", userId=" + userId + ", authority="
				+ authority + "]";
	}

}