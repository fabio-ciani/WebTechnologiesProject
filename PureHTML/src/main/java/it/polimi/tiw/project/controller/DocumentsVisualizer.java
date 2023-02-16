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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet("/documents")
public class DocumentsVisualizer extends HttpServlet {
	private Connection c = null;
	private TemplateEngine tEngine;

	public DocumentsVisualizer() {
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
		
		try {
			subfolder = Integer.parseInt(request.getParameter("subfolder"));

			if (subfolder == null)
				throw new InvalidDataException("Missing value");
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		DocumentDAO d = new DocumentDAO(c);
		List<DocumentBean> documents = new ArrayList<>();
		FolderBean f = new FolderBean();
		
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
			documents = d.getDocuments(subfolder, u.getUserID());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: failed documents fetch");
			return;
		}
		
		int subfoldersCount;
		
		try {
			subfoldersCount = new FolderDAO(c).getSubfoldersCount(u.getUserID());
			
			// Note: the given case will never happen.
			if (subfoldersCount < 0) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Subfolders count cannot be fetched");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: failed subfolders count fetch");
			return;
		}

		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		ctx.setVariable("documents", documents);
		ctx.setVariable("subfolder", f);
		ctx.setVariable("subfoldersCount", subfoldersCount);
		tEngine.process("WEB-INF/view/documents.html", ctx, response.getWriter());
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