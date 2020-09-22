package com.mmoscovich.webcontab.services;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mmoscovich.webcontab.dao.CuentaBaseRepository;
import com.mmoscovich.webcontab.dao.OrganizacionRepository;
import com.mmoscovich.webcontab.dto.mapper.CuentaMapper;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.CuentaUtilizadaException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Organizacion;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio comun para Categorias y Cuentas.
 * <p>Se encarga de validar y completar los atributos comunes a ambas.</p>
 */
@Service
@Slf4j
public abstract class CuentaBaseService<T extends CuentaBase> {

	@Inject
	private CuentaBaseRepository dao;
	
	@Inject
	private CategoriaService categoriaService;
	
	@Inject
	protected AsientoService asientoService;
	
	@Inject
	protected OrganizacionRepository orgDao;
	
	@Inject
	protected CuentaMapper cuentaMapper;
	
//	protected abstract void checkCuentaNoUtilizadaEnEjercicio(Ejercicio ejercicio, T cuenta) throws CuentaUtilizadaException;
	
	/**
	 * Valida los datos de la categoria o cuenta y completa los que faltan (organizacion, padre, codigo). 
	 * @param org organizacion a la que pertecene
	 * @param item datos del item
	 * @param allowRoot indica si se permite que el item no tenga padre 
	 * @param numero numero de categoria o cuenta dentro del padre
	 * @throws InvalidRequestException si no tiene padre y no se permite que sea root
	 * @throws ConflictException si ya existe cuenta o categoria con ese codigo o con el mismo padre y descripcion.
	 * @throws EntityNotFoundException si el padre no existe en la base de datos
	 */
    protected void validarYCompletar(Organizacion org, T item, boolean allowRoot, Short numero) throws InvalidRequestException, ConflictException, EntityNotFoundException {
    	// Se asigna la organizacion a la que pertenece la cuenta
    	item.setOrganizacion(org);
    	
    	// Buscar la categoria
    	if(item.getCategoria() != null && item.getCategoria().getId() != null) {
    		// Asocia el padre persistido
    		item.setCategoria(categoriaService.getByIdOrThrow(org, item.getCategoria().getId()));
    	
    		// Se calcula el codigo a partir del codigo del padre y el numero
    		item.setCodigo(item.getCategoria().getCodigo() + "." + numero);

    	} else if(allowRoot) {
    		// Si es una categoria root, su codigo es el numero
    		item.setCodigo(numero.toString());
    		
    		// No tiene categoria (el mapper pone una con id = null)
    		item.setCategoria(null);
    		
    	} else {
    		throw new InvalidRequestException("No se especifico la categoria padre");
    	}
    	
    	// Valida que no sea un duplicado
    	this.validarDuplicados(item, item.getDescripcion(), true);
    	
    	// Empiezan activas y el ID lo asigna la DB
    	item.setId(null);
    	item.setActiva(true);
    }
    
    /**
     * Valida que la categoria o cuenta no sea un duplicado.
     * <p>No se permiten dos categorias/cuentas con el mismo codigo o con el mismo [padre-descripcion].</p>
     * @param cuenta cuenta cuyo duplicado se busca
     * @param descripcion descripcion a buscar
     * @param checkCodigo si es true, tambien busca si ya existe el codigo de la cuenta o categoria.
     */
    protected void validarDuplicados(CuentaBase cuenta, String descripcion, boolean checkCodigo) {
    	// Comprobar que no se repita el codigo y que no haya un item (cuenta o categoria) con esa descripcion en la misma categoria
    	Optional<Long> existingId = dao.findDuplicado(cuenta, descripcion, checkCodigo);
    	if(existingId.isPresent()) throw new ConflictException("Ya existe una cuenta o categoria con ese codigo o la categoria ya tiene una cuenta/categoria con esa descripcion");
    	
    }
    
    /**
     * Actualiza el item existente con los datos del nuevo.
     * <p>Solo se permite actualizar algunos campos</p>
     * @param existing categoria o cuenta existente en la base de datos
     * @param changes cambios a actualizar
     * @throws InvalidRequestException
     * @throws EntityNotFoundException
     * @throws CuentaUtilizadaException
     */
    protected void merge(T existing, T changes) throws InvalidRequestException, EntityNotFoundException, CuentaUtilizadaException {

    	// Por ahora solo se puede modificar descripcion, alias y si esta activa
    	
    	// Descripcion
    	if(!StringUtils.isEmpty(changes.getDescripcion()) && !changes.getDescripcion().equals(existing.getDescripcion())) {
    		
    		// Como se actualiza la descripcion, hay que ver que no haya colision con otros
    		this.validarDuplicados(existing, changes.getDescripcion(), false);

    		existing.setDescripcion(changes.getDescripcion());
    	}
    	
    	// Alias
    	if(!StringUtils.isEmpty(changes.getAlias()) && !changes.getAlias().equals(existing.getAlias())) {
    		existing.setAlias(changes.getAlias());
    	}
    	
    	// Si se pide modificar "activa" y el valor es disinto al que estaba en la base
    	if(changes.getActiva() != null && !changes.getActiva().equals(existing.getActiva())) {
    		
    		// Si se esta deshabilitando, asegurarse que no tenga imputaciones en el ejercicio 
    		// TODO REVISAR
//    		if(changes.getActiva() == false) this.checkCuentaNoUtilizadaEnEjercicio(ejercicio, existing);
    		
    		// Se modifica
    		existing.setActiva(changes.getActiva());
    	}
    }	
    
    /**
     * Devuelve las categorias o cuentas que son hijas directas de la especificada
     * @param categoria
     * @return
     */
    public List<CuentaBase> findByCategoria(Categoria categoria) {
		return dao.findByCategoriaOrderByOrden(categoria);
	}
}
