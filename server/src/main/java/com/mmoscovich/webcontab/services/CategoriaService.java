package com.mmoscovich.webcontab.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.CategoriaRepository;
import com.mmoscovich.webcontab.dto.CuentaDTO;
import com.mmoscovich.webcontab.dto.IdNameModel;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.CuentaUtilizadaException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.exception.ServerException;
import com.mmoscovich.webcontab.exporter.ExcelPlanDeCuentasExporter;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de Categorias
 */
@Slf4j
@Service
public class CategoriaService extends CuentaBaseService<Categoria> {
	
//	@Value("#{cacheManager.getCache('Categoria')}")
//	private JCacheCache cache;

	@Inject
	private CategoriaRepository dao;
	
	@Inject
	private EntityManager em;
	
	/**
	 * Busca una categoria por id
	 * @param organizacion organizacion en la que buscar
	 * @param id id a buscar
	 * @return la categoria con ese id
	 * @throws EntityNotFoundException si no existe categoria con ese id
	 */
    public Categoria getByIdOrThrow(Organizacion organizacion, Long id) throws EntityNotFoundException {
    	// Se busca en la base (o en la cache)
    	Categoria cat = dao.findById(id).orElseThrow(() -> new EntityNotFoundException(Categoria.class, id));
    	
    	// Si existe pero no pertenece a la organizacion, no se lo devuelve
    	if(!cat.perteceneA(organizacion)) throw new EntityNotFoundException(Categoria.class, id);
    
    	return cat;
    }
	
	/**
	 * Obtiene un conjunto de categorias por ids
	 * @param org organizacion en la que buscar
	 * @param ids ids de las categorias
	 */
	public List<Categoria> findByIds(Organizacion org, List<Long> ids) {
		return CollectionUtils.filter(this.list(org), cat -> ids.contains(cat.getId()));
	}
	
	/**
	 * Busca las categorias que incluyan la query en su descripcion o cuyo codigo comience con la query.
	 * @param org organizacion en la que buscar
	 * @param query
	 * @return
	 */
	public List<Categoria> search(Organizacion org, String query) {
		log.debug("Buscando Categorias con query {}", query);

		final String queryMinuscula = query.toLowerCase();

//		// Usando query
//		return dao.searchByText(org, queryMinuscula + "%");
		
		// Usando cache y filtro en memoria
		return CollectionUtils.filter(this.list(org), cat -> {
			return cat.getDescripcion().toLowerCase().contains(queryMinuscula) || cat.getCodigo().startsWith(queryMinuscula);
		});
	}
	
	/**
	 * Obtiene y cachea la lista de categorias de una organizacion
	 */
	public Collection<Categoria> list(Organizacion org) {
		return dao.findByOrganizacion(org);
	}
	
	/**
	 * Busca las categorias raiz (sin padre) de una organizacion
	 */
	public List<Categoria> findRoots(Organizacion org) {
//		// Usando Query
//		return dao.findRoots(org);
		
		// Usando cache y filtrando en memoria
		return CollectionUtils.filter(this.list(org), Categoria::isRaiz);
	}
	
	/** Obtiene las cuentas de resultados (con resultado = true) */
	public List<Categoria> findCategoriasDeResultados(Organizacion org) {
		return dao.findCategoriasDeResultados(org);
	}
	
	/**
	 * Genera un Reporte Excel con el Plan de cuentas de la organizacion a partir de la categoria especificada,
	 * o sea con esta y todos sus descendientes
	 * 
	 * @return referencia al archivo excel generado
	 */
	@Transactional(readOnly = true)
	public Path exportarPlan(Organizacion org, Categoria raiz) {
		// Ejecuta la query que obtiene toda la descendencia
    	Stream<CuentaBase> cuentas = raiz == null ? dao.getPlan(org) : dao.getPlan(org, raiz.getCodigo() + ".%");
    	
    	// Si se envio una categoria raiz, se incluye tambien esta en el reporte 
    	if(raiz != null) cuentas = Stream.concat(Stream.of(raiz), cuentas);

    	// Se genera el reporte
    	return new ExcelPlanDeCuentasExporter().exportar(em, org, cuentas);
    }
	
	/**
	 * Crea una nueva categoria
	 * @param org organizacion a la que pertenece
	 * @param categoria datos de la categoria
	 * @param numero numero de categoria dentro del padre
	 * @return la categoria persistida
	 * @throws InvalidRequestException si hay algun error de validacion
	 * @throws ConflictException si la categoria entra en conflicto con alguna existente
	 * @throws EntityNotFoundException si no existe el padre
	 */
    @Transactional
    public Categoria crear(Organizacion org, Categoria categoria, Short numero) throws InvalidRequestException, ConflictException, EntityNotFoundException {
    	// Se validan y completan los datos comunes entre categorias y cuentas
    	// Se permite que sean categorias raiz
    	super.validarYCompletar(org, categoria, true, numero);
    	
    	// DATOS ESPECIFICOS DE CATEGORIA
    	
    	// Flag de categoria de resultado
    	if(categoria.getResultado() == null) categoria.setResultado(false);

    	// Se guarda
		return dao.save(categoria);
    }
    
    /**
     * Actualiza una categoria.
     * @param org organizacion a la que pertenece
     * @param categoria cambios a aplicar
     * @return la categoria actualizada
     * 
     * @throws InvalidRequestException si hay algun error de validacion
     * @throws EntityNotFoundException si no existe el padre
     * @throws CuentaUtilizadaException si se intenta deshabilitar una categoria en uso
     */
    @Transactional
    public Categoria actualizar(Organizacion org, Categoria categoria) throws InvalidRequestException, EntityNotFoundException, CuentaUtilizadaException {
    	// Se busca la categoria persistida
    	Categoria existing = this.getByIdOrThrow(org, categoria.getId());
    	
    	// Se actualizan los datos comunes entre categoria y cuenta
    	this.merge(existing, categoria);
    	
    	// DATOS ESPECIFICOS DE CATEGORIA
    	
    	// Flag de categoria de resultado
    	if(categoria.getResultado() != null) existing.setResultado(categoria.getResultado());
    	
    	// Se guarda
    	return dao.save(existing);
    }
    
    /**
     * Elimina una categoria de una organizacion.
     * <p>Solo permite eliminar categorias que no tengan hijos.</p>
     * 
     * @param organizacion organizacion en la que buscar
     * @param id id de la categoria
     * @throws EntityNotFoundException si no existe la categoria
     * @throws CuentaUtilizadaException si la categoria no se puede eliminar porque esta siendo utilizada
     */
    @Transactional
    public void eliminar(Organizacion organizacion, Long id) throws EntityNotFoundException, CuentaUtilizadaException {
    	Categoria categoria = this.getByIdOrThrow(organizacion, id);
    	
    	// Si hay categorias o cuentas hijas, no se permite eliminar 
    	if(this.findByCategoria(categoria).size() > 0) throw new CuentaUtilizadaException(categoria);
    	
    	dao.delete(categoria);
    }
    
    /**
	 * Borra todas las categorias de una organizacion
	 */
    @Transactional
    public void eliminarTodas(Organizacion organizacion) {
    	dao.removeHierarchyByOrganizacion(organizacion);
    	dao.deleteByOrganizacion(organizacion);
    }
    
    /**
     * Dada una categoria o cuenta, obtiene el path.
     * <br>Este consiste en una lista de items [id, descripcion] con la ruta desde la raiz hasta la cuenta
     * especificada.
     * <p>Se usa para generar la {@link CuentaDTO}.</p>
     * @param cuenta
     * @return
     */
	public List<IdNameModel<Long>> getPath(CuentaBase cuenta) {
		// Cuando el item es raiz, el path es null
 		if(cuenta.getCategoria() == null) return null;

 		Categoria cat = this.dao.findById(cuenta.getCategoria().getId())
 				.orElseThrow(() -> new ServerException("No se encontro una categoria del Path (id=" + cuenta.getCategoria().getId() + ")"));
		
 		// Se obtiene el path del padre recursivamente
 		List<IdNameModel<Long>> r = this.getPath(cat);
 		
		if(r == null) r = new ArrayList<>();
		r.add(new IdNameModel<>(cat.getId(), cat.getDescripcion()));
		return r;
	}
	
//	@Override
//	protected void checkCuentaNoUtilizadaEnEjercicio(Ejercicio ejercicio, Categoria categoria) throws CuentaUtilizadaException {
//		// TODO Revisar
//		throw new CuentaUtilizadaException(categoria);
//		if(asientoService.categoriaTieneImputaciones(categoria, ejercicio)) {
//			throw new CuentaUtilizadaException(categoria, ejercicio);
//		}
//	}
}
