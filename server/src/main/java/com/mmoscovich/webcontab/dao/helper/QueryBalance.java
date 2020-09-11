package com.mmoscovich.webcontab.dao.helper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dto.informes.BalanceCuenta;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.util.JpaUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Clase que simplifica la query de balance
 *
 */
public class QueryBalance {
	
	private static enum TipoBusquedaCategorias {
		SOLO_EN_ESAS, EXCEPTO_EN_ESAS
	}
	
	private static final String FROM = "FROM Cuenta c \n" +
		    						   " LEFT JOIN imputacion i on c.id = i.cuenta_id \n" +
		    						   " LEFT JOIN asiento a on i.asiento_id = a.id\n";
	
	private static final String GROUP_BY = "GROUP BY c.id, c.codigo, c.descripcion, c.moneda_id\n";
	private static final String GROUP_BY_COUNT = "GROUP BY c.id\n";
	private static final String ORDER_BY = "ORDER BY c.orden\n";
	
	/** 
	 * Clase que contiene los filtros posibles para el balance.
	 * <p>Estos filtros afectan a las cuentas buscadas y a los asientos incluidos.</p>
	 *
	 */
	@Getter
	public static class FiltroBalance {
		private Ejercicio ejercicio;
		
		/** Fecha a partir de la cual se deben buscar los asientos. */
		@Setter
		private LocalDate desde;
		
		/** Fecha hasta la cual se deben buscar los asientos. */
		@Setter
		private LocalDate hasta;
		
		private TipoBusquedaCategorias tipo;
		
		/** 
		 * Lista de categorias usada para filtrar las cuentas.
		 * <p>Segun {@link #tipo}, puede ser:
		 * <br>categorias a las cuales deben pertenecer las cuentas.
		 * <br>categorias a las cuales <b>NO</b> deben pertenecer las cuentas.
		 * </p>
		 */
		private List<Categoria> categorias;
		
		@Setter
		private boolean incluirCero;
		
		public FiltroBalance(Ejercicio ejercicio) {
			this.ejercicio = ejercicio;
		}
		
		/** 
		 * Indica que se deben buscar los asientos entre estas dos fechas.
		 * <br>Si una es null, solo se limita por la otra.
		 * @param desde
		 * @param hasta
		 */
		public void setPeriodo(LocalDate desde, LocalDate hasta) {
			this.desde = desde;
			this.hasta = hasta;
		}
		
		/**
		 * Solo buscar en las categorias indicadas.
		 */
		public void soloEnCategorias(List<Categoria> categorias) {
			this.categorias = categorias;
			this.tipo = TipoBusquedaCategorias.SOLO_EN_ESAS;
		}
		
		/**
		 * Incluir todas las categorias <b>excepto</b> las indicadas.
		 * @param categorias
		 */
		public void excluirCategorias(List<Categoria> categorias) {
			this.categorias = categorias;
			this.tipo = TipoBusquedaCategorias.EXCEPTO_EN_ESAS;
		}
	}
	
	private FiltroBalance filtro;
	
	@Setter
	private Pageable pageReq;

	public QueryBalance(FiltroBalance filtro) {
		this.filtro = filtro;
	}
	
	/**
	 * Ejecuta la query obteniendo una pagina <b>sin</b> total ({@link Slice}).
	 * @param em EntityManager
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Slice<BalanceCuenta> getSlice(EntityManager em) {
		Query query = em.createNativeQuery(this.buildSQL());
		this.addParameters(query);
		this.addPaginationParameters(query);
		
		List<Object[]> rows = query.getResultList();
		
		int resultSize = rows.size();

		// Se obtiene la lista con el tamanio de pagina como maximo
		rows = rows.subList(0, Math.min(pageReq.getPageSize(), resultSize));
		
		List<BalanceCuenta> result = rows.stream().map(this::mapRowToBalanceCuenta).collect(Collectors.toList());
		
		return new SliceImpl<BalanceCuenta>(result, pageReq, rows.size() < resultSize);
	}
	
	/**
	 * Ejecuta la query obteniendo una pagina <b>con</b> total ({@link Page}).
	 * @param em EntityManager
	 */
	@Transactional(readOnly = true)
	public Page<BalanceCuenta> getPage(EntityManager em) {
		// Se obtienen los items
		List<BalanceCuenta> items = this.getSlice(em).getContent();
		
		// Se arma la query para el count
		Query countQuery = em.createNativeQuery(this.buildCountSQL());
		this.addParameters(countQuery);
		
		// Se obtiene el count
        Long count = ((BigInteger) countQuery.getSingleResult()).longValue();
        
        return new PageImpl<>(items, pageReq, count);
	}
	
	/**
	 * Ejecuta la query obteniendo un Stream con todos los resultados (sin paginar)
	 * @param em EntityManager
	 * @param fetchSize hint para la DB con el tamanio de fetch a utlilizar
	 * @return
	 */
	@Transactional(readOnly = true)
	public Stream<BalanceCuenta> stream(EntityManager em, int fetchSize) {
		Query query = em.createNativeQuery(this.buildSQL());
		this.addParameters(query);
		
		Stream<Object[]> rows = JpaUtils.getStreamFromQuery(query, fetchSize);
		
		return rows.map(this::mapRowToBalanceCuenta);
	}
	
	/**
	 * Mapea un row de resultados (Object[]) a {@link BalanceCuenta}.
	 * @param row
	 * @return
	 */
	private BalanceCuenta mapRowToBalanceCuenta(Object[] row) {
		return new BalanceCuenta(((BigInteger)row[0]).longValue(), (String)row[1], (String)row[2], ((BigInteger)row[3]).longValue(), (BigDecimal)row[4]);
	}
	
	/** Agrega los parametros a la query */
	private void addParameters(Query query) {
		query.setParameter("ejercicioId", filtro.ejercicio.getId());
		if(filtro.desde != null) query.setParameter("desde", filtro.desde);
		if(filtro.hasta != null) query.setParameter("hasta", filtro.hasta);
	}
	
	/** Agrega los parametros a la query de count*/
	private void addPaginationParameters(Query query) {
		if(pageReq != null) {
			query.setParameter("offset", pageReq.getOffset());
			
			// Se pide uno mas que lo necesario para saber si hay next
			query.setParameter("size", pageReq.getPageSize() + 1);
		}
	}
	
	/** Construye el SQL para obtener los items de balance */
	String buildSQL() {
		String select = "SELECT c.id, c.codigo, c.descripcion, c.moneda_id, IFNULL(SUM(i.importe), 0) as balance\n"; 
		
		return select + FROM + where() + GROUP_BY + having() + ORDER_BY + pagination(); 
	}
	
	/** Construye el SQL para obtener el total de items de balance */
	String buildCountSQL() {
		// Count de la query anterior pero sin orden ni paginacion.
		return "SELECT count(*) FROM (SELECT c.id\n" + FROM + where() + GROUP_BY_COUNT + having() + ")";  
	}
	
	
	/** Construye el WHERE */
	private String where() {
		return "WHERE " + 
				new SQLCondition()
				// Que sea Cuenta (imputable)
				.add(this.getImputableFilter())
				// Filtra los asientos
				.add(this.getAsientoFilter())
				// Filtra las categorias
				.add(this.getCategoriaFilter())
				.isRoot()
				.buildAnd() + "\n";
	}
	
	/** Construye el Having que filtra o no las cuentas con saldo cero */
	private String having() {
		if(filtro.incluirCero) return ""; 
		
		return "HAVING SUM(i.importe) <> 0\n"; 
	}
	
	private String pagination() {
		if(pageReq == null) return "";
		return "LIMIT :size OFFSET :offset";
	}
	
	/** Al ser query nativa, se debe indicar que sean solo cuentas (imputables) */
	private String getImputableFilter() {
		return "c.imputable = 1";
	}
	
	/** 
	 * Filtra los asientos por ejercicio y fechas.
	 * <br>Tambien incluye las filas que tienen asientos NULL (para incluir cuentas sin imputaciones). 
	 */
	private String getAsientoFilter() {
		String filtroAsientos = new SQLCondition()
		.equals("a.ejercicio_id", ":ejercicioId")
		.add(this.getDateFilter())
		.buildAnd();
		
		return new SQLCondition()
				.isNull("a.ejercicio_id")
				.add(filtroAsientos)
				.buildOr();
	}
	
	private String getDateFilter() {
		SQLCondition cond = new SQLCondition();
		if(filtro.desde != null && filtro.hasta != null) {
			cond.between("a.fecha", ":desde", ":hasta");
		} else if(filtro.desde != null) {
			cond.greaterThanOrEqual("a.fecha", ":desde");
		} else if(filtro.hasta != null) {
			cond.lessThanOrEqual("a.fecha", ":hasta");
		}
		
		return cond.buildAnd();
	}
	
	/** Filtro que incluye o excluye a los descendientes de determinadas categorias */
	private String getCategoriaFilter() {
		if(filtro.categorias == null || filtro.categorias.isEmpty()) return "";
		
		// Se transforman las categorias a predicates que piden las cuentas que pertenecen
		return (filtro.tipo == TipoBusquedaCategorias.SOLO_EN_ESAS) ? 
				pertenecientesACategorias(filtro.categorias) :
				exceptoCategorias(filtro.categorias);
		
	}
	
	private String pertenecientesACategorias(List<Categoria> categorias) {
		return new SQLCondition()
				.like("c.codigo", categorias.stream().map(c -> c.getCodigo() + ".%").collect(Collectors.toList()))
				.buildOr();
	}
	
	private String exceptoCategorias(List<Categoria> categorias) {
		return new SQLCondition()
				.notLike("c.codigo", categorias.stream().map(c -> c.getCodigo() + ".%").collect(Collectors.toList()))
				.buildAnd();
	}
}
