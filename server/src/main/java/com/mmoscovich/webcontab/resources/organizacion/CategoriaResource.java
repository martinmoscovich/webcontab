package com.mmoscovich.webcontab.resources.organizacion;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mmoscovich.webcontab.dto.CuentaDTO;
import com.mmoscovich.webcontab.dto.mapper.CuentaMapper;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.services.CategoriaService;
import com.mmoscovich.webcontab.services.SessionService;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.Data;

@Component
@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
public class CategoriaResource {

	@Inject
	private CategoriaService service;
	
	@Inject
	private CuentaMapper mapper;
	
	@Inject
	private SessionService session;
	
	/**
	 * Obtiene una lista de categorias de una organizacion.
	 * <p>Permite buscar todas las categorias, solo las raices o hacer una busqueda por descripcion, codigo o alias.</p> 
	 * @param params
	 * @return
	 */
	@GET
	public Collection<CuentaDTO> list(@BeanParam CategoriaQuery params) {
		Organizacion org = session.getOrganizacionOrThrow();

		if(params.isSearchQuery()) return mapper.toDtoWithPath(service.search(org, params.getSearchText()));
		
		return mapper.toDto(params.isRoot() ? service.findRoots(org) : service.list(org));
	}
	
	/**
	 * Devuelve una categoria por id
	 * @param id
	 * @param includePath indica si debe incluir el path completo de la categoria.
	 * @return
	 * @throws EntityNotFoundException
	 */
    @GET
    @Path("{id}")
    public CuentaDTO getById(@PathParam("id") @NotNull @Min(1) Long id, @QueryParam("path") boolean includePath) throws EntityNotFoundException {
    	Categoria cat = service.getByIdOrThrow(session.getOrganizacionOrThrow(), id);
    	
    	return includePath ? mapper.toDtoWithPath(cat) : mapper.toDto(cat);
    }
    
    /**
     * Devuelve los hijos directos de una categoria (tanto categorias como cuentas)
     * @param id
     * @return
     * @throws EntityNotFoundException
     */
    @GET
    @Path("{id}/hijos")
    public List<CuentaDTO> findByCategoria(@PathParam("id") @NotNull @Min(1) Long id) throws EntityNotFoundException {
    	return mapper.toDto(service.findByCategoria(service.getByIdOrThrow(session.getOrganizacionOrThrow(), id)));
    }
    
    /**
     * Genera el Plan de cuenta completo en Excel y permite su descarga
     */
    @GET
    @Path("xls")
    public Response exportarPlan() {
    	return this.exportarPlan(null);
    }
    
    /**
     * Genera el Plan de cuenta completo en Excel y permite su descarga, a partir de la categoria especificada.
     * @param id id de la categoria a partir de la cual se genera el plan.
     */
    @GET
    @Path("{id}/xls")
	public Response exportarPlan(@PathParam("id") @NotNull @Min(1) Long id) throws EntityNotFoundException {
    	final Organizacion org = session.getOrganizacionOrThrow();
    	
    	// Se guarda el Excel y se obtiene el path al archivo
    	java.nio.file.Path xls = service.exportarPlan(org, id == null ? null : service.getByIdOrThrow(org, id));
    	
    	// Se calcula el nombre (<org>-Plan)
		String fileName = org.getNombre().replace(".",  "").replace("\\", "") + "-Plan de Cuentas.xlsx";
		
		// Se genera el response de download
		return Response.ok(xls.toFile(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.build();
    }
    
    /**
     * Crea una nueva categoria
     * @param categoria
     * @return categoria persistida
     * @throws InvalidRequestException si hay algun error de validacion
     * @throws ConflictException si hay algun conflicto (como numero repetido)
     * @throws EntityNotFoundException si no existe la categoria padre
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CuentaDTO crear(@Valid CuentaDTO categoria) throws InvalidRequestException, ConflictException, EntityNotFoundException {
    	return mapper.toDto(service.crear(session.getOrganizacionOrThrow(), mapper.toCategoriaModel(categoria), categoria.getNumero()));
    }
    
    /**
     * Actualiza una categoria
     * @param id
     * @param categoria
     * @return categoria actualizada
     * @throws InvalidRequestException si hay algun error de validacion
     * @throws EntityNotFoundException si no existe la categoria a modificar
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public CuentaDTO actualizar(@PathParam("id") @NotNull @Min(1) Long id, @Valid @ConvertGroup(from = Default.class, to=UpdateValidation.class) CuentaDTO categoria) throws InvalidRequestException, EntityNotFoundException {
    	categoria.setId(id);
    	return mapper.toDto(service.actualizar(session.getOrganizacionOrThrow(), mapper.toCategoriaModel(categoria)));
    }
    
    /**
     * Elimina una categoria
     */
    // TODO Se usara?
    @DELETE
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminar(@PathParam("id") @NotNull @Min(1) Long id) {
    	service.eliminar(session.getOrganizacionOrThrow(), id);
    }
    
    /**
     * DTO de los tipos de query posible para buscar categorias
     */
    @Data
    public static class CategoriaQuery {
    	@QueryParam("query") 
    	private String searchText;
    	
    	@QueryParam("root") 
    	private boolean root;
    	
    	
    	public boolean isSearchQuery() {
    		return !StringUtils.isEmpty(this.searchText);
    	}
    }
}