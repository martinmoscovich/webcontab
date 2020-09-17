	package com.mmoscovich.webcontab.model;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import com.mmoscovich.webcontab.util.CreateValidation;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Clase base para categorias y cuentas.
 * <p>Posee los atributos comunes.</p>
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(region = "Cuenta", usage = CacheConcurrencyStrategy.READ_WRITE)
// Se distinguen por el atributo IMPUTABLE
@DiscriminatorColumn(name = "IMPUTABLE", discriminatorType = DiscriminatorType.INTEGER)
@Table(
	name = "CUENTA",
	// Se indexa por codigo legacy (para importar) y por orden (para ordenar)
	indexes =  { @Index(columnList = "legacyCodigo"), @Index(columnList = "orden")},
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"organizacion_id", "categoria_id", "descripcion"}),
		@UniqueConstraint(columnNames = {"organizacion_id", "codigo"}),
		@UniqueConstraint(columnNames = {"organizacion_id", "orden"}),
		@UniqueConstraint(columnNames = {"organizacion_id", "alias"})
	}
)
public abstract class CuentaBase extends PersistentEntity {
	
	/** El plan de cuentas pertenece a una organizacion */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Organizacion organizacion;
	
	@NotNull
	@Min(1)
    private Short numero;
	
	@NotEmpty
	@Length(max = 35) // Alcanza para 8 niveles al max
	private String codigo;
    
	/** 
	 * Campo que permite ordenar las cuentas jerarquicamente.
	 * Similar al codigo, pero separa con "/" en lugar de "." y tiene
	 * padding de ceros para que un determinado nivel siempre tenga la misma cantidad de digitos.
	 * <p>35 caracteres alcanza para 8 niveles al max</p>
	 */
	@Length(max = 35)
	private String orden;
    
	/** Codigo original en WinContab */
	@Column(length = 12)
    private String legacyCodigo;
    
	/** Alias que permite buscar la cuenta por otro nombre (por ej el codigo legacy) */
	@Length(max = 20)
    private String alias;

    @NotNull
    @Length(min = 1, max = 50, groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    private String descripcion;
    
    // TODO: Pasar a Cuenta
    @NotNull
    private Boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Categoria categoria;
    
    /** Asigna la categoria, actualizando el codigo y el orden (estan todos asociados) */
    public void setCategoria(Categoria categoria) {
    	this.categoria = categoria;
    	if(StringUtils.isEmpty(this.codigo)) return;
    	
    	// Si no hay parent o el parent no tiene codigo (no deberia pasar), no hay prefijo
    	final String prefix = (categoria == null || StringUtils.isEmpty(categoria.getCodigo()) ? "" : categoria.getCodigo() + ".");
    	
		this.setCodigo(prefix + numero);
    }
    
    /** Asigna el codigo, actualizando el orden */
    public void setCodigo(String codigo) {
    	this.codigo = codigo;
    	if(StringUtils.isEmpty(this.codigo)) return;
    	
    	// Calcula el orden a partir del codigo
    	// Hace pad de ceros. Los primeros 2 niveles usan 2 digitos, los demas 4
    	String[] parts = this.codigo.split("\\.");
    	
    	this.orden = IntStream.range(0, parts.length)
    		.mapToObj(i -> StringUtils.leftPad(parts[i], (i < 2) ? 2 : 4, "0"))
    		.collect(Collectors.joining("/"));
    }

    public CuentaBase(Organizacion org, Short numero, String descripcion) {
    	this.organizacion = org;
    	this.numero = numero;
        this.setCodigo(numero != null ? numero.toString() : null);
        this.descripcion = descripcion;
        this.activa = true;
    }
    public CuentaBase(Organizacion org, Short numero, String descripcion, Categoria categoria) {
        this(org,numero,descripcion);
        this.setCategoria(categoria);
    }
    
//    /**
//     * Dado el codigo (ej 1.1.2.4), devuelve el numero (en el ejemplo 4).
//     */
//    public Integer getNumero() {
//    	if(StringUtils.isEmpty(this.codigo)) return null;
//    	String[] parts = this.codigo.split("\\.");
//    	return Integer.parseInt(parts[parts.length - 1]);
//    }
    
    /** Indica si esta categoria o cuenta pertenece a la organizacion especificada */
    public boolean perteceneA(Organizacion organizacion) {
    	return this.organizacion.getId().equals(organizacion.getId());
    }
    
    /**
     * Determina si la cuenta o categoria pertenece a la categoria especificada
     * @param categoria
     * @return
     */
    public boolean perteneceA(Categoria categoria) {
    	if(categoria.getId() == this.getId()) return false;
    	
    	// Para ser descendiente, tiene que empezar con el codigo de esa cat y "."
    	// ej: 1.2.1.4 es descendiente de 1.2 y 1.2.1
    	return this.getCodigo().startsWith(categoria.getCodigo() + ".");
    }
    
}