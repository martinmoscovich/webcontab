package com.mmoscovich.webcontab.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import com.mmoscovich.webcontab.util.CreateValidation;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa una organizacion
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@EqualsAndHashCode(callSuper = true)
public class Organizacion extends PersistentEntity {
	
	@NotEmpty
	@Column(unique = true)
	@Length(max = 12)
	private String cuit;

	@NotNull
    @Column(unique = true)
    @Length(min = 1, max = 25, groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    private String nombre;
	
	@Override
	public String toString() {
		return String.format("Organizacion [id: %d, nombre: %s, CUIT: %s]", getId(), nombre, cuit); 
	}
    
}
