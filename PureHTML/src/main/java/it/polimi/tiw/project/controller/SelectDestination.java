package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.DocumentBean;
import it.polimi.tiw.project.bean.FolderBean;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.exception.ForbiddenOperationException;
import it.polimi.tiw.project.exception.InvalidDataException;
import it.polimi.tiw.project.DAO.DocumentDAO;
import it.polimi.tiw.project.DAO.FolderDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet("/move")
public class SelectDestination extends HttpServlet {
	private Connection c = null;
	private TemplateEngine tEngine;

	public SelectDestination() {
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Integer subfolder = null;
		Integer documentID = null;
		
		try {
			subfolder = Integer.parseInt(request.getParameter("subfolder"));
			documentID = Integer.parseInt(request.getParameter("document"));

			if (subfolder == null || documentID == null)
				throw new InvalidDataException("Missing values");
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		DocumentDAO d = new DocumentDAO(c);
		DocumentBean document = new DocumentBean();
		FolderBean f = new FolderBean();
		
		try {
			int subfoldersCount = new FolderDAO(c).getSubfoldersCount(u.getUserID());
			
			// Note: the given case will never happen.
			if (subfoldersCount < 0) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Subfolders count cannot be fetched");
				return;
			}
			
			if (subfoldersCount == 1)
				throw new ForbiddenOperationException("Disallowed request");
		} catch (ForbiddenOperationException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: failed subfolders count fetch");
			return;
		}
		
		try {
			f = new FolderDAO(c).getSubfolder(subfolder, u.getUserID());
			
			if (f == null)
				throw new InvalidDataException("Subfolder cannot be fetched");
		} catch (InvalidDataException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: failed target subfolder fetch");
			return;
		}

		try {
			document = d.getDocument(documentID, subfolder, u.getUserID());
			
			if (document == null)
				throw new InvalidDataException("Document cannot be fetched");
		} catch (InvalidDataException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: failed target document fetch");
			return;
		}
		
		RequestDispatcher reqDispatcher = request.getRequestDispatcher("/home");
		request.setAttribute("document", document);
		request.setAttribute("sourceFolder", f);
		request.setAttribute("requireMove", true);
		reqDispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The operation does not support POST method");
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(c);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}