package com.jsi.alert.model.notification;

public class ItemNotification extends Notification {

	private static final long serialVersionUID = -5271981637015590457L;

	private String url;
	private Double similarity;
	private String content;
	private String subject;
	
	public ItemNotification() {
		super(Type.ITEM);
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Double getSimilarity() {
		return similarity;
	}
	public void setSimilarity(Double similarity) {
		this.similarity = similarity;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
}
