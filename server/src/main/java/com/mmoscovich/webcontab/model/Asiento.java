package com.mmoscovich.webcontab.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import com.mmoscovich.webcontab.exception.EjercicioFechaInvalidaException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.util.ValidationUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa un Asiento
 */
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
// Ejercicio y numero es la clave unica
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"ejercicio_id", "numero"})})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Asiento extends PersistentEntity {
	
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Ejercicio ejercicio;
	
	@Min(1)
	@Column(nullable = false)
	private Short numero;

	@NotNull
	private LocalDate fecha;

	@Length(max = 70)
	private String detalle;

	@NotEmpty
	@Valid
	@OrderBy("orden")
	@OneToMany(mappedBy = "asiento", fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH})
	private List<Imputacion> imputaciones = new ArrayList<>();

	public Asiento(Ejercicio ejercicio, Short numero, LocalDate fecha, String detalle, List<Imputacion> imputaciones) {
		this.ejercicio = ejercicio;
		this.numero = numero;
		this.fecha = fecha;
		this.detalle = detalle;
		if (imputaciones != null) this.imputaciones = imputaciones;
	}
	
	/**
	 * Indica si este asiento pertenece al ejercicio especificado
	 */
	public boolean perteneceA(Ejercicio ejercicio) {
		return ejercicio.getId().equals(this.ejercicio.getId());
	}

	/**
	 * Agrega una imputacion al asiento, realizando la asociacion bidireccional.
	 */
	public void agregarImputacion(Imputacion imputacion) {
		this.imputaciones.add(imputacion);
		imputacion.setAsiento(this);
	}
	
	/** 
	 * Agrega una lista de imputaciones al asiento, realizando las asociaciones bidireccionales.
	 */
	public void agregarImputaciones(Collection<Imputacion> imputaciones) {
		for(Imputacion i : imputaciones) this.agregarImputacion(i);
	}

	/**
	 * Valida el asiento.
	 * <p>
	 * Debe pertenecer a un ejercicio y que la fecha este dentro del mismo.<br>
	 * Cada imputacion debe tener una cuenta asociada.<br>
	 * Si se indica, tambien se valida que el saldo del asiento para cada moneda sea cero.
	 * </p>
	 * @param checkSaldo indica si se debe validar el saldo
	 * @throws InvalidRequestException
	 * @throws ConstraintViolationException
	 * @throws EjercicioFechaInvalidaException
	 */
	public void validar(boolean checkImputaciones, boolean checkSaldo) throws InvalidRequestException, ConstraintViolationException, EjercicioFechaInvalidaException {
		ValidationUtils.validate(this);
		
		// Validar el ejercicio
		if(this.getEjercicio() == null) throw new InvalidRequestException("El asiento debe estar asociado a un ejercicio");
		
		// Validar que el asiento este dentro del ejercicio y luego de la fecha de confirmacion
		this.getEjercicio().validateFecha(this.getFecha());

		if(checkImputaciones) {
			for(Imputacion i : this.imputaciones) i.validar();
		}
		
		// Validar que los saldos sean 0
		if(checkSaldo) this.validarSaldo();
	}
	
	/**
	 * Valida que todos los saldos en cada moneda involucrada sean cero.
	 * @throws InvalidRequestException
	 */
	public void validarSaldo() throws InvalidRequestException {
		Map<Long, BigDecimal> saldos = this.getSaldos();
		// Si cualquier saldo no es cero, debe fallar
		if(saldos.entrySet().stream().anyMatch(e -> e.getValue().signum() != 0)) throw new InvalidRequestException("El saldo del asiento debe ser cero");
	}
	
	/**
	 * Calcula los saldos para cada moneda involucrada
	 * @return un mapa con el id de la moneda y su saldo.
	 */
	@Transient
	public Map<Long, BigDecimal> getSaldos() {
		Map<Long, BigDecimal> saldos = new HashMap<>();
		for(Imputacion i : this.imputaciones) {
			Long monedaId = i.getCuenta().getMoneda().getId();
			BigDecimal importe = i.getImporte();
			
			// Si ya habia saldo para esa moneda, se suma. Si no, se considera que era cero.
			saldos.compute(monedaId, (k,v) -> (v == null) ? importe : v.add(importe));
		}
		return saldos;
	}
}