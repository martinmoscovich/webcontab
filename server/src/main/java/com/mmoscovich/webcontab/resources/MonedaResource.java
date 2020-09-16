package com.mmoscovich.webcontab.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mmoscovich.webcontab.dao.InflacionRepository;
import com.mmoscovich.webcontab.dao.MonedaRepository;
import com.mmoscovich.webcontab.model.Moneda;
import com.mmoscovich.webcontab.services.CuentaService;

/**
 * Resource para ABM de monedas
 *
 */
@Component
@Path("/monedas")
@Produces(MediaType.APPLICATION_JSON)
public class MonedaResource extends ListableSimpleResource<Moneda, Moneda> {

	@Inject
	private MonedaRepository dao;
	
	@Inject
	private CuentaService cuentaService;
	
	@Inject
	private InflacionRepository inflacionDao;

	@Override
	protected JpaRepository<Moneda, Long> getRepo() {
		return dao;
	}

	@Override
	protected Class<Moneda> getEntityClass() {
		return Moneda.class;
	}

	@Override
	protected void updateItem(Moneda existing, Moneda modified) {
		// Si se modifico el nombre, codigo o simbolo, actualizarlo
		if(!StringUtils.isEmpty(modified.getNombre())) existing.setNombre(modified.getNombre());
		if(!StringUtils.isEmpty(modified.getCodigo())) existing.setCodigo(modified.getCodigo());
		if(!StringUtils.isEmpty(modified.getSimbolo())) existing.setSimbolo(modified.getSimbolo());

		// Default
		if(modified.isDefault() != existing.isDefault()) {

			// Si se lo pone como default, se quita el anterior
			if(modified.isDefault() && !existing.isDefault()) dao.removeDefault();
			existing.setDefault(modified.isDefault());
		}
		
		// Ajustable
		if(modified.isAjustable() != existing.isAjustable()) {
			
			// Si se pone en false, se debe quitar el flag "ajustable" y "balanceAjustables" en las cuentas de esta moneda
			// y tambien los indices relacionados
			if(!modified.isAjustable()) {
				cuentaService.desactivarCuentasAjustablesYBalanceadora(existing);
				inflacionDao.deleteByMoneda(existing);
			}
			
			existing.setAjustable(modified.isAjustable());
		}
	}
	
	@Override
	protected void beforeGet(Long id) {
	}

	@Override
	protected void beforeCreate(Moneda entity) {
		// Si se esta creando uno default, se quita el anterior
		if(entity.isDefault()) {
			dao.removeDefault();
		}
	}

	@Override
	protected void beforeDelete(Moneda entity) {
	}

	@Override
	protected Moneda toDto(Moneda model) {
		return model;
	}

	@Override
	protected List<Moneda> toDto(List<Moneda> models) {
		return models;
	}

	
}