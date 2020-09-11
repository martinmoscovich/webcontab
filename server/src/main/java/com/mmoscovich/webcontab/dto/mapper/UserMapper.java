package com.mmoscovich.webcontab.dto.mapper;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mmoscovich.webcontab.dto.UserDTO;
import com.mmoscovich.webcontab.dto.MemberDTO;
import com.mmoscovich.webcontab.model.User;
import com.mmoscovich.webcontab.model.Member;

/**
 * Mapper de usuario
 */
@Mapper
public interface UserMapper {

	@Mapping(target = "type", ignore = true)
	UserDTO toDto(User user);
	
	List<UserDTO> toDto(Collection<User> users);
	
	@Mapping(target = "organizacionId", source = "organizacion.id")
	@Mapping(target = "readonly", constant = "false")
	MemberDTO toMemberDto(Member model);
	
	List<MemberDTO> toMemberDtos(Collection<Member> users);
}
