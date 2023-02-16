package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.ConnectionHandler;
import it.polimi.tiw.project.bean.DocumentBean;
import it.polimi.tiw.project.bean.FolderBean;
import it.polimi.tiw.project.bean.UserBean;
import it.polimi.tiw.project.DAO.DocumentDAO;
import it.polimi.tiw.project.DAO.FolderDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

@WebServlet("/home")
public class HomepageData extends HttpServlet {
	private Connection c = null;

	public HomepageData() {
		super();
	}

	public void init() throws ServletException {
		c = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession s = request.getSession();
		UserBean u = (UserBean) s.getAttribute("user");

		FolderDAO f = new FolderDAO(c);
		DocumentDAO d = new DocumentDAO(c);
		List<FolderBean> folders = new ArrayList<>();
		Map<Integer, List<FolderBean>> subfolders = new HashMap<>();
		Map<Integer, List<DocumentBean>> documents = new HashMap<>();

		try {
			folders = f.getFolders(u.getUserID());
			
			for (FolderBean root : folders) {
				subfolders.put(root.getID(), f.getSubfolders(root.getID(), u.getUserID()));
				
				for (FolderBean element : f.getSubfolders(root.getID(), u.getUserID())) {
					documents.put(element.getID(), d.getDocuments(element.getID(), u.getUserID()));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error has occured: failed entities fetch");
			return;
		}
		
		Object[] ans = new Object[3];
		ans[0] = folders;
		ans[1] = subfolders;
		ans[2] = documents;
		
		Gson gson = new Gson();
		String json = gson.toJson(ans);
		
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