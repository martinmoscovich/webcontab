package com.mmoscovich.webcontab.resources.organizacion;

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
import javax.ws.rs.DefaultValue;
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
import com.mmoscovich.webcontab.dto.PageDTO;
import com.mmoscovich.webcontab.dto.PageReq;
import com.mmoscovich.webcontab.dto.mapper.CuentaMapper;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.services.CuentaService;
import com.mmoscovich.webcontab.services.SessionService;
import com.mmoscovich.webcontab.util.CollectionUtils;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.Data;

/**
 * Resource de Cuentas imputables
 */
@Component
@Path("/cuentas")
@Produces(MediaType.APPLICATION_JSON)
public class CuentaResource {

	@Inject
	private SessionService session;
	
	@Inject
	private CuentaService service;
	
	@Inject
	private CuentaMapper mapper;
	
	/**
	 * Provee dos funcionalidades (segun los query param):
	 * <ul>
	 * <li>Busca por un texto en el codigo, descripcion o alias (para autocomplete) y devuelve una pagina de cuentas o de cuentas y categorias.</li>
	 * <li>Busca cuentas por id y devuelve la lista (sin paginar).</li> 
	 * </ul>
	 * @param params
	 * @return page o list, segun el tipo de busqueda
	 * @throws InvalidRequestException si no se enviaron los parametros requeridos
	 */
    @GET
    public Response list(@BeanParam CuentaQuery params, @Valid @BeanParam PageReq pageParams) throws InvalidRequestException {
    	Organizacion org = session.getOrganizacionOrThrow();
		
        if(params.isSearchQuery()) {
        	PageDTO<CuentaDTO> result = mapper.toDtoWithPath(service.search(org, params.getSearchText(), params.isIncludeCategories(), pageParams.toPageable()));
        	return Response.ok(result).build();
        }
        if(params.isFindByIds()) {
        	List<CuentaDTO> result = mapper.toDto(service.findByIds(org, params.getIds()));
        	return Response.ok(result).build();
        }
        
        throw new InvalidRequestException("Debe usar el parametro query o el parametro ids");
    }
    
    /**
     * Busca una categoria por id
     * @param id
     * @param includePath indica si se debe incluir el path
     * @return
     * @throws EntityNotFoundException
     */
    @GET
    @Path("{id}")
    public CuentaDTO getById(@PathParam("id") @NotNull @Min(1) Long id, @QueryParam("path") boolean includePath) throws EntityNotFoundException {
    	Cuenta cuenta = service.getByIdOrThrow(session.getOrganizacionOrThrow(), id);
    	
    	return includePath ? mapper.toDtoWithPath(cuenta) : mapper.toDto(cuenta);
    }
    
    /**
     * Crea una nueva categoria
     * @param cuenta
     * @return
     * @throws InvalidRequestException si hay un error de validacion
     * @throws ConflictException si hay un conflicto de negocio (mismo numero o descripcion, etc)
     * @throws EntityNotFoundException si no existe la categoria padre
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CuentaDTO crear(@Valid CuentaDTO cuenta) throws InvalidRequestException, ConflictException, EntityNotFoundException {
    	return mapper.toDto(service.crear(session.getOrganizacionOrThrow(), mapper.toCuentaModel(cuenta), cuenta.getNumero()));
    }
    
    /**
     * Actualiza una cuenta.
     * 
     * @param id
     * @param cuenta
     * @return
     * @throws InvalidRequestException si hay un error de validacion
     * @throws EntityNotFoundException si no existe la cuenta a modificar
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public CuentaDTO actualizar(@PathParam("id") @NotNull @Min(1) Long id, @Valid @ConvertGroup(from = Default.class, to=UpdateValidation.class) CuentaDTO cuenta) throws InvalidRequestException, EntityNotFoundException {
    	cuenta.setId(id);
    	return mapper.toDto(service.actualizar(session.getOrganizacionOrThrow(), mapper.toCuentaModel(cuenta)));
    }
    
    /**
     * Elimina una cuenta
     */
    // TODO Se usara?
    @DELETE
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminar(@PathParam("id") @NotNull @Min(1) Long id) {
    	service.eliminar(session.getOrganizacionOrThrow(), id);
    }
    
    /**
     * DTO con los posibles tipos de busqueda de cuentas
     *
     */
    @Data
    public static class CuentaQuery {
    	@QueryParam("query") 
    	private String searchText;
    	
    	@QueryParam("categories")
    	@DefaultValue("false")
    	private boolean includeCategories;
    	
    	@QueryParam("ids") 
    	private String idsString;
    	
    	/**
    	 * Retorna la lista de ids
    	 */
    	public List<Long> getIds() throws InvalidRequestException {
    		return CollectionUtils.parseLongList(idsString);
    	}
    	
    	public boolean isSearchQuery() {
    		return !StringUtils.isEmpty(this.searchText);
    	}
    	public boolean isFindByIds() {
    		return !StringUtils.isEmpty(this.idsString);
    	}
    }
}