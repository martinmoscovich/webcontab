package com.mmoscovich.webcontab.services;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.UserRepository;
import com.mmoscovich.webcontab.dto.UserWithPasswordDTO;
import com.mmoscovich.webcontab.exception.AuthorizationException;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.model.User.UserType;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de manejo de usuarios
 *
 */
@Slf4j
@Service
public class UserService {

	@Inject
	private UserRepository dao;
	
	@Inject
	private PasswordEncoder passwordEncoder;
	
	@Inject
	private SessionService session;
	
	/**
	 * Busca un usuario por id
	 * @param id
	 * @return el usuario
	 * @throws EntityNotFoundException si no existe el usuario
	 */
	public User getByIdOrThrow(Long id) throws EntityNotFoundException {
		return dao.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));
	}
	
	/**
	 * Determina si existen usuario en la base de datos.
	 * <p>Esto es util para generar el primer usuario al ejecutar el sistema por primera vez.</p>
	 * @return
	 */
	public boolean hasAny() {
		return dao.hasAny();
	}
	
	/** Obtiene la lista de administradores */
	public List<User> getAdmins() {
		return dao.findAdmins();
	}
	
	/** Obtiene la lista de todos los usuarios */
	public List<User> getAll() {
		return this.dao.findAll();
	}
	
	/** 
	 * Permite buscar usuarios por nombre o username.
	 * <p>Util para autocomplete.</p>
	 * @param query
	 * @return
	 */
	public List<User> search(String query) {
		log.debug("Buscando usuario con username o nombre que empiece con {}", query);
		return this.dao.searchByText(query.toLowerCase() + "%");
	}
	
	/**
	 * Crea un nuevo usuario
	 * @param dto datos del usuario, incluyendo sus credenciales
	 * @return el usuario creado
	 * @throws InvalidRequestException si hay algun error de validacion
	 * @throws ConflictException si ya existe un usuario con ese username o email
	 */
	@Transactional
	public User create(UserWithPasswordDTO dto) throws InvalidRequestException, ConflictException {
		if(StringUtils.isEmpty(dto.getUsername())) throw new InvalidRequestException("Debe especificar nombre de usuario");
		if(StringUtils.isEmpty(dto.getPassword())) throw new InvalidRequestException("Debe especificar password");
		if(StringUtils.isEmpty(dto.getName())) throw new InvalidRequestException("Debe especificar nombre");
		if(StringUtils.isEmpty(dto.getEmail())) throw new InvalidRequestException("Debe especificar email");
		
		if(dto.getType() == null) dto.setType(UserType.USER);
		
		// No debe existir usuario con ese username o mail
		checkExistingUser(dto.getUsername(), dto.getEmail(), null);
		
		// Se crea el usuario usando el hash del password
		User user = new User(dto.getUsername(), passwordEncoder.encode(dto.getPassword()), dto.getType(), dto.getName(), dto.getEmail(), dto.getAvatarUrl());
		
		log.debug("Se crea el {}", user);
		
		return dao.save(user);
	}
	
	/**
	 * Actualiza los datos de un usuario, incluyendo su password
	 * @param dto nuevos datos del usuario
	 * @return el usuario actualizado
	 * @throws EntityNotFoundException si no se encuentra el usuario
	 * @throws AuthorizationException si un usuario normal intenta cambiar el tipo de usuario
	 * @throws ConflictException si ya existe un usuario con ese username o email
	 */
	@Transactional
	public User update(UserWithPasswordDTO dto) throws EntityNotFoundException, AuthorizationException, ConflictException {
		User existing = dao.findById(dto.getId()).orElseThrow(() -> new EntityNotFoundException(User.class, dto.getId()));
		
		log.debug("Actualizando el {}", existing);
		
		// Solo admin puede cambiar el tipo de usuario
		if(dto.getType() != null && !dto.getType().equals(existing.getType())) {
			if(!session.isAdmin()) throw new AuthorizationException("Solo los administradores puede cambiar el tipo de usuario");
			existing.setType(dto.getType());
		}
		
		if(StringUtils.isNotEmpty(dto.getUsername())) existing.setUsername(dto.getUsername());
		if(StringUtils.isNotEmpty(dto.getPassword())) existing.setPassword(passwordEncoder.encode(dto.getPassword()));

		if(dto.getAvatarUrl() != null) existing.setAvatarUrl(dto.getAvatarUrl());
		if(StringUtils.isNotEmpty(dto.getEmail())) existing.setEmail(dto.getEmail());
		if(StringUtils.isNotEmpty(dto.getName())) existing.setName(dto.getName());
		
		// No debe existir otro usuario con ese username o mail
		checkExistingUser(existing.getUsername(), existing.getEmail(), existing.getId());
		
		return dao.save(existing);
	}
	
	/**
	 * Comprueba que no exista un usuario con ese username o email.
	 * <p>Si se indica id y el usuario encontrado tiene ese id, no lanza error.</p>
	 * @param username
	 * @param email
	 * @param id
	 * @throws ConflictException
	 */
	private void checkExistingUser(String username, String email, Long id) throws ConflictException {
		// Busca usuario con ese username o email
		Optional<User> existing = dao.findByUsernameOrEmail(username, email);
		
		// Si no hay, OK
		if(existing.isEmpty()) return;
		
		// Si hay, OK si es el usuario con id especificado. Si no, error
		if(!existing.get().getId().equals(id)) throw new ConflictException("El usuario ya existe");
	}
}
