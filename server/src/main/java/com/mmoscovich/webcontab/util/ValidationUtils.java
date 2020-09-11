package com.mmoscovich.webcontab.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * Clase con metodos de validacion
 */
public class ValidationUtils {

	private static final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

	/**
	 * Valida un POJO en los grupos especificados usando Bean Validation.
	 * @param <T>
	 * @param object POJO
	 * @param groups grupos de validacion
	 * @return lista de violations encontradas
	 */
	public static <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		return validator.validate(object, groups);
	}
}
