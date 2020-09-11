package com.mmoscovich.webcontab.security;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mmoscovich.webcontab.dao.UserRepository;

/**
 * Service requerido por Spring Security para buscar el usuario al autenticarse.
 */
public class WebContabUserDetailsService implements UserDetailsService {

	@Inject
	private UserRepository dao;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return dao.findByUsername(username)
				.map(UserAdapter::new)
				.orElseThrow(() -> new UsernameNotFoundException("No se encontro el usuario " + username));
	}
}
