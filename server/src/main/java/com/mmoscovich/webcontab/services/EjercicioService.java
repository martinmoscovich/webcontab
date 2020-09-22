package com.mmoscovich.webcontab.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.EjercicioRepository;
import com.mmoscovich.webcontab.dto.Periodo;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.EjercicioFechaInvalidaException;
import com.mmoscovich.webcontab.exception.EjercicioFinalizadoException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Organizacion;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de ejercicios.
 * <p>
 * Maneja la logica para crear, eliminar, cerrar y reabrir ejercicios.
 * </p>
 */
@Slf4j
@Service
public class EjercicioService {

	@Inject
	private EjercicioRepository dao;

	@Inject
	private AsientoService asientoService;

	@Inject
	private EntityManager em;

	/**
	 * Lista los ejercicios de una organizacion.
	 * 
	 * @param org
	 * @return
	 */
	public List<Ejercicio> findByOrganizacion(Organizacion org) {
		return dao.findByOrganizacion(org);
	}

	/**
	 * Obtiene un ejercicio de una organizacion por id
	 * 
	 * @param organizacion
	 * @param id
	 * @return
	 */
	public Ejercicio getByIdOrThrow(Organizacion organizacion, Long id) {
		Ejercicio ejercicio = dao.findById(id).orElseThrow(() -> new EntityNotFoundException(Ejercicio.class, id));
		if(!ejercicio.perteceneA(organizacion)) throw new EntityNotFoundException(Ejercicio.class, id);
		return ejercicio;
	}

	/**
	 * Elimina el ejercicio junto con sus asientos e imputaciones (cascade manual)
	 * 
	 * @param ej
	 */
	@Transactional
	public void eliminar(Organizacion organizacion, Ejercicio ej) {
		ej = this.getByIdOrThrow(organizacion, ej.getId());
		
		log.debug("Eliminando {}", ej);
		
		asientoService.eliminarTodos(ej);
		dao.delete(ej);
		em.flush();
		em.clear();
	}

	/**
	 * Crea un ejercicio en una organizacion.
	 * 
	 * @param org          organizacion donde crear el ejercicio
	 * @param periodo      periodo del ejercicio
	 * @param cerrarUltimo indica si se debe cerrar el ejercicio anterior (pueden
	 *                     estar ambos activos)
	 * @return el nuevo ejercicio
	 * @throws InvalidRequestException
	 */
	@Transactional
	public Ejercicio crearSiguiente(Organizacion org, Periodo periodo, boolean cerrarUltimo) throws InvalidRequestException {
		periodo.validar(true);
		log.info("Creando nuevo ejercicio para organizacion {} y periodo [{}]", org.getNombre(), periodo);

		// Se obtiene el ejercicio que finaliza ultimo en la org
		Optional<Ejercicio> optLast = dao.findEjercicioQueFinalizaUltimo(org);

		Ejercicio ejercicio = new Ejercicio(org, periodo.getDesde(), periodo.getHasta());

		// Si no hay otros ejercicios, simplemente se crea (no hay asiento de apertura)
		if(optLast.isEmpty()) {
			log.info("No hay ejercicios previos, se crea el nuevo con saldos en cero");
			return dao.save(ejercicio);
		}

		Ejercicio last = optLast.get();

		// Si hay anterior, este debe empezar despues
		if(!ejercicio.getInicio().isAfter(last.getFinalizacion())) {
			throw new InvalidRequestException("El ejercicio debe comenzar despues de " + last.getFinalizacion());
		}

		// Si se pidio cerrar el anterior y no esta finalizado, se lo cierra
		if(cerrarUltimo && !last.isFinalizado()) last = this.cerrarEjercicio(last);

		// Se debe crear el asiento de apertura, inverso al de cierre anterior.
		Asiento asientoCierreAnterior = null;

		// Si existe cierre anterior, se lo busca (con imputaciones) para crear el de apertura
		if(last.getAsientoCierreId() != null) asientoCierreAnterior = asientoService.getByIdOrThrow(last, last.getAsientoCierreId(), true);

		// Si no existe cierre anterior, se simula dicho asiento (pero sin modificar ese ejercicio)
		if(asientoCierreAnterior == null) {
			log.info("El ejercicio anterior no esta cerrado, se calcula la apertura sin los asientos requeridos");
			asientoCierreAnterior = asientoService.simularCierre(last);
		}

		// Se crea el ejercicio
		ejercicio = dao.save(ejercicio);

		// Se crea el asiento de apertura usando las imputaciones (reales o simuladas) del cierre anterior.
		Asiento apertura = asientoService.crearApertura(ejercicio, asientoCierreAnterior.getImputaciones());

		// Necesario para poder modificar el ejercicio sin tener error de lock optimista
		em.flush();
		em.refresh(ejercicio);

		// Se actualiza el ejercicio con el asiento
		ejercicio.setAsientoAperturaId(apertura.getId());
		return dao.save(ejercicio);
	}

	/**
	 * Cierra el ejercicio indicado, creando los asientos de refundicion de cuentas
	 * de resultado y de cierre.
	 * 
	 * @param ej
	 * @return el ejercicio cerrado
	 * 
	 * @throws EjercicioFinalizadoException si el ejercicio ya esta cerrado.
	 */
	@Transactional
	public Ejercicio cerrarEjercicio(Ejercicio ej) throws EjercicioFinalizadoException {
		log.info("Cerrando {}", ej);

		// Si ya esta cerrado, lanzar error
		if(ej.isFinalizado()) throw new EjercicioFinalizadoException(ej);

		// Obtiene el prox num de asiento
		Short numAsiento = asientoService.getProximoNumero(ej);

		// Se calcula y guarda el asiento de refundicion de cuentas de resultado
		// Este solo sera necesario si las cuentas de resultado tienen saldo <> 0
		Optional<Asiento> refundicion = asientoService.crearRefundicion(ej, numAsiento);

		// Si hay refundicion, se incrementa el num de asiento y se asocia al ejercicio
		if(refundicion.isPresent()) {
			numAsiento++;
			ej.setAsientoRefundicionId(refundicion.get().getId());
		}

		// Se calcula, persiste y asocia el asiento de cierre
		Asiento cierre = asientoService.crearCierre(ej, numAsiento);
		ej.setAsientoCierreId(cierre.getId());

		// Se indica que el ejercicio esta finalizado
		ej.setFinalizado(true);

		return dao.save(ej);
	}

	/**
	 * Reabre un ejercicio cerrado.
	 * <p>
	 * Para reabrir alcanza con cambiar el estado y eliminar los asientos de
	 * refundicion y cierre.
	 * </p>
	 * 
	 * @param ej
	 * @return el ejercicio reabierto
	 * @throws ConflictException si el ejercicio esta abierto
	 */
	@Transactional
	public Ejercicio reabrirEjercicio(Ejercicio ej) throws ConflictException {
		log.info("Reabriendo {}", ej);
		if(!ej.isFinalizado()) throw new ConflictException("El ejercicio no esta cerrado");

		// Se pone en no finalizado primero para que al eliminar los asientos no lance error.
		ej.setFinalizado(false);

		// Se eliminan los asientos de refundicion y cierre
		log.info("Se eliminan los asientos de refundicion y cierre en el ejercicio {}", ej);
		asientoService.eliminar(ej, Set.of(ej.getAsientoRefundicionId(), ej.getAsientoCierreId()), true);

		ej.setAsientoRefundicionId(null);
		ej.setAsientoCierreId(null);

		em.flush();
		em.clear();

		return dao.save(ej);
	}
	
	/**
	 * Crea o recalcula y actualiza el asiento de ajuste por inflacion del ejercicio.
	 * 
	 * @param ej ejercicio en el cual crear o actualizar el asiento de ajuste
	 * 
	 * @return el ejercicio ajustado
	 */
	@Transactional
	public Ejercicio ajustarPorInflacion(Ejercicio ej) {
		Optional<Asiento> inflacion;
		
		if(ej.getAsientoAjusteId() == null) {
			// Si el ejercicio NO tiene asiento de ajuste, se intenta crea
			inflacion = asientoService.crearAjustePorInflacion(ej);
			
			// Si no se creo el asiento, devuelve el mismo ejercicio sin modificar
			if(inflacion.isEmpty()) return ej;
			
			// Se asigna el id del asiento de ajuste en el ejercicio
			ej.setAsientoAjusteId(inflacion.get().getId());
			
		} else {
			// Si el ejercicio ya tiene asiento se ajuste, se busca
			Asiento ajuste = asientoService.getByIdOrThrow(ej, ej.getAsientoAjusteId(), false);
			
			// Se actualiza
			inflacion = asientoService.actualizarAjustePorInflacion(ajuste);
			
			// Si se elimino, se borra el id del ejercicio
			if(inflacion.isEmpty()) ej.setAsientoAjusteId(null);
		}
		
		return dao.save(ej);
	}

	/**
	 * Renumera los asientos de un ejercicio y establece o actualiza la fecha de confirmacion de asientos del ejercicio.
	 * <p>Se renumeran <b>TODOS</b> los asientos del ejercicio.
	 * <br>La fecha se utiliza para establecer hasta que fecha estan <b>confirmados</b> los asientos.
	 * No se podran crear, modificar ni borrar asientos anteriores o que ocurran en esa fecha.
	 * <br>El ejercicio queda <b>read only</b> hasta dicha fecha.
	 * </p>
	 * <p>
	 * Se renumeran todos los asientos para evitar que algun asiento posterior a
	 * esta fecha genere conflicto con los confirmados. (quizas queda un asiento
	 * posterior con el mismo numero). <br>
	 * Por eso, para evitarlo, renumeran todos, aunque los posteriores pueden ser
	 * modificados.
	 * </p>
	 * 
	 * @param ej                ejercicio a confirmar
	 * @param fechaConfirmacion fecha hasta la cual estan confirmados los asientos
	 *                          (inclusive)
	 * @return el ejercicio actualizado con la nueva fecha de confirmacion
	 * 
	 * @throws EjercicioFechaInvalidaException si la fecha de confirmacion no esta dentro del ejercicio o es anterior a la fecha
	 * de confirmacion actual
	 * 
	 */
	@Transactional
	public Ejercicio confirmarAsientos(Ejercicio ej, LocalDate fechaConfirmacion) throws EjercicioFechaInvalidaException {
		// Valida que la fecha este dentro del ejercicio y sea posterior a la fecha de confirmacion actual
		ej.validateFecha(fechaConfirmacion);
		
		// Se renumeran los asientos
		asientoService.renumerarAsientos(ej);
		
		// Se actualiza la fecha de confirmacion
		log.info("Se actualiza la fecha de confirmacion a {} en el ejericicio {}", fechaConfirmacion, ej);
		ej.setFechaConfirmada(fechaConfirmacion);
		
		return dao.save(ej);
	}
}
