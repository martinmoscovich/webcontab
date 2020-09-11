package com.mmoscovich.webcontab.services;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.CuentaBaseRepository;
import com.mmoscovich.webcontab.dao.CuentaRepository;
import com.mmoscovich.webcontab.dao.MonedaRepository;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.CuentaUtilizadaException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Moneda;
import com.mmoscovich.webcontab.model.Organizacion;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de Cuentas imputables
 */
@Service
@Slf4j
public class CuentaService extends CuentaBaseService<Cuenta> {

	@Inject
	private CuentaRepository dao;
	
	@Inject
	private CuentaBaseRepository baseDao;
	
	@Inject
	private CategoriaService catService;
	
	@Inject
	private ImputacionService imputacionService;
	
	@Inject
	private MonedaRepository monedaDao;
	
	/**
	 * Busca una cuenta por id
	 * @param organizacion organizacion en la que buscar
	 * @param id id a buscar
	 * @return la cuenta con ese id
	 * @throws EntityNotFoundException si no existe cuenta con ese id
	 */
    public Cuenta getByIdOrThrow(Organizacion org, Long id) throws EntityNotFoundException {
    	// Se busca en la base (o en la cache)
    	Cuenta cuenta = dao.findById(id).orElseThrow(() -> new EntityNotFoundException(Cuenta.class, id));
    	
    	// Si existe pero no pertenece a la organizacion, no se lo devuelve
    	if(!cuenta.perteceneA(org)) throw new EntityNotFoundException(Cuenta.class, id);
    	
    	return cuenta;
    }
    
    /**
	 * Obtiene un conjunto de cuentas por ids
	 * @param org organizacion en la que buscar
	 * @param ids ids de las categorias
	 */
	public List<Cuenta> findByIds(Organizacion org, List<Long> ids) {
		return dao.findByIds(org, ids);
	}
    
    /**
     * Busca las cuentas de una organizacion cuya descripcion o alias incluyan la query o cuyo codigo empiece con la query.
     * @param org organizacion en la que buscar
     * @param query texto a buscar
     * @param includeCategories si debe incluir categorias o solo cuentas
     * @return
     */
	public List<? extends CuentaBase> search(Organizacion org, String query, boolean includeCategories) {
		log.debug("Buscando Cuentas{} con query {}", includeCategories ? " y Categorias" : "", query);
    	
		// Se busca categorias con esa descripcion o codigo primero
		List<Categoria> categorias = catService.search(org, query);
		
		// Se busca cuentas o categorias que tienen esa descripcion, codigo o que son hijas de las categorias encontradas
		return includeCategories ? baseDao.searchAllByText(org, query.toLowerCase(), categorias) : baseDao.searchCuentasByText(org, query.toLowerCase(), categorias);
	}
	
	/** 
	 * Obtiene todas las cuentas de la organizacion descendientes de las categorias especificadas.
	 */
	public List<Cuenta> findCuentasDescendientes(Organizacion org, List<Categoria> categorias) {
		return baseDao.getCuentasDescendientes(org, categorias);
	}
	
	/** 
	 * Obtiene todas las cuentas de la organizacion que balancean resultados (una por moneda).
	 * <br>Se usa para la refundicion de cuentas de resultados. 
	 */
	public List<Cuenta> findCuentasQueBalanceanResultados(Organizacion org) {
		return dao.findCuentasQueBalanceanResultados(org);
	}

    /**
     * Crea una cuenta en la organizacion
     * @param org organizacion en la que se crea
     * @param cuenta datos de la cuenta
     * @param numero numero de cuenta dentro del padre
     * @return la cuenta persistida
     * 
     * @throws InvalidRequestException si hay errores de validacion
     * @throws ConflictException si genera un conflicto con cuentas existentes
     * @throws EntityNotFoundException si no existe el padre
     */
    @Transactional
    public Cuenta crear(Organizacion org, Cuenta cuenta, Short numero) throws InvalidRequestException, ConflictException, EntityNotFoundException {
    	// valida y completa los datos faltantes
    	this.validarYCompletar(org, cuenta, numero);
    	
    	// Guarda
    	return dao.save(cuenta);
    }
    
    /**
     * Actualiza una cuenta de la organizacion
     * @param org organizacion donde buscar
     * @param cuenta cambios a actualizar
     * @return la cuenta actualizada
     * 
     * @throws InvalidRequestException si hay errores de validacion
     * @throws EntityNotFoundException si no existe el padre
     * @throws CuentaUtilizadaException si genera un conflicto con cuentas existentes
     */
    @Transactional
    public Cuenta actualizar(Organizacion org, Cuenta cuenta) throws InvalidRequestException, EntityNotFoundException, CuentaUtilizadaException {
    	// Se busca la cuenta persistida
    	Cuenta existing = this.getByIdOrThrow(org, cuenta.getId());
    	
    	// Se actualizan los datos comunes entre categoria y cuenta
    	this.merge(existing, cuenta);
    	
    	// DATOS ESPECIFICOS DE CUENTA
    	
    	// Moneda
    	if(cuenta.getMoneda() != null && !existing.getMoneda().getId().equals(cuenta.getMoneda().getId())) {
    		// Si la cuenta tiene imputaciones, no se puede cambiar la moneda, desbalancearia los asientos
        	if(imputacionService.cuentaTieneImputaciones(existing)) {
        		throw new InvalidRequestException("La cuenta tiene imputaciones, no se puede cambiar la moneda");
        	}
        	existing.setMoneda(monedaDao.findById(cuenta.getMoneda().getId()).orElseThrow(() -> new EntityNotFoundException(Moneda.class, cuenta.getMoneda().getId())));
    	}
    	
    	// Flag de cuenta individual
    	if(cuenta.getIndividual() != null) existing.setIndividual(cuenta.getIndividual());
    	
    	// Flag de cuenta ajustable
    	if(cuenta.getAjustable() != null) existing.setAjustable(cuenta.getAjustable());
    	
    	// Flag de cuenta que balancea resultados 
    	// Se hace luego de actualizar la moneda, ya que debe desactivar la anterior
    	if(cuenta.getBalanceaResultados() != null) {
    		this.validarYCompletarBalanceadoraDeResultados(existing, cuenta.getBalanceaResultados());
    	}
    	
    	return dao.save(existing);
    }
    
    /**
     * Valida y completa los datos de una cuenta nueva
     * @param org organizacion en la que se crea
     * @param cuenta datos de la cuenta
     * @param numero numero de cuenta dentro del padre
     * 
     * @throws InvalidRequestException si hay errores de validacion
     * @throws ConflictException si genera un conflicto con cuentas existentes
     * @throws EntityNotFoundException si no existe el padre
     */
    private void validarYCompletar(Organizacion org, Cuenta cuenta, Short numero) throws InvalidRequestException, ConflictException, EntityNotFoundException {
    	// Se validan y completan los datos comunes entre categorias y cuentas
    	// Deben tener padre
    	super.validarYCompletar(org, cuenta, false, numero);
    	
    	// Valida que su nivel sea 3 o mas (Las categorias raiz no aceptan cuentas, solo otras categorias)
    	if(cuenta.getCategoria().isRaiz()) {
    		throw new InvalidRequestException("Las categorías raíz (" + cuenta.getCategoria().getCodigo() + " - " + cuenta.getCategoria().getDescripcion() + ") solo pueden tener subcategorías, no cuentas (" + cuenta.getDescripcion() + ")");
    	}
    	
    	// Busca la moneda
    	if(cuenta.getMoneda() == null) throw new InvalidRequestException("Debe completar la moneda de la cuenta");
    	cuenta.setMoneda(monedaDao.findById(cuenta.getMoneda().getId()).orElseThrow(() -> new EntityNotFoundException(Moneda.class, cuenta.getMoneda().getId())));

    	if(cuenta.getAjustable() == null) cuenta.setAjustable(false);
    	if(cuenta.getIndividual() == null) cuenta.setIndividual(false);
    	
    	// Determina si esta cuenta balancea resultados
    	boolean balancea = cuenta.getBalanceaResultados() == null ? false : cuenta.getBalanceaResultados(); 
    	// Actualiza el flag de balanceo, validando que la cuenta lo pueda usar y actualizando las otras cuentas si es necesario 
		this.validarYCompletarBalanceadoraDeResultados(cuenta, balancea);
    }
    
    /**
     * Valida que la cuenta pueda tomar el valor requerido del flag "balancea resultados" y actualiza
     * el resto de las cuentas balanceadoras si es necesario.
     * <p>
     * Si el valor es false, siempre es valido y se actualiza solo esta cuenta.
     * <br>Si el valor es true, se comprueba que la cuenta pueda balancear y se le quita el flag a la cuenta que lo tenia.
     * </p>
     * @param cuenta cuenta a actualizar
     * @param valor valor del flag
     */
    private void validarYCompletarBalanceadoraDeResultados(Cuenta cuenta, boolean valor) {
    	// Si se modifico y se puso en false, solo se actualiza
		// Si se puso en true:
		// - Se verfica que se pueda usar esa cuenta
		// - Se desactiva cualquier otra cuenta de la org como balanceadora para esa moneda
		if(valor) {
			this.validarCuentaPuedeBalancear(cuenta);
			this.desactivaCuentaBalanceadora(cuenta.getOrganizacion(), cuenta.getMoneda());
		}
		
		cuenta.setBalanceaResultados(valor);
    }
    
    /**
     * Elimina una cuenta de la organizacion.
     * <p>Solo permite eliminar cuentas que no tengan imputaciones en ningun ejercicio.</p>
     * @param organizacion organizacion en la que buscar
     * @param id id de la cuenta
     * 
     * @throws EntityNotFoundException si no existe cuenta con ese id
     * @throws CuentaUtilizadaException si la cuenta tiene imputaciones en cualquier ejercicio
     */
    @Transactional
    public void eliminar(Organizacion organizacion, Long id) throws EntityNotFoundException, CuentaUtilizadaException {
    	// Se busca la cuenta
    	Cuenta cuenta = this.getByIdOrThrow(organizacion, id);
    	
    	// Si la cuenta tiene imputaciones, no se puede eliminar
    	if(imputacionService.cuentaTieneImputaciones(cuenta)) throw new CuentaUtilizadaException(cuenta);
    	
    	dao.delete(cuenta);
    }
    
    /**
	 * Borra todas las cuentas de una organizacion
	 */
    @Transactional
    public void eliminarTodas(Organizacion organizacion) {
    	dao.deleteByOrganizacion(organizacion);
    }
    
    /**
     * Determina si esta cuenta puede balancear resultados.
     * <br>Para ello NO debe ser descendiente de una categoria de resultados.
     * @param cuenta
     */
    private void validarCuentaPuedeBalancear(Cuenta cuenta) {
    	// Buscar las cuentas de resultados
    	List<Categoria> categorias = this.catService.findCategoriasDeResultados(cuenta.getOrganizacion());
    	
    	// Busca si la cuenta pertenece a alguna de las categorias de resultados
    	if(categorias.stream().anyMatch(cuenta::perteneceA)) {
    		throw new InvalidRequestException("La cuenta no puede balancear resultados ya que pertence a una categoria de resultados");
    	}
    }
    
    /**
     * Desactiva el flag "balancea resultados" de la cuenta con la moneda especificada en la organizacion 
     * @param org organizacion en la que buscar
     * @param moneda moneda que debe tener la cuenta
     */
    private void desactivaCuentaBalanceadora(Organizacion org, Moneda moneda) {
    	// Obtiene las cuentas que balancean resultados
    	this.findCuentasQueBalanceanResultados(org).stream()
    	// Busca la cuenta que tiene la moneda deseada
    	.filter(c -> c.getMoneda().getId().equals(moneda.getId()))
    	.findFirst()
    	// Si existe una cuenta, quitarle el flag de balanceadora
    	.ifPresent(c -> {
    		c.setBalanceaResultados(false);
    		dao.save(c);
    	});
    }
    
//	@Override
//	protected void checkCuentaNoUtilizadaEnEjercicio(Ejercicio ejercicio, Cuenta cuenta) throws CuentaUtilizadaException {
//		if(imputacionService.cuentaTieneImputaciones(cuenta, ejercicio)) {
//			throw new CuentaUtilizadaException(cuenta, ejercicio);
//		}
//	}
}
