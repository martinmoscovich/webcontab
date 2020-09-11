package com.mmoscovich.webcontab.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// TODO Se usara?
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Proveedor extends PersistentEntity {
    
	@NotEmpty
    @Length(max = 50)
	private String nombre;
    
    @NotEmpty
    @Length(max = 12)
    private String cuil;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cuenta cuentaAsociada;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Cuenta cuentaRubro;

    private Integer ivaDefault;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Provincia provincia;
}