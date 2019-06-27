package com.noxob.spygame.models;

import java.util.ArrayList;
import java.util.List;

public class Location {
	
	private int id;
	private String name;
	private List<String> roles;
	
	public Location(int id, String name, List<String> roles) {
		this.id = id;
		this.name = name;
		this.roles = roles;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	
	
}
