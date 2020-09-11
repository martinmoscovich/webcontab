package com.mmoscovich.webcontab.services;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.AsientoRepository;
import com.mmoscovich.webcontab.dao.InformeRepository;
import com.mmoscovich.webcontab.dao.helper.QueryBalance.FiltroBalance;
import com.mmoscovich.webcontab.dto.AsientoFilter;
import com.mmoscovich.webcontab.dto.BalanceFilter;
import com.mmoscovich.webcontab.dto.informes.BalanceCuenta;
import com.mmoscovich.webcontab.exception.EjercicioNoSeleccionadoException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exporter.ExcelBalanceExporter;
import com.mmoscovich.webcontab.exporter.ExcelDiarioExporter;
import com.mmoscovich.webcontab.exporter.ExcelMayorExporter;
import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;
import com.mmoscovich.webcontab.services.ImputacionService.ImputacionesCuenta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Servicio de informes (Diario, Mayor, Balance).
 * <p>
 * Diario: Lista de asientos con sus imputaciones.<br>
 * Mayor: Imputaciones agrupadas por cuenta.<br>
 * Balance: Saldos de las cuentas.
 * </p>
 *
 */
@Service
public class InformeService {
	
	@Inject
	private InformeRepository dao;

	@Inject
	private CategoriaService categoriaService;
	
	@Inject
	private AsientoRepository asientoDao;
	
	@Inject
	private EntityManager em;

	/**
	 * Obtiene una pagina del balance.
	 * 
	 * @param ejercicio ejercicio para el cual se genera el balance
	 * @param filter filtro para limitar cuentas y asientos incluidos en el balance
	 * @param includeCount indica si se debe obtener la cantidad total de items del balance
	 * @param pagination datos de paginacion
	 */
	public Slice<BalanceCuenta> getBalance(Ejercicio ejercicio, BalanceFilter filtro, boolean includeCount, Pageable pagination) {
		// Se obtiene la categoria si solo se desea el balance de una
		List<Categoria> categorias = (filtro.getCategoriaId() != null) ? 
				Arrays.asList(categoriaService.getByIdOrThrow(ejercicio.getOrganizacion(), filtro.getCategoriaId())) : 
				Collections.emptyList();
		
		// Se arma el filtro para la query
		FiltroBalance filtroQuery = new FiltroBalance(ejercicio);
		filtroQuery.setPeriodo(filtro.getDesde(), filtro.getHasta());
		filtroQuery.setIncluirCero(filtro.isIncludeCuentasEnCero());
		filtroQuery.soloEnCategorias(categorias);
		
		// Se realiza la query
		return dao.getBalance(filtroQuery, includeCount, pagination);			
	}
	
	/**
	 * Obtiene los saldos del balance para cada moneda.
	 * <p>Solo se ejecuta la query si se filtran cuentas, ya que en caso contrario,
	 * debe ser siempre cero (todos los asientos tienen saldo cero).</p>
	 * 
	 * @param ejercicio ejercicio para el cual se genera el balance
	 * @param filter filtro para limitar cuentas y asientos incluidos en el balance
	 * @return un mapa que tiene como clave el id de la moneda y como value el saldo en dicha moneda
	 */
	public Map<Long, BigDecimal> getTotalesBalance(Ejercicio ejercicio, BalanceFilter filtro) {
		// Si no se filtran las cuentas, se devuelve un mapa vacio, ya que todos los saldos seran 0.
		if(filtro.getCategoriaId() == null) return new HashMap<>();

		if(filtro.getDesde() == null) filtro.setDesde(ejercicio.getInicio());
		if(filtro.getHasta() == null) filtro.setHasta(ejercicio.getFinalizacion());
		
		// Se arma el filtro para la query
		FiltroBalance filtroQuery = new FiltroBalance(ejercicio);
		filtroQuery.setPeriodo(filtro.getDesde(), filtro.getHasta());
		filtroQuery.soloEnCategorias(List.of(categoriaService.getByIdOrThrow(ejercicio.getOrganizacion(), filtro.getCategoriaId())));
		
		return dao.getBalanceTotales(filtroQuery);
	}
	
	/**
	 * Genera un reporte de balance en Excel.
	 * 
	 * @param ejercicio ejercicio para el cual se genera el balance
	 * @param filtro filtro para limitar cuentas y asientos incluidos en el balance
	 * @return el path al archivo excel generado
	 */
	@Transactional(readOnly = true)
    public Path exportarBalance(Ejercicio ejercicio, BalanceFilter filtro) {
		// Se obtiene la categoria si solo se desea el balance de una
		List<Categoria> categorias = (filtro.getCategoriaId() != null) ? 
				Arrays.asList(categoriaService.getByIdOrThrow(ejercicio.getOrganizacion(), filtro.getCategoriaId())) : 
				Collections.emptyList();
				
		// Se arma el filtro para la query
		FiltroBalance filtroQuery = new FiltroBalance(ejercicio);
		filtroQuery.setPeriodo(filtro.getDesde(), filtro.getHasta());
		filtroQuery.setIncluirCero(filtro.isIncludeCuentasEnCero());
		filtroQuery.soloEnCategorias(categorias);
		
		// Se obtiene el stream
		Stream<BalanceCuenta> rows = dao.streamBalance(filtroQuery);
				
		// Se genera el reporte
		return new ExcelBalanceExporter().exportar(em, ejercicio, rows);
    }
	
	/**
	 * Obtiene una pagina del Mayor de una cuenta.
	 * <p>Permite filtrar los asientos incluidos.</p>
	 * 
	 * @param ejercicio ejercicio para el cual se genera el mayor
	 * @param asientoFilter filtro que limita los asientos incluidos
	 * @param cuenta
	 * @param pagination datos de paginacion
	 * @return una pagina de imputaciones de la cuenta y el saldo anterior a las mismas
	 * @throws EntityNotFoundException
	 * @throws EjercicioNoSeleccionadoException
	 */
	public ImputacionesCuenta getMayor(Ejercicio ejercicio, Cuenta cuenta, AsientoFilter asientoFilter, Pageable pagination) {
		// Obtiene la pagina de mayor
		Page<Imputacion> page = dao.getMayor(ejercicio, cuenta, asientoFilter, pagination);
		
		BigDecimal saldo;
		if(asientoFilter.esFiltroNumeros()) {
			// Si se filtro por num de asiento, no se calcula saldo anterior
			saldo = null;
		} else {
			// Si se filtro por fecha, se busca el saldo anterior a dicha fecha
			LocalDate desde = asientoFilter.getDesde();
			LocalDate hasta = asientoFilter.getHasta() == null ? ejercicio.getFinalizacion() : asientoFilter.getHasta();
			
			saldo = calculateSaldoAnterior(ejercicio, cuenta, page, desde == null || desde.isAfter(ejercicio.getInicio()), hasta);
		}
		
		return new ImputacionesCuenta(page, saldo);
	}
	
	/**
	 * Calcula el saldo de la cuenta anterior a las imputaciones especificadas
	 * @param ejercicio ejercicio para el cual se genera el mayor
	 * @param cuenta
	 * @param page pagina de imputaciones que se va a retornar al usuario
	 * @param cotaInferiorFiltrada indica si se esta mostrando la primera imputacion del ejercicio (para devolver saldo anterior = 0)
	 * @param hasta fecha maxima en la que buscar si no hay items en la pagina
	 * @return
	 */
	private BigDecimal calculateSaldoAnterior(Ejercicio ejercicio, Cuenta cuenta, Page<Imputacion> page, boolean cotaInferiorFiltrada, LocalDate hasta) {
		if(page.isFirst() && !cotaInferiorFiltrada) {
			// Si es la primera pagina y la cota inferior no filtra nada, el saldo sera cero 
			return BigDecimal.ZERO;
		}
		
		Long firstId = null;
		// Si hay items en la pagina, se obtiene el saldo anterior al primer item de la misma
		// Si no hay items en la pagina, se obtiene el saldo hasta la fecha final pedida
		if(!page.getContent().isEmpty()) {
			Imputacion i = page.getContent().get(0);
			firstId = i.getId();
			hasta = i.getAsiento().getFecha();
		}
		
		// Se busca el saldo anterior de la cuenta
		Map<Long, BigDecimal> result = dao.getMayorSaldoAnterior(ejercicio, Set.of(cuenta.getId()), hasta, firstId);
		
		// Como la query anterior esta pensada para multiples cuentas pero se envio una, se pide solo la primera entry.
		return result.isEmpty() ? BigDecimal.ZERO : IterableUtils.get(result.values(), 0);
	}
	
	/**
	 * Genera un reporte de Mayor en Excel.
	 * <p>A diferencia de {@link #getMayor(Ejercicio, Cuenta, AsientoFilter, Pageable)}, el reporte puede incluir el mayor de multiples cuentas.</p>
	 * 
	 * @param ejercicio ejercicio para el cual se genera el mayor
	 * @param cuentasIds cuentas a incluir (ids separados por comas)
	 * @param filter filtro que limita los asientos incluidos
	 * @return el path al archivo excel generado.
	 */
    @Transactional(readOnly = true)
    public Path exportarMayor(Ejercicio ejercicio, Set<Long> cuentasIds, AsientoFilter filter) {
    	// Genera el stream de imputaciones
    	Stream<Imputacion> imputaciones = dao.streamMayor(ejercicio, cuentasIds, filter);
    	
    	// Si se estan filtrando imputaciones, se busca el saldo anterior para cada cuenta
    	Map<Long, BigDecimal> saldosAnteriores = null;
    	if(filter.esFiltroFechas() && filter.getDesde() != null) {
    		saldosAnteriores = dao.getMayorSaldoAnterior(ejercicio, cuentasIds, filter.getDesde(), null);
    	}
    	
    	// Se genera el reporte
    	return new ExcelMayorExporter().exportar(em, new MayorExporterContext(ejercicio, saldosAnteriores), imputaciones);
    }
    
    /**
	 * Genera el reporte Diario en Excel, filtrando los asientos por rango de numero.
	 * 
	 * @param ejercicio ejercicio para el cual se genera el mayor
	 * @param min numero minimo de asiento a buscar
	 * @param max numero maximo de asiento a buscar
	 * @return el path al archivo excel 
	 */
    @Transactional(readOnly = true)
	public Path crearDiarioPorNumero(Ejercicio ejercicio, Short min, Short max) {
		if(min == null) min = 0;
		if(max == null) max = Short.MAX_VALUE; 
		
		// Genera el stream de asientos
		Stream<Asiento> asientos = asientoDao.findByNumerosReport(ejercicio, min, max);
		
		// Se genera el reporte
		return new ExcelDiarioExporter().exportar(em, ejercicio, asientos);
	}
	
    /**
	 * Genera el reporte Diario en Excel, filtrando los asientos por fechas.
	 * 
	 * @param ejercicio ejercicio para el cual se genera el mayor
	 * @param desde fecha desde la cual se deben incluir los asientos
	 * @param hasta fecha hasta la cual se deben incluir los asientos
	 * @return el path al archivo excel 
	 */
    @Transactional(readOnly = true)
	public Path crearDiarioPorPeriodo(Ejercicio ejercicio, LocalDate desde, LocalDate hasta) {
		if(desde == null) desde = ejercicio.getInicio();
		if(hasta == null) hasta = ejercicio.getFinalizacion();
		
		// Genera el stream de asientos
		Stream<Asiento> asientos = asientoDao.findByPeriodoReport(ejercicio, desde, hasta);
		
		// Se genera el reporte
		return new ExcelDiarioExporter().exportar(em, ejercicio, asientos);
	}
	
    /**
     * Contexto del reporte de Mayor
     */
	@Getter
	@AllArgsConstructor
	public static class MayorExporterContext {
		/** Ejercicio */
		private Ejercicio ejercicio;
		
		/** Mapa que contiene como clave el id de una cuenta y como value el saldo anterior para dicha cuenta */
		private Map<Long, BigDecimal> saldosAnteriores;
	}
}
