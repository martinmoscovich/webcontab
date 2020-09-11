package com.mmoscovich.webcontab.dto;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Categorias y Cuentas.
 */
@Data
@NoArgsConstructor
public class CuentaDTO {
	
	private Long id;
	
	@Length(max = 35)
    private String codigo;
	
    @NotNull
    @Min(1) @Max(9999)
	private Short numero;
    
    @Length(max = 20)
    private String alias;
    
    @Length(max = 50)
	@NotEmpty(groups = {Default.class, UpdateValidation.class})
	private String descripcion;
	
	@NotNull(groups = {Default.class, UpdateValidation.class})
    private Boolean activa;
    
	private Long categoriaId;
	
	private List<IdNameModel<Long>> path;
    private TipoItem tipo;
    
	// De cuenta
	private Long monedaId;
    private Boolean individual;
    private Boolean ajustable;
    private Boolean balanceaResultados;
    
    // De Categoria
    private Boolean resultado;

    public CuentaDTO(Long id) {
    	this.id = id;
    }
    
    public static enum TipoItem {
    	CUENTA, CATEGORIA
    }
}
