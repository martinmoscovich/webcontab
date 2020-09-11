package com.mmoscovich.webcontab.dto;

import javax.validation.constraints.NotNull;

import com.mmoscovich.webcontab.model.Member.Rol;

import lombok.Data;

/**
 * DTO de una membresia de un usuario a una organizacion
 */
@Data
public class MemberDTO {
	private UserDTO user;
	private Long organizacionId;
	
	@NotNull
	private Rol rol;
	
	/** 
	 * Indica si se puede modificar esta membresia.
	 * (quitar o cambiar el rol).
	 * <p>Por ej: los admin del sistema se agregan como miembros de todas las organizaciones y no se puede modificar.</p>
	 */
	private boolean readonly;
}
