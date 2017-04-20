package com.main.service;

import com.main.entity.User;

public interface UserService {
	public User findUserByEmail(String email);
	public User findUserByName(String name);
	public void saveUser(User user);
}