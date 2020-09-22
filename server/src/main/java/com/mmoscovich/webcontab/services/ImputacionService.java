package com.mmoscovich.webcontab.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.CuentaRepository;
import com.mmoscovich.webcontab.dao.ImputacionRepository;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;
import com.mmoscovich.webcontab.model.Moneda;
import com.mmoscovich.webcontab.model.Moneda.MonedaDefaultIdComparator;
import com.mmoscovich.webcontab.util.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de imputaciones
 */
@Slf4j
@Service
public class ImputacionService {
	/** Comparador de monedas para ordenar las imputaciones */
	private static final MonedaDefaultIdComparator MONEDA_COMPARATOR = new MonedaDefaultIdComparator();

	@Inject
	private CuentaRepository cuentaDao;
	
	@Inject 
	private ImputacionRepository dao;
	
	/** 
	 * Busca las imputaciones de una serie de asientos.
	 * Util cuando ya se tiene los asientos y se quieren cargar sus imputaciones
	 * 
	 * @return lista plana de imputaciones de todos los asientos pedidos 
	 */
	public List<Imputacion> findByAsientos(Collection<Asiento> asientos) {
		return dao.findByAsientos(asientos);
	}
	
	/**
	 * Guarda una lista de imputaciones
	 */
	@Transactional
	public void persistir(List<Imputacion> imputaciones) {
		if(imputaciones.isEmpty()) return; 
		
		this.ordenarImputacionesPorMoneda(imputaciones);
		dao.saveAll(imputaciones);
	}

	/**
	 * Elimina un conjunto de imputaciones en una sola query
	 */
	@Transactional
	public void eliminar(List<Imputacion> imputaciones) {
		if(!imputaciones.isEmpty()) dao.deleteByIds(CollectionUtils.toIdList(imputaciones));
	}
	
	/**
	 * Elimina todas las imputaciones de un asiento en una sola query.
	 * <p>Se utiliza al eliminar uno o mas asientos.</p>
	 */
	@Transactional
	public void eliminarByAsiento(Asiento asiento) {
		dao.deleteByAsiento(asiento);
	}
	
	/**
	 * Elimina todas las imputaciones de <b>TODOS</b> los asientos de un ejercicio en una sola query.
	 * <p>Se utiliza al eliminar un ejercicio.</p>
	 */
	@Transactional
	public void eliminarByEjercicio(Ejercicio ejercicio) {
		log.debug("Eliminando todos las imputaciones del ejercicio con id {}: {}", ejercicio.getId(), ejercicio);
		
		dao.deleteByEjercicio(ejercicio);
	}
	
	/**
	 * Actualiza las imputaciones de un asiento.
	 * <br>Eso puede incluir agregar nuevos y actualizar y/o borrar existentes
	 * @param asiento asiento a actualizar
	 * @param nuevasImputaciones imputaciones que deben quedar en el asiento
	 * @return las imputaciones que se deben borrar
	 * @throws InvalidRequestException
	 * @throws EntityNotFoundException
	 */
	public List<Imputacion> actualizarImputaciones(Asiento asiento, Collection<Imputacion> nuevasImputaciones) throws InvalidRequestException, EntityNotFoundException {
		List<Imputacion> imputacionesABorrar = new ArrayList<>();
		
		// Se dejan en el asiento las imputaciones que no se deben borrar (las que se mantuvieron o actualizaron)
		asiento.getImputaciones().removeIf(existing -> {
			Imputacion nueva = CollectionUtils.find(nuevasImputaciones, existing);
			if(nueva != null) {
				// Si la imputacion existe en el asiento y en las nuevas, se mantiene o actualiza
				this.actualizarImputacion(existing, nueva);
				return false;
			}
			// Si la imputacion no esta en la nueva lista, 
			// se quita del asiento y se agrega a la imputaciones a eliminar
			imputacionesABorrar.add(existing);
			return true;
		});
		
		// Se agregan las nuevas imputaciones al asiento
		for(Imputacion nueva : nuevasImputaciones) {
			if(CollectionUtils.find(asiento.getImputaciones(), nueva) == null) {
				// Se asocia y se asigna al asiento
				this.asociarImputacion(nueva, asiento);
				asiento.getImputaciones().add(nueva);
			}
		}
		
		// Se devuelve la lista de imputaciones a borrar
		// Las demas estan contenidas en el asiento
		return imputacionesABorrar;
	}

	/**
	 * Asigna el asiento a la imputacion y busca y asocia la Cuenta persistida.
	 * @param imputacion imputacion a asociar
	 * @param asiento asiento al que pertenece
	 * @throws InvalidRequestException si no existe en la base la cuenta asociada
	 */
	public void asociarImputacion(Imputacion imputacion, Asiento asiento) throws InvalidRequestException {
		// Se asocia el asiento padre
		imputacion.setAsiento(asiento);

		// Se busca la cuenta o se lanza excepcion
		Cuenta cuenta = cuentaDao.findById(imputacion.getCuenta().getId())
				 .orElseThrow(() -> new InvalidRequestException("La cuenta a imputar (id: " + imputacion.getCuenta().getId() + ") no existe"));
		
		// Se asocia la cuenta
		imputacion.setCuenta(cuenta);
	}

	/**
	 * Actualiza una imputacion con nuevos datos
	 * @param existing imputacion existente
	 * @param nueva nuevos datos
	 * @return la imputacion actualizada (sin persistir)
	 * @throws InvalidRequestException si la cuenta no existe
	 */
	private Imputacion actualizarImputacion(Imputacion existing, Imputacion nueva) throws InvalidRequestException {

		// Se actualizan los datos
		existing.setDetalle(nueva.getDetalle());
		existing.setImporte(nueva.getImporte());
		existing.setCuenta(nueva.getCuenta());

		// Se asocia a la nueva cuenta
		this.asociarImputacion(existing, existing.getAsiento());
		
		return existing;
	}
	
	/** Determina si existen imputaciones para una cuenta determinada en <b>cualquier</b> ejercicio */
	public boolean cuentaTieneImputaciones(Cuenta cuenta) {
		return dao.existsByCuenta(cuenta);
	}
	
	/**
	 * Ordena la lista de imputaciones por:
	 * <ul>
	 * 	<li>Moneda primero, priorizando la default y luego por id de moneda</li>
	 * 	<li>A misma moneda, primero las de DEBE y luego las de HABER</li>
	 * 	<li>A misma moneda y tipo, por orden de ingreso</li>
	 * </ul>
	 * 
	 * @param imputaciones lista a ordenar
	 */
	private void ordenarImputacionesPorMoneda(List<Imputacion> imputaciones) {
		Map<Moneda, List<Imputacion>> monedas = new HashMap<>();
		
		// Se recorren las imputaciones y se crea una lista por cada moneda
		for(Imputacion imp : imputaciones) {
			List<Imputacion> impMoneda = monedas.computeIfAbsent(imp.getCuenta().getMoneda(), m -> new ArrayList<>());
			impMoneda.add(imp);
		}
		
		// Se limpian las imputaciones originales para colocarlas ordenadas
		imputaciones.clear();
		
		// Se ordenan las imputaciones por moneda
		monedas.keySet().stream().sorted(MONEDA_COMPARATOR).forEach(m -> {
			List<Imputacion> impMoneda = monedas.get(m);
			
			// Se ordena por debe-haber
			this.ordenarImputacionesPorDebeHaber(impMoneda);
			
			// Se agrega a la lista final
			imputaciones.addAll(impMoneda);
		});
		
		// Una vez ordenados, se incluye el orden (empezando de 1)
		for(int i = 0; i < imputaciones.size(); i++) {
			imputaciones.get(i).setOrden((short) (i + 1));
		}
	}
	
	/**
	 * Ordena la lista de imputaciones especificada, colocando las de DEBE antes que las de HABER.
	 * <br>El orden entre las similares es el mismo que estaba.
	 * 
	 * @param imputaciones lista a ordenar
	 */
	private void ordenarImputacionesPorDebeHaber(List<Imputacion> imputaciones) {
		List<Imputacion> haber = new ArrayList<>();

		// Deja las que son DEBE y quita las que son HABER pero antes las guarda en otra lista
		imputaciones.removeIf(imputacion -> {
			if(imputacion.esHaber()) return haber.add(imputacion);
			return false;
		});
		
		// Agrega al final las del DEBE
		imputaciones.addAll(haber);
	}
	
//	/** Determina si existen imputaciones para una cuenta determinada en un ejercicio especifico. */
//	public boolean cuentaTieneImputaciones(Cuenta cuenta, Ejercicio ejercicio) {
//		return dao.existsByEjercicioAndCuenta(ejercicio, cuenta);
//	}
	
	/**
	 * Modelo que contiene una pagina de imputaciones y el saldo anterior
	 * 
	 * @author Martin Moscovich
	 *
	 */
	@Data
	@AllArgsConstructor
	public static class ImputacionesCuenta {
		private Page<Imputacion> page;
		private BigDecimal saldoAnterior;
	}
}
