package com.mmoscovich.webcontab.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.importer.mdb.MDBImporter;
import com.mmoscovich.webcontab.importer.mdb.MDBImporter.CuentaImportStrategy;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.model.User;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de importacion.
 * <p>
 * Encargado de administrar las tareas de importacion ({@link ImportTask}).
 * <br>Crea, obtiene, ejecuta y elimina las tareas.
 * </p>
 * <p>Delega la logica de importacion en si al {@link MDBImporter}.</p>
 */
@Slf4j
@Service
public class ImportService {
	
	/** Mapa de tareas por UUID */
	private ConcurrentHashMap<UUID, ImportTask> importTasks = new ConcurrentHashMap<>();
	
	@Inject
	private Importer importer;

	/** Devuelve todas las tareas existentes de importacion */
	public Collection<ImportTask> getAll() {
		return this.importTasks.values();
	}
	
	/**
	 * Obtiene una tarea de importacion por UUID.
	 * @param uuid
	 * @return la tarea
	 * @throws EntityNotFoundException si no existe tarea con ese UUID.
	 */
	public ImportTask getOrThrow(UUID uuid) throws EntityNotFoundException {
		ImportTask task = importTasks.get(uuid);
		if(task == null) throw new EntityNotFoundException(ImportTask.class, uuid);
		return task;
	}
	
	/**
	 * Crea una tarea de importacion.
	 * 
	 * @param file Path al archivo MDB
	 * @param fileName nombre original del archivo
	 * @param user usuario que pide la importacion
	 * @param orgPermitida si se especifica, la organizacion del MDB debe ser la misma.
	 * @return la tarea de importacion creada
	 * @throws IOException
	 */
	public ImportTask create(Path file, String fileName, User user, Organizacion orgPermitida) throws IOException {
		log.info("Analizando el archivo {} y creando la tarea de importacion", fileName);
		
		// Delega la creacion de la tarea al Importer, que es quien puede leer el archivo
		ImportTask task = importer.read(file, fileName);
		
		// Se guarda el usuario que pide la importacion
		task.setUser(user);
		
		// Si se especifico una organizacion, la del archivo debe ser la misma
		if(orgPermitida != null && !orgPermitida.getId().equals(task.getOrganizacion().getId())) {
			// Se borra el archivo y no se agrega la task, pero se devuelve para informar al usuario
			Files.delete(task.getFile());
			task.setUuid(null);
			return task;
		}
		
		// Agrega la tarea al mapa
		this.importTasks.put(task.getUuid(), task);
		return task;
	}
	
	/**
	 * Ejecuta de manera async una tarea de importacion
	 * @param task
	 * @param cuentaStrategy estrategia para importar cuentas
	 * @param includeAsientos indica si se deben importar asientos e imputaciones.
	 */
	@Async
	public void run(ImportTask task, CuentaImportStrategy cuentaStrategy, boolean includeAsientos) {
		log.info("Importando archivo {}", task.getFile().getFileName().toString());
		this.importer.run(task, cuentaStrategy, includeAsientos);
	}
	
	/**
	 * Elimina una tarea de importacion
	 * @param uuid
	 */
	public void deleteTask(UUID uuid) {
		ImportTask task = getOrThrow(uuid);
		importTasks.remove(task.getUuid());
		try {
			Files.delete(task.getFile());
		} catch(Exception e) {
			log.error("Error al borrar {}. Eliminelo manualmente", task.getFile().toAbsolutePath());
		}
	}

}
