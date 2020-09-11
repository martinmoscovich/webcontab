package com.mmoscovich.webcontab.dto;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO del usuario que incluye el password.
 * <p>Solo se utiliza cuando el cliente crea un usuario o actualiza sus datos (incluyendo password).
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserWithPasswordDTO extends UserDTO {
	@NotEmpty
	@Length(min=4)
	private String password;
}
