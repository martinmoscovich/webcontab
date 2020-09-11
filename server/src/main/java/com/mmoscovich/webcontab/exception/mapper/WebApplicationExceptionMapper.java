package com.mmoscovich.webcontab.exception.mapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.mmoscovich.webcontab.dto.ErrorDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Excepcion Mapper que captura las excepciones de JAX-RS.
 * <br>El objetivo es que no se escape ninguna excepcion. 
 *
 */
@Slf4j
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

	@Override
	public Response toResponse(WebApplicationException exception) {
		log.error("Error al procesar request", exception);
		ErrorDTO error = new ErrorDTO("rest_error", exception.getMessage());
		return Response.status(exception.getResponse().getStatus()).entity(error).build();
	}

}
