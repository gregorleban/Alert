package com.jsi.alert.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jsi.alert.model.AlertUser;
import com.jsi.alert.utils.Configuration;

/**
 * A <code>Servlet</code>
 */
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = -8506312155279509194L;
	
	private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
	
	private static final String EMAIL_PARAM = "email";
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (log.isInfoEnabled()) log.info("Received login request...");
		
		try {
			String email = request.getParameter(EMAIL_PARAM);
			if (email != null) {
				if (log.isDebugEnabled())
					log.debug("Saving credentials to session [" + email + "]...");
				
				// create a session and redirect to the index page
				HttpSession session = request.getSession();
				
				AlertUser user = new AlertUser();
				user.setEmail(email);
				
				session.setAttribute(Configuration.USER_PRINCIPAL, user);
				response.sendRedirect("index.xhtml");
			} else {
				if (log.isInfoEnabled())
					log.info("Cannot create a session: email=" + email + ", redirecting to login page!");
				response.sendRedirect(Configuration.LOGIN_URL);
			}
		} catch (Throwable t) {
			log.error("Failed to create a session!", t);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
