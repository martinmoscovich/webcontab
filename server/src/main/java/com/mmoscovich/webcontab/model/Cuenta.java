package com.mmoscovich.webcontab.model;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.mmoscovich.webcontab.services.AsientoService;
import com.mmoscovich.webcontab.util.CreateValidation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Representa una cuenta imputable
 * @author Martin
 *
 */
@Entity
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Cacheable
@DiscriminatorValue("1") // Se diferencia en la base por ser IMPUTABLE = 1
public class Cuenta extends CuentaBase {
	
	/** Moneda asociada a la cuenta */
	@NotNull(groups = {CreateValidation.class})
	@ManyToOne(fetch = FetchType.LAZY)
	private Moneda moneda;
	
	/** 
	 * Indica que es una cuenta individual (por ej un cliente o empleado especifico).
	 * <br>Se usa para poder filtrarla en los reportes (puede haber muchas)
	 */
	@NotNull
	private Boolean individual = false;
	
	/**
	 * Indica si la cuenta debe incluirse en el asiento de ajuste por inflacion.
	 */
	@NotNull
	private Boolean ajustable = false;
	
	/**
	 * Indica que es la cuenta que balancea los resultados para esta moneda.
	 * <br>Se usa para el asiento de Refundicion de cuentas de resultados ({@link AsientoService#crearRefundicion(Ejercicio, Short)})
	 * y en el asiento de cierre ({@link AsientoService#crearCierre(Ejercicio, Short)}).
	 */
	@NotNull
	private Boolean balanceaResultados = false;
	
	/**
	 * Indica que es la cuenta que balancea las cuentas ajustables por inflacion para esta moneda.
	 * <br>Se usa para el asiento de Ajuste por inflacion ({@link AsientoService#crearAjustePorInflacion(Ejercicio, Short)}).
	 */
	@NotNull
	private Boolean balanceaAjustables = false;
	
	public Cuenta(Organizacion org, Short numero, String descripcion, boolean individual, boolean ajustable, Moneda moneda) {
        this(org, numero, descripcion, null, individual, ajustable, moneda);
    }
    public Cuenta(Organizacion org, Short numero, String descripcion, Categoria categoria, boolean individual, boolean ajustable, Moneda moneda) {
        super(org, numero, descripcion, categoria);
        this.moneda = moneda;
        this.individual = individual;
        this.ajustable = ajustable;
    }
    
}