package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.DocumentBean;
import it.polimi.tiw.project.bean.FolderBean;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.exception.DuplicateException;
import it.polimi.tiw.project.exception.ForbiddenOperationException;
import it.polimi.tiw.project.exception.InvalidDataException;
import it.polimi.tiw.project.DAO.DocumentDAO;
import it.polimi.tiw.project.DAO.FolderDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/move")
public class DocumentMove extends HttpServlet {
	private Connection c = null;

	public DocumentMove() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Integer source = null;
		Integer destination = null;
		Integer documentID = null;
		
		try {
			source = Integer.parseInt(request.getParameter("source"));
			destination = Integer.parseInt(request.getParameter("destination"));
			documentID = Integer.parseInt(request.getParameter("document"));

			if (source == null || destination == null || documentID == null)
				throw new InvalidDataException("Missing values");
			
			if (source == destination)
				throw new InvalidDataException("Source and destination cannot coincide");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		DocumentDAO d = new DocumentDAO(c);
		DocumentBean document = new DocumentBean();
		FolderBean src = new FolderBean();
		FolderBean dest = new FolderBean();
		
		try {
			int subfoldersCount = new FolderDAO(c).getSubfoldersCount(u.getUserID());
			
			// Note: the given case will never happen.
			if (subfoldersCount < 0) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Subfolders count cannot be fetched");
				return;
			}
			
			if (subfoldersCount == 1)
				throw new ForbiddenOperationException("Disallowed request");
		} catch (ForbiddenOperationException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed subfolders count fetch");
			return;
		}
		
		try {
			src = new FolderDAO(c).getSubfolder(source, u.getUserID());
			
			if (src == null)
				throw new InvalidDataException("Source subfolder cannot be fetched");
			
			dest = new FolderDAO(c).getSubfolder(destination, u.getUserID());
			
			if (dest == null)
				throw new InvalidDataException("Destination subfolder cannot be fetched");
		} catch (InvalidDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed target subfolder(s) fetch");
			return;
		}

		try {
			document = d.getDocument(documentID, source, u.getUserID());
			
			if (document == null)
				throw new InvalidDataException("Document cannot be fetched");
		} catch (InvalidDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed target document fetch");
			return;
		}
		
		try {		
			// If the document (name and type) is duplicated, then the method returns true.
			if (d.checkDocumentAvailability(document.getName(), document.getType(), destination, u.getUserID())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Document already exists in destination subfolder");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed document duplication check");
			return;
		}
		
		try {
			d.moveDocument(documentID, destination);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed document move");
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