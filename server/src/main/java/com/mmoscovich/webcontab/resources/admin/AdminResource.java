package com.mmoscovich.webcontab.resources.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.services.DBService;
import com.mmoscovich.webcontab.services.DBService.BackupItem;
import com.mmoscovich.webcontab.services.DBService.BackupType;
import com.mmoscovich.webcontab.updater.SemVersion;
import com.mmoscovich.webcontab.updater.UpdateService;
import com.mmoscovich.webcontab.updater.Updater.UpdateStatus;

/**
 * Resource administrativo
 */
@Component
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

	@Inject
	private DBService dbService;
	
	@Inject
	private UpdateService updateService;
	
	/*************************************
	 * 			  DB BACKUP
	 *************************************/
	
	/**
	 * Lista los backups de base de datos realizados (leyendo el directorio)
	 * @return
	 * @throws IOException
	 */
	@GET
	@Path("db/backups")
	public List<BackupItem> list() throws IOException {
		return dbService.list();
	}
	
	/**
	 * Obtiene el ultimo backup de base de datos realizado o 204 si no se realizo ninguno
	 * @return info del backup
	 * @throws IOException
	 */
	@GET
	@Path("db/backups/last")
	public Response getLast() throws IOException {
		return dbService.getLastBackup()
		.map(item -> Response.ok(item))
		.orElse(Response.noContent())
		.build();
	}
	
	/**
	 * Realiza un nuevo backup de base de datos, usando la fecha como nombre
	 * @return info del backup creado
	 * @throws IOException
	 */
	@POST
	@Path("db/backups")
	public BackupItem createBackup() throws IOException {
		return dbService.backUpWithDate(BackupType.DB);
	}
	
	/**
	 * Realiza un nuevo backup de base de datos, usando el nombre especificado
	 * @return info del backup creado
	 * @throws IOException
	 */
	@PUT
	@Path("db/backups/{name}")
	public BackupItem createBackup(@PathParam("name") String name) throws IOException {
		return dbService.backup(name, BackupType.DB);
	}
	
//	@PUT
//	@Path("db/restores/{name}")
//	public void restoreBackup(@PathParam("name") String name) throws IOException {
//		dbService.restoreSQL(name);
//	}
	
	/*************************************
	 * 			  APP UPDATER
	 *************************************/

	
	/**
	 * Comprueba el estado actual del actualizador (PENDIENTE, EN PROGRESO, etc).
	 * @return
	 */
	@GET
	@Path("update/status")
	public UpdateStatus getUpdateStatus() {
		return updateService.getStatus();
	}
	
	/**
	 * Indica si hay una nueva version para instalar. 
	 * @return si hay, indica la nueva version 
	 * @throws IOException
	 */
	@GET
	@Path("update/check")
	public Map<String, String> getPending() throws IOException {
		Map<String, String> response = new HashMap<>();
		SemVersion pending = updateService.checkRemote();
		if(pending == null) {
			response.put("pending", "false");
		} else {
			response.put("pending", "true");
			response.put("version", pending.toString());
		}
		return response;
	}
	
	/**
	 * Descarga la actualizacion del servidor remoto
	 */
	@POST
	@Path("update/download")
	public void downloadUpdate() {
		this.updateService.downloadUpdate();
	}
	
	/**
	 * Ejecuta la actualizacion
	 */
	@POST
	@Path("update/apply")
	public void runUpdate() {
		this.updateService.update();
	}
}
