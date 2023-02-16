package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.DAO.FolderDAO;
import it.polimi.tiw.project.exception.DuplicateException;
import it.polimi.tiw.project.exception.InvalidDataException;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/FolderCreation")
public class FolderCreation extends HttpServlet {
	private Connection c = null;

	public FolderCreation() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = null;
		
		try {
			name = request.getParameter("name");

			if (name == null || name.isEmpty())
				throw new InvalidDataException("Missing or empty value");

			if (name.length() > 64) {
				response.sendRedirect(getServletContext().getContextPath() + "/manager?folderLengthError=true");
				return;
			}
		} catch (InvalidDataException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}

		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		FolderDAO f = new FolderDAO(c);
		
		try {
			// If the folder name is duplicated, then the method returns true.
			if (f.checkFolderAvailability(name, u.getUserID())) {
				response.sendRedirect(getServletContext().getContextPath() + "/manager?folderNameError=true");
				return;
			}
			
			f.createFolder(name, u);
		/*
		} catch (DuplicateException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			return;
		*/
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: the folder couldn't be created");
			return;
		}

		response.sendRedirect(getServletContext().getContextPath() + "/home");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The operation does not support GET method");
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(c);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}