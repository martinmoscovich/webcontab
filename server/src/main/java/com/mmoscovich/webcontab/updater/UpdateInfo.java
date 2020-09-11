package com.mmoscovich.webcontab.updater;

import java.util.Properties;

import lombok.Data;

/**
 * DTO que lee la ultima version disponible en el server remoto.
 */
@Data
public class UpdateInfo {
	
	/** Ultima version disponible en el server remoto */
	private SemVersion version;
	
	/** Checksum de la actualizacion, para verificar que se descargo correctamente */
	private String checksum;
	
	public UpdateInfo(Properties p) {
		this.version = new SemVersion(p.getProperty("version"));
		this.checksum = p.getProperty("checksum");
	}
}
