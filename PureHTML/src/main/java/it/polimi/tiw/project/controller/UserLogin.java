package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.DAO.UserDAO;
import it.polimi.tiw.project.exception.InvalidDataException;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet({ "", "/login" })
public class UserLogin extends HttpServlet {
	private Connection c = null;
	private TemplateEngine tEngine;

	public UserLogin() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());

		ServletContext servletCtx = getServletContext();
		ServletContextTemplateResolver templateRes = new ServletContextTemplateResolver(servletCtx);
		templateRes.setTemplateMode(TemplateMode.HTML);
		this.tEngine = new TemplateEngine();
		this.tEngine.setTemplateResolver(templateRes);
		templateRes.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = null;
		String pwd = null;
		
		try {
			username = request.getParameter("username");
			pwd = request.getParameter("pwd");

			if (username == null || username.isEmpty() || pwd == null || pwd.isEmpty())
				throw new InvalidDataException("Missing or empty values");
		} catch (InvalidDataException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}

		UserDAO d = new UserDAO(c);
		UserBean u = null;

		try {
			u = d.checkCredentials(username, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: failed credentials check");
			return;
		}

		if (u == null) {
			final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());

			ctx.setVariable("error", "Incorrect username or password");
			tEngine.process("WEB-INF/view/login.html", ctx, response.getWriter());
		} else {
			request.getSession().setAttribute("user", u);
			response.sendRedirect(getServletContext().getContextPath() + "/home");
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		tEngine.process("WEB-INF/view/login.html", ctx, response.getWriter());
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(c);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}