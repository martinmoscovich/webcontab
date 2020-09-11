package com.mmoscovich.webcontab.dto.mapper;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import com.mmoscovich.webcontab.dto.ImputacionDTO;
import com.mmoscovich.webcontab.dto.PageDTO;
import com.mmoscovich.webcontab.model.Imputacion;

/**
 * Mapper de imputaciones.
 *
 */
@Mapper
public interface ImputacionMapper {
	
//	@BeforeMapping
//	default CuentaDTO mapJpaProxy(CuentaBase categoria) {
//		if(!JpaUtils.isProxy(categoria)) return null;
//		return new CuentaDTO(categoria.getId());
//	}

	ImputacionDTO toDto(Imputacion imputacion);
	
	List<ImputacionDTO> toDto(Collection<? extends Imputacion> model);
	
	default PageDTO<ImputacionDTO> toDto(Page<? extends Imputacion> page) {
		return PageDTO.adapt(page, toDto(page.getContent()));
	}
}
