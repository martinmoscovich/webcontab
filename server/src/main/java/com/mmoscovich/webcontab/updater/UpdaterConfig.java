package com.mmoscovich.webcontab.updater;

import java.net.URI;
import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuracion del Updater
 *
 */
@ConfigurationProperties(prefix = "webcontab.updater")
@Data
public class UpdaterConfig {
	
	/** URL del servidor de actualizacion */
	private URI url;
	
	/** Nombre del archivo properties en el servidor de actualizacion que indica la ultima version */
	private String versionFile;
	
	/** Directorio donde se descargan las actualizaciones */
	private Path backupDir;
	
	/** Obtiene la URI completa del archivo properties en el servidor de actualizacion */
	public URI getRemoteVersionURL() {
		return this.url.resolve(this.versionFile);
	}
	
	/** Obtiene el path al archivo properties local con la version actual */
	public Path getLocalVersionFile() {
		return Path.of(this.versionFile);
	}
}
