package com.jsi.alert.model.notification;

public class EventNotification extends Notification {

	private static final long serialVersionUID = -5222367640719734164L;

	private String name;
	private String description;
	private String url;
	
	public EventNotification() {
		super(Type.EVENT);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
