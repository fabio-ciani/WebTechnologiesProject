package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.DAO.UserDAO;
import it.polimi.tiw.project.exception.InvalidDataException;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet({ "", "/login" })
@MultipartConfig
public class UserLogin extends HttpServlet {
	private Connection c = null;

	public UserLogin() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = null;
		String pwd = null;
		
		try {
			email = request.getParameter("email");
			pwd = request.getParameter("pwd");

			if (email == null || email.isEmpty() || pwd == null || pwd.isEmpty())
				throw new InvalidDataException("Missing or empty values");
		} catch (InvalidDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}

		UserDAO d = new UserDAO(c);
		UserBean u = null;

		try {
			u = d.checkCredentials(email, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed credentials check");
			return;
		}

		if (u == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Incorrect e-mail or password");
		} else {
			request.getSession().setAttribute("user", u);
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.getWriter().println(email);
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect(getServletContext().getContextPath() + "/login.html");
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(c);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}