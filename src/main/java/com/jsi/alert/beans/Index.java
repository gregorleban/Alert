package com.jsi.alert.beans;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.alert.model.UserPrincipal;
import com.jsi.alert.model.notification.Notification;
import com.jsi.alert.service.AuthenticatorService;
import com.jsi.alert.service.NotificationService;
import com.jsi.alert.utils.Configuration;

/**
 * A presenter for the index page.
 */
@ManagedBean
@RequestScoped
public class Index {
	
	private static final Logger log = LoggerFactory.getLogger(Index.class);
	
	private HttpSession session;
	private UserPrincipal user;
	
	private List<Notification> notifications;
	
	/**
	 * Fetches the session and checks if the user is authenticated.
	 */
	@PostConstruct
	public void init() {
		notifications = new ArrayList<>();
		
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		session = request.getSession();
		
		if (authenticateUser()) {
			fetchNotifications();
		}
	}
	
	
	/**
	 * Authenticates the user and saves their credentials into the session.
	 * 
	 * @return
	 */
	private boolean authenticateUser() {
		boolean authenticated = AuthenticatorService.authenticateUser(session);
		setUser(authenticated ? (UserPrincipal) session.getAttribute(Configuration.USER_PRINCIPAL) : null);
		
		return authenticated;
	}
	
	/**
	 * Fetches notifications and stores them in this class.
	 */
	public void fetchNotifications() {
		if (user == null || user.getUuid() == null) {
			log.warn("Tried to fetch notifications for an invalid user...");
			return;
		}
		
		List<Notification> newNotifications = NotificationService.fetchNotifications(user.getUuid());
		notifications.addAll(newNotifications);
	}
	
	public boolean isUserLoggedIn() {
		return user != null;
	}
	
	public String getLoginUrl() {
		return Configuration.LOGIN_URL;
	}
	
	public String getLogoutUrl() {
		return Configuration.LOGOUT_URL;
	}

	public UserPrincipal getUser() {
		return user;
	}

	public void setUser(UserPrincipal user) {
		this.user = user;
	}


	public List<Notification> getNotifications() {
		return notifications;
	}


	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}
	
	public int getNNotifications() {
		return notifications == null ? 0 : notifications.size();
	}
}
