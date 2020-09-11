package com.mmoscovich.webcontab.dto.informes;

import java.math.BigDecimal;

import com.mmoscovich.webcontab.dto.ImputacionDTO;
import com.mmoscovich.webcontab.dto.PageDTO;
import com.mmoscovich.webcontab.services.ImputacionService.ImputacionesCuenta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * DTO de {@link ImputacionesCuenta}.
 * <p>
 * Se utiliza en el Mayor (imputaciones por cuenta).
 * <br>Contiene una pagina de imputaciones y el saldo anterior.
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ImputacionesCuentaDTO extends PageDTO<ImputacionDTO> {
	private BigDecimal saldoAnterior;
	
	public ImputacionesCuentaDTO(PageDTO<ImputacionDTO> page, BigDecimal saldoAnterior) {
		this.setHasNext(page.isHasNext());
		this.setItems(page.getItems());
		this.setNumber(page.getNumber());
		this.setSize(page.getSize());
		this.setTotal(page.getTotal());
		
		this.setSaldoAnterior(saldoAnterior);
	}
}
