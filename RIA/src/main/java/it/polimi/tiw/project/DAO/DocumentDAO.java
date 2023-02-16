package it.polimi.tiw.project.DAO;

import it.polimi.tiw.project.bean.DocumentBean;
import it.polimi.tiw.project.bean.FolderBean;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

public class DocumentDAO {
	private Connection c;

	public DocumentDAO(Connection c) {
		this.c = c;
	}

	public List<DocumentBean> getDocuments(int subfolderID, int userID) throws SQLException {
		List<DocumentBean> documents = new ArrayList<>();

		// Note: explicitly querying F.parent_folder IS NOT NULL is unnecessary, because the web application itself prevents the user to create documents inside folders.
		String query = "SELECT D.id, D.name, D.type, D.owner, D.creation_date, D.summary FROM Folder F JOIN Document D ON F.id = D.subfolderID WHERE F.id = ? AND F.parent_folder IS NOT NULL AND F.user = ? ORDER BY D.name";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, subfolderID);
			pStatement.setInt(2, userID);

			try (ResultSet res = pStatement.executeQuery();) {
				while (res.next()) {
					DocumentBean d = new DocumentBean();

					d.setID(res.getInt("id"));
					d.setName(res.getString("name"));
					d.setType(res.getString("type"));
					d.setOwner(res.getString("owner"));
					d.setCreationDate(res.getDate("creation_date"));
					d.setSummary(res.getString("summary"));
					d.setSubfolderID(subfolderID);

					documents.add(d);
				}
			}
		}

		return documents;
	}
	
	// Check if insertion would cause a duplication.
	public boolean checkDocumentAvailability(String name, String type, int subfolderID, int userID) throws SQLException {
		// Note: explicitly querying F.parent_folder IS NOT NULL is unnecessary, because the web application itself prevents the user to create documents inside folders.
		String query = "SELECT COUNT(*) AS occurences FROM Folder F JOIN Document D ON F.id = D.subfolderID WHERE F.id = ? AND F.parent_folder IS NOT NULL AND F.user = ? AND D.name = ? AND D.type = ?";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, subfolderID);
			pStatement.setInt(2, userID);
			pStatement.setString(3, name);
			pStatement.setString(4, type);

			try (ResultSet res = pStatement.executeQuery();) {
				res.next();

				return res.getInt("occurences") == 1;
			}
		}
	}
	
	public void createDocument(String name, String type, String summary, FolderBean s) throws SQLException {
		String query = "INSERT INTO Document VALUES (NULL, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setString(1, name);
			pStatement.setString(2, type);
			pStatement.setString(3, s.getOwner());
			pStatement.setDate(4, new Date(System.currentTimeMillis()));
			pStatement.setString(5, summary);
			pStatement.setInt(6, s.getID());
			pStatement.executeUpdate();
		}
	}
	
	// Note: the method must return null or a single value extracted from the query.
	public DocumentBean getDocument(int documentID, int subfolderID, int userID) throws SQLException {
		// Note: explicitly querying F.parent_folder IS NOT NULL is unnecessary, because the web application itself prevents the user to create documents inside folders.
		String query = "SELECT D.name, D.type, D.owner, D.creation_date, D.summary FROM Folder F JOIN Document D ON F.id = D.subfolderID WHERE F.id = ? AND F.parent_folder IS NOT NULL AND F.user = ? AND D.id = ?";

		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, subfolderID);
			pStatement.setInt(2, userID);
			pStatement.setInt(3, documentID);
			
			try (ResultSet res = pStatement.executeQuery();) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();

					DocumentBean d = new DocumentBean();
					d.setID(documentID);
					d.setName(res.getString("name"));
					d.setType(res.getString("type"));
					d.setOwner(res.getString("owner"));
					d.setCreationDate(res.getDate("creation_date"));
					d.setSummary(res.getString("summary"));
					d.setSubfolderID(subfolderID);

					return d;
				}
			}
		}
	}
	
	public void moveDocument(int documentID, int destinationID) throws SQLException {
		String query = "UPDATE Document SET subfolderID = ? WHERE id = ?";
		
		c.setAutoCommit(false);
		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, destinationID);
			pStatement.setInt(2, documentID);
			
			// All previous checks have been handled by the web application.
			pStatement.executeUpdate();
		} catch (SQLException e) {
			c.rollback();
			throw e;
		} finally {
			c.setAutoCommit(true);
		}
	}
	
	public void deleteDocument(int documentID) throws SQLException {
		String query = "DELETE FROM Document WHERE id = ?";
		
		c.setAutoCommit(false);
		try (PreparedStatement pStatement = c.prepareStatement(query);) {
			pStatement.setInt(1, documentID);
			
			// All previous checks have been handled by the web application.
			pStatement.executeUpdate();
		} catch (SQLException e) {
			c.rollback();
			throw e;
		} finally {
			c.setAutoCommit(true);
		}
	}
}