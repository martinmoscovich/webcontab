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
	
	/** Indica si el ejercicio finalizo */
	private boolean finalizado;
	
	/** Id del asiento de apertura */
	private Long asientoAperturaId;
	
	/** Id del asiento de cierre */
	private Long asientoCierreId;
	
	/** Id del asiento de refundicion */
	private Long asientoRefundicionId;
	
	public Ejercicio(Organizacion organizacion, LocalDate inicio, LocalDate finalizacion) {
		this(organizacion, inicio, finalizacion, false, null, null, null);
	}
	
	/** Valida que el ejercicio este activo */
	public void validateActivo() throws EjercicioFinalizadoException {
		if(this.isFinalizado()) throw new EjercicioFinalizadoException(this);
	}
	
	/** Determina si la fecha especificada esta dentro del ejercicio */
	public boolean esFechaValida(LocalDate date) {
		return (!date.isBefore(this.inicio)) && (!date.isAfter(this.finalizacion));
	}
	
	/** Valida que la fecha especificada este dentro del ejercicio */
	public void validateFecha(LocalDate date) throws EjercicioFechaInvalidaException {
		if(!this.esFechaValida(date)) throw new EjercicioFechaInvalidaException(this, date); 
	}
	
	/** Indica si el ejercicio pertenece a la organizacion especificada */
	public boolean perteceneA(Organizacion organizacion) {
		return this.getOrganizacion().getId().equals(organizacion.getId());
	}
}
