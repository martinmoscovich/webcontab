package com.mmoscovich.webcontab.dto;

import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.Data;

/**
 * DTO de los datos de paginacion
 */
@Data
public class PageReq {
	
	@Min(1)
	@DefaultValue("1")
	@QueryParam("page")
    private int number;
	
	@Min(1)
	@DefaultValue("25")
	@QueryParam("size")
    private int size;

	public Pageable toPageable() {
		return PageRequest.of(this.number - 1, this.size);
	}
}