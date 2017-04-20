package com.main.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.main.service.UserService;


public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private UserService userService;
	
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${jwt.token.name}")
    private String tokenName;
    
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String authToken = "";
		Cookie cookies[] = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if(cookie.getName().equals(tokenName)) {
					authToken = cookie.getValue();
				}
			}
		}
        
        if (!authToken.isEmpty()) {
        	String username = jwtUtil.getUsernameFromToken(authToken);
	        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	        	JwtUser jwtUser = JwtUser.create(userService.findUserByName(username));
	            if (jwtUtil.validateToken(authToken, jwtUser)) {
	                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities());
	                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                logger.info("authenticated user " + username + ", setting security context");
	                SecurityContextHolder.getContext().setAuthentication(authentication);
	            }
	        }
        }
        chain.doFilter(request, response);
	}

}
