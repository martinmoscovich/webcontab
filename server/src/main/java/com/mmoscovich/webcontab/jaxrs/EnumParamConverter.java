package com.mmoscovich.webcontab.jaxrs;

import javax.ws.rs.ext.ParamConverter;

import com.mmoscovich.webcontab.exception.InvalidRequestException;

import lombok.AllArgsConstructor;

/**
 * Converter JAX-RS para parametros de tipo Enum.
 * <p>Permite mostrar un error mas descriptivo en caso de que el valor recibido no pueda convertirse al Enum</p>
 *
 * @param <T>
 */
@AllArgsConstructor
public class EnumParamConverter<T extends Enum<T>> implements ParamConverter<Enum<T>> {
	private Class<T> cls;
	
	@Override
	public Enum<T> fromString(String value) {
		try {
			return Enum.valueOf(cls, value);
		} catch(IllegalArgumentException e) {
			throw new InvalidRequestException("El valor " + value + " no es valido para " + cls.getSimpleName() + ". Debe ser uno de " + cls.getEnumConstants());
//			throw new WebContabExceptionWrapper(new InvalidRequestException("El valor " + value + " no es valido para " + cls.getSimpleName() + ". Debe ser uno de " + cls.getEnumConstants()));
		}
	}

	@Override
	public String toString(Enum<T> value) {
		return value.name();
	}

}
