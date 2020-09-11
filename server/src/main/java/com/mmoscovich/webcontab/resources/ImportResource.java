package com.mmoscovich.webcontab.resources;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.exception.AuthorizationException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.importer.ImportTask;
import com.mmoscovich.webcontab.importer.ImportTask.ImportStatus;
import com.mmoscovich.webcontab.importer.mdb.MDBImporter.CuentaImportStrategy;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.services.SessionService;
import com.mmoscovich.webcontab.importer.ImportService;

/**
 * Endpoints de Importacion.
 * <p>Permite administrar las tareas de importacion, que se ejecutan asincronicamente.</p>
 */
@Component
@Path("/import")
@Produces(MediaType.APPLICATION_JSON)
public class ImportResource {
	
	@Inject
	private ImportService importer;
	
	@Inject
	private SessionService session;
	
	/**
	 * @return las tareas de importacion existentes
	 */
	@GET
	public Collection<ImportTask> getAll() {
		return this.importer.getAll();
	}
	
	/**
	 * Busca una tarea por id
	 * @param uuid
	 * @return
	 * @throws EntityNotFoundException
	 */
	@GET
	@Path("{uuid}")
	public ImportTask getTask(@PathParam("uuid") UUID uuid) throws EntityNotFoundException {
		return this.importer.getOrThrow(uuid);
	}
	
	/**
	 * Crea una nueva tarea de imporacion, realizando la subida del archivo a importar, que es analizado.
	 * <p><b>NO ejecuta la tarea, espera confirmacion del usuario.</b></p>
	 * @param file archivo subido
	 * @param fileInfo info del archivo
	 * @param currentOrg flag que indica si se debe importar en la organizacion actual (solo admins pueden importar otras)
	 * @return la nueva tarea de importacion (pendiente)
	 * @throws IOException
	 */
	@POST
	public ImportTask create(@NotNull @FormDataParam("file") File file, @NotNull @FormDataParam("file") FormDataContentDisposition fileInfo, @DefaultValue("true") @FormDataParam("enActual") boolean currentOrg) throws IOException {
		if(!FilenameUtils.getExtension(fileInfo.getFileName()).equalsIgnoreCase("mdb")) {
			throw new InvalidRequestException("Solo se pueden importar base de datos MDB");
		}
		
		Organizacion org = null;
		if(currentOrg) {
			// Si se especifico que sea dentro de la org, se la busca
			org = session.getOrganizacionOrThrow();
		} else {
			// Si es general, se chequea que tenga el permiso y se pasa null como org
			boolean puedeImportarOtrasOrganizaciones = session.isAdmin();
			if(!puedeImportarOtrasOrganizaciones) throw new AuthorizationException("El usuario actual solo puede importar ejercicios y cuentas ingresando a la organizacion deseada");
		}
		return importer.create(file.toPath(), fileInfo.getFileName(), org);
	}
	
	/**
	 * Se inicia (en background) la importacion de la tarea especificada.
	 * @param uuid id de la tarea a iniciar
	 * @param cuentaMode estrategia para importar las cuentas
	 * @param asientos indica si se deben importar asientos o no.
	 * @throws EntityNotFoundException
	 */
	@PUT
	@Path("{uuid}")
	public void start(@PathParam("uuid") UUID uuid, @QueryParam("cuentas") CuentaImportStrategy cuentaMode, @QueryParam("asientos") boolean asientos) throws EntityNotFoundException {
		ImportTask task = this.getTask(uuid);
		if(task.getStatus() != ImportStatus.PENDING) throw new InvalidRequestException("La tarea esta en progreso o ya finalizo");
		this.importer.run(task, cuentaMode, asientos);
	}
}
