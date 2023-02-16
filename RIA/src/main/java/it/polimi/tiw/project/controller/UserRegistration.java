package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.DAO.UserDAO;
import it.polimi.tiw.project.exception.InvalidDataException;
import it.polimi.tiw.project.exception.RegexException;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.regex.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/register")
@MultipartConfig
public class UserRegistration extends HttpServlet {
	private Connection c = null;

	Pattern emailPattern = Pattern.compile("^[a-z0-9\\.\\-_]+@[a-z0-9\\.-]+\\.[a-z]{2,4}\\z");
	Pattern pwdPattern = Pattern.compile("^[\\w\\.\\-\\!$%&?#@]+\\z");

	public UserRegistration() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = null;
		String surname = null;
		String email = null;
		String pwd = null;
		String repeat = null;
		
		try {
			name = request.getParameter("name");
			surname = request.getParameter("surname");
			email = request.getParameter("email");
			pwd = request.getParameter("pwd");
			repeat = request.getParameter("repeat");

			if (name == null || name.isEmpty() || surname == null || surname.isEmpty() || email == null || email.isEmpty() || pwd == null || pwd.isEmpty() || repeat == null || repeat.isEmpty())
				throw new InvalidDataException("Missing or empty values");
			
			boolean lengthError = name.length() > 32 || surname.length() > 32 || email.length() > 128 || pwd.length() < 4;

			Matcher emailMatcher = emailPattern.matcher(email);
			boolean emailBool = emailMatcher.matches();

			Matcher pwdMatcher = pwdPattern.matcher(pwd);
			boolean pwdBool = pwdMatcher.matches();
			
			boolean credentialsError = !emailBool || !pwdBool;
			
			boolean pwdError = !(pwd.equals(repeat));
			
			if (lengthError || credentialsError || pwdError) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				
				if (lengthError)
					response.getWriter().println("Too long/short values");
				
				if (credentialsError)
					response.getWriter().println("Invalid format on form fields");
				
				if (pwdError)
					response.getWriter().println("Passwords do not coincide");
				
				return;
			}
		} catch (InvalidDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}

		UserDAO u = new UserDAO(c);

		try {
			// If the email is already taken, then the method returns true.
			if (u.checkEmailAvailability(email)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Already taken email");
				return;
			}
			
			u.registerUser(name, surname, email, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: the user couldn't be registered");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect(getServletContext().getContextPath() + "/register.html");
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(c);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}