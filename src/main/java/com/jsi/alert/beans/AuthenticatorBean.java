package com.jsi.alert.beans;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.alert.model.AlertUser;
import com.jsi.alert.utils.Configuration;
import com.jsi.alert.utils.UserAuthenticator;

/**
 * A presenter for the index page.
 */
@ManagedBean
@RequestScoped
public class AuthenticatorBean {
	
	private static final Logger log = LoggerFactory.getLogger(AuthenticatorBean.class);
	
	private HttpSession session;
	private AlertUser user;
	
	/**
	 * Fetches the session and checks if the user is authenticated.
	 */
	@PostConstruct
	public void init() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		session = request.getSession();
		if (authenticateUser())
			setUser((AlertUser) session.getAttribute(Configuration.USER_PRINCIPAL));
	}
	
	private boolean authenticateUser() {
		return UserAuthenticator.authenticateUser(session);
	}
	
	public boolean isUserLoggedIn() {
		return user != null;
	}
	
	public String getLoginUrl() {
		return Configuration.LOGIN_URL;
	}
	
	public String getLogoutUrl() {
		return getLoginUrl();
	}

	public AlertUser getUser() {
		return user;
	}

	public void setUser(AlertUser user) {
		this.user = user;
	}
}
