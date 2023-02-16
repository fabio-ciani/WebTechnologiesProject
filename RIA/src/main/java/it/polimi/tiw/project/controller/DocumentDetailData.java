package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.DocumentBean;
import it.polimi.tiw.project.bean.FolderBean;
import it.polimi.tiw.project.bean.UserBean;
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

import com.google.gson.Gson;

@WebServlet("/detail")
public class DocumentDetailData extends HttpServlet {
	private Connection c = null;

	public DocumentDetailData() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		DocumentDAO d = new DocumentDAO(c);
		DocumentBean document = new DocumentBean();
		FolderBean f = new FolderBean();
		
		try {
			f = new FolderDAO(c).getSubfolder(subfolder, u.getUserID());
			
			if (f == null)
				throw new InvalidDataException("Subfolder cannot be fetched");
		} catch (InvalidDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed target subfolder fetch");
			return;
		}

		try {
			document = d.getDocument(documentID, subfolder, u.getUserID());
			
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
		
		Gson gson = new Gson();
		String json = gson.toJson(document);
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.getWriter().println(json);
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