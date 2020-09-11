package com.mmoscovich.webcontab.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.mmoscovich.webcontab.dto.ErrorDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Excepcion Mapper que captura cualquier excepcion que no fue manejada por otro Mapper
 */
@Slf4j
@Provider
public class UnhandledExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable exception) {
		log.error("Error al procesar request", exception);
		ErrorDTO error = new ErrorDTO();
		error.setCode("server_error");
		error.setDescription("Error interno del servidor. Consulte al administrador");
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
	}
	
}
