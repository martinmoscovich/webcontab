package com.mmoscovich.webcontab.model;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mmoscovich.webcontab.services.AsientoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Representa una categoria
 */
@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Cacheable
@DiscriminatorValue("0") // Se diferencia en la base por ser IMPUTABLE = 0
public class Categoria extends CuentaBase {
	
	/**
	 * Indica que es una categoria con cuentas de resultado.
	 * <br>Se usa para el asiento de Refundicion de cuentas de resultados ({@link AsientoService#crearRefundicion(Ejercicio, Short)}).
	 */
	@NotNull
	private Boolean resultado = false;

    public Categoria(Organizacion org, Short numero, String descripcion, boolean resultado) {
        this(org, numero, descripcion, null, resultado);
        
    }
    public Categoria(Organizacion org, Short numero, String descripcion, Categoria categoria, boolean resultado) {
        super(org, numero, descripcion, categoria);
        this.resultado = resultado;
    }
    
    @JsonIgnore
    public boolean isRaiz() {
    	return this.getCategoria() == null;
    }
}