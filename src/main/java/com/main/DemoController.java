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
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.add("Set-Cookie", "authToken=" + token + "; secure");
//	        headers.add("Access-Control-Allow-Credentials", "true");
	        response.addCookie(cookie);
	        response.setStatus(HttpServletResponse.SC_OK);
	    }

//	    @RequestMapping(value = "/authenticate/refresh", method = RequestMethod.GET)
//	    public void refreshAndGetAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {
//	        String token = request.getHeader(tokenHeader);
//	        String username = jwtUtil.getUsernameFromToken(token);
//	        JwtUser jwtUser = JwtUser.create(userService.findUserByName(username));
//
//	        if (jwtUtil.canTokenBeRefreshed(token, jwtUser.getLastPasswordResetDate())) {
//	            String refreshedToken = jwtUtil.refreshToken(token);
//	            Cookie cookie = new Cookie(tokenHeader, refreshedToken);
//		        cookie.setSecure(true);
//		        response.addCookie(cookie);
//		        response.setStatus(HttpServletResponse.SC_OK);
//	        } else {
//	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//	        }
////	        return response;
//	    }	  
	  
	  @RequestMapping(value = "/resource", method = RequestMethod.GET)
	  public Map<String,Object> getProtectedResource() {
		  System.out.println("inside resource");
		  Map<String,Object> model = new HashMap<String,Object>();
		  model.put("id", UUID.randomUUID().toString());
		  model.put("content", "Hello World");
		  return model;
	  }

//	@RequestMapping("/user")
//	  public Principal user(Principal user) {
//	    return user;
//	  }
	
//	@RequestMapping(value={"/authenticate"}, 
//			method = RequestMethod.POST)
//	public ResponseEntity<Void> login(@RequestBody String creds){
//		String[] authParams = new String(Base64.decode(creds.getBytes())).split(":");
//		User user = new User();
//		user.setName(authParams[0]);
//		user.setPassword(authParams[1]);
//		
//		User userExists = userService.findUserByName(user.getName());
//		ResponseEntity.ok().build();
//		= userExists == null ? HttpStatus.UNAUTHORIZED : HttpStatus.OK;
//	}
	
	
//	@RequestMapping(value="/registration", method = RequestMethod.GET)
//	public ModelAndView registration(){
//		ModelAndView modelAndView = new ModelAndView();
//		User user = new User();
//		modelAndView.addObject("user", user);
//		modelAndView.setViewName("registration");
//		return modelAndView;
//	}
//	
//	@RequestMapping(value = "/registration", method = RequestMethod.POST)
//	public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
//		ModelAndView modelAndView = new ModelAndView();
//		User userExists = userService.findUserByEmail(user.getEmail());
//		if (userExists != null) {
//			bindingResult
//					.rejectValue("email", "error.user",
//							"There is already a user registered with the email provided");
//		}
//		if (bindingResult.hasErrors()) {
//			modelAndView.setViewName("registration");
//		} else {
//			userService.saveUser(user);
//			modelAndView.addObject("successMessage", "User has been registered successfully");
//			modelAndView.addObject("user", new User());
//			modelAndView.setViewName("registration");
//			
//		}
//		return modelAndView;
//	}
//	
//	@RequestMapping(value="/admin/home", method = RequestMethod.GET)
//	public ModelAndView home(){
//		ModelAndView modelAndView = new ModelAndView();
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		User user = userService.findUserByEmail(auth.getName());
//		modelAndView.addObject("userName", "Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
//		modelAndView.addObject("adminMessage","Content Available Only for Users with Admin Role");
//		modelAndView.setViewName("admin/home");
//		return modelAndView;
//	}
	
}
