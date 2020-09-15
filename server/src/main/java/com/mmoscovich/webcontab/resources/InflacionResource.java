package com.mmoscovich.webcontab.resources;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.dao.InflacionRepository;
import com.mmoscovich.webcontab.dao.MonedaRepository;
import com.mmoscovich.webcontab.dto.Periodo;
import com.mmoscovich.webcontab.dto.PeriodoMensual;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.InflacionMes;
import com.mmoscovich.webcontab.model.Moneda;
import com.mmoscovich.webcontab.services.SessionService;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Resource para ABM de indices de inflacion
 *
 */
@Component
@Path("/admin/inflacion")
@Produces(MediaType.APPLICATION_JSON)
public class InflacionResource extends SimpleResource<InflacionMes, InflacionMes> {

	@Inject
	private InflacionRepository dao;
	
	@Inject
	private SessionService session;
	
	@Inject
	private MonedaRepository monedaDao;
	
	/**
     * Busca los indices de inflacion dentro del periodo especificado.
     * <p>Los que no encuentra, los devuelve igual con indice = 0</p>
     * 
     * @param filter filtro de periodo y moneda 
     * 
     * @return lista de indices de inflacion para el periodo y moneda indicados
     * @throws InvalidRequestException si no se encuentra la moneda
     */
    @GET
    public List<InflacionMes> search(@Valid @BeanParam InflacionFilter filter) throws InvalidRequestException {
    	// Se busca la moneda (por id o default) y falla si no existe
    	Moneda moneda = this.getMonedaOrThrow(filter.getMonedaId());
    	
    	// Se completa el filtro si es necesario
    	PeriodoMensual periodo = this.buildPeriodo(filter);
    	
    	// Se buscan los persistidos
    	List<InflacionMes> persistidos = dao.findByMonedaAndPeriodo(moneda, periodo.getDesde().atDay(1), periodo.getHasta().atEndOfMonth());
    	
    	// Resultado a devolver
    	List<InflacionMes> result = new ArrayList<>();
    	
    	// Se iteran todos los meses pedidos (esten o no en la base)
    	// Se inicia "desde" y se incrementa de a un mes hasta que el actual sobrepase a "hasta"
    	YearMonth mes = periodo.getDesde();
    	while(!mes.isAfter(periodo.getHasta())) {
    		// Si el mes estaba persistido, se agrega ese.
    		// Si no, se agrega uno con indice 0.
    		result.add(filterByMes(persistidos, mes).orElse(new InflacionMes(moneda, mes)));
    		
    		// Se suma un mes
    		mes = mes.plusMonths(1);
    	}
    	
    	return result;
    }
    
    /**
     * Obtiene la instancia persistida de la moneda, ya sea usando el id indicado o, si es null,
     * buscando la moneda default.
     * 
     * @param monedaId id de moneda a buscar o <code>null</code> para usar el default.
     * @return la instancia persistida de la moneda
     * 
     * @throws EntityNotFoundException si no existe moneda con ese id o no hay default
     */
    private Moneda getMonedaOrThrow(Long monedaId) throws EntityNotFoundException {
    	Optional<Moneda> result = monedaId != null ? 
    			// Si se indico un id se busca esa moneda
    			monedaDao.findById(monedaId) : 

    			// Si no, se busca la default
				monedaDao.findAll().stream().filter(Moneda::isDefault).findFirst(); 
    	
    	// Si no se encontro, lanzar error
		return result.orElseThrow(() -> new EntityNotFoundException("No se encontro la moneda deseada"));
    }
    
    /**
     * Genera el filtro de periodo mensual a partir de los parametros.
     * <br>Completa los datos en caso de que alguna cota llegue vacia.
     * <p>
     * Si hay fechas definidas, usa esas, pero si no:
     * <ul>
     * <li>Si esta dentro de un ejercicio, se usa las fechas de inicio y/o fin del ejercicio.</li>
     * <li>Si no, se busca hasta el mes actual y desde un anio atras que "hasta"</li>
     * </ul>
     * <p>
     * @param param filtro a completar
     */
    private PeriodoMensual buildPeriodo(Periodo param) {
    	// Crea el periodo mensual a partir de los parametros
    	PeriodoMensual periodo = new PeriodoMensual(param);
    	
    	
    	Ejercicio ej = session.getEjercicio().orElse(null);

    	// Si no se definio "hasta", se usa la fecha de fin del ejercicio o la actual
    	if(periodo.getHasta() == null) periodo.setHasta(ej != null ? YearMonth.from(ej.getFinalizacion()) : YearMonth.now());
    	
    	// Si no se indico "desde", se usa el incio del ejercicio o un anio antes que "hasta" 
    	if(periodo.getDesde() == null) periodo.setDesde(ej != null ? YearMonth.from(ej.getInicio()) : periodo.getHasta().minusYears(1));
    	
    	return periodo;
    }
    
    /**
     * Busca un indice de la lista en base al mes
     * @param items
     * @param mes
     * @return
     */
    private Optional<InflacionMes> filterByMes(List<InflacionMes> items, YearMonth mes) {
    	for(InflacionMes item : items) {
    		if(item.getYearMonth().equals(mes)) return Optional.of(item);
    	}
    	return Optional.empty();
    }

	@Override
	protected JpaRepository<InflacionMes, Long> getRepo() {
		return dao;
	}

	@Override
	protected Class<InflacionMes> getEntityClass() {
		return InflacionMes.class;
	}

	@Override
	protected void updateItem(InflacionMes existing, InflacionMes modified) {
		// Solo se permite modificar el indice
    	if(modified.getIndice() != null) existing.setIndice(modified.getIndice());
	}
	
	@Override
	protected void beforeGet(Long id) {
	}

	@Override
	protected void beforeCreate(InflacionMes entity) {
		// Se asocia a la moneda persistida o lanza error si no existe
		entity.setMoneda(this.getMonedaOrThrow(entity.getMoneda().getId()));
	}

	@Override
	protected void beforeDelete(InflacionMes entity) {
	}

	@Override
	protected InflacionMes toDto(InflacionMes model) {
		return model;
	}

	@Override
	protected List<InflacionMes> toDto(List<InflacionMes> models) {
		return models;
	}
	
	/**
	 * Filtro para busqueda de indices
	 */
	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class InflacionFilter extends Periodo {
		
		@NotNull
		@QueryParam("moneda")
		private Long monedaId;
	}
}