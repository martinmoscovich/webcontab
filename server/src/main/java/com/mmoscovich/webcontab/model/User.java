package com.mmoscovich.webcontab.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa un usuario de la aplicacion
 *
 */
@Entity
@NoArgsConstructor
@Data
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EqualsAndHashCode(callSuper = true)
public class User extends PersistentEntity {
    public static enum UserType { USER, ADMIN };
	
	@NotEmpty
    @Column(unique = true)
	@Length(max = 25)
    private String username;
    
    @JsonIgnore
    @Length(max = 100)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private UserType type;
    
    private boolean enabled = true;
    
    @Length(max = 50)
    private String name;
    
    @Column(unique = true)
    @Length(max = 50)
    private String email;
    
    private String avatarUrl;
    
    public User(String username, String password, UserType type, String name, String email, String avatarUrl) {
    	this.username = username;
    	this.password = password;
    	this.type = type;
    	this.name = name;
    	this.email = email;
    	this.avatarUrl = avatarUrl;
    }
}