package it.polimi.tiw.project.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter({ "/home", "/manager", "/documents", "/FolderCreation", "/SubfolderCreation", "/DocumentCreation", "/detail", "/move", "/ApplyMove", "/logout" })
public class Logging implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession s = req.getSession();

		if (s.isNew() || s.getAttribute("user") == null) {
			res.sendRedirect(req.getServletContext().getContextPath() + "/");
			return;
		}

		chain.doFilter(request, response);
	}
}