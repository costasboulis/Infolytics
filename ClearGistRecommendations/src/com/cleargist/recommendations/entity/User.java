package com.cleargist.recommendations.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Maps to tenant. 
 */
@Entity
@Table(name = "users")
public class User {
	
	@Id
	@Column(name="USER_ID")
	private int id;
	@Column(name="USERNAME", nullable=false)
	private String username;
	@Column(name="PASSWORD", nullable=false)
	private String password;
	@Column(name="ENABLED", nullable=false, columnDefinition="INT(1) default '0'")
	private int active;


	public User() {
		super();
	}
	
	

	public User(int id, String username, String password, int active) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.active = active;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public int getActive() {
		return active;
	}


	public void setActive(int active) {
		this.active = active;
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password="
				+ password + ", active=" + active + "]";
	}

	
	

}