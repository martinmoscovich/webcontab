package com.mmoscovich.webcontab.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.json.OnlyIdSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa una imputacion dentro de un asiento.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
// Se crea un indice para orden
@Table(indexes = {@Index(columnList = "asiento_id, orden")})
public class Imputacion extends PersistentEntity {
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JsonSerialize(using = OnlyIdSerializer.class)
	private Asiento asiento;
	
	/** Orden dentro del asiento (empieza en 1) */
	@Positive
	private Short orden;
	
    @NotNull
	@ManyToOne(fetch = FetchType.LAZY)
    private Cuenta cuenta;

    @NotNull
    @Column(precision = 15, scale = 2)
    private BigDecimal importe;
    
    @NotEmpty
    @Length(max = 50)
    private String detalle;

    public Imputacion(Cuenta cuenta, BigDecimal importe, String detalle) {
        this.cuenta = cuenta;
        this.importe = importe;
        this.detalle = detalle;
    }
    
    /**
     * Valida que la imputacion tenga cuenta, importe != 0 y detalle
     * @throws InvalidRequestException si no es valida
     */
    public void validar() throws InvalidRequestException {
    	if(this.cuenta == null || this.cuenta.getId() == null || this.cuenta.getId() < 0) throw new InvalidRequestException("La cuenta de la imputaccion no es valida");
    	if(this.importe == null || this.importe.signum() == 0) throw new InvalidRequestException("El importe no puede ser 0");
    	if(StringUtils.isEmpty(this.detalle)) throw new InvalidRequestException("La imputacion debe tener detalle");
    }
    
    /**
     * Crea una imputacion para equilibrar esta (con el importe inverso).
     * 
     * @param detalle detalle de la nueva imputacion.
     * @return
     */
    public Imputacion crearInversa(String detalle) {
    	return new Imputacion(this.cuenta, this.importe.negate(), detalle);
    }
    
    /**
     * Indica si esta imputacion pertenece al asiento especificado
     * @param asiento
     * @return
     */
    public boolean perteneceA(Asiento asiento) {
    	return this.asiento.getId().equals(asiento.getId());
    }
    
    /** Indica si la imputacion es parte del DEBE */
    public boolean esDebe() {
    	return this.importe != null && this.importe.signum() >= 0;
    }
    
    /** Indica si la imputacion es parte del HABER */
    public boolean esHaber() {
    	return this.importe != null && this.importe.signum() < 0;
    }
    
    @Override
    public String toString() {
    	String id = this.getId() != null ? "(id: " + this.getId().toString() + ")" : "nueva";
    	return String.format("Imputacion %s de %s con detalle '%s' en la cuenta %s", id, this.importe, this.detalle, this.cuenta.getId());
    }
}