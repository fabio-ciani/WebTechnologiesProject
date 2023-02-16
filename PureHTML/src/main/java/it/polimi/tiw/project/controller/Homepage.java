package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.DocumentBean;
import it.polimi.tiw.project.bean.FolderBean;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.DAO.FolderDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@WebServlet("/home")
public class Homepage extends HttpServlet {
	private Connection c = null;
	private TemplateEngine tEngine;

	public Homepage() {
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
		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		FolderDAO f = new FolderDAO(c);
		List<FolderBean> folders = new ArrayList<>();
		Map<Integer, List<FolderBean>> subfolders = new HashMap<>();

		try {
			folders = f.getFolders(u.getUserID());
			
			for (FolderBean element : folders)
				subfolders.put(element.getID(), f.getSubfolders(element.getID(), u.getUserID()));
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An error has occured: failed folders fetch");
			return;
		}

		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		ctx.setVariable("folders", folders);
		ctx.setVariable("subfolders", subfolders);
		ctx.setVariable("subfoldersSize", subfolders.values().stream().mapToInt(List::size).sum());
		// Handle a request for moving a document.
		if (request.getAttribute("requireMove") != null && (boolean) request.getAttribute("requireMove"))
			processMoveRequest(request, ctx);
		tEngine.process("WEB-INF/view/home.html", ctx, response.getWriter());
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
	
	private void processMoveRequest(HttpServletRequest req, WebContext context) {
		context.setVariable("requireMove", true);
		context.setVariable("document", (DocumentBean) req.getAttribute("document"));
		context.setVariable("sourceFolder", (FolderBean) req.getAttribute("sourceFolder"));
	}
}