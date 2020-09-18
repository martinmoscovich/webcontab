package com.mmoscovich.webcontab.dto.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mapstruct.BeforeMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Slice;

import com.mmoscovich.webcontab.dto.CuentaDTO;
import com.mmoscovich.webcontab.dto.CuentaDTO.TipoItem;
import com.mmoscovich.webcontab.dto.PageDTO;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.services.CategoriaService;
import com.mmoscovich.webcontab.util.JpaUtils;

/**
 * Mapper de cuentas y categorias.
 * <p>El uses permite llamar a {@link CategoriaService#getPath(CuentaBase)} para armar el path a mostrar.
 */
@Mapper(uses = CategoriaService.class)
public interface CuentaMapper {
	
	@BeforeMapping
	default CuentaDTO mapJpaProxy(CuentaBase categoria) {
		if(!JpaUtils.isProxy(categoria)) return null;
		return new CuentaDTO(categoria.getId());
	}
	
	/**
	 * Mapea una cuenta a DTO
	 */
	@Mapping(target = "path", ignore = true)
	@Mapping(target = "resultado", ignore = true)
	@Mapping(source = "cuenta", target = "tipo")
	@Mapping(source = "categoria.id", target = "categoriaId")
	@Mapping(source = "moneda.id", target = "monedaId")
	CuentaDTO toDto(Cuenta cuenta);

	/**
	 * Mapea una categoria a DTO
	 */
	@Mapping(target = "ajustable", ignore = true)
	@Mapping(target = "individual", ignore = true)
	@Mapping(target = "monedaId", ignore = true)
	@Mapping(target = "path", ignore = true)
	@Mapping(target = "balanceaResultados", ignore = true)
	@Mapping(target = "balanceaAjustables", ignore = true)
	@Mapping(source = "categoria", target = "tipo")
	@Mapping(source = "categoria.id", target = "categoriaId")
	CuentaDTO toDto(Categoria categoria);
	
	/**
	 * Mapea una cuenta a DTO, incluyendo el Path (cat 1 -> cat 2 -> cuenta)
	 */
	@Mapping(source = "cuenta", target = "path")
	@Mapping(source = "cuenta", target = "tipo")
	@Mapping(source = "categoria.id", target = "categoriaId")
	@Mapping(source = "moneda.id", target = "monedaId")
	@Mapping(target = "resultado", ignore = true)
	CuentaDTO toDtoWithPath(Cuenta cuenta);
	
	/**
	 * Mapea una categoria a DTO, incluyendo el Path (cat 1 -> cat 2 -> cat)
	 */
	@Mapping(target = "ajustable", ignore = true)
	@Mapping(target = "individual", ignore = true)
	@Mapping(target = "monedaId", ignore = true)
	@Mapping(target = "balanceaResultados", ignore = true)
	@Mapping(target = "balanceaAjustables", ignore = true)
	@Mapping(source = "c", target = "path")
	@Mapping(source = "c", target = "tipo")
	@Mapping(source = "categoria.id", target = "categoriaId")
	CuentaDTO toDtoWithPath(Categoria c);
	
	/** Mapea el DTO al model, al recibirlo del cliente */
	@InheritInverseConfiguration(name = "toDto")
	Cuenta toCuentaModel(CuentaDTO dto);
	
	/** Mapea el DTO al model, al recibirlo del cliente */
	@Mapping(source = "categoriaId", target = "categoria.id")
	Categoria toCategoriaModel(CuentaDTO dto);
	
	default List<CuentaDTO> toDto(Collection<? extends CuentaBase> model) {
		List<CuentaDTO> result = new ArrayList<>();
		for(CuentaBase base : model) {
			if(base instanceof Cuenta) {
				result.add(this.toDto((Cuenta)base));
			} else {
				result.add(this.toDto((Categoria)base));
			}
		}
		return result;
	}
	
	default List<CuentaDTO> toDtoWithPath(Collection<? extends CuentaBase> model) {
		List<CuentaDTO> result = new ArrayList<>();
		for(CuentaBase base : model) {
			if(base instanceof Cuenta) {
				result.add(this.toDtoWithPath((Cuenta)base));
			} else {
				result.add(this.toDtoWithPath((Categoria)base));
			}
		}
		return result;
	}
	
	/** Mapea el enum de tipo en el DTO */
	default TipoItem getTipo(CuentaBase cuenta) {
		if(cuenta instanceof Cuenta) return TipoItem.CUENTA;
		
		return TipoItem.CATEGORIA;
	}
	
	/** Mapea un Page o Slice de CuentaBase a PageDTO de CuentaDTO */
	default PageDTO<CuentaDTO> toDto(Slice<? extends CuentaBase> page) {
		return PageDTO.adapt(page, toDto(page.getContent()));
	}
	
	/** Mapea un Page o Slice de CuentaBase a PageDTO de CuentaDTO con los path en cada uno */
	default PageDTO<CuentaDTO> toDtoWithPath(Slice<? extends CuentaBase> page) {
		return PageDTO.adapt(page, toDtoWithPath(page.getContent()));
	}
}
