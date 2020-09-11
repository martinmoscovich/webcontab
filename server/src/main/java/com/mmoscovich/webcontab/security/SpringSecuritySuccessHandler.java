package com.mmoscovich.webcontab.security;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmoscovich.webcontab.dto.SessionDTO;
import com.mmoscovich.webcontab.dto.mapper.UserMapper;
import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.services.SessionService;

/**
 * Clase que escribe las respuestas cuando el usuario se autentica y cuando hace logout.
 * <p>Al ser una app Rest, no se debe redirigir al usuario, si no que se envia una respuesta AJAX y  
 * el cliente se encargara de manejarla y mostrar la pantalla correcta.</p>
 */
@Service
public class SpringSecuritySuccessHandler implements AuthenticationSuccessHandler, LogoutSuccessHandler {

	@Inject
	private ObjectMapper json;
	
	@Inject
	private UserMapper mapper;
	
	@Inject
	private SessionService session;
	
	/**
	 * Escribe la respuesta cuando se autentico correctamente un usuario.
	 * <p>Envia los datos de la sesion (usuario y roles) en formato JSON.</p>
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		// Se obtienen los datos de la sesion
		User user = session.getUserOrThrow();
		Set<String> roles = session.getCurrentRoles();
		SessionDTO s = new SessionDTO(mapper.toDto(user), null, null, roles);
		
		// Se escribe en formato JSON
		response.setContentType(MediaType.APPLICATION_JSON);
		response.getWriter().print(json.writeValueAsString(s));
	}
	
	/**
	 * Escribe la respuesta cuando el usuario hace logout.
	 * <p>Simplemente devuelve OK</p>
	 */
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		response.setStatus(Status.NO_CONTENT.getStatusCode());		
	}

}
