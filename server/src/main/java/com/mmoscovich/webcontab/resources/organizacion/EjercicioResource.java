package com.mmoscovich.webcontab.resources.organizacion;

import java.time.LocalDate;

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
import com.mmoscovich.webcontab.exception.EjercicioFechaInvalidaException;
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
	 * Crea el asiento de ajuste por inflacion del ejercicio
	 * @return el ejercicio actualizado con el id del asiento de ajuste
	 * @throws EntityNotFoundException si no se encuentra el ejercicio en la organizacion actual
	 * @throws OrganizacionNoSeleccionadaException si no se selecciono organizacion
	 */
	@PUT
	@Path("{id}/inflacion")
	public Ejercicio ajustarPorInflacion(@PathParam("id") @Min(1) Long id) throws EntityNotFoundException, OrganizacionNoSeleccionadaException {
		return service.ajustarPorInflacion(this.getById(id));
	}
	
	/**
	 * Recalcula y modifica el asiento de apertura del ejercicio.
	 * <p>
	 * Este asiento se pudo haber generado en base a un ejercicio anterior abierto, que se siguio modificando.
	 * <br>Por lo tanto se debe poner recalcular con los saldos actuales de dicho ejercicio. 
	 * </p>
	 * @return el ejercicio actualizado 
	 * @throws EntityNotFoundException si no se encuentra el ejercicio en la organizacion actual
	 * @throws OrganizacionNoSeleccionadaException si no se selecciono organizacion
	 */
	@PUT
	@Path("{id}/apertura")
	public Ejercicio recalcularApertura(@PathParam("id") @Min(1) Long id) throws EntityNotFoundException, OrganizacionNoSeleccionadaException {
		return service.recalcularApertura(this.getById(id));
	}
	
	/**
	 * Renumera los asientos de un ejercicio y establece o actualiza la fecha de confirmacion de asientos del ejercicio.
	 * <p>Se renumeran <b>TODOS</b> los asientos del ejercicio.
	 * <br>La fecha se utiliza para establecer hasta que fecha estan <b>confirmados</b> los asientos.
	 * No se podran crear, modificar ni borrar asientos anteriores a esa fecha.
	 * <br>El ejercicio queda <b>read only</b> hasta el dia anterior a esa fecha.
	 * </p>
	 * <p>
	 * Se renumeran todos los asientos para evitar que algun asiento posterior a esta fecha genere conflicto con los confirmados.
	 * (quizas queda un asiento posterior con el mismo numero).
	 * <br>Por eso, para evitarlo, renumeran todos, aunque los posteriores pueden ser modificados.
	 * </p>
	 * 
	 * @param id id del ejercicio
	 * @param fechaConfirmacion fecha hasta la cual estan confirmados los asientos (se excluye dicha fecha)
	 * @return el ejercicio actualizado con la nueva fecha de confirmacion
	 * 
	 * @throws EntityNotFoundException si no se encuentra el ejercicio
	 * @throws OrganizacionNoSeleccionadaException si no se selecciono organizacion
	 * @throws EjercicioFechaInvalidaException si la fecha de confirmacion no esta dentro del ejercicio
	 */
	@PUT
	@Path("{id}/confirmacion")
	public Ejercicio confirmarAsientos(@PathParam("id") @Min(1) Long id, @QueryParam("fecha") LocalDate fechaConfirmacion) throws EntityNotFoundException, OrganizacionNoSeleccionadaException, EjercicioFechaInvalidaException {
		return service.confirmarAsientos(this.getById(id), fechaConfirmacion);
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
