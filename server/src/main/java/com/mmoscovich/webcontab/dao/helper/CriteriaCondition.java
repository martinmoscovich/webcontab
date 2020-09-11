package com.mmoscovich.webcontab.dao.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * Builder que permite crear un predicate (AND u OR) de manera mas sencilla.  
 *
 */
public class CriteriaCondition {
	/** Lista de predicates */
	private List<Predicate> predicates = new ArrayList<>();
	
	/** Agrega un predicate a la lista */
	public CriteriaCondition add(Predicate p) {
		predicates.add(p);
		return this;
	}
	
	/** Agrega un predicate a partir de un opcional, solo si no esta vacio */ 
	public CriteriaCondition add(Optional<Predicate> p) {
		if(p.isPresent()) predicates.add(p.get());
		return this;
	}
	
	/** Crea un predicate que implica <b>AND</b> de todos los que fueron incluidos previamente */
	public Predicate buildAnd(CriteriaBuilder cb) {
		return cb.and(this.predicates.toArray(new Predicate[0]));
	}
	
	/** Crea un predicate que implica <b>OR</b> de todos los que fueron incluidos previamente */
	public Predicate buildOr(CriteriaBuilder cb) {
		return cb.or(this.predicates.toArray(new Predicate[0]));
	}
}
