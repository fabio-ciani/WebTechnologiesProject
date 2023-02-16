package it.polimi.tiw.project.bean;

import java.sql.Date;

public class DocumentBean {
	private int id;
	private String name, type, owner;
	private Date creation_date;
	private String summary;
	private int subfolderID;

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
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getSubfolderID() {
		return subfolderID;
	}

	public void setSubfolderID(int subfolderID) {
		this.subfolderID = subfolderID;
	}
}