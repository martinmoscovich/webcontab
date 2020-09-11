package com.mmoscovich.webcontab.resources;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mmoscovich.webcontab.dao.OrganizacionRepository;
import com.mmoscovich.webcontab.dto.MemberDTO;
import com.mmoscovich.webcontab.dto.Periodo;
import com.mmoscovich.webcontab.exception.AuthorizationException;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.services.CategoriaService;
import com.mmoscovich.webcontab.services.CuentaService;
import com.mmoscovich.webcontab.services.EjercicioService;
import com.mmoscovich.webcontab.services.MembershipService;
import com.mmoscovich.webcontab.services.SessionService;
import com.mmoscovich.webcontab.util.CreateValidation;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Resource de ABM de organzaciones
 */
@Component
@Path("/organizaciones")
@Produces(MediaType.APPLICATION_JSON)
public class OrganizacionResource extends SimpleResource<Organizacion, Organizacion> {

	@Inject
	private OrganizacionRepository orgDao;
	
	@Inject
	private MembershipService memberService;
	
	@Inject
	private EjercicioService ejercicioService;
	
	@Inject
	private CategoriaService catService;
	
	@Inject
	private CuentaService cuentaService;
	
	@Inject
	private SessionService session;
	
	@GET
	@Override
    public List<Organizacion> list() {
		// Si es admin puede ver todas
		if(session.isAdmin()) return orgDao.findAll();

		// Si no es admin, solo puede ver las que tiene asignadas
		return orgDao.findByUser(session.getUserOrThrow());
    }
	
	/**
	 * Obtiene los ejercicios de una organizacion
	 * @param orgId
	 * @return
	 * @throws EntityNotFoundException
	 */
	@GET
	@Path("{id}/ejercicios")
	public List<Ejercicio> getEjercicios(@PathParam("id") @NotNull @Min(1) Long orgId) throws EntityNotFoundException {
		return ejercicioService.findByOrganizacion(this.getByIdOrThrow(orgId));
	}
	
	/**
	 * Crea un ejercicio dentro de la organizacion
	 * @param orgId
	 * @param payload
	 * @return
	 * @throws EntityNotFoundException
	 */
	@POST
	@Path("{id}/ejercicios")
	public Ejercicio crearEjercicio(@PathParam("id") @NotNull @Min(1) Long orgId, @Valid @ConvertGroup(to = CreateValidation.class) CrearEjercicioPayload payload) throws EntityNotFoundException {
		return ejercicioService.crearSiguiente(this.getByIdOrThrow(orgId), payload, payload.isCerrarUltimo());
	}
	
	/**
	 * Obtiene los usuarios de una organizacion con sus roles
	 * @param orgId
	 * @return
	 * @throws EntityNotFoundException
	 */
	@GET
	@Path("{id}/miembros")
	public List<MemberDTO> getMiembros(@PathParam("id") @NotNull @Min(1) Long orgId) throws EntityNotFoundException {
		if(!session.isAdmin()) throw new AuthorizationException("No tiene permisos suficientes");
		
		return memberService.getMembers(this.getByIdOrThrow(orgId));
	}
	
	/**
	 * Agrega un usuario como miembro de una organizacion
	 * @param orgId id de la organizacion
	 * @param userId id del usuario
	 * @param dto rol asignado
	 * @return
	 * @throws EntityNotFoundException
	 */
	@POST
	@Path("{id}/miembros/{userId}")
	public MemberDTO agregarMiembro(@PathParam("id") @NotNull @Min(1) Long orgId, @PathParam("userId") @NotNull @Min(1) Long userId, @Valid MemberDTO dto) throws EntityNotFoundException {
		if(!session.isAdmin()) throw new AuthorizationException("No tiene permisos suficientes");
		
		return memberService.addMember(this.getByIdOrThrow(orgId), userId, dto.getRol());
	}
	
	/**
	 * Remueve un usuario de una organizacion.
	 * @param orgId
	 * @param userId
	 * @throws EntityNotFoundException
	 */
	@DELETE
	@Path("{id}/miembros/{userId}")
	public void quitarMiembro(@PathParam("id") @NotNull @Min(1) Long orgId, @PathParam("userId") @NotNull @Min(1) Long userId) throws EntityNotFoundException {
		if(!session.isAdmin()) throw new AuthorizationException("No tiene permisos suficientes");
		
		memberService.removeMember(this.getByIdOrThrow(orgId), userId);
	}

	@Override
	protected JpaRepository<Organizacion, Long> getRepo() {
		return orgDao;
	}

	@Override
	protected Class<Organizacion> getEntityClass() {
		return Organizacion.class;
	}

	@Override
	protected void updateItem(Organizacion existing, Organizacion modified) {
		if(!StringUtils.isEmpty(modified.getCuit())) existing.setCuit(modified.getCuit());
		if(!StringUtils.isEmpty(modified.getNombre())) existing.setNombre(modified.getNombre());
		
//		if(orgDao.existsByCuitOrNombre(existing.getCuit(), existing.getNombre())) throw new ConflictException("Ya existe una organizacion con ese CUIT o nombre");
	}
	
	@Override
	protected void beforeGet(Long id) {
		// Si es admin, puede obtener cualquier organizacion
		// Si no, solo puede obtener la actual
		if(!session.isAdmin() && !session.getOrganizacionOrThrow().getId().equals(id)) {
			throw new EntityNotFoundException(Organizacion.class, id);
		}
	}

	@Override
	protected void beforeCreate(Organizacion entity) {
		// Dos organizaciones no pueden tener el mismo nombre o CUIT
		List<Organizacion> organizaciones = orgDao.findByCuitOrNombre(entity.getCuit(), entity.getNombre());
		if(!organizaciones.isEmpty()) throw new ConflictException("Ya existe una organizacion con ese CUIT o nombre");
		
	}

	@Override
	protected void beforeDelete(Organizacion org) {
		List<Ejercicio> ejercicios = ejercicioService.findByOrganizacion(org);
		if(!ejercicios.isEmpty()) throw new ConflictException("No puede eliminarse una organizacion con ejercicios");
		
		// Elimina todas las cuentas
		cuentaService.eliminarTodas(org);
		catService.eliminarTodas(org);
		
		// Remueve todos los usuarios
		memberService.removeAll(org);
	}
	
	
	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class CrearEjercicioPayload extends Periodo {
		private boolean cerrarUltimo;
	}


	@Override
	protected Organizacion toDto(Organizacion model) {
		return model;
	}

	@Override
	protected List<Organizacion> toDto(List<Organizacion> models) {
		return models;
	}
}
