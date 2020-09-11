package com.mmoscovich.webcontab.resources;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.dto.UserDTO;
import com.mmoscovich.webcontab.dto.UserWithPasswordDTO;
import com.mmoscovich.webcontab.dto.mapper.UserMapper;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.User.UserType;
import com.mmoscovich.webcontab.services.UserService;

@Component
@Path("/first")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FirstTimeResource {
	
	@Inject
	private UserService service;
	
	@Inject
	private UserMapper mapper;
	
	/**
	 * Permite crear el primer usuario (administrador)
	 * <p>Llamado solo la primera vez que se ejecuta la aplicacion y no existen usuarios.</p>
	 * <p>Este endpoint es de acceso publico, ya que no hay usuarios aun.</p>
	 * @return el nuevo usuario
	 */
	@POST
	public UserDTO createFirst(@Valid UserWithPasswordDTO dto) {
		// Si ya existe un usuario, no se permite utilizar este endpoint
		if(service.hasAny()) throw new InvalidRequestException("Ya existen usuarios en la base de datos");
		
		// El usuario sera ADMIN
		dto.setType(UserType.ADMIN);
		
		return mapper.toDto(service.create(dto));
	}
}
