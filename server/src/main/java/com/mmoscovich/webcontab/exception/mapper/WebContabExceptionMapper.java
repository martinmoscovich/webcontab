package com.mmoscovich.webcontab.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.mmoscovich.webcontab.dto.ErrorDTO;
import com.mmoscovich.webcontab.exception.WebContabException;

import lombok.extern.slf4j.Slf4j;

/**
 * Excepcion Mapper que maneja todas las excepciones propias de la aplicacion
 *
 */
@Slf4j
@Provider
public class WebContabExceptionMapper implements ExceptionMapper<WebContabException> {

	@Override
	public Response toResponse(WebContabException exception) {
		ErrorDTO error = new ErrorDTO(exception.getErrorCode(), exception.getMessage());
		log.error(exception.getMessage());
		return Response.status(exception.getStatusCode()).entity(error).build();
	}

}
