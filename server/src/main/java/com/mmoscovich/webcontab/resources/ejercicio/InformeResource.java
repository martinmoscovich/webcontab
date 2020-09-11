package com.mmoscovich.webcontab.resources.ejercicio;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.dto.AsientoFilter;
import com.mmoscovich.webcontab.dto.BalanceFilter;
import com.mmoscovich.webcontab.dto.PageDTO;
import com.mmoscovich.webcontab.dto.PageReq;
import com.mmoscovich.webcontab.dto.informes.BalanceCuenta;
import com.mmoscovich.webcontab.dto.informes.ImputacionesCuentaDTO;
import com.mmoscovich.webcontab.dto.mapper.ImputacionMapper;
import com.mmoscovich.webcontab.exception.EjercicioNoSeleccionadoException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.services.CategoriaService;
import com.mmoscovich.webcontab.services.CuentaService;
import com.mmoscovich.webcontab.services.ImputacionService.ImputacionesCuenta;
import com.mmoscovich.webcontab.services.InformeService;
import com.mmoscovich.webcontab.services.SessionService;
import com.mmoscovich.webcontab.util.CollectionUtils;

/**
 * Resource que produce los informes (diario, mayor, balance).
 * <p>
 * Diario: Lista de asientos con sus imputaciones.<br>
 * Mayor: Imputaciones agrupadas por cuenta.<br>
 * Balance: Saldos de las cuentas.
 * </p>
 */
@Component
@Path("/informes")
@Produces(MediaType.APPLICATION_JSON)
public class InformeResource {

	@Inject
	private InformeService service;
	
	@Inject
	private SessionService session;
	
	@Inject
	private CategoriaService catService;
	
	@Inject
	private CuentaService cuentaService;
	
	@Inject
	private ImputacionMapper asientoMapper;
	
	/**
	 * Genera el reporte Diario en Excel y permite su descarga.
	 * <p>El diario para visualizar en la app se genera en {@link AsientoResource#list(boolean, AsientoFilter, PageReq)}.
	 * @param filtro filtro que limita los asientos incluidos
	 * @return el archivo excel en binario, con un nombre acorde
	 */
	@GET
	@Path("diario")
	public Response exportarDiario(@BeanParam AsientoFilter filtro) {
		Ejercicio ej = session.getEjercicioOrThrow();
		
		// Se genera el archivo y se obtiene el path
		java.nio.file.Path xls = filtro.esFiltroFechas() ? 
				service.crearDiarioPorPeriodo(ej, filtro.getDesde(), filtro.getHasta()) : 
				service.crearDiarioPorNumero(ej, filtro.getMin(), filtro.getMax());

		// Se calcula el nombre que debe tener el archivo (<org>-diario.xlsx)
		String fileName = ej.getOrganizacion().getNombre().replace(".",  "").replace("\\", "") + "-diario.xlsx";
				
		// Se genera el response de descarga
		return Response.ok(xls.toFile(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.build();
	}
	
	/**
	 * Obtiene una pagina del balance
	 * @param filter filtro para limitar cuentas y asientos incluidos en el balance
	 * @param includeCount indica si se debe obtener la cantidad total de items del balance
	 * @param pagination datos de paginacion
	 * @return
	 */
	@GET
    @Path("balance")
    public PageDTO<BalanceCuenta> getBalance(@BeanParam BalanceFilter filter, @QueryParam("count") boolean includeCount, @BeanParam @Valid PageReq pagination) {
    	return PageDTO.adapt(service.getBalance(session.getEjercicioOrThrow(), filter, includeCount, pagination.toPageable()));
    }
	
	/**
	 * Obtiene los saldos del balance para cada moneda
	 * @param filter filtro para limitar cuentas y asientos incluidos en el balance
	 * @return un mapa que tiene como clave el id de la moneda y como value el saldo en dicha moneda
	 */
	@GET
    @Path("balance/totales")
    public Map<Long, BigDecimal> getTotalesBalance(@BeanParam BalanceFilter filter) {
    	return service.getTotalesBalance(session.getEjercicioOrThrow(), filter);
    }
	
	/**
	 * Genera un reporte de balance en Excel y permite su descarga
	 * @param filter filtro para limitar cuentas y asientos incluidos en el balance
	 * @return el archivo excel en binario, con un nombre acorde
	 * @throws EntityNotFoundException
	 * @throws EjercicioNoSeleccionadoException
	 */
	@GET
    @Path("balance/xls")
	public Response exportarBalance(@BeanParam BalanceFilter filter) throws EntityNotFoundException, EjercicioNoSeleccionadoException {
    	
		final Ejercicio ej = session.getEjercicioOrThrow();
		
		// Se genera el archivo y se obtiene el path
		java.nio.file.Path xls = service.exportarBalance(ej, filter);
    	
		// Se calcula el nombre que debe tener el archivo (<org>-balance.xlsx)
		String fileName = ej.getOrganizacion().getNombre().replace(".",  "").replace("\\", "") + "-balance.xlsx";
		
		// Se genera el response de descarga
		return Response.ok(xls.toFile(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.build();
    }
	
	/**
	 * Obtiene una pagina del Mayor de una cuenta.
	 * <p>Permite filtrar los asientos incluidos.</p>
	 * @param cuentaId id de la cuenta
	 * @param filtro filtro que limita los asientos incluidos
	 * @param page datos de paginacion
	 * @return una pagina de imputaciones de la cuenta y el saldo anterior a las mismas
	 * @throws EntityNotFoundException si no existe la cuenta
	 * @throws EjercicioNoSeleccionadoException si no se selecciono un ejercicio
	 */
	@GET
    @Path("mayor")
    public ImputacionesCuentaDTO getMayor(
    		@QueryParam("cuenta") @NotNull @Min(1) Long cuentaId,
    		@BeanParam AsientoFilter filtro,
    		@Valid @BeanParam PageReq page
    ) throws EntityNotFoundException, EjercicioNoSeleccionadoException {
    	
		final Ejercicio ej = session.getEjercicioOrThrow();
		final Cuenta cuenta = cuentaService.getByIdOrThrow(ej.getOrganizacion(), cuentaId);
		
		// Se obtiene la pagina del mayor
		ImputacionesCuenta result = service.getMayor(ej, cuenta, filtro, page.toPageable());
    	
    	return new ImputacionesCuentaDTO(asientoMapper.toDto(result.getPage()), result.getSaldoAnterior());
    }
	
	/**
	 * Genera un reporte de Mayor en Excel y permite su descarga.
	 * <p>A diferencia del metodo anterior, el reporte puede incluir el mayor de multiples cuentas.</p>
	 * @param categorias categorias cuyas cuentas se deben incluir (ids separados por comas)
	 * @param cuentas cuentas a incluir (ids separados por comas)
	 * @param filtro filtro que limita los asientos incluidos
	 * @return el archivo excel en binario, con un nombre acorde
	 * @throws EntityNotFoundException si no existe la cuenta
	 * @throws EjercicioNoSeleccionadoException si no se selecciono un ejercicio
	 */
	@GET
    @Path("mayor/xls")
	public Response exportarMayor(
			@QueryParam("categorias") String categorias,
    		@QueryParam("cuentas") String cuentas,
    		@BeanParam AsientoFilter filtro
    ) throws EntityNotFoundException, EjercicioNoSeleccionadoException {
    	
		final Ejercicio ej = session.getEjercicioOrThrow();
		
		// Se buscan los ids de las cuentas incluidas, en base a las categorias y cuentas deseadas.
		Set<Long> ids = this.getCuentasIds(ej.getOrganizacion(), categorias, cuentas);
		
		// Si no hay ids, no tiene sentido el reporte
		if(ids.isEmpty()) throw new InvalidRequestException("No se especificaron cuentas");
		
		// Se genera el archivo y se obtiene el path
		java.nio.file.Path xls = service.exportarMayor(ej, ids, filtro);
    	
		// Se calcula el nombre que debe tener el archivo (<org>-mayor.xlsx)
		String fileName = ej.getOrganizacion().getNombre().replace(".",  "").replace("\\", "") + "-mayor.xlsx";
		
		// Se genera el response de descarga
		return Response.ok(xls.toFile(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.build();
    }
	
	/**
	 * Dada una lista de ids de categorias y cuentas, genera la lista de ids de las cuentas finales.
	 * @param org organizacion actual
	 * @param categorias categorias cuyas cuentas se debe incluir
	 * @param cuentas cuentas especificas a incluir
	 * @return
	 */
	private Set<Long> getCuentasIds(Organizacion org, String categorias, String cuentas) {
		Set<Long> ids = new HashSet<>(); 
		
		if(categorias != null) {
			// Si hay categorias, se buscan
			List<Categoria> parents = catService.findByIds(org, CollectionUtils.parseLongList(categorias));
			
			// Se buscan los ids de las cuentas descendientes de estas categorias
			List<Cuenta> descendientes = cuentaService.findCuentasDescendientes(org, parents);
			for(Cuenta c : descendientes) ids.add(c.getId());
		}
		
		// Las cuentas se agregan directamente
		if(cuentas != null) ids.addAll(CollectionUtils.parseLongList(cuentas));
		
		return ids;
	}
}
