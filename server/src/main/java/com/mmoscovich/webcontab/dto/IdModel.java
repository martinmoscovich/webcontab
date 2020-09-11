package com.mmoscovich.webcontab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de un modelo que tiene ID
 *
 * @param <ID> tipo de id
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdModel<ID> {
	private ID id;
}
