package com.main;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.main.jwt.JwtAuthRequest;
import com.main.jwt.JwtUser;
import com.main.jwt.JwtUtil;
import com.main.service.UserService;


@RestController
public class DemoController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserService userService;
		
	@Autowired
	private JwtUtil jwtUtil;
	
    @Value("${jwt.token.name}")
    private String tokenName;
		  
	@RequestMapping(value = "/authenticate", 
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public void createAuthenticationToken(@RequestBody JwtAuthRequest authRequest, HttpServletResponse response) {
		// Perform the security
		final Authentication authentication = authenticationManager.authenticate(
		        new UsernamePasswordAuthenticationToken(
		                authRequest.getUsername(),
		                authRequest.getPassword()
		        )
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		// Reload password post-security so we can generate token
		JwtUser jwtUser = JwtUser.create(userService.findUserByName(authRequest.getUsername()));
		final String token = jwtUtil.generateToken(jwtUser);
		Cookie cookie = new Cookie(tokenName, token);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		response.setStatus(HttpServletResponse.SC_OK);
    }
  
	  
	@RequestMapping(value = "secure/resource", method = RequestMethod.GET)
	public Map<String,Object> getProtectedResource() {
		System.out.println("inside resource");
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("id", UUID.randomUUID().toString());
		model.put("content", "Hello World");
		return model;
	}
	
}
