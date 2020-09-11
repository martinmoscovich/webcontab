package com.mmoscovich.webcontab.model;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mmoscovich.webcontab.util.JpaUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Clase base para todas las entidades JPA.
 * <br>Posee tres campos basicos: Id (Long), creationDate y updateDate 
 *
 * @author Martin Moscovich
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
public abstract class PersistentEntity {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@JsonIgnore
	@CreatedDate
	@Column(nullable = false, updatable = false)
    private Date creationDate;
    
	@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @CreatedBy
    private User creationUser;
	
	// Se usa date porque Instant generaba errores de locking 
	@JsonIgnore
    @LastModifiedDate
    @Version
    private Date updateDate;
    
	@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @LastModifiedBy
    private User updateUser;

    /**
	 * @return el id de la entidad
	 */
	public final Long getId() {
		// Hack para que se pueda pedir el ID sin cargar el objeto
		// Hibernate lo puede hacer pero solo si se anotan los getters en lugar de los atributos
        if(JpaUtils.isProxy(this)) 
        	return JpaUtils.getId(this);
        else 
        	return this.id;
    }

}
