package com.mmoscovich.webcontab.jaxrs;

import java.time.LocalDate;

import javax.ws.rs.ext.ParamConverter;

import com.mmoscovich.webcontab.exception.InvalidRequestException;

/**
 * Permite convertir parametros de tipo LocalDate en Resources JAX-RS
 *
 */
public class LocalDateParamConverter implements ParamConverter<LocalDate> {

	@Override
	public LocalDate fromString(String value) {
		try {
			if(value == null) return null;
			return LocalDate.parse(value);
			
		} catch(Exception e) {
			throw new InvalidRequestException("La fecha no es valida", e);
//			throw new WebContabExceptionWrapper(new InvalidRequestException("La fecha no es valida", e));
		}
	}

	@Override
	public String toString(LocalDate value) {
		return value == null ? null : value.toString();
	}

}
