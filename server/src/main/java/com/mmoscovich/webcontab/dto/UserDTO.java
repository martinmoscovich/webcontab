package com.mmoscovich.webcontab.dto;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import com.mmoscovich.webcontab.model.User.UserType;

import lombok.Data;

/**
 * DTO del usuario
 */
@Data
public class UserDTO {
	private Long id;
	
	@NotEmpty
	@Length(max = 25)
	private String username;
	
	private UserType type;

	@NotEmpty
	@Length(max = 50)
	private String name;
	
	@NotEmpty
	@Length(max = 50)
	private String email;
	
	private String avatarUrl;
}
