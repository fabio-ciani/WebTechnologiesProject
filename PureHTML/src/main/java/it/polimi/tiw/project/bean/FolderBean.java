package it.polimi.tiw.project.bean;

import java.sql.Date;

public class FolderBean {
	private int id;
	private String name, owner;
	private Date creation_date;
	private int parent_folder, user;

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getCreationDate() {
		return creation_date;
	}

	public void setCreationDate(Date creation_date) {
		this.creation_date = creation_date;
	}

	public int getParentFolder() {
		return parent_folder;
	}

	public void setParentFolder(int parent_folder) {
		this.parent_folder = parent_folder;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}
}