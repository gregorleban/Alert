package com.jsi.alert.model.notification;

import java.io.Serializable;
import java.util.Date;

public abstract class Notification implements Serializable {
	
	private static final long serialVersionUID = 5418781769222210517L;
	
	enum Type {
		EVENT(0),
		IDENTITY(1),
		ISSUE(2),
		ITEM(3);
		
		public final int value;
		
		private Type(int value) {
			this.value = value;
		}
	}
	
	private int type;
	private String title;
	private String link;
	private Date publishDate;
	
	public Notification(Type type) {
		setType(type.value);
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public Date getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
