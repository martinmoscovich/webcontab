package com.mmoscovich.webcontab.model;

import java.util.Objects;

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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa una moneda (peso, dolar, etc)
 *
 */
@Entity
@NoArgsConstructor
@Getter @Setter
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		
		// Si el id esta definido, es el mismo (super.equals) y es Moneda, es true
		if(this.getId() != null) return true;

		// Si el id no esta definido, se usa el codigo
		return Objects.equals(codigo, ((Moneda) obj).codigo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		
		// Si el id esta definido, se usa para el hashcode
		if(this.getId() == null) {
			// Si no esta definido se usa el codigo
			result = prime * result + Objects.hash(codigo);
		}
		return result;
	}
    
	@Override
    public String toString() {
    	return String.format("Moneda %s (%s)", nombre, codigo);
    }
    
	/** 
	 * Compara 2 monedas, cololando la default primero y, en caso de ninguna ser default, ordenando por id 
	 */
	public static final class MonedaDefaultIdComparator extends PersistentEntity.IdComparator<Moneda> { 

		@Override
		public int compare(Moneda m1, Moneda m2) {
			// Si alguna es default, va esa primero
			if(m1.isDefault()) return -1;
			if(m2.isDefault()) return 1;
			
			// Si ninguna es default, se ordena por id
			return super.compare(m1, m2);
		} 
	}
}