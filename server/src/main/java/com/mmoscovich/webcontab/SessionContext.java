package com.mmoscovich.webcontab;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Data;

/**
 * Datos de sesion 
 */
@Data
@Component
@SessionScope
public class SessionContext {
	
	/** Id del ejercicio actual */
	private Long ejercicioId;
	
	/** Id de la organizacion actual */
	private Long organizacionId;
}
