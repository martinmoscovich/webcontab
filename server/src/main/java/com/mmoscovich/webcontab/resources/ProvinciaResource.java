package com.mmoscovich.webcontab.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mmoscovich.webcontab.dao.ProvinciaRepository;
import com.mmoscovich.webcontab.model.Provincia;

// TODO Se usara?
@Component
@Path("/provincias")
@Produces(MediaType.APPLICATION_JSON)
public class ProvinciaResource extends SimpleResource<Provincia, Provincia> {

	@Inject
	private ProvinciaRepository dao;
	
    @GET
    public List<Provincia> list() {
        return dao.findAll();
    }
    

	@Override
	protected JpaRepository<Provincia, Long> getRepo() {
		return dao;
	}

	@Override
	protected Class<Provincia> getEntityClass() {
		// TODO Auto-generated method stub
		return Provincia.class;
	}

	@Override
	protected void updateItem(Provincia existing, Provincia modified) {
		if(!StringUtils.isEmpty(modified.getNombre())) existing.setNombre(modified.getNombre());
    	if(modified.getPercepcion() != null) existing.setPercepcion(modified.getPercepcion());
	}

	@Override
	protected void beforeGet(Long id) {
	}

	@Override
	protected void beforeCreate(Provincia entity) {
	}

	@Override
	protected void beforeDelete(Provincia entity) {
	}

	@Override
	protected Provincia toDto(Provincia model) {
		return model;
	}


	@Override
	protected List<Provincia> toDto(List<Provincia> models) {
		return models;
	}
}