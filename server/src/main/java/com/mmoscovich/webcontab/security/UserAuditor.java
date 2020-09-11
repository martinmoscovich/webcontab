package com.mmoscovich.webcontab.security;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.services.SessionService;

/**
 * Obtiene el user actual para auditar la creacion y modificacion de entidades en JPA.
 *
 */
@Service
public class UserAuditor implements AuditorAware<User> {
	
	@Inject
	private SessionService session;

	@Override
	public Optional<User> getCurrentAuditor() {
		return session.getUser();
	}

}
