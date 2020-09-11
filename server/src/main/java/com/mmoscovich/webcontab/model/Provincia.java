package com.mmoscovich.webcontab.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
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

// TODO Se usara?
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Provincia extends PersistentEntity {
    @NotNull
    @Column(unique = true)
    @Length(min = 1, max = 25, groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    private String nombre;
    
    @NotNull
    @PositiveOrZero(groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    private Float percepcion;
}