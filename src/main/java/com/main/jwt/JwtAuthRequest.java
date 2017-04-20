package com.main.jwt;

import java.io.Serializable;

public class JwtAuthRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7973995349530210291L;
	
	private String username;
	private String password;
	
	
	public JwtAuthRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public JwtAuthRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	

}
