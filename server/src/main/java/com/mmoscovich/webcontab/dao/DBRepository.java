package com.mmoscovich.webcontab.dao;

import java.nio.file.Path;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO de tareas administrativas de Base de Datos
 */
@Repository
public class DBRepository {
	
	@Inject
	private JdbcTemplate jdbc;
	
	/**
	 * Crea un backup de la base de datos en formato de Script SQL.
	 * @param filePath path donde guardar el archivo
	 */
	@Transactional(readOnly = true)
	public void backupSQL(Path filePath) {
		jdbc.execute("SCRIPT TO '" + filePath + "'");
	}
	
	/**
	 * Ejecuta un archivo Script SQL
	 * @param sqlScript Path del archivo SQL
	 */
	@Transactional
	public void runSQL(Path sqlScript) {
		jdbc.execute("RUNSCRIPT FROM  '" + sqlScript + "'");
	}
	
	/**
	 * Crea un backup de la base de datos en formato binario (una copia de la base).
	 * @param filePath path donde guardar el archivo
	 */
	@Transactional(readOnly = true)
	public void backupDB(Path filePath) {
		jdbc.execute("BACKUP TO '" + filePath + "'");
	}

}
