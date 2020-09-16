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
import javax.validation.constraints.Positive;
import javax.validation.groups.Default;

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
    private LocalDate mes;
    
    /** Moneda a la que pertenece este indice */
	@NotNull(groups = {CreateValidation.class})
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonSerialize(using = OnlyIdSerializer.class)
	private Moneda moneda;
    
    /** Indice del mes */
    @NotNull
    @Positive(groups = {Default.class, CreateValidation.class, UpdateValidation.class})
    @Column(precision = 11, scale = 4)
    private BigDecimal indice;
    
    /**
     * Obtiene una instancia de {@link YearMonth} a partir del mes.
     * <p>Se serializa este valor en lugar de la fecha para el "mes"</p>
     * @return
     */
    @JsonProperty("mes")
    public YearMonth getYearMonth() {
    	return YearMonth.from(this.mes);
    }
    
    /**
     * Guarda la instancia de {@link LocalDate} a partir del mes indicado, usando el primer dia del mes.
     */
    @JsonProperty("mes")
    public void setYearMonth(YearMonth mes) {
    	this.mes = mes.atDay(1);
    }
}