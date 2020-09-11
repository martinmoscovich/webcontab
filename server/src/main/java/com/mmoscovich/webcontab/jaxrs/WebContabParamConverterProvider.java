package com.mmoscovich.webcontab.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 * Provider que permite usar los {@link ParamConverter} custom.
 */
@Provider
public class WebContabParamConverterProvider implements ParamConverterProvider {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if(LocalDate.class.isAssignableFrom(rawType)) return (ParamConverter<T>) new LocalDateParamConverter();
		if(Enum.class.isAssignableFrom(rawType)) return new EnumParamConverter(rawType);
		return null;
	}

}
