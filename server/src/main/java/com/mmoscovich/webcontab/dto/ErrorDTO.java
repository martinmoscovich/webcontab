package com.mmoscovich.webcontab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un error
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
	/** Codigo de error */
	private String code;
	
	/** Descripcion del error */
	private String description;
}
