package com.mmoscovich.webcontab.exception.mapper;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.mmoscovich.webcontab.dto.ValidationErrorDTO;
import com.mmoscovich.webcontab.dto.ValidationErrorDTO.FieldError;

/**
 * Exception Mapper para errores de validacion de Bean Validation
 *
 */
@Provider
public class BeanValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(ConstraintViolationException exception) {
		ValidationErrorDTO error = new ValidationErrorDTO();
		error.setCode("invalid_request");
		error.setDescription("Error de validacion");
		error.setFields(
				exception.getConstraintViolations().stream().map(this::map).collect(Collectors.toList())
		);
		return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
	}
	
	private FieldError map(ConstraintViolation<?> violation) {
		FieldError error = new FieldError();
		String path = violation.getPropertyPath().toString();
		error.setCode(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
		error.setField(path.substring(path.indexOf(".") + 1));
		error.setDescription(violation.getMessage());
		return error;
	}

}
