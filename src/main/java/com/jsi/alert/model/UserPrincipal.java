package com.jsi.alert.model;

import java.io.Serializable;

public class UserPrincipal implements Serializable {

	private static final long serialVersionUID = -6511117109181574707L;
	
	private String email;
	private String uuid;
	private Boolean admin;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Boolean getAdmin() {
		return admin;
	}
	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}
}
