package com.mmoscovich.webcontab.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.security.access.AccessDeniedException;

import com.mmoscovich.webcontab.dto.ErrorDTO;
import com.mmoscovich.webcontab.exception.AuthorizationException;

/**
 * Exception Mapper para errores de seguridad de Spring Security
 *
 */
@Provider
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {
	private static final AuthorizationException ae = new AuthorizationException("El usuario no tiene permiso para acceder al recurso");

	@Override
	public Response toResponse(AccessDeniedException exception) {
		ErrorDTO error = new ErrorDTO(ae.getErrorCode(), ae.getMessage());
		return Response.status(ae.getStatusCode()).entity(error).build();
	}

}
