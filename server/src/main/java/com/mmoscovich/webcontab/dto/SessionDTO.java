package com.mmoscovich.webcontab.dto;

import java.util.Collection;

import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Organizacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de la sesion de usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
	/** Usuario actual */
	private UserDTO user;
	
	/** Organizacion en la que esta trabajando */
	private Organizacion organizacion;
	
	/** Ejercicio en el que esta trabajando */
	private Ejercicio ejercicio;
	
	/** Roles del usuario en la organizacion */
	private Collection<String> roles;
	
	public boolean isAuthenticated() {
		return user != null;
	}
}
