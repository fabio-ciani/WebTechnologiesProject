package it.polimi.tiw.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

public class ConnectionHandler {
	public static Connection getConnection(ServletContext context) throws UnavailableException {
		Connection c = null;

		try {
			String driver = context.getInitParameter("DB_DRIVER");
			String url = context.getInitParameter("DB_URL");
			String user = context.getInitParameter("DB_USER");
			String password = context.getInitParameter("DB_PWD");

			Class.forName(driver);
			c = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("An error has occured: the database driver couldn't be loaded.");
		} catch (SQLException e) {
			throw new UnavailableException("An error has occured: the database connection couldn't be gotten.");
		}
		
		return c;
	}

	public static void closeConnection(Connection c) throws SQLException {
		if (c != null) {
			c.close();
		}
	}
}