package com.mmoscovich.webcontab.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mmoscovich.webcontab.model.PersistentEntity;

/**
 * Resource base para ABMs simples, que ademas define un metodo {@link #list()} que trae todos los elementos.
 *
 * @param <T> Tipo de entidad a persistir
 * @param <D> DTO
 */
@Produces(MediaType.APPLICATION_JSON)
public abstract class ListableSimpleResource<T extends PersistentEntity, D> extends SimpleResource<T, D> {

    /**
     * Obtiene una lista de todas las entidades
     */
    @GET
    public List<D> list() {
    	return toDto(getRepo().findAll());
    }
}