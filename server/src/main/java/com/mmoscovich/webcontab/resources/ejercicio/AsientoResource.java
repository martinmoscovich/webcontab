package com.mmoscovich.webcontab.resources.ejercicio;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.dto.AsientoFilter;
import com.mmoscovich.webcontab.dto.PageDTO;
import com.mmoscovich.webcontab.dto.PageReq;
import com.mmoscovich.webcontab.exception.EjercicioFechaInvalidaException;
import com.mmoscovich.webcontab.exception.EjercicioFinalizadoException;
import com.mmoscovich.webcontab.exception.EjercicioNoSeleccionadoException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.services.AsientoService;
import com.mmoscovich.webcontab.services.SessionService;

/**
 * Resource de Asientos
 */
@Component
@Path("/asientos")
@Produces(MediaType.APPLICATION_JSON)
public class AsientoResource {

	@Inject
	private AsientoService asientoService;

	@Inject
	private SessionService session;
	
	/**
	 * Devuelve una pagina de asientos del ejercicio actual, permite aplicar filtros.
	 * 
	 * @param includeImputaciones indica si se deben incluir las imputaciones de cada asiento.
	 * @param filtro filtro para limitar los asientos devueltos.
	 * @param page datos de paginacion
	 * @return
	 */
	@GET
	public PageDTO<Asiento> list(
			@QueryParam("imputaciones") boolean includeImputaciones,
			@BeanParam AsientoFilter filtro,
			@Valid @BeanParam PageReq page) {
		
		Ejercicio ej = session.getEjercicioOrThrow();
		
		Page<Asiento> asientos = filtro.esFiltroFechas() ? 
			// Buscar dentro de un periodo
			asientoService.findByPeriodo(ej, filtro.getDesde(), filtro.getHasta(), includeImputaciones, page.toPageable()) :
			// Buscar asientos dentro de un rango de numeros
			asientoService.findByNumeros(ej, filtro.getMin(), filtro.getMax(), includeImputaciones, page.toPageable());
		
		return PageDTO.adapt(asientos);
	}

	/** 
	 * Crea un nuevo asiento.
	 * @param asiento
	 * @return asiento persistido (incluyendo el numero)
	 * 
	 * @throws InvalidRequestException si hay algun error de validacion
	 * @throws EjercicioNoSeleccionadoException si no se selecciono un ejercicio
	 * @throws EjercicioFinalizadoException si el ejercicio esta finalizado (no se puede modificar)
	 * @throws EjercicioFechaInvalidaException EjercicioFechaInvalidaException si la fecha no esta dentro del ejercicio 
	 * o es anterior a la confirmada del ejercicio (no se pueden crear asientos).
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Asiento crear(@Valid Asiento asiento) throws InvalidRequestException, EjercicioNoSeleccionadoException, EjercicioFechaInvalidaException, EjercicioFinalizadoException {
		return asientoService.crear(asiento, session.getEjercicioOrThrow());
	}

	/**
	 * Obtiene un asiento por id
	 * @param id
	 * @param includeImputaciones indica si se deben traer las imputaciones
	 * @return
	 * @throws EntityNotFoundException si no existe asiento con ese id
	 * @throws EjercicioNoSeleccionadoException si no se selecciono un ejercicio
	 */
	@GET
	@Path("{id}")
	public Asiento getById(@PathParam("id") @NotNull @Min(1) Long id, @QueryParam("imputaciones") boolean includeImputaciones) throws EntityNotFoundException, EjercicioNoSeleccionadoException {
		return asientoService.getByIdOrThrow(session.getEjercicioOrThrow(), id, includeImputaciones);
	}

	/**
	 * Obtiene la ultima fecha de asiento cargada en el ejercicio
	 * @throws EjercicioNoSeleccionadoException si no se selecciono un ejercicio
	 */
	@GET
	@Path("last/date")
	public LocalDate getUltimaFechaDeAsiento() throws EjercicioNoSeleccionadoException {
		return asientoService.getUltimaFechaAsiento(session.getEjercicioOrThrow()).orElse(null);
	}
	
	/**
	 * Elimina un asiento 
	 * @param id
	 * @throws EntityNotFoundException si no existe asiento con ese id
	 * @throws EjercicioFinalizadoException si el ejercicio esta finalizado (no se puede modificar)
	 * @throws EjercicioNoSeleccionadoException si no se selecciono un ejercicio
	 * @throws EjercicioFechaInvalidaException si el asiento esta dentro de los confirmados (no se puede modificar)
	 */
	@DELETE
	@Path("{id}")
	public void eliminar(@PathParam("id") @NotNull @Min(1) Long id) throws EntityNotFoundException, EjercicioFinalizadoException, EjercicioFechaInvalidaException, EjercicioNoSeleccionadoException {
		asientoService.eliminar(session.getEjercicioOrThrow(), id);
	}

	/**
	 * Actualiza un asiento, incluyendo sus imputaciones
	 * @param id
	 * @param asiento
	 * @return asiento modificado
	 * @throws InvalidRequestException si hay algun error de validacion en el asiento
	 * @throws EntityNotFoundException si no existe asiento con ese id
	 * @throws EjercicioFinalizadoException si el ejercicio esta finalizado (no se puede modificar)
	 * @throws EjercicioNoSeleccionadoException si no se selecciono un ejercicio
	 * @throws EjercicioFechaInvalidaException si la fecha del asiento no esta dentro del ejercicio o 
	 * la fecha actual o la nueva son anteriores a la confirmada (no se puede modificar).
	 */
	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Asiento actualizar(@PathParam("id") @NotNull @Min(1) Long id, @Valid Asiento asiento)
			throws InvalidRequestException, EntityNotFoundException, EjercicioFinalizadoException,
			EjercicioFechaInvalidaException, EjercicioNoSeleccionadoException {
		asiento.setId(id);
		return asientoService.actualizar(session.getEjercicioOrThrow(), asiento);
	}
}