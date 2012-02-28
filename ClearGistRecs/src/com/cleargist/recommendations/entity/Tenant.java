package com.cleargist.recommendations.entity;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * Maps to tenant. 
 */
@Entity
@Table(name = "TENANT")
public class Tenant/* implements Serializable, UserDetails*/ {


	@Id
	@Column(name="ID")
	private String id;
	@Column(name="TOKEN")
	private int token;
	@Column(name="FIRSTNAME")
	private String firstName;
	@Column(name="LASTNAME")
	private String lastName;
	@Column(name="PHONE")
	private String phone;
	@Column(name="EMAIL", nullable=false)
	private String email;
	@Column(name="COMPANY")
	private String company;
	@Column(name="SITEURL")
	private String url;
	@Column(name="USER", nullable=false)
	private String username;
	@Column(name="PASS", nullable=false)
	private String password;
	@Column(name="REGDATE")
	@Temporal(TemporalType.DATE)
	private Date regDate = Calendar.getInstance().getTime();
	@Column(name="LATESTLOGIN")
	@Temporal(TemporalType.DATE)
	private Date latestLogin = Calendar.getInstance().getTime();
	@Column(name="LATESTPROFILE")
	@Temporal(TemporalType.DATE)
	private Date latestProfile = Calendar.getInstance().getTime();
	@Column(name="PROFILEHORIZON", nullable=false, columnDefinition="INT(1) default '0'")
	private int profileHorizon;
	@Column(name="CURRENTMODEL")
	private String currentModel;
	@Column(name="BACKUPMODEL")
	private String backUpModel;
	@Column(name="CATALOGSTATUS", nullable=false)
	private CatalogStatus catalogStatus;
	@Column(name="CATALOGMESSAGE")
	private String catalogStatusMessage;
	@Column(name="ACTIVE", nullable=false, columnDefinition="INT(1) default '0'")
	private int active;
	@OneToMany(mappedBy="tenant", targetEntity=Widget.class,fetch=FetchType.LAZY)
	private List<Widget> widgets= new ArrayList<Widget>();


	public Tenant() {
		super();
	}


	public Tenant(String id, int token, String firstName, String lastName,
			String phone, String email, String company, String url,
			String username, String password, Date regDate, int active) {
		super();
		this.id = id;
		this.token = token;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.email = email;
		this.company = company;
		this.url = url;
		this.username = username;
		this.password = password;
		this.regDate = regDate;
		this.active = active;
	}



	public Tenant(String id, int token, String firstName, String lastName,
			String phone, String email, String company, String url,
			String username, String password, Date regDate, Date latestLogin,
			Date latestProfile, int profileHorizon, String currentModel,
			String backUpModel, CatalogStatus catalogStatus,
			String catalogStatusMessage, int active) {
		super();
		this.id = id;
		this.token = token;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.email = email;
		this.company = company;
		this.url = url;
		this.username = username;
		this.password = password;
		this.regDate = regDate;
		this.latestLogin = latestLogin;
		this.latestProfile = latestProfile;
		this.profileHorizon = profileHorizon;
		this.currentModel = currentModel;
		this.backUpModel = backUpModel;
		this.catalogStatus = catalogStatus;
		this.catalogStatusMessage = catalogStatusMessage;
		this.active = active;
	}


	public String getId() {
		return id;
	}



	public int getToken() {
		return token;
	}



	public void setToken(int token) {
		this.token = token;
	}



	public void setId(String id) {
		this.id = id;
	}


	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public String getPhone() {
		return phone;
	}


	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getCompany() {
		return company;
	}


	public void setCompany(String company) {
		this.company = company;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
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


	public Date getRegDate() {
		return regDate;
	}


	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}


	public Date getLatestLogin() {
		return latestLogin;
	}


	public void setLatestLogin(Date latestLogin) {
		this.latestLogin = latestLogin;
	}


	public int getActive() {
		return active;
	}


	public void setActive(int active) {
		this.active = active;
	}
	
	
	public Date getLatestProfile() {
		return latestProfile;
	}


	public void setLatestProfile(Date latestProfile) {
		this.latestProfile = latestProfile;
	}


	public int getProfileHorizon() {
		return profileHorizon;
	}


	public void setProfileHorizon(int profileHorizon) {
		this.profileHorizon = profileHorizon;
	}


	public String getCurrentModel() {
		return currentModel;
	}


	public void setCurrentModel(String currentModel) {
		this.currentModel = currentModel;
	}


	public String getBackUpModel() {
		return backUpModel;
	}


	public void setBackUpModel(String backUpModel) {
		this.backUpModel = backUpModel;
	}
	

	public CatalogStatus getCatalogStatus() {
		return catalogStatus;
	}


	public void setCatalogStatus(CatalogStatus catalogStatus) {
		this.catalogStatus = catalogStatus;
	}


	public String getCatalogStatusMessage() {
		return catalogStatusMessage;
	}


	public void setCatalogStatusMessage(String catalogStatusMessage) {
		this.catalogStatusMessage = catalogStatusMessage;
	}

	
	public List<Widget> getWidgets() {
		return widgets;
	}


	public void setWidgets(List<Widget> widgets) {
		this.widgets = widgets;
	}


	/**
	 * All methods below this point are to implement the UserDetails interface.  
	 * They are all Transient because they are hard coded in the class for the
	 * time being.  If account expiration, etc, were implemented as part of a broader
	 * move to make this a multi-user app, these would be stored as well.
	 */
	/*@Transient
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
	
	@Transient
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Transient
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Transient
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Transient
	public boolean isEnabled() {
		return true;
	}*/



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + active;
		result = prime * result + ((company == null) ? 0 : company.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result
				+ ((latestLogin == null) ? 0 : latestLogin.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result + ((regDate == null) ? 0 : regDate.hashCode());
		result = prime * result + token;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tenant other = (Tenant) obj;
		if (active != other.active)
			return false;
		if (company == null) {
			if (other.company != null)
				return false;
		} else if (!company.equals(other.company))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (latestLogin == null) {
			if (other.latestLogin != null)
				return false;
		} else if (!latestLogin.equals(other.latestLogin))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (regDate == null) {
			if (other.regDate != null)
				return false;
		} else if (!regDate.equals(other.regDate))
			return false;
		if (token != other.token)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Tenant [id=" + id + ", token=" + token + ", firstName="
				+ firstName + ", lastName=" + lastName + ", phone=" + phone
				+ ", email=" + email + ", company=" + company + ", url=" + url
				+ ", username=" + username + ", password=" + password
				+ ", regDate=" + regDate + ", latestLogin=" + latestLogin
				+ ", active=" + active + "]";
	}

}