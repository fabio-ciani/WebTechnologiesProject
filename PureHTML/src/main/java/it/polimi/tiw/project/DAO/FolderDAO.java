package it.polimi.tiw.project.DAO;

import it.polimi.tiw.project.bean.FolderBean;
import it.polimi.tiw.project.bean.UserBean;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

public class FolderDAO {
	private Connection c;

	public FolderDAO(Connection c) {
		this.c = c;
	}

	public List<FolderBean> getFolders(int userID) throws SQLException {
		List<FolderBean> folders = new ArrayList<>();
		String query = "SELECT id, name, owner, creation_date FROM Folder WHERE parent_folder IS NULL AND user = ? ORDER BY name";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, userID);

			try (ResultSet res = pStatement.executeQuery();) {
				while (res.next()) {
					FolderBean f = new FolderBean();

					f.setID(res.getInt("id"));
					f.setName(res.getString("name"));
					f.setOwner(res.getString("owner"));
					f.setCreationDate(res.getDate("creation_date"));
					f.setUser(userID);

					folders.add(f);
				}
			}
		}

		return folders;
	}

	// Check if insertion would cause a duplication.
	public boolean checkFolderAvailability(String name, int userID) throws SQLException {
		String query = "SELECT COUNT(*) AS occurences FROM Folder WHERE name = ? AND parent_folder IS NULL AND user = ?";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, name);
			pStatement.setInt(2, userID);

			try (ResultSet res = pStatement.executeQuery();) {
				res.next();

				return res.getInt("occurences") == 1;
			}
		}
	}

	public void createFolder(String name, UserBean u) throws SQLException {
		String query = "INSERT INTO Folder VALUES (NULL, ?, ?, ?, NULL, ?)";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, name);
			pStatement.setString(2, u.getName() + " " + u.getSurname());
			pStatement.setDate(3, new Date(System.currentTimeMillis()));
			pStatement.setInt(4, u.getUserID());
			pStatement.executeUpdate();
		}
	}

	public List<FolderBean> getSubfolders(int folderID, int userID) throws SQLException {
		List<FolderBean> subfolders = new ArrayList<>();
		String query = "SELECT id, name, owner, creation_date FROM Folder WHERE parent_folder = ? AND user = ? ORDER BY name";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, folderID);
			pStatement.setInt(2, userID);

			try (ResultSet res = pStatement.executeQuery();) {
				while (res.next()) {
					FolderBean f = new FolderBean();

					f.setID(res.getInt("id"));
					f.setName(res.getString("name"));
					f.setOwner(res.getString("owner"));
					f.setCreationDate(res.getDate("creation_date"));
					f.setParentFolder(folderID);
					f.setUser(userID);

					subfolders.add(f);
				}
			}
		}

		return subfolders;
	}

	// Check if insertion would cause a duplication.
	public boolean checkSubfolderAvailability(String name, int folderID, int userID) throws SQLException {
		String query = "SELECT COUNT(*) AS occurences FROM Folder WHERE name = ? AND parent_folder = ? AND user = ?";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, name);
			pStatement.setInt(2, folderID);
			pStatement.setInt(3, userID);

			try (ResultSet res = pStatement.executeQuery();) {
				res.next();

				return res.getInt("occurences") == 1;
			}
		}
	}

	public void createSubfolder(String name, int folderID, UserBean u) throws SQLException {
		String query = "INSERT INTO Folder VALUES (NULL, ?, ?, ?, ?, ?)";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, name);
			pStatement.setString(2, u.getName() + " " + u.getSurname());
			pStatement.setDate(3, new Date(System.currentTimeMillis()));
			pStatement.setInt(4, folderID);
			pStatement.setInt(5, u.getUserID());
			pStatement.executeUpdate();
		}
	}
	
	// Note: the method must return null or a single value extracted from the query.
	public FolderBean getSubfolder(int subfolderID, int userID) throws SQLException {
		String query = "SELECT name, owner, creation_date, parent_folder FROM Folder WHERE id = ? AND parent_folder IS NOT NULL AND user = ?";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, subfolderID);
			pStatement.setInt(2, userID);

			try (ResultSet res = pStatement.executeQuery();) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();

					FolderBean s = new FolderBean();
					s.setID(subfolderID);
					s.setName(res.getString("name"));
					s.setOwner(res.getString("owner"));
					s.setCreationDate(res.getDate("creation_date"));
					s.setParentFolder(res.getInt("parent_folder"));
					s.setUser(userID);

					return s;
				}
			}
		}
	}
	
	public int getSubfoldersCount(int userID) throws SQLException {
		String query = "SELECT COUNT(*) AS occurences FROM Folder WHERE parent_folder IS NOT NULL AND user = ?";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, userID);

			try (ResultSet res = pStatement.executeQuery();) {
				res.next();

				return res.getInt("occurences");
			}
		}
	}
}