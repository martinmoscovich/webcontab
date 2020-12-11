package com.mmoscovich.webcontab.resources;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

/**
 * Resource de usuarios accesible por usuarios normales.
 * <p>Permite obtener info de los usuarios por id y actualizar el usuario actual.</p>
 */
@Component
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	@Inject
	private UserService service;
	
	@Inject
	private SessionService session;
	
	@Inject
	private UserMapper mapper;
	
	/**
	 * Permite al usuario actualizar sus datos.
	 * @return
	 * @throws InvalidRequestException
	 * @throws EntityNotFoundException
	 */
	@PUT
	@Path("me")
    @Consumes(MediaType.APPLICATION_JSON)
    public UserDTO update(UserWithPasswordDTO dto) throws InvalidRequestException, EntityNotFoundException {
		dto.setId(session.getUserOrThrow().getId());
		return mapper.toDto(service.update(dto));
	}
	
	/**
	 * Obtiene la info de un usuario por id
	 */
	@GET
    @Path("{id}")
    public UserDTO getById(@PathParam("id") @NotNull @Min(1) Long id) throws EntityNotFoundException {
		return mapper.toDto(service.getByIdOrThrow(id));
	}
}
