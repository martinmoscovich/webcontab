package com.mmoscovich.webcontab.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.SessionContext;
import com.mmoscovich.webcontab.dao.EjercicioRepository;
import com.mmoscovich.webcontab.dao.MemberRepository;
import com.mmoscovich.webcontab.dao.OrganizacionRepository;
import com.mmoscovich.webcontab.dao.UserRepository;
import com.mmoscovich.webcontab.exception.AuthenticationException;
import com.mmoscovich.webcontab.exception.EjercicioNoSeleccionadoException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.OrganizacionNoSeleccionadaException;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Member.Rol;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.model.User.UserType;
import com.mmoscovich.webcontab.security.UserAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que maneja la sesion del usuario.
 */
@Slf4j
@Service
@DependsOn("webSecurityConfig") // Necesario para evitar dep circular al inyectar authMapper
public class SessionService {

	@Inject
	private SessionContext ctx;

	@Inject
	private UserRepository userDao;
	
	@Inject
	private OrganizacionRepository orgDao;
	
	@Inject
	private EjercicioRepository ejDao;
	
	@Inject
	private MemberRepository rolDao;
	
	@Inject
	private GrantedAuthoritiesMapper authorityMapper;

	/**
	 * Obtiene el ejercicio actual de la sesion, si existe.
	 * <br>En caso contrario, devuelve un optional vacio.
	 */
	public Optional<Ejercicio> getEjercicio() {
		if (ctx.getEjercicioId() == null) return Optional.empty();
		return ejDao.findById(ctx.getEjercicioId());
	}

	/**
	 * Obtiene el ejercicio actual de la sesion, si existe.
	 * @throws EjercicioNoSeleccionadoException si no hay ejercicio seleccionado
	 */
	public Ejercicio getEjercicioOrThrow() throws EjercicioNoSeleccionadoException {
		return this.getEjercicio().orElseThrow(() -> new EjercicioNoSeleccionadoException());
	}

	/**
	 * Obtiene la organizacion actual de la sesion, si existe.
	 * <br>En caso contrario, devuelve un optional vacio.
	 */
	public Optional<Organizacion> getOrganizacion() {
		if (ctx.getOrganizacionId() == null) return Optional.empty();
		return orgDao.findById(ctx.getOrganizacionId());
	}

	/**
	 * Obtiene la organizacion actual de la sesion, si existe.
	 * @throws OrganizacionNoSeleccionadaException si no hay organizacion seleccionada
	 */
	public Organizacion getOrganizacionOrThrow() throws OrganizacionNoSeleccionadaException {
		return this.getOrganizacion().orElseThrow(() -> new OrganizacionNoSeleccionadaException());
	}

	/**
	 * Obtiene el usuario logueado, si existe. 
	 * <br>En caso contrario, devuelve un optional vacio.
	 * <p>Usa Spring Security para obtener dicho usuario</p>
	 */
	public Optional<User> getUser() {
		// Se pide el usuario a Spring Security
		Authentication auth =  SecurityContextHolder.getContext().getAuthentication();

		// Si no existe o es anonimo, devuelve optional vacio
		if(auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) return Optional.empty();
		
		// Busca el usuario en la base (o la cache)
		return userDao.findById((((UserAdapter)auth.getPrincipal()).getId()));
	}
	
	/**
	 * Obtiene el usuario logueado, si existe. 
	 * <p>Usa Spring Security para obtener dicho usuario</p>
	 * @throws AuthenticationException si no hay usuario logueado
	 */
	public User getUserOrThrow() throws AuthenticationException {
		return this.getUser().orElseThrow(() -> new AuthenticationException("No hay usuario logueado"));
	}
	
	/**
	 * Obtiene la lista de roles actuales del usuario.
	 * <p>Estos dependen del usuario logueado y de la organizacion en la que se encuentre,
	 * ya que puede tener distintos roles en cada una.</p>
	 */
	public Set<String> getCurrentRoles() {
		return this.getCurrentRolesStream().collect(Collectors.toSet());
	}
	
	/** Indica si el usuario actual es ADMIN general del sistema (no de la org) */
	public boolean isAdmin() {
		return this.hasRole(UserType.ADMIN.name());
	}
	
	/**
	 * Indica si el usuario tiene el rol deseado
	 * @param role rol a buscar
	 */
	public boolean hasRole(String role) {
		return this.getCurrentRolesStream().anyMatch(r -> r.equals(role));
	}
	
//	/** Indica si es ADMIN de la organizacion actual */
//	public boolean isOrgAdmin() {
//		return this.isAdmin() || this.hasOrgRole(Rol.ADMIN);
//	}
	
	public boolean hasOrgRole(Rol rol) {
		return this.hasRole("ORG:" + rol.name());
	}
	
	/**
	 * Obtiene un stream con los roles actuales del usuario.
	 * <p>Les quita el prefijo "ROLE_".</p> 
	 */
	private Stream<String> getCurrentRolesStream() {
		return SecurityContextHolder.getContext()
				.getAuthentication()
				.getAuthorities()
				.stream()
				.map(a -> a.getAuthority().replace("ROLE_", ""));
	}
	
	/**
	 * Asigna una organizacion a la sesion actual
	 * <p>Solo usuarios autorizados pueden entrar en una organizacion.
	 * <br>Este metodo cambia el contexto y por lo tanto, los roles del usuario, ya que dependen
	 * de la organizacion.</p>
	 * @param orgId id de la organizacion.
	 * @throws EntityNotFoundException si no se encuentra la organizacion o no es miembro
	 */
	public void setOrganizacion(Long orgId) throws EntityNotFoundException {
		User user = this.getUserOrThrow();
		
		// Lista de roles del usuario en la org.
		List<String> roles = new ArrayList<>();
		
		Organizacion org = null;
		if (orgId != null) {
			// Se busca la organizacion
			org = orgDao.findById(orgId).orElseThrow(() -> new EntityNotFoundException(Organizacion.class, orgId));
			
			// Se busca si el usuario es miembro y con que rol
			// Los roles de org tiene el prefijo "ORG:"
			rolDao.findByUserAndOrganizacion(user, org).map(r -> "ORG:" + r.getRol().toString())
				.ifPresent(roles::add);
		}
		
		// Si el usuario es admin, puede ingresar sin necesidad de ser miembro 
		if(user.getType() == UserType.ADMIN) roles.add(UserType.ADMIN.name());
		
		// Si no hay roles, significa que no tiene permisos para esta organizacion,
		// siempre que esta no sea null (o sea, esta saliendo)
		if(org != null && roles.isEmpty()) throw new EntityNotFoundException(Organizacion.class, orgId);
		
		// Actualiza los roles de la sesion
		updateRoles(roles);
		
		// Setea la org
		if(org != null) {
			log.info("El {} ingresa a la {} con los roles {}",  user, org, roles);
		} else {
			log.info("El {} sale de la organizacion actual. Roles {}", roles);
		}
		
		ctx.setOrganizacionId(orgId);
	}

	/**
	 * Asigna un ejercicio a la sesion actual.
	 * @param ejId
	 * @throws EntityNotFoundException si no se encuentra el ejercicio o no pertenece a la organizacion actual
	 * @throws OrganizacionNoSeleccionadaException si no se selecciono una organizacion
	 */
	public void setEjercicio(Long ejId) throws EntityNotFoundException, OrganizacionNoSeleccionadaException {
		User user = this.getUserOrThrow();
		if (ejId != null) {
			// Busca el ejercicio y comprueba que pertenezca a la organizacion
			Ejercicio ej = ejDao.findById(ejId).orElseThrow(() -> new EntityNotFoundException(Ejercicio.class, ejId));
			if(!ej.perteceneA(this.getOrganizacionOrThrow())) throw new EntityNotFoundException(Ejercicio.class, ejId);
			
			log.info("El {} entra en el {}", user, ej);
		} else {
			log.info("El {} sale del ejercicio", user);
		}
		ctx.setEjercicioId(ejId);
	}
	
	/**
	 * Metodo que actualiza los roles de la sesion con los especificados.
	 * <p>En lugar de manejar un sistema complejo de roles por entidad (organizacion),
	 * se utiliza un enfoque simplificado de roles generales, pero que se pueden "pisar" segun el contexto actual.
	 * <br>O sea, cuando se cambia la organizacion se modifican los roles generales del usuario en la sesion.
	 * <br>Esto funciona bien porque solo puede estar en una organizacion por vez.</p>
	 * <p>Este metodo reasigna la autenticacion de Spring Security para que funcione con los nuevos permisos</p>
	 * 
	 * @param roles nuevos roles a asignar en la sesion del usuario
	 */
	private void updateRoles(Collection<String> roles) {
		// Se obtiene la sesion actual de Spring Security
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// Se genera la nueva lista de roles
		List<GrantedAuthority> actualAuthorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        
		// Se genera la nueva autenticacion con los mismo datos pero los nuevos roles
		Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorityMapper.mapAuthorities(actualAuthorities));
        
		// Se asigna la nueva autenticacion (seria como reloguear al usuario)
		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}
}
