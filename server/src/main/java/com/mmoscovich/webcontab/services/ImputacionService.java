package com.mmoscovich.webcontab.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.mmoscovich.webcontab.util.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Servicio de imputaciones
 */
@Service
public class ImputacionService {

	@Inject
	private CuentaRepository cuentaDao;
	
	@Inject 
	private ImputacionRepository dao;
	
	/**
	 * Guarda una lista de imputaciones
	 */
	@Transactional
	public void persistir(List<Imputacion> imputaciones) {
		if(!imputaciones.isEmpty()) dao.saveAll(imputaciones);
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

		// Se asocia la cuenta
		imputacion.setCuenta(
				cuentaDao.findById(imputacion.getCuenta().getId()).orElseThrow(() -> new InvalidRequestException(
						"La cuenta a imputar (id: " + imputacion.getCuenta().getId() + ") no existe")));
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
//		return dao.save(existing);
	}
	
	/** Determina si existen imputaciones para una cuenta determinada en <b>cualquier</b> ejercicio */
	public boolean cuentaTieneImputaciones(Cuenta cuenta) {
		return dao.existsByCuenta(cuenta);
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

	public List<Imputacion> findByAsientos(Collection<Asiento> asientos) {
		return dao.findByAsientos(asientos);
	}

}
