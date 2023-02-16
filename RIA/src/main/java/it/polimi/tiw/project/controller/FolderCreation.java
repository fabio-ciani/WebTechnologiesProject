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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/FolderCreation")
@MultipartConfig
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

			if (name.length() > 64)
				throw new InvalidDataException("Too long value in form field");
		} catch (InvalidDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}

		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		FolderDAO f = new FolderDAO(c);
		
		try {
			// If the folder name is duplicated, then the method returns true.
			if (f.checkFolderAvailability(name, u.getUserID())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Folder already exists in file-tree hierarchy");
				return;
			}
			
			f.createFolder(name, u);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: the folder couldn't be created");
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);
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