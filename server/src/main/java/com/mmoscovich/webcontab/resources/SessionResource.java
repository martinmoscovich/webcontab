package com.mmoscovich.webcontab.resources;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.dao.UserRepository;
import com.mmoscovich.webcontab.dto.SessionDTO;
import com.mmoscovich.webcontab.dto.mapper.UserMapper;
import com.mmoscovich.webcontab.exception.AuthenticationException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.NoUsersDefinedException;
import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.services.CategoriaService;
import com.mmoscovich.webcontab.services.SessionService;

/**
 * Resource que maneja la sesion de usuario
 */
@Component
@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

	@Inject
	private SessionService session;
	
	@Inject
	private CategoriaService catService;
	
	@Inject
	private UserRepository userDao;
	
	@Inject
	private UserMapper mapper;
	
	/**
	 * Obtiene los datos de la sesion actual
	 */
	@GET
	public SessionDTO getSession() {
		User user = session.getUser().orElse(null);
		
		if(user == null) {
			// Si no hay usuario actual y no existe NINGUN usuario, se lanza una excepcion especial
			// para que el cliente permite crear el primer usuario (admin)
			if(!this.userDao.hasAny()) throw new NoUsersDefinedException();

			// Si no hay usuario pero existen otros, simplemente se tira error de seguridad
			throw new AuthenticationException("Credenciales requeridas");
		}
		
		return new SessionDTO(mapper.toDto(user), session.getOrganizacion().orElse(null), session.getEjercicio().orElse(null), session.getCurrentRoles());
	}

	/**
	 * Asigna una organizacion a la sesion actual
	 * @param id id de la org
	 * @return la sesion actualizada
	 * @throws AuthenticationException si no hay usuario logueado
	 * @throws EntityNotFoundException si no existe o no se tiene acceso a la organizacion
	 */
	@PUT
	@Path("organizacion/{id}")
	public SessionDTO seleccionarOrganizacion(@PathParam("id") @NotNull Long id) throws AuthenticationException, EntityNotFoundException {
		// Se quita de la sesion cualquier ejercicio que este seleccionado
		this.session.setEjercicio(null);

		// Se asigna la organizacion
		this.session.setOrganizacion(id);
		
		// Cuando se selecciona una org, se cachean sus categorias
		catService.list(this.session.getOrganizacionOrThrow());
		
		return this.getSession();
	}
	
	/**
	 * Remueve la organizacion de la sesion.
	 * @return la sesion actualizada
	 * @throws AuthenticationException si no hay usuario logueado
	 * @throws EntityNotFoundException
	 */
	@DELETE
	@Path("organizacion")
	public SessionDTO salirDeOrganizacion() throws AuthenticationException, EntityNotFoundException {
		// Se quita de la sesion cualquier ejercicio que este seleccionado
		this.session.setEjercicio(null);
		
		// Se quita de la sesion cualquier organizacion que este seleccionada
		this.session.setOrganizacion(null);
		
		return this.getSession();
	}
	
	/**
	 * Asigna un ejercicio a la sesion
	 * @param id id del ejercicio
	 * @return la sesion actualizada
	 * @throws AuthenticationException si no hay usuario logueado
	 * @throws EntityNotFoundException si no existe el ejercicio o no pertecene a la organizacion actual
	 */
	@PUT
	@Path("ejercicio/{id}")
	public SessionDTO seleccionarEjercicio(@PathParam("id") @NotNull Long id) throws AuthenticationException, EntityNotFoundException {
		this.session.setEjercicio(id);
		return this.getSession();
	}
	
	/**
	 * Remueve el ejercicio de la sesion.
	 * @return
	 * @throws AuthenticationException si no hay usuario logueado
	 */
	@DELETE
	@Path("ejercicio")
	public SessionDTO salirDeEjercicio() throws AuthenticationException {
		this.session.setEjercicio(null);
		return this.getSession();
	}
}