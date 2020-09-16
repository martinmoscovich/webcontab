package com.mmoscovich.webcontab.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import com.mmoscovich.webcontab.util.CreateValidation;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa una moneda (peso, dolar, etc)
 *
 */
@Entity
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Moneda extends PersistentEntity {
	
    @NotNull
    @Column(unique = true)
    @Length(min = 1, max = 10, groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    private String nombre;
    
    @NotNull
    @Column(unique = true)
    @Length(min = 1, max = 3, groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    private String simbolo;
    
    @NotNull
    @Column(unique = true)
    @Length(min = 1, max = 3, groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    private String codigo;
    
    /** 
     * Indica si es la moneda default.
     * <p>Esto genera que toda nueva cuenta se asocie a esta por defecto.</p>
     */
    @NotNull
    @Column(unique = true)
    private boolean isDefault;
    
    /** Indica que la moneda se debe ajustar por inflacion */
    @NotNull
    private boolean ajustable;
    
    public Moneda(String nombre, String simbolo, String codigo, boolean isDefault, boolean ajustable) {
    	this.nombre = nombre;
    	this.simbolo = simbolo;
    	this.codigo = codigo;
    	this.isDefault = isDefault;
    	this.ajustable = ajustable;
    }
    
}