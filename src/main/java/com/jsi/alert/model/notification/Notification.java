package com.jsi.alert.model.notification;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
	
	private static final long serialVersionUID = 5418781769222210517L;
	
	private String title;
	private String link;
	private String publishDate;
	private String content;
	
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
	public String getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
