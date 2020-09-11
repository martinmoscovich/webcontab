package com.mmoscovich.webcontab.updater;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.exception.UpdaterBusyException;
import com.mmoscovich.webcontab.updater.Updater.UpdateStatus;

/**
 * Servicio que maneja las actualizaciones, delegando en {@link Updater}.
 */
@Service
public class UpdateService {

	@Inject
	private Updater updater;
	
	
	/**
	 * Comprueba si existe una actualizacion pendiente en el servidor remoto.
	 * @return la nueva version si existe o null en caso contrario
	 */
	public SemVersion checkRemote() {
		UpdateInfo info = updater.checkRemoteForUpdate();
		return info == null ? null : info.getVersion();
	}
	
	/**
	 * Comprueba el estado actual del actualizador (PENDIENTE, EN PROGRESO, etc).
	 */
	public UpdateStatus getStatus() {
		return updater.getStatus();
	}
	
	/**
	 * Descarga la actualizacion del servidor remoto.
	 * <p>Este metodo ejecuta de manera async, por lo que finaliza instantaneamente.</p>
	 * <p>Si se desea comprobar el estado, se debe llamar a {@link #getStatus()}.</p>
	 * @throws UpdaterBusyException si el actualizador esta ocupado con otra tarea.
	 */
	@Async
	public void downloadUpdate() throws UpdaterBusyException {
		updater.checkIdle();
		updater.downloadUpdate();
	}
	
	/**
	 * Instala la actualizacion descargada del servidor remoto.
	 * <p>Este metodo ejecuta de manera async, por lo que finaliza instantaneamente.</p>
	 * <p>Si se desea comprobar el estado, se debe llamar a {@link #getStatus()}.</p>
	 * @throws UpdaterBusyException si el actualizador esta ocupado con otra tarea.
	 */
	@Async
	public void update() throws UpdaterBusyException {
		updater.checkIdle();
		updater.update();
	}
}
