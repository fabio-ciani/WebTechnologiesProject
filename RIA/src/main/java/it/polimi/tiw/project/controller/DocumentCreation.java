package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.FolderBean;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.DAO.FolderDAO;
import it.polimi.tiw.project.DAO.DocumentDAO;
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

@WebServlet("/DocumentCreation")
@MultipartConfig
public class DocumentCreation extends HttpServlet {
	private Connection c = null;

	public DocumentCreation() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = null;
		String type = null;
		String summary = null;
		Integer subfolder = null;
		
		try {
			name = request.getParameter("name");
			type = request.getParameter("type");
			summary = request.getParameter("summary");
			subfolder = Integer.parseInt(request.getParameter("subfolder"));
			
			if (name == null || name.isEmpty() || type == null || type.isEmpty() || summary == null || subfolder == null)
				throw new InvalidDataException("Missing or empty values");
			
			if (name.length() > 64 || type.length() > 32 || summary.length() > 512)
				throw new InvalidDataException("Too long values in form field");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		DocumentDAO d = new DocumentDAO(c);
		
		try {
			if (d.checkDocumentAvailability(name, type, subfolder, u.getUserID())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Document already exists in parent subfolder");
				return;
			}
			
			FolderBean f = new FolderDAO(c).getSubfolder(subfolder, u.getUserID());
			if (f == null)
				throw new InvalidDataException("Document cannot be created");
			
			d.createDocument(name, type, summary, f);
		} catch (InvalidDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: the document couldn't be created");
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