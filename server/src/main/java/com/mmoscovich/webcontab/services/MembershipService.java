package com.mmoscovich.webcontab.services;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.MemberRepository;
import com.mmoscovich.webcontab.dto.MemberDTO;
import com.mmoscovich.webcontab.dto.mapper.UserMapper;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.model.Member;
import com.mmoscovich.webcontab.model.Member.Rol;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.util.CollectionUtils;

/**
 * Servicio que maneja las membresias de un usuario a una organizacion.
 */
@Service
public class MembershipService {
	
	@Inject
	private UserService userService;
	
	@Inject
	private MemberRepository dao;
	
	@Inject
	private UserMapper mapper;
	
	/**
	 * Obtiene la lista de miembros de una organizacion.
	 * <p>Esta lista siempre incluye a los administradores del sistema.</p>
	 */
	@Transactional(readOnly = true)
	public List<MemberDTO> getMembers(Organizacion org) {
		
		// Se buscan los usuarios asignados a la organizacion
		List<MemberDTO> members = mapper.toMemberDtos(dao.findByOrganizacion(org));
		
		// Se buscar los admins del sistema y se los incluye como miembros ADMIN
		List<MemberDTO> admins = CollectionUtils.map(userService.getAdmins(), user -> {
			MemberDTO dto = new MemberDTO();
			dto.setUser(mapper.toDto(user));
			dto.setOrganizacionId(org.getId());
			dto.setRol(Rol.ADMIN);
			dto.setReadonly(true);
			return dto;
		});
		
		// Se hace merge de las listas
		members.addAll(admins);
		
		return members;
	}
	
	/**
	 * Agrega un usuario a una organizacion con el rol especificado
	 * @param org organizacion a la cual agregar al usuario
	 * @param userId id del usuario a agregar
	 * @param rol rol que tendra el usuario en la organizacion
	 * @return la membresia persistida
	 */
	@Transactional
	public MemberDTO addMember(Organizacion org, Long userId, Rol rol) {
		// Se busca el usuario
		User user = userService.getByIdOrThrow(userId);
		
		// Se busca si ya existe esta membresia, o se crea una
		Member member = dao.findByUserAndOrganizacion(user, org).orElse(new Member());
		
		// Si es nueva, se completa
		if(member.getId() == null) {
			member.setUser(user);
			member.setOrganizacion(org);
		}
		
		// Se asigna o actualiza el rol
		member.setRol(rol);
		
		return mapper.toMemberDto(dao.save(member));
	}
	
	/**
	 * Elimina una membresia (quitando al usuario de la organizacion)
	 * @param org
	 * @param userId
	 */
	@Transactional
	public void removeMember(Organizacion org, Long userId) {
		User user = userService.getByIdOrThrow(userId);
		
		Member member = dao.findByUserAndOrganizacion(user, org).orElseThrow(() -> new EntityNotFoundException("No existe la membresia"));
		
		dao.delete(member);
	}
	
	/**
	 * Elimina todos los usuarios de una organizacion
	 * @param org
	 */
	@Transactional
	public void removeAll(Organizacion org) {
		dao.deleteByOrganizacion(org);
	}
	
}
