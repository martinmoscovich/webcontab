package com.mmoscovich.webcontab.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Excepcion Mapper para errores de Jackson
 */
@Provider
public class JacksonMapppingExceptionMapper implements ExceptionMapper<JsonMappingException> {

	private UnhandledExceptionMapper mapper = new UnhandledExceptionMapper();
	
	@Override
	public Response toResponse(JsonMappingException exception) {
		return mapper.toResponse(exception);
	}
}
