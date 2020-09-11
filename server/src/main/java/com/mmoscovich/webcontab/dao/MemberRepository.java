package com.mmoscovich.webcontab.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.model.Member;

/**
 * DAO de Miembros (asociacion usuario - organzacion)
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
	
	/** Busca una membresia por usuario y organizacion */
	@Query("FROM Member WHERE user = :user AND organizacion = :org")
	Optional<Member> findByUserAndOrganizacion(User user, Organizacion org);
	
	/** Busca todas las membresias de la organizacion */
	@Query("FROM Member m join fetch m.user WHERE m.organizacion = :org")
	List<Member> findByOrganizacion(Organizacion org);
	
	/**
	 * Borra todas los miembros de una organizacion
	 */
	@Modifying
	@Query("DELETE FROM Member WHERE organizacion = :org")
	void deleteByOrganizacion(Organizacion org);
}
