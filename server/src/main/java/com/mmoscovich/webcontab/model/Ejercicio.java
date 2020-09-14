package com.mmoscovich.webcontab.model;

import java.time.LocalDate;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.mmoscovich.webcontab.exception.EjercicioFechaInvalidaException;
import com.mmoscovich.webcontab.exception.EjercicioFinalizadoException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa un ejercicio dentro de la organizacion
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Ejercicio extends PersistentEntity {
	
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
	private Organizacion organizacion;
	
	@NotNull
	private LocalDate inicio;
	
	@NotNull
	private LocalDate finalizacion;
	
	/** 
	 * Fecha hasta la cual estan <b>confirmados</b> los asientos <b>(inclusive)</b>.
	 * <br>No se pueden crear ni modificar asientos hasta esa fecha.
	 * Se ejecuta al renumerar y busca evitar que cambien los numeros de los asientos hasta 
	 * esa fecha. 
	 */
	private LocalDate fechaConfirmada;
	
	/** Indica si el ejercicio finalizo */
	private boolean finalizado;
	
	/** Id del asiento de apertura */
	private Long asientoAperturaId;
	
	/** Id del asiento de cierre */
	private Long asientoCierreId;
	
	/** Id del asiento de refundicion */
	private Long asientoRefundicionId;
	
	public Ejercicio(Organizacion organizacion, LocalDate inicio, LocalDate finalizacion) {
		this(organizacion, inicio, finalizacion, null, false, null, null, null);
	}
	
	/** Indica si el ejercicio pertenece a la organizacion especificada */
	public boolean perteceneA(Organizacion organizacion) {
		return this.getOrganizacion().getId().equals(organizacion.getId());
	}
	
	/** Valida que el ejercicio este activo */
	public void validateActivo() throws EjercicioFinalizadoException {
		if(this.isFinalizado()) throw new EjercicioFinalizadoException(this);
	}
	
	/** 
	 * Valida que la fecha especificada este dentro del ejercicio y sea posterior a la confirmada (si existe). 
	 */
	public void validateFecha(LocalDate date) throws EjercicioFechaInvalidaException {
		if(!this.esFechaDentroDelEjercicio(date)) throw EjercicioFechaInvalidaException.fueraDelEjercicio(this, date); 
		if(!this.esFechaHabilitada(date)) throw EjercicioFechaInvalidaException.fechaConfirmada(this, date);
	}
	
	/** Determina si la fecha especificada esta dentro del ejercicio */
	private boolean esFechaDentroDelEjercicio(LocalDate date) {
		return (!date.isBefore(this.inicio)) && (!date.isAfter(this.finalizacion));
	}
	
	/**
	 * Determina si la fecha esta habilitada dentro del ejercicio (para crear o modificar asientos).
	 * Solo se pueden crear o modificar asientos posteriores a la fecha de confirmacion
	 */
	private boolean esFechaHabilitada(LocalDate date) {
		// Si no hay fecha confirmada, siempre estara habilitada
		if(this.fechaConfirmada == null) return true;
		
		// Esta habilitada si es posterior a la fecha confirmada
		return date.isAfter(this.fechaConfirmada);
	}
	
	@Override
	public String toString() {
		return String.format("Ejercicio [%s, periodo: %s -> %s]", organizacion, inicio, finalizacion);  
	}
}
