package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.DAO.UserDAO;
import it.polimi.tiw.project.exception.InvalidDataException;
import it.polimi.tiw.project.exception.RegexException;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.regex.*;

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

@WebServlet("/register")
public class UserRegistration extends HttpServlet {
	private Connection c = null;
	private TemplateEngine tEngine;

	Pattern usernamePattern = Pattern.compile("^[a-zA-Z\\d]+(?:(?:-[a-zA-Z\\d]+)*|(?:\\.[a-zA-Z\\d]+)*)\\z");
	Pattern pwdPattern = Pattern.compile("^[\\w\\.\\-\\!$%&?#@]+\\z");

	public UserRegistration() {
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
	
	// th:if could be used iff a UserRegistration servlet exists, because static files do not experience any variable setup process.
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = null;
		String surname = null;
		String username = null;
		String pwd = null;
		
		try {
			name = request.getParameter("name");
			surname = request.getParameter("surname");
			username = request.getParameter("username");
			pwd = request.getParameter("pwd");

			if (name == null || name.isEmpty() || surname == null || surname.isEmpty() || username == null || username.isEmpty() || pwd == null || pwd.isEmpty())
				throw new InvalidDataException("Missing or empty values");
			
			boolean lengthError = name.length() > 32 || surname.length() > 32 || username.length() > 32 || pwd.length() < 4;

			Matcher usernameMatcher = usernamePattern.matcher(username);
			boolean usernameBool = usernameMatcher.matches();
			
			/*
			if (!usernameBool)
				throw new RegexException("Invalid username format");
			*/

			Matcher pwdMatcher = pwdPattern.matcher(pwd);
			boolean pwdBool = pwdMatcher.matches();
			
			/*
			if (!pwdBool)
				throw new RegexException("Invalid password format");
			*/
			
			boolean credentialsError = !usernameBool || !pwdBool;
			
			if (lengthError || credentialsError) {
				final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
				
				if (name.length() > 32)
					ctx.setVariable("nameError", "Too long value");
				
				if (surname.length() > 32)
					ctx.setVariable("surnameError", "Too long value");
				
				if (username.length() > 32)
					ctx.setVariable("usernameError", "Too long value");
				
				if (pwd.length() < 4)
					ctx.setVariable("pwdError", "Too short value");
				
				if (!usernameBool)
					ctx.setVariable("usernameFormatError", "Invalid username format");
				
				if (!pwdBool)
					ctx.setVariable("pwdFormatError", "Invalid password format");
				
				tEngine.process("WEB-INF/view/register.html", ctx, response.getWriter());
				return;
			}
		/*
		} catch (InvalidDataException | RegexException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		*/
		} catch (InvalidDataException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}

		UserDAO u = new UserDAO(c);

		try {
			// If the username is already taken, then the method returns true.
			if (u.checkUsernameAvailability(username)) {
				final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
				
				ctx.setVariable("usernameNotAvailable", "Already taken username");
				tEngine.process("WEB-INF/view/register.html", ctx, response.getWriter());
				return;
			}
			
			u.registerUser(name, surname, username, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: the user couldn't be registered");
			return;
		}
		
		response.sendRedirect(getServletContext().getContextPath() + "/");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		tEngine.process("WEB-INF/view/register.html", ctx, response.getWriter());
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(c);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}