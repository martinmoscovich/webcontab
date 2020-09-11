package com.mmoscovich.webcontab.importer;

import java.nio.file.Path;

import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.importer.mdb.MDBImporter.CuentaImportStrategy;

/**
 * Interfaz del importador de datos
 *
 */
public interface Importer {

	/**
	 * Lee el archivo a importar, lo analiza y crea la tarea de importacion
	 * @param file archivo a importar
	 * @param fileName nombre para sobreescribir el del archivo
	 * @return la tarea de importacion
	 * @throws InvalidRequestException
	 */
	ImportTask read(Path file, String fileName) throws InvalidRequestException;

	/**
	 * Ejecuta la tarea de importacion.
	 * 
	 * @param task tarea a ejecutar
	 * @param cuentaStrategy estrategia para importar las cuentas
	 * @param includeAsientos indica si incluir los asientos e imputaciones.
	 */
	void run(ImportTask task, CuentaImportStrategy cuentaStrategy, boolean includeAsientos);

	/**
	 * Lee el archivo a importar, lo analiza y crea la tarea de importacion
	 * @param file archivo
	 * @return la tarea de importacion
	 * @throws InvalidRequestException
	 */
	@Transactional(readOnly = true)
	default ImportTask read(Path file) throws InvalidRequestException {
		return this.read(file, file.getFileName().toString());
	}
}