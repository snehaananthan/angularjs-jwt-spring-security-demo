package com.main.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.main.JwtAuthEntryPoint;
import com.main.jwt.JwtFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}

	@Autowired
	private DataSource dataSource;
	
	@Value("${spring.queries.users-query}")
	private String usersQuery;
	
	@Value("${spring.queries.authorities-query}")
	private String authoritiesQuery;
	
	@Autowired
	private JwtAuthEntryPoint jwtAuthEntryPoint;
	
	@Bean
    public JwtFilter jwtFilterBean() throws Exception {
        return new JwtFilter();
    }

	@Autowired
	protected void configureGlobal(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.
			jdbcAuthentication()
				.usersByUsernameQuery(usersQuery)
				.authoritiesByUsernameQuery(authoritiesQuery)
				.dataSource(dataSource)
				.passwordEncoder(bCryptPasswordEncoder);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
			// don't create session
			.exceptionHandling().authenticationEntryPoint(jwtAuthEntryPoint)
			.and()
	        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        .and()
			.authorizeRequests()
				.antMatchers("/", 
						"/login", 
						"/authenticate/**").permitAll()
				.antMatchers("/resources/**", 
						"/static/**", 
						"/css/**", 
						"/js/**", 
						"/bower_components/**", 
						"/html/**", 
						"/images/**").permitAll()
//				.antMatchers("/admin/**").hasAuthority("ADMIN")
				.anyRequest()
				.authenticated()
				.and()
//				.csrf().disable()
				.csrf()
		        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		        .and()
				.formLogin()
				.loginPage("/login")
				.permitAll()
//				.loginProcessingUrl("/authenticate")
//				.failureUrl("/login?error=true")
//				.defaultSuccessUrl("/admin/home")
//				.usernameParameter("username")
//				.passwordParameter("password")
				.and()
				.logout()
			    .logoutUrl("/logout")
			    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
			    .deleteCookies("XSRF_TOKEN", "AuthToken");
//				.and().exceptionHandling()
//				.accessDeniedPage("/access-denied")
				;
		
		// Custom JWT based security filter
		http.addFilterBefore(jwtFilterBean(), UsernamePasswordAuthenticationFilter.class);
        
		// disable page caching
        http.headers().cacheControl();
	}
	
//	@Override
//	public void configure(WebSecurity web) throws Exception {
//	    web
//	       .ignoring()
//	       .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/bower_components/**", "/html/**", "/images/**");
//	}
}