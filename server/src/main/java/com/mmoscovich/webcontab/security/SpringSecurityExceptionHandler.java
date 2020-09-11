package com.mmoscovich.webcontab.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.mmoscovich.webcontab.exception.AuthenticationException;
import com.mmoscovich.webcontab.exception.AuthorizationException;
import com.mmoscovich.webcontab.exception.WebContabException;
import com.mmoscovich.webcontab.exception.mapper.AccessDeniedExceptionMapper;

/**
 * Clase que maneja errores de auth en Spring Security.
 * <p>Estos se ejecutan en filtros anteriores a JAX-RS, por lo que no se pueden usar los {@link ExceptionMapper}.
 * <br>Por lo tanto, esta clase se asegura que se retorne un error JSON con el formato correcto.</p>
 * 
 * @author Martin Moscovich
 *
 */
public class SpringSecurityExceptionHandler implements AccessDeniedHandler, AuthenticationFailureHandler, AuthenticationEntryPoint {
	
	private static final String ERROR_TEMPLATE = "{\"code\": \"%s\", \"description\": \"%s\"}";
	private static final AuthorizationException forbidden = new AuthorizationException("El usuario no tiene permiso para acceder al recurso");
	private static final AuthenticationException credentiasInvalid = new AuthenticationException("Credenciales invalidas");
	private static final AuthenticationException credentialsRequired = new AuthenticationException("Credenciales requeridas");
	
	/**
	 * Maneja la falta de permisos.
	 * <p>La excepcion de Acceso Denegado por annotation en los metodos o que se tira manualmente se captura en
	 * {@link AccessDeniedExceptionMapper}.</p>
	 * 
	 * @author Martin Moscovich
	 *
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		this.writeError(response, forbidden);
	}
	
	/**
	 * Maneja el error de credenciales invalidas
	 */
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException exception) throws IOException, ServletException {
		
		this.writeError(response, credentiasInvalid);
	}
	
	/**
	 * Maneja el error de credenciales requeridas
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException authException)
			throws IOException, ServletException {
		
		this.writeError(response, credentialsRequired);
	}	
	
	private void writeError(HttpServletResponse response, WebContabException ex) throws IOException {
		if (!response.isCommitted()) {
			response.setStatus(ex.getStatusCode());
	    	response.setContentType(MediaType.APPLICATION_JSON);
			response.getWriter().print(String.format(ERROR_TEMPLATE, ex.getErrorCode(), ex.getMessage()));
		}
	}

}
