package com.mmoscovich.webcontab.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO de un error de validacion de uno o mas campos.
 * @author Martin
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorDTO extends ErrorDTO {

	private List<FieldError> fields;
	
	@Data
	public static class FieldError {
		private String field;
		private String code;
		private String description;
	}
}
