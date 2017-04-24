# Angularjs JDBC Authentication JWT Spring Security demo

## Overview
This is a demo of a Spring boot application with JDBC authentication of Spring Security along with JSON Web Token (JWT) authorization (with CSRF) with AngularJS front-end. This is just a demo application with two simple UI pages.
One, a login page. Two, a Greeting page. And a logout page. The Greeting page appears once the user is authenticated. The subsequent interactions are based on JWT authorization (using [JWT library for Java](https://github.com/jwtk/jjwt)).

## Pre-requisites
* Java 1.8
* MySQL server (v5.7)
* AngularJS (v1.6.4)
* Bower - for installing JS libraries/packages 
* Npm - for installing grunt plugins
* Apache Maven (v3.3.9) - to manage project dependencies, build & run.
* Grunt

## Features
* Spring Security - JDBC Authentication
* JWT authorization (along with CSRF protection)
* HTTPS enabled, HTTP to HTTPS automatic redirection (requires [creating a keystore](https://drissamri.be/blog/java/enable-https-in-spring-boot/))
* RESTful API endpoint based interaction
* AngularJS v1.6.4
* Used Grunt tasks to include JS/CSS files of libraries to index.html

## Build/Run
The angularjs template page can be built by running `grunt` in command prompt (after navigating to the project's directory). 
Then start the application with the Spring Boot maven plugin command - `mvn spring-boot:run`. The application is running at https://localhost:8443

## Features Explained
### Spring Security - JDBC Authentication
With MySQL server installed in the machine, the following configuration properties are included in our `application.properties`.
```
# ===============================
# = DATA SOURCE
# ===============================
spring.datasource.url=jdbc:mysql://localhost:3306/db_example
spring.datasource.username=springuser
spring.datasource.password=ThePassword
spring.datasource.testWhileIdle = true
spring.datasource.validationQuery = SELECT 1

# ===============================
# = JPA / HIBERNATE
# ===============================
spring.jpa.show-sql = true
spring.jpa.hibernate.ddl-auto = update
spring.jpa.hibernate.naming-strategy = org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQL5Dialect
```
Here, `db_example` is the schema that needs to be created in the MySQL server under the user `springuser` whose password is `ThePassword`. (This can be done using MySQL workbench)

The property value of `spring.jpa.hibernate.ddl-auto` has to be `create` the first time you run the application. After which, you can change it to `update`. 
(The lines are self-explanatory)

The JDBC authentication of Spring security is enabled by the following lines of code in one of our Configurtion classes,
```
protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.
			jdbcAuthentication()
				.usersByUsernameQuery(usersQuery)
				.authoritiesByUsernameQuery(authoritiesQuery)
				.dataSource(dataSource)
				.passwordEncoder(bCryptPasswordEncoder);
}
```
The parameters `usersQuery` & `authoritiesQuery` are defined in the class that bind the values from our `application.properties`.The `dataSource` properties are also defined in our `application.properties`.

```
spring.queries.users-query=select name, password, enabled from user where name=?
spring.queries.authorities-query=select u.name, a.authority from user u inner join user_authority ua on(u.user_id=ua.user_id) inner join authority a on(ua.authority_id=a.authority_id) where u.name=?
```
So, the application authenticates the user based on entries in three tables `user` `user_authority` & `authority`. Therefore, insert the records into the tables like,
```
INSERT INTO db_example.user VALUES(1,'sneha@test.com', true, 'ananthan', now(), 'sneha', '$2a$04$oRXJV.xZW8k2nXDIBvMXFOOpflEaXGVyxA.WxwoAQuZTIBI871RyG');

INSERT INTO db_example.authority VALUES (1,'ADMIN');

INSERT INTO db_example.user_authority VALUES (1, 1);
```
We use [bcrypt](https://en.wikipedia.org/wiki/Bcrypt) to encrypt the passwords. Here is an online [Bcrypt Generator](https://bcrypt-generator.com/). 
For encrypting passwords in our application, we use `BCryptPasswordEncoder` class provided by Spring Security itself. (Easy right!?)

### JWT authorization with CSRF protection
Once the user is authenticated, the server returns a JWT token to the client. This JWT is used for authorising all the requests made by the logged-in user henceforth.

When it comes to securing web-apps against various attacks (Cross-site-request-forgery (CSRF), Cross-site-scripting (XSS), Eavesdropping, etc.. to list a few), there's a lot of discussion on different forums about choosing between the different standards. 

Our app is secure against...
* **Eavesdropping** - This application's traffic uses SSL tunneling.
* **CSRF attack** 
  * The username & password string is encrypted using **JWT standard**. Hence, extracting the username, password from the JWT token is impossible without the **secret key** *(which only the server is aware of)*.
  * Every request made to the server is authorised using **two tokens** : 
    * **JWT token** sent by the browser in the cookie
    * **CSRF token** in the request header 
    >CSRF token is appended by the JS application & not the browser. Thus, in a CSRF attack, the CSRF token is not sent to the malicious site!)
* **XSS attack** - Storing the JWT token in the local storage exposes the same in an cross-site-scripting attack. Hence, the JWT token is stored in a `secure`,  `httpOnly`cookie.
    >`secure` cookie can be sent only through SSL tunnels (HTTPS). `httpOnly` cookie cannot be manipulated through scripts and can only be accessed by the browser.
* **Session Hijacking** - We do not create any session for a logged-in user. Instead, we use token based authorization.

So, the JWT authorization is handled by the user-defined `JWTFilter` class which extends `OncePerRequestFilter` class provided by Spring framework. The `doFilterInternal` method is overridden where the JWT token is decrypted using the secret key, the username extracted and verified against the database. 

(The password is not verified in this step as it is done by the `UsernamePasswordAuthenticationFilter` in the filter chain). 

Next, the token is also validated against expiration time, etc. On passing these validations, the user details are passed on to the next filter in the chain - `UsernamePasswordAuthenticationFilter`.

In order to add our custom JWT filter `JWTFilter` in the Spring Security's filter chain, the following lines of code added in our `SecurityConfiguration` class.
```
http.addFilterBefore(jwtFilterBean(), UsernamePasswordAuthenticationFilter.class);
```
We also include a couple of other configuration properties in this class.
```
http
	.exceptionHandling().authenticationEntryPoint(jwtAuthEntryPoint)
	.and()
	// don't create session
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
		.anyRequest()
		.authenticated()
		.and()
		.csrf()
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .and()
		.formLogin()
		.loginPage("/login")
		.permitAll()
		.and()
		.logout()
	    .logoutUrl("/logout")
	    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
	    .deleteCookies("XSRF_TOKEN", "AuthToken");
```

* A custom `JWTAuthEntryPoint` class defines what is returned to the client when a request is rejected by the server. 
* `SessionCreationPolicy.STATELESS` will not create a session (thereby, making it a secure, stateless application).
* `anyRequest().authenticated()` ensures that any request to our application requires the user to be authenticated.
* Any authenticated user's requests are permitted if the URL in the request matches any of the `antMatchers` URL patterns.
* After successful authentication, the server returns a CSRF token in a non-HttpOnly cookie (so that they can be accessed by the JS application).
* `formLogin().loginPage("/login").permitAll()` specifies the location of the login page & grants all users (i.e. unauthenticated users) access to our log in page.
* `logout().logoutUrl("/logout")` provides logout support with the URL that triggers log out to occur.
* Upon successful logout, the `HttpStatusReturningLogoutSuccessHandler` allows you to provide a plain HTTP status code to be returned. If not configured a status code 200 will be returned by default.
* Finally, `deleteCookies("XSRF_TOKEN", "AuthToken")` allows specifying the names of cookies to be removed on logout success.

## GruntJS script to include JS/CSS files of libraries in index.html
In a **Single Page Application** which uses AngularJS, it's ideal to include the scripts & stylesheets in index.html using task runners like Grunt/Gulp. In this application, we have written Grunt tasks that would first  [lint](http://www.javascriptlint.com/) the JS pages and then add them to our index.html. 
>Linting of scripts is important to ensure cross-browser compatibility of an application.

In order for the `grunt-include-source` task to work correctly, please add a `"sources"` property with value of the relative path of the JS/CSS files of the module to be included. It is included inside the `bower.json` file of each of the bower modules.

*demo/src/main/resources/static/bower_components/angular/bower.json*
```
  {
  "name": "angular",
  "version": "1.6.4",
  "license": "MIT",
  "main": "./angular.js",
  "ignore": [],
  "sources": {
    "js": "./*.js"
  },
  "dependencies": {
  }
}
```
By default, the `sources` property is not present in a bower.json file.

Since our application's security is strictly controlled by the URL patterns added in the Spring security configuration properties, it's a good practice to use `$locationProvider.html5Mode(true)` in our angular app's config block in `config.js`. And a `<base href="/">` element in our index.html. Here's a nice [article](https://scotch.io/tutorials/pretty-urls-in-angularjs-removing-the-hashtag) on why we include these.

That's all about this application. Please share your feedback, if any. Any questions, please post as comments. Cheers! :)

### References
* https://github.com/szerhusenBC/jwt-spring-security-demo
* https://www.jamesward.com/2013/05/13/securing-single-page-apps-and-rest-services
* https://drissamri.be/blog/java/enable-https-in-spring-boot/
* https://medium.com/@gustavo.ponce.ch/spring-boot-spring-mvc-spring-security-mysql-a5d8545d837d
* https://www.toptal.com/java/rest-security-with-jwt-spring-security-and-java
* https://spring.io/guides/tutorials/spring-security-and-angular-js/#_the_api_gateway_pattern_angular_js_and_spring_security_part_iv


