package com.mmoscovich.webcontab.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.model.User;

/**
 * DAO de organizacion 
 */
public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {
	
	/** Busca las organzaciones que tengan el cuit o el nombre deseados */
	List<Organizacion> findByCuitOrNombre(String cuit, String nombre);

	/** Busca las organizaciones a las que puede acceder un usuario */
	@Query("SELECT DISTINCT o FROM Member r inner join r.organizacion o WHERE r.user = :user")
	List<Organizacion> findByUser(User user);
}
