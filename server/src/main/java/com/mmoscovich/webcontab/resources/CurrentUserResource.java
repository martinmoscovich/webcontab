package com.mmoscovich.webcontab.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.dto.UserDTO;
import com.mmoscovich.webcontab.dto.UserWithPasswordDTO;
import com.mmoscovich.webcontab.dto.mapper.UserMapper;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.services.SessionService;
import com.mmoscovich.webcontab.services.UserService;

@Component
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CurrentUserResource {

	@Inject
	private UserService service;
	
	@Inject
	private UserMapper mapper;
	
	@Inject
	private SessionService session;
	
	/**
	 * Permite al usuario actualizar sus datos.
	 * @return
	 * @throws InvalidRequestException
	 * @throws EntityNotFoundException
	 */
	@PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public UserDTO update(UserWithPasswordDTO dto) throws InvalidRequestException, EntityNotFoundException {
		dto.setId(session.getUserOrThrow().getId());
		return mapper.toDto(service.update(dto));
	}
	
}
