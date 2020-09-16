package com.mmoscovich.webcontab.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.helper.CriteriaCondition;
import com.mmoscovich.webcontab.dao.helper.QueryBalance;
import com.mmoscovich.webcontab.dao.helper.QueryBalance.FiltroBalance;
import com.mmoscovich.webcontab.dto.AsientoFilter;
import com.mmoscovich.webcontab.dto.informes.BalanceCuenta;
import com.mmoscovich.webcontab.dto.informes.BalanceMensualCuenta;
import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Asiento_;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Cuenta_;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;
import com.mmoscovich.webcontab.model.Imputacion_;
import com.mmoscovich.webcontab.model.Moneda_;
import com.mmoscovich.webcontab.util.CriteriaUtils;

/**
 * DAO de Informes (Mayor y Balance). 
 * <br>El diario se genera en {@link AsientoRepository}.
 * 
 * <p>Se usa un repositorio manual por la complejidad de las queries.</p>
 *  
 * @author Martin
 *
 */
@Repository
public class InformeRepository {
	
	@Inject
	private EntityManager em;
	
	/**
	 * Obtiene los datos para el excel de Mayor (lista de imputaciones por cuenta).
	 * <br>
	 * Devuelve un stream con todas las imputaciones segun los filtros.
	 *  
	 * @param ejercicio ejercicio a buscar
	 * @param cuentasIds ids de las cuentas para las cuales se genera el mayor
	 * @param asientoFilter filtro de los asientos cuyas imputaciones se devolveran
	 * @return
	 */
	@Transactional(readOnly = true)
	public Stream<Imputacion> streamMayor(Ejercicio ejercicio, Set<Long> cuentasIds, AsientoFilter asientoFilter) {
		CriteriaQuery<Imputacion> criteria = this.buildMayorQuery(ejercicio, cuentasIds, asientoFilter, true);
		
        return CriteriaUtils.getStream(em, criteria, 1000);
	}
	
	/**
	 * Obtiene los datos para una pagina del Mayor (lista de imputaciones por cuenta).
	 * <br>
	 * Devuelve una pagina de imputaciones segun los filtros.
	 *  
	 * @param ejercicio ejercicio a buscar
	 * @param cuentasIds ids de las cuentas para las cuales se genera el mayor
	 * @param asientoFilter filtro de los asientos cuyas imputaciones se devolveran
	 * @param pagination datos de paginacion
	 * @return
	 */
	public Page<Imputacion> getMayor(Ejercicio ejercicio, Cuenta cuenta, AsientoFilter asientoFilter, Pageable pagination) {
		CriteriaQuery<Imputacion> criteria = this.buildMayorQuery(ejercicio, Set.of(cuenta.getId()), asientoFilter, false);
		
		return CriteriaUtils.getPage(em, criteria, pagination);
	}
	
	/**
	 * Metodo que genera la query de mayor (lista de imputaciones por cuenta).
	 * <p>Los parametros permiten configurar los filtros y los datos devueltos.
	 * <br>Luego se puede utilizar para pedir un stream o una pagina.</p>
	 * @param ejercicio ejercicio a buscar
	 * @param cuentasIds ids de las cuentas para las cuales se genera el mayor
	 * @param asientoFilter filtro de los asientos cuyas imputaciones se devolveran
	 * @param fetchCuenta indica si se deben traer los datos de las cuentas (join)
	 * @return
	 */
	private CriteriaQuery<Imputacion> buildMayorQuery(Ejercicio ejercicio, Set<Long> cuentasIds, AsientoFilter asientoFilter, boolean fetchCuenta) {
		//FROM Imputacion i inner join i.asiento a WHERE a.ejercicio = :ejercicio AND i.cuenta.id in :cuentasIds AND a.fecha BETWEEN :desde AND :hasta ORDER by i.cuenta.orden, a.fecha, a.numero, i.id")
		//FROM Imputacion i inner join i.asiento a WHERE a.ejercicio = :ejercicio AND i.cuenta.id in :cuentasIds AND a.numero BETWEEN :min AND :max ORDER by a.fecha, a.numero, i.id")
		
		// Se debe fetchear la cuenta si se pide explicitamente o si se busca mas de una (para ordenar con el campo "orden")
		fetchCuenta = cuentasIds.size() > 1 || fetchCuenta;
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Imputacion> criteria = builder.createQuery(Imputacion.class);
        
        // FROM Imputacion
        Root<Imputacion> imputacion = criteria.from(Imputacion.class);

        // Join con asiento
        Path<Asiento> asiento = imputacion.get(Imputacion_.ASIENTO);
        imputacion.fetch(Imputacion_.ASIENTO);
        
        // Si se necesita, join con cuenta
        if(fetchCuenta) imputacion.fetch(Imputacion_.CUENTA);
        
        CriteriaCondition cond = new CriteriaCondition();
        
        // Filtro por ejercicio
        cond.add(builder.equal(asiento.get(Asiento_.EJERCICIO), ejercicio));
        
        // Filtro por cuentas
        List<Cuenta> cuentas = cuentasIds.stream().map(id -> em.getReference(Cuenta.class, id)).collect(Collectors.toList());
        if(cuentas.size() == 1) {
        	// Si es una, se hace equals
    		cond.add(builder.equal(imputacion.get(Imputacion_.CUENTA), cuentas.get(0)));
    	} else {
    		// Si son varias se hace in
    		cond.add(imputacion.get(Imputacion_.CUENTA).in(cuentas));
    	}

        // Filtro por fecha o numeros de asientos
        if(asientoFilter.esFiltroFechas()) {
        	cond.add(CriteriaUtils.between(builder, asiento.get(Asiento_.FECHA), asientoFilter.getDesde(), asientoFilter.getHasta()));
        } else {
        	cond.add(CriteriaUtils.between(builder, asiento.get(Asiento_.NUMERO), asientoFilter.getMin(), asientoFilter.getMax()));
        }
        
        criteria.where(cond.buildAnd(builder));
        
        // Orden
        List<Order> orderFields = new ArrayList<Order>();
        
        // Si se pidio obtener las cuentas, se ordena primero por cuenta
        if(fetchCuenta) orderFields.add(builder.asc(imputacion.get(Imputacion_.CUENTA).get(Cuenta_.ORDEN)));
        
        // Luego se ordena por fecha y numero de asiento. Finalmente por orden de imputaciones
        CollectionUtils.addAll(orderFields, builder.asc(asiento.get(Asiento_.FECHA)), builder.asc(asiento.get(Asiento_.NUMERO)), builder.asc(imputacion.get(Imputacion_.ID)));
        criteria.orderBy(orderFields);
        
        return criteria;
	}
	
	/** Query para obtener el saldo anterior del mayor */
	private static final String MAYOR_SALDO_ANTERIOR_QUERY = 
			"SELECT i.cuenta.id, SUM(i.importe) " +
			"FROM Imputacion i inner join i.asiento a " +
			"WHERE " +
			"a.ejercicio = :ejercicio AND " +
			"i.cuenta.id in :cuentasIds AND " +
			"(a.fecha < :hasta OR (a.fecha = :hasta AND i.id < :firstId))";
	
	/**
	 * Obtiene los saldos anteriores a las imputaciones que se mostraran en el mayor. Uno por cada cuenta.
	 * <p>El mayor va mostrando el saldo ante cada imputacion. Para que tenga sentido, no se puede 
	 * arrancar de saldo cero, si no que debe obtenerse el que se tenia antes de la primera imputacion.</p>
	 * <p>Permite obtener el saldo anterior para una o mas cuentas (util para el reporte).</p>
	 * 
	 * @param ejercicio ejercicio a buscar
	 * @param cuentasIds ids de las cuentas para las cuales se genera el mayor
	 * @param hasta fecha a partir de la cual se muestra el mayor (o sea, se debe buscar saldo previo a esta fecha)
	 * @param firstId en caso de no ser la primera pagina, se indica el primer Id de la misma para buscar los ids previos
	 * @return mapa que tiene como clave el id de la cuenta y como value el saldo anterior en dicha cuenta
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Map<Long, BigDecimal> getMayorSaldoAnterior(Ejercicio ejercicio, Set<Long> cuentasIds, LocalDate hasta, Long firstId) {
		
		String query = MAYOR_SALDO_ANTERIOR_QUERY + " GROUP BY i.cuenta";;
		
		List<Object[]> rows = em.createQuery(query)
			.setParameter("ejercicio", ejercicio)
			.setParameter("cuentasIds", cuentasIds)
			.setParameter("hasta", hasta)
			.setParameter("firstId", firstId == null ? 0 : firstId)
			.getResultList();
		
		// Se mapea cada row (Object[]) en un mapa.
		return rows.stream().collect(Collectors.toMap(r -> (Long)r[0], r -> r[1] == null ? BigDecimal.ZERO : (BigDecimal)r[1]));
	}
	
	/**
	 * Obtiene una pagina del balance (saldo por cuenta), permitiendo filtrar tanto las cuentas como el saldo.
	 * 
	 * @param filtro filtro de cuentas e imputaciones a incluir
	 * @param incluirCount indica si devuelve la cantidad total de items o no
	 * @param pageReq datos de paginacion
	 * @return lista de items que incluyen datos basicos de la cuenta y su saldo
	 */
	@Transactional(readOnly = true)
	public Slice<BalanceCuenta> getBalance(FiltroBalance filtro, boolean incluirCount, Pageable pageReq) {
		QueryBalance query = new QueryBalance(filtro);
		query.setPageReq(pageReq);
		
		// Devuelve un page o slice segun si se pidio el count
		return incluirCount ? query.getPage(em) : query.getSlice(em);
	}
	
	/**
	 * Obtiene un stream completo del balance (saldo por cuenta), permitiendo filtrar tanto las cuentas como el saldo.
	 * @param filtro filtro de cuentas e imputaciones a incluir
	 * @return
	 */
	@Transactional(readOnly = true)
	public Stream<BalanceCuenta> streamBalance(FiltroBalance filtro) {
		QueryBalance query = new QueryBalance(filtro);
		return query.stream(em, 500);
	}
	
	/**
	 * Obtiene un stream completo del balance <b>POR MES</b> (saldo por cuenta), permitiendo filtrar tanto las cuentas como el saldo.
	 * @param filtro filtro de cuentas e imputaciones a incluir
	 * @return
	 */
	@Transactional(readOnly = true)
	public Stream<BalanceMensualCuenta> streamBalanceMensual(FiltroBalance filtro) {
		QueryBalance query = new QueryBalance(filtro);
		return query.streamMensual(em, 500);
	}
	
	
	/**
	 * Permite obtener los saldos globales del balance por moneda.
	 * <p>
	 * Tiene sentido solo cuando se filtran cuentas, ya que en caso contrario, debe ser siempre cero.
	 * (Todos los asientos tienen saldo cero).
	 * </p>
	 * @param filtro filtro de cuentas e imputaciones a incluir
	 * @return mapa que tiene como clave el id de la moneda y como value el saldo.
	 */
	@Transactional(readOnly = true)
	public Map<Long, BigDecimal> getBalanceTotales(FiltroBalance filtro) {
		//	SELECT c.moneda.id, SUM(i.importe)  FROM Imputacion i inner join i.asiento a inner join i.cuenta c
		//	WHERE a.ejercicio = :ejercicio AND a.fecha BETWEEN :desde AND :hasta [AND c.codigo LIKE :query, ...] 
		//	GROUP BY c.moneda.id 
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteria = builder.createQuery(Object[].class);
        
        // FROM Imputacion
        Root<Imputacion> imputacion = criteria.from(Imputacion.class);

        // Join con asiento y cuenta
        Join<Imputacion, Asiento> asiento = imputacion.join(Imputacion_.ASIENTO);
        Join<Imputacion, Cuenta> cuenta = imputacion.join(Imputacion_.CUENTA);
        
        // Ref a c.moneda.id
        Path<Object> monedaId = cuenta.get(Cuenta_.MONEDA).get(Moneda_.ID);
	
        // Select de la moneda y el sum de las imputaciones
        criteria.multiselect(monedaId, builder.sum(imputacion.get(Imputacion_.IMPORTE)));
        
        // Condiciones
        Predicate where = new CriteriaCondition()
        	// Ejercicio
        	.add(builder.equal(asiento.get(Asiento_.EJERCICIO), filtro.getEjercicio()))
        	// Fechas
        	.add(builder.between(asiento.get(Asiento_.FECHA), filtro.getDesde(), filtro.getHasta()))
        	// Pertenecientes a las categorias (si esta vacio busca en todas)
        	.add(pertenecientesACategorias(builder, cuenta, filtro.getCategorias()))
        	
        	.buildAnd(builder);
        
        criteria.where(where);
        
        // Group by moneda
        criteria.groupBy(monedaId);

        // Ejecuta y convierte al Map que usamos
        return em.createQuery(criteria).getResultList().stream().collect(Collectors.toMap(i -> (Long)i[0], i -> (BigDecimal)i[1]));
	}
	
	private Optional<Predicate> pertenecientesACategorias(CriteriaBuilder cb, Path<Cuenta> cuenta, List<Categoria> categorias) {
		Collection<Predicate> p = categorias.stream()
				.map(c -> cb.like(cuenta.get(Cuenta_.CODIGO), c.getCodigo() + ".%"))
				.collect(Collectors.toList());
		
		if(p.isEmpty()) return Optional.empty();
		
		return Optional.of(cb.or(p.toArray(new Predicate[0])));
	}
}
