package com.mmoscovich.webcontab.resources.organizacion;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.exception.OrganizacionNoSeleccionadaException;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.services.EjercicioService;
import com.mmoscovich.webcontab.services.SessionService;

/**
 * Resource de ejercicios
 */
@Component
@Path("/ejercicios")
@Produces(MediaType.APPLICATION_JSON)
public class EjercicioResource {

	@Inject
	private EjercicioService service;
	
	@Inject
	private SessionService session;
	
	/**
	 * Obtiene un ejercicio de la organizacion
	 * @throws EntityNotFoundException si no se encuentra el ejercicio en la organizacion actual
	 * @throws OrganizacionNoSeleccionadaException si no se selecciono organizacion
	 */
	@GET
	@Path("{id}")
	public Ejercicio getById(@PathParam("id") @Min(1) Long id) throws EntityNotFoundException, OrganizacionNoSeleccionadaException {
		return service.getByIdOrThrow(session.getOrganizacionOrThrow(), id);
	}
	
	/**
	 * Cierra un ejercicio.
	 * @return el ejercicio actualizado
	 * 
	 * @throws EntityNotFoundException si no se encuentra el ejercicio
	 * @throws OrganizacionNoSeleccionadaException si no se selecciono organizacion
	 * @throws InvalidRequestException si no hay cuentas que balanceen los resultados
	 * @throws ConflictException si el ejercicio ya esta cerrado
	 */
	@PUT
	@Path("{id}/cierre")
	public Ejercicio cerrar(@PathParam("id") @Min(1) Long id) throws EntityNotFoundException, OrganizacionNoSeleccionadaException, InvalidRequestException, ConflictException {
		return service.cerrarEjercicio(this.getById(id));
	}
	
	/**
	 * Reabre un ejercicio cerrado.
	 * @return el ejercicio actualizado
	 * 
	 * @throws EntityNotFoundException si no se encuentra el ejercicio
	 * @throws OrganizacionNoSeleccionadaException si no se selecciono organizacion
	 * @throws ConflictException si el ejercicio no esta cerrado
	 */
	@DELETE
	@Path("{id}/cierre")
	public Ejercicio reabrir(@PathParam("id") @Min(1) Long id) throws EntityNotFoundException, OrganizacionNoSeleccionadaException, ConflictException {
		return service.reabrirEjercicio(this.getById(id));
	}
	
	/**
	 * Elimina un ejercicio.
	 * 
	 * @param id id del ejercicio
	 * @param nombreOrg nombre de la organizacion a la que pertenece (usado para confirmacion)
	 * 
	 * @throws InvalidRequestException si no se ingreso correctamente el nombre de la organizacion.
	 */
	@DELETE
	@Path("{id}")
	@Transactional
	public void eliminar(@PathParam("id") @Min(1) Long id, @QueryParam("organizacion") String nombreOrg) {
		Ejercicio ej = this.getById(id);
		if(!ej.getOrganizacion().getNombre().equals(nombreOrg)) throw new InvalidRequestException("La confirmacion fallo");
		
		service.eliminar(session.getOrganizacionOrThrow(), ej);
	}
	
}
