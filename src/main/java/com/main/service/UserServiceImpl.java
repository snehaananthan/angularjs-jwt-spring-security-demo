package com.main.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.main.entity.Authority;
import com.main.entity.User;
import com.main.repository.AuthorityRepository;
import com.main.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepository;
	@Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void saveUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        Authority authority = authorityRepository.findByAuthority("ADMIN");
        user.setAuthorities(new ArrayList<Authority>(Arrays.asList(authority)));
		userRepository.save(user);
	}

	@Override
	public User findUserByName(String name) {
		return userRepository.findByName(name);
	}

}
