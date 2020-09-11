package com.mmoscovich.webcontab.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuracion de Spring Security
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private SpringSecuritySuccessHandler successHandler;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		SpringSecurityExceptionHandler exHandler = new SpringSecurityExceptionHandler();
		
		http.csrf().disable()
		// Modifica las respuestas a JSON
		.formLogin().successHandler(successHandler).failureHandler(exHandler).and()
		
		// Cambia la URL y la respuesta de Logout. Maneja la sesion.
		.logout().logoutUrl("/api/logout").deleteCookies("JSESSIONID").logoutSuccessHandler(successHandler).invalidateHttpSession(true).and()
		
		// Modifica las respuestas de error a JSON
		.exceptionHandling().authenticationEntryPoint(exHandler).accessDeniedHandler(exHandler).and()
		
		// Crea sesion si no existe
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
		
		// Permite usar la consola H2
		.headers().frameOptions().disable().and() // Para H2

		.authorizeRequests()
			// Recursos estaticos y h2 (publicos)
			.antMatchers("/", "/h2", "/h2/*", "/favicon.ico", "/js/**", "/css/**").permitAll()
			
			// Rutas del front (las filtra el front directamente)
			.antMatchers(
				"/asientos/*", "/informes/diario", "/informes/mayor", "/informes/balance", "/categorias", "/categorias/*", "/cuentas/*",
				"/ejercicios", "/admin/*", "/perfil"
			).permitAll()
			
			// Login y primer uso (publicos)
			.antMatchers("/login", "/api/first").permitAll()
			
			// Chequear si esta logueado al iniciar (publico)
			.antMatchers(HttpMethod.GET, "/api/session").permitAll()
			
			// Cualquier otra interaccion con la sesion o el perfil de usuario ya tiene que estar logueado
			.antMatchers("/api/session/**", "/api/user").authenticated()
			
			// Cualquier llamada a admin
			.antMatchers("/api/admin/**").hasRole("ADMIN")
			 //"/api/organizaciones/*/miembros"
			
			// Cualquier otra llamada GET tiene que estar logueado
			.antMatchers(HttpMethod.GET).authenticated()
			
			// Cualquier llamada POST, PUT o DELETE (tiene que ser usuario o admin, NO solo lectura)
			.anyRequest().hasAnyRole("ADMIN", "ORG:ADMIN", "ORG:USER");
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder(12);
	}
	
	/**
	 * Mapper de roles de Spring Security para que se agregue el prefijo "ROLE_" a los roles.
	 */
	@Bean
	public GrantedAuthoritiesMapper roleMapper() {
		return new SimpleAuthorityMapper();
	}


	@Bean
	@Override
	public UserDetailsService userDetailsServiceBean() throws Exception {
		return new WebContabUserDetailsService();
	}
	
	/**
	 * Si no se definio un provider custom pero si hay un datasource de usuarios (custom o el hardcoded),
	 * se define un provider que simplemente se basa en ese datasource.
	 */
	@Bean
	public AuthenticationProvider daoAuthProvider(UserDetailsService userService, GrantedAuthoritiesMapper roleMapper) {
		DaoAuthenticationProvider ap = new DaoAuthenticationProvider();
		ap.setUserDetailsService(userService);
		ap.setAuthoritiesMapper(roleMapper);
		ap.setPasswordEncoder(passwordEncoder());
				
		return ap;
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, AuthenticationProvider provider) throws Exception {
		auth.authenticationProvider(provider);
	}
}