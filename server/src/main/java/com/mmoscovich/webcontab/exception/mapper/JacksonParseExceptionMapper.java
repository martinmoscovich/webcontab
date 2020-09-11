package com.mmoscovich.webcontab.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mmoscovich.webcontab.dto.ErrorDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Excepcion Mapper para errores de parseo de Jackson
 */
@Slf4j
@Provider
public class JacksonParseExceptionMapper implements ExceptionMapper<JsonProcessingException> {

	@Override
	public Response toResponse(JsonProcessingException exception) {
		log.error("Error al procesar JSON", exception);
		ErrorDTO error = new ErrorDTO("invalid_request", "El payload no se pudo parsear");
		return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
	}
}
