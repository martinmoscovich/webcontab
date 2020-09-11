package com.mmoscovich.webcontab.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.model.User.UserType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Adapter que permite que nuestro modelo de usuario se pueda usar con Spring Security (que necesita un UserDetails)
 * @author Martin
 *
 */
@Getter
@RequiredArgsConstructor
public class UserAdapter implements UserDetails {
	private static final long serialVersionUID = -7341107029288254189L;

	private Long id;
	private String username;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;
	private boolean enabled;
	
	public UserAdapter(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.enabled = user.isEnabled();
		
		this.authorities = (user.getType() == UserType.USER) ? 
					Collections.emptySet() :
					Arrays.asList(new SimpleGrantedAuthority(UserType.ADMIN.name())); 
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}
