package com.mmoscovich.webcontab.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO de un modelo que tiene ID y nombre
 *
 * @param <ID> tipo de id
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class IdNameModel<ID> extends IdModel<ID> {
	private String name;

	public IdNameModel(ID id, String name) {
		super(id);
		this.name = name;
	}
}
