package com.mmoscovich.webcontab.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO de un objeto que tiene ID y fecha
 * @author Martin
 *
 * @param <ID> tipo de id
 * @param <DATE> tipo de fecha
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class IdFechaModel<ID, DATE> extends IdModel<ID> {
	private DATE fecha;

	public IdFechaModel(ID id, DATE fecha) {
		super(id);
		this.fecha = fecha;
	}
}