package com.mmoscovich.webcontab.resources.admin;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.dto.UserDTO;
import com.mmoscovich.webcontab.dto.UserWithPasswordDTO;
import com.mmoscovich.webcontab.dto.mapper.UserMapper;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.services.UserService;

/**
 * Resource de ABM de usuarios para el administrador
 */
@Component
@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	@Inject
	private UserService service;
	
	@Inject
	private UserMapper mapper;
	
	/**
	 * Crea un nuevo usuario
	 */
	@POST
	public UserDTO create(@Valid UserWithPasswordDTO dto) {
		dto.setId(null);
		return mapper.toDto(service.create(dto));
	}
	
	/**
	 * Actualiza un usuario
	 */
	@PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public UserDTO update(@PathParam("id") @NotNull @Min(1) Long id, UserWithPasswordDTO dto) throws InvalidRequestException, EntityNotFoundException {
		dto.setId(id); 
		return mapper.toDto(service.update(dto));
	}
	
	/**
	 * Lista los usuarios, opcionalmente pasando un fragmento de su nombre
	 * @param query
	 * @return
	 */
	@GET
	public List<UserDTO> list(@QueryParam("query") String query) {
		return mapper.toDto(StringUtils.isEmpty(query) ? service.getAll() : service.search(query));
	}

}
