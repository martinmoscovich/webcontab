package com.mmoscovich.webcontab.updater;

import java.time.Instant;
import java.util.Properties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la info de una release
 */
@Data
@NoArgsConstructor
public class ReleaseInfo {
	
	/** Version de la aplicacion */
	private SemVersion releaseVersion;
	
	/** Fecha de la version */
	private Instant releaseDate;
	
	/** 
	 * Version del server, que es independiente del de la aplicacion,
	 * ya que una nueva release puede contener cambios solo en el cliente.
	 * <p>Util para determinar si es necesario reiniciar el server luego de actualizar</p>
	 */
	private SemVersion serverVersion;
	
	/** Tamanio del archivo de actualizacion */
	private Long fileSize; 
	
	/** Indica si requiere reinicio del server */
	private Boolean requiresRestart;

	/**
	 * Obtiene la info de un properties
	 */
	public ReleaseInfo(Properties p, Long fileSize) {
		this.setReleaseVersion(new SemVersion(p.getProperty("release.version")));
		this.setServerVersion(new SemVersion(p.getProperty("server.version")));
		this.setReleaseDate(Instant.parse(p.getProperty("release.date")));
		this.fileSize = fileSize;
	}
}
