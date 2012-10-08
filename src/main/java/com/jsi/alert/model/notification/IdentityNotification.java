package com.jsi.alert.model.notification;

public class IdentityNotification extends Notification {

	private static final long serialVersionUID = 3811649128276710480L;

	private String name;
	private String profile;
	private String profileUrl;
	private String imageUrl;
	
	public IdentityNotification() {
		super(Type.IDENTITY);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	public String getProfileUrl() {
		return profileUrl;
	}
	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	
}
