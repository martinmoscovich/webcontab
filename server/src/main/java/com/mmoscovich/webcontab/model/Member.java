package com.mmoscovich.webcontab.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa la membresia de un usuario en una organizacion.
 * <p>Determina que el usuario puede acceder a dicha organizacion y en que rol.</p>
 *
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
	uniqueConstraints = {
		// Un usuario solo puede tener un rol en la organizacion
		@UniqueConstraint(columnNames = {"organizacion_id", "user_id"}),
	}
)
public class Member extends PersistentEntity {
	
	/** Roles posibles */
	public static enum Rol {
		READ_ONLY, USER, ADMIN
	}
    
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;
    
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Organizacion organizacion;
    
	@Enumerated(EnumType.STRING)
    private Rol rol;
}