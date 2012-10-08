package com.jsi.alert.model.notification;

public class IssueNotification extends Notification {

	private static final long serialVersionUID = 3252145903887426771L;

	private String bugId;
	private String subject;
	private String summary;
	private String url;
	
	public IssueNotification() {
		super(Type.ISSUE);
	}
	
	public String getBugId() {
		return bugId;
	}
	public void setBugId(String bugId) {
		this.bugId = bugId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
