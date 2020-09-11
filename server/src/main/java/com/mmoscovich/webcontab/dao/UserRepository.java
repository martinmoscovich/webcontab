package com.mmoscovich.webcontab.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.User;

/**
 * DAO de usuarios
 */
public interface UserRepository extends JpaRepository<User, Long> {
	
	/** Busca el usuario por username */
	Optional<User> findByUsername(String username);
	
	/** Busca el usuario por username o email */
	Optional<User> findByUsernameOrEmail(String username, String email);
	
	/**
	 * Determina si existen usuario en la base de datos.
	 * <p>Esto es util para generar el primer usuario al ejecutar el sistema por primera vez.</p>
	 * @return
	 */
	@Query("SELECT count(*) <> 0 FROM User")
	boolean hasAny();
	
	/** Obtiene la lista de administradores */
	@Query("FROM User WHERE type = 'ADMIN'")
	List<User> findAdmins();
	
	/** 
	 * Permite buscar usuarios por nombre o username.
	 * <p>Util para autocomplete.</p>
	 * @param query
	 * @return
	 */
	@Query("FROM User WHERE lower(name) LIKE :query or lower(username) LIKE :query")
	List<User> searchByText(String query);
}
