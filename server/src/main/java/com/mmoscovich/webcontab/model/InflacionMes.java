package com.mmoscovich.webcontab.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mmoscovich.webcontab.json.OnlyIdSerializer;
import com.mmoscovich.webcontab.util.CreateValidation;
import com.mmoscovich.webcontab.util.UpdateValidation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa el indice de inflacion de un mes
 *
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
	name="INFLACION",
	uniqueConstraints = {
		// Solo puede haber un indice para una determinada moneda en un mes
		@UniqueConstraint(columnNames = {"moneda_id", "mes"}),
	}
)
public class InflacionMes extends PersistentEntity {
	
	/** Mes */
    @NotNull
    @JsonIgnore
    private LocalDate mes;
    
    /** Moneda a la que pertenece este indice */
	@NotNull(groups = {CreateValidation.class})
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonSerialize(using = OnlyIdSerializer.class)
	private Moneda moneda;
    
    /** Indice del mes */
    @NotNull
    @PositiveOrZero(groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    @Column(precision = 11, scale = 4)
    private BigDecimal indice;
    
    /**
     * Crea un indice de inflacion mensual de 0.
     * @param moneda
     * @param mes
     */
    public InflacionMes(Moneda moneda, YearMonth mes) {
    	this.moneda = moneda;
    	this.mes = mes.atDay(1);
    	this.indice = BigDecimal.ZERO;
    }
    
    /**
     * Obtiene una instancia de {@link YearMonth} a partir del mes.
     * <p>Se serializa este valor en lugar de la fecha para el "mes"</p>
     * @return
     */
    @JsonProperty("mes")
    public YearMonth getYearMonth() {
    	return YearMonth.from(this.mes);
    }
    
}