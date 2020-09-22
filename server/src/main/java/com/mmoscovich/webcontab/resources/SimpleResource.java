package com.mmoscovich.webcontab.resources;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.PersistentEntity;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.extern.slf4j.Slf4j;

/**
 * Resource base para ABMs simples.
 *
 * @param <T> Tipo de entidad a persistir
 * @param <D> DTO
 */
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public abstract class SimpleResource<T extends PersistentEntity, D> {

    protected abstract JpaRepository<T, Long> getRepo();
    protected abstract Class<T> getEntityClass();

    /** Metodo que se llama antes del get by id */
    protected abstract void beforeGet(Long id);
    
    /**
     * Metodo llamado antes de crear una entidad para permitir validarla y modificarla
     */
    protected abstract void beforeCreate(T entity);
    
    /**
     * Metodo llamado antes de eliminar una entidad para determinar si se permite
     */
    protected abstract void beforeDelete(T entity);
    
    /**
     * Metodo llamado antes de actualizar una entidad para permitir validarla y hacer el merge con la existente
     * @param existing entidad existente en la base
     * @param modified datos nuevos
     */
    protected abstract void updateItem(T existing, T modified);
    protected abstract D toDto(T model);
    protected abstract List<D> toDto(List<T> models);
    
    /**
     * Obtiene una entidad por ID
     * @param id
     * @return
     */
    @GET
    @Path("{id}")
    public D getById(@PathParam("id") @NotNull @Min(1) Long id) throws EntityNotFoundException {
    	return toDto(this.getByIdOrThrow(id));
    }
    
    /**
     * Crea una nueva entidad
     * @param item 
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public D crear(@Valid T item) {
    	// Se asegura que no tenga id
    	item.setId(null);
    	
    	// Se llama al metodo que permite validar y modificar la entidad antes de guardarla
    	this.beforeCreate(item);
    	
    	return toDto(getRepo().save(item));
    }
    
    /**
     * Elimina una entidad
     * @param id
     * @throws EntityNotFoundException
     */
    @DELETE
    @Path("{id}")
    @Transactional
    public void eliminar(@PathParam("id") @NotNull @Min(1) Long id) throws EntityNotFoundException {
    	log.debug("Eliminando {} con id {} de la organizacion {}", this.getEntityClass().getSimpleName(), id);
    	
    	T entity = this.getByIdOrThrow(id);
    	
    	// Se llama al metodo para validar si se permite eliminar
    	this.beforeDelete(entity);
    	
    	getRepo().delete(entity);
    }
    
    /**
     * Actualiza una entidad
     * @param id
     * @param modified
     * @return
     * @throws InvalidRequestException
     * @throws EntityNotFoundException
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public T actualizar(@PathParam("id") @NotNull @Min(1) Long id, @Valid @ConvertGroup(from = Default.class, to=UpdateValidation.class) T modified) throws InvalidRequestException, EntityNotFoundException {
    	T existing = this.getByIdOrThrow(id);
    	
    	// Se llama al metodo que valida y realiza el merge de la existente y la nueva
    	this.updateItem(existing, modified);
    	
    	return getRepo().save(existing);
    }
    
    protected T getByIdOrThrow(Long id) {
    	this.beforeGet(id);
    	return getRepo().findById(id).orElseThrow(() -> new EntityNotFoundException(getEntityClass(), id));
    }
}