package com.mmoscovich.webcontab.dao.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Builder que permite crear mas facilmente condiciones en SQL o JPQL
 */
public class SQLCondition {
	private List<String> conditions = new ArrayList<>();
	private boolean root = false;
	
	/** 
	 * Indica que la condicion a construir en la condicion principal de la query.
	 * <br>La condicion principal incluye cada subcondicion en una nueva linea y no es wrapeada en parentesis. 
	 */
	public SQLCondition isRoot() {
		this.root = true;
		return this;
	}
	
	/**
	 * Construye el String de la condicion
	 * @param operator AND u OR
	 * @return
	 */
	private String build(String operator) {
		if(this.conditions.isEmpty()) return "";
		
		String br = (root ? "\n " : "");
		String cond =  br;
		cond += this.conditions.stream().collect(Collectors.joining(" " + operator + " " + br));
		if(!root && this.conditions.size() > 1) return "(" + cond + ")";
		return cond;
	}
	
	/**
	 * Construye la condicion en base a las incluidas previamente usando AND
	 */
	public String buildAnd() {
		return this.build("AND");
	}
	
	/**
	 * Construye la condicion en base a las incluidas previamente usando OR
	 */
	public String buildOr() {
		return this.build("OR");
	}
	
	/**
	 * Agrega la condicion especificada como string
	 * @param predicate
	 * @return
	 */
	public SQLCondition add(String predicate) {
		if(StringUtils.isNotEmpty(predicate)) this.conditions.add(predicate);
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea <b>NULL</b>
	 * @param field nombre del campo
	 */
	public SQLCondition isNull(String field) {
		this.add(field + " IS NULL");
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo <b>no</b> sea <b>NULL</b>
	 * @param field nombre del campo
	 */
	public SQLCondition isNotNull(String field) {
		this.add(field + " IS NOT NULL");
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea igual al valor
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition equals(String field, String value) {
		this.add(field + " = " + value);
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo este entre el minimo y el maximo
	 * @param field nombre del campo
	 * @param min valor minimo (o puede ser un parametro ":nombre")
	 * @param max valor maximo (o puede ser un parametro ":nombre")
	 * @return
	 */
	public SQLCondition between(String field, String min, String max) {
		this.add(field + " BETWEEN " + min + " AND " + max);
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea mayor o igual al valor
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition greaterThanOrEqual(String field, String value) {
		this.add(field + " >= " + value);
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea mayor al valor
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition greaterThan(String field, String value) {
		this.add(field + " > " + value);
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea menor o igual al valor
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition lessThanOrEqual(String field, String value) {
		this.add(field + " <= " + value);
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea menor al valor
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition lessThan(String field, String value) {
		this.add(field + " < " + value);
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo este dentro de los valores indicados en el value
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition in(String field, String value) {
		this.add(field + " IN (" + value + ")");
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea "LIKE" el valor
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition like(String field, String value) {
		this.add(field + " LIKE '" + value + "'");
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo sea "LIKE" alguno de los valores especificados.
	 * <br>Se implementa como un LIKE por cada uno, unidos por "OR".
	 * @param field nombre del campo
	 * @param values valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition like(String field, List<String> values) {
		String cond = values.stream().map(value -> field + " LIKE '" + value + "'").collect(Collectors.joining(" OR "));
		this.add("(" + cond + ")");
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo <b>no</b> sea "LIKE" el valor
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition notLike(String field, String value) {
		this.add(field + " NOT LIKE '" + value + "'");
		return this;
	}
	
	/**
	 * Agrega la condicion de que el campo <b>no</b> sea "LIKE" ninguno de los valores especificados.
	 * <br>Se implementa como un NOT LIKE por cada uno, unidos por "AND".
	 * @param field nombre del campo
	 * @param value valor (o puede ser un parametro ":nombre")
	 */
	public SQLCondition notLike(String field, List<String> values) {
		String cond = values.stream().map(value -> field + " NOT LIKE '" + value + "'").collect(Collectors.joining(" AND "));
		this.add("(" + cond + ")");
		return this;
	}

}
