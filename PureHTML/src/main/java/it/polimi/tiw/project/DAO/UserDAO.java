package it.polimi.tiw.project.DAO;

import it.polimi.tiw.project.bean.UserBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
	private Connection c;

	public UserDAO(Connection c) {
		this.c = c;
	}
	
	// Check if a username is already taken.
	public boolean checkUsernameAvailability(String username) throws SQLException {
		String query = "SELECT COUNT(*) AS occurences FROM User WHERE username = ?";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, username);

			try (ResultSet res = pStatement.executeQuery();) {
				res.next();

				return res.getInt("occurences") == 1;
			}
		}
	}

	public void registerUser(String name, String surname, String username, String pwd) throws SQLException {
		String query = "INSERT INTO User VALUES (NULL, ?, SHA2(?, 256), ?, ?)";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, username);
			pStatement.setString(2, pwd);
			pStatement.setString(3, name);
			pStatement.setString(4, surname);
			pStatement.executeUpdate();
		}
	}

	public UserBean checkCredentials(String username, String pwd) throws SQLException {
		String query = "SELECT userID, username, name, surname FROM User WHERE username = ? AND pwd_hash = SHA2(?, 256)";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, username);
			pStatement.setString(2, pwd);

			try (ResultSet res = pStatement.executeQuery();) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();

					UserBean u = new UserBean();
					u.setUserID(res.getInt("userID"));
					u.setName(res.getString("name"));
					u.setSurname(res.getString("surname"));
					u.setUsername(res.getString("username"));

					return u;
				}
			}
		}
	}
}