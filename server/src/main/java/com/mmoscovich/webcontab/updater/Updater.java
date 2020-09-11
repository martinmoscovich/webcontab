package com.mmoscovich.webcontab.updater;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.exception.UpdaterBusyException;
import com.mmoscovich.webcontab.exception.UpdaterException;
import com.mmoscovich.webcontab.exception.WebContabException;
import com.mmoscovich.webcontab.services.DBService;
import com.mmoscovich.webcontab.services.DBService.BackupType;
import com.mmoscovich.webcontab.updater.Updater.UpdateStatus.UpdateAction;
import com.mmoscovich.webcontab.util.ExecUtils;
import com.mmoscovich.webcontab.util.FileUtils;
import com.mmoscovich.webcontab.util.ZipUtils;

import lombok.Data;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 * Actualizador de la aplicacion
 */
@Slf4j
@Service
public class Updater {
	
	/**
	 * DTO que representa el estado actual del actualizador
	 */
	@Data
	public static class UpdateStatus {
		public static enum UpdateAction {
			IDLE, DOWNLOADING, UPDATING, RESTARTING
		}
		private UpdateAction action;
		private ReleaseInfo currentRelease;
		private ReleaseInfo targetRelease;
		
		@Synchronized
		public void change(UpdateAction action) {
			this.action = action;
			this.targetRelease = null;
		}
		
		@Synchronized
		public void change(UpdateAction action, ReleaseInfo targetRelease) {
			this.action = action;
			this.targetRelease = targetRelease;
		}
		
		public void change(UpdateAction action, SemVersion targetVersion) {
			ReleaseInfo target = new ReleaseInfo();
			target.setReleaseVersion(targetVersion);
			this.change(action, target);
		}
	}
	
	/** Directorio base de todos los archivos */
	private static final Path WORK_DIR = Path.of(".");
	
	/** Nombre del jar de la aplicacion */
	private static final String APP_JAR_NAME = "webcontab.jar";
	
	/** Nombre temporal del nuevo jar descargado */
	private static final String TMP_JAR_NAME = "webcontab_new.jar";
	
	/** Comando que se debe ejecutar para levanar la app (en Windows) */
	private static final String APP_START_CMD = "cmd.exe /c start webcontab.bat";
	
	@Getter
	private UpdateStatus status = new UpdateStatus();
	private UpdaterConfig config;
	private Path tmpBaseDir;
	
	/** Info de la ultima actualizacion descargada (en el filesystem) */
	private ReleaseInfo lastDownloadedUpdate;
	
	@Inject
	private ConfigurableApplicationContext context;
	
	@Inject
	private DBService dbService;

	@Inject
	public Updater(UpdaterConfig config) throws IOException {
		this.config = config;
		
		// Carga del properties la info de la version actual
		this.loadCurrentVersion();
		
		// Obtiene el directorio temporal donde descargar las actualizaciones
		this.tmpBaseDir = this.config.getBackupDir().resolve("tmp");
		
		// Se crean los directorios necesarios
		if(Files.notExists(this.config.getBackupDir())) {
			log.info("No existe el directorio de backup de actualizaciones, se crea");
			Files.createDirectories(this.config.getBackupDir());
		}
		if(Files.notExists(this.tmpBaseDir)) Files.createDirectory(this.tmpBaseDir);
		
		// Se recorre el directorio de updates y se busca la ultima descargada (quizas quedo pendiente la actualizacion)
		this.lastDownloadedUpdate = this.findLastDownloadedUpdateinFileSystem();
		
		status.change(UpdateAction.IDLE, this.getPendingDownloadedUpdate());
		if(this.status.targetRelease != null) {
			log.warn("Hay un update pendiente descargado: '{}'", lastDownloadedUpdate);
		}
	}
	
	/** Obtiene info de la version actual */
	private void loadCurrentVersion() throws IOException {
		status.setCurrentRelease(new ReleaseInfo(FileUtils.readProps(config.getLocalVersionFile()), null));
	}
	
	/**
	 * Devuelve la actualizacion descargada pendiente o <code>null</code> si no hay pendientes.
	 */
	public ReleaseInfo getPendingDownloadedUpdate() {
		// Si no hay update pendiente, devuelve null
		if(this.lastDownloadedUpdate == null) return null;
		
		// Si no hay updates o hay pero son anteriores o iguales a la version actual, tampoco hay update pendiente
		if(!mustUpdate(status.getCurrentRelease().getReleaseVersion(), this.lastDownloadedUpdate.getReleaseVersion())) return null;
		
		return this.lastDownloadedUpdate;
	}
	
	/** 
	 * Chequea si hay una actualizacion pendiente en el servidor remoto
	 * @return info de la actualizacion pendiente o null si no hay 
	 */
	public UpdateInfo checkRemoteForUpdate() {
		// Se obtiene la info de la ultima version en el server
		UpdateInfo updateInfo = this.getUpdateInfo(this.config.getRemoteVersionURL());
		
		// Si no es necesaria la descarga, se devuelve null
		if(!mustDownloadVersion(updateInfo.getVersion())) return null;
		
		return updateInfo;
	}
	
	/**
	 * Descarga la ultima actualizacion del servidor remoto
	 */
	public void downloadUpdate() {
		
		// Si el actualizador esta ocupado, no se puede continuar
		this.checkIdle();
		
		try {
			// Se indica que el actualizador esta descargando
			status.change(UpdateAction.DOWNLOADING);
			
			// Se obtiene la info de la ultima version en el server
			UpdateInfo updateInfo = this.checkRemoteForUpdate();
			
			// Si no es necesaria la descarga, no se hace mas nada
			if(updateInfo == null) return;
			
			// Se actualiza el status con la version a descargar
			status.change(UpdateAction.DOWNLOADING, updateInfo.getVersion());
			
			// Se descarga el archivo corresponsiente a la version
			URI downloadUrl = this.config.getUrl().resolve(this.getFilename(updateInfo.getVersion()));
			Path updateFile = this.downloadFile(downloadUrl, config.getBackupDir(), "application/zip");
			log.info("Archivo '{}' descargado exitosamente en '{}'", downloadUrl, this.config.getBackupDir());
			
			// Se actualiza la ultima version descargada
			this.lastDownloadedUpdate = getInfoFromFile(updateFile);
			
		} finally {
			// Se vuelve a estado disponible
			status.change(UpdateAction.IDLE, this.getPendingDownloadedUpdate());
		}
	}
	
	private String getFilename(SemVersion version) {
		return "webcontab-" + version + ".zip";
	}
	
	/**
	 * Comprueba que el actualizador este disponible.
	 * @throws UpdaterBusyException si el actualizador no esta disponible
	 */
	public void checkIdle() throws UpdaterBusyException {
		if(this.status.action != UpdateAction.IDLE) throw new UpdaterBusyException("El Updater esta ocupado en estado: " + this.status);
	}
	
	/**
	 * Actualiza la aplicacion a la ultima version descargada
	 * @throws InvalidRequestException si no hay una nueva version descargada
	 */
	public void update() throws InvalidRequestException {
		// Si el actualizador esta ocupado, no se puede continuar
		this.checkIdle();
		
		try {
			// Se cambia el status
			status.change(UpdateAction.UPDATING, this.lastDownloadedUpdate);
			
			// Se obtiene info de la actualizacion a aplicar
			ReleaseInfo updateRelease = this.getPendingDownloadedUpdate();
			// Si no hay update pendiente no se hace nada
			if(updateRelease == null) throw new InvalidRequestException("No hay updates pendientes");
			
			// Se actualiza el status con la version que se esta actualizando
			status.change(UpdateAction.UPDATING, updateRelease);
			
			Path updateFile = config.getBackupDir().resolve(this.getFilename(updateRelease.getReleaseVersion()));
			Path tmpDir = Files.createTempDirectory(this.tmpBaseDir, updateRelease.getReleaseVersion().toString() + "_");
			
			// Se descomprime el update en el directorio temporal
			ZipUtils.unzip(updateFile, tmpDir);
			
			// Esto en realidad no es necesario, ya lo tengo
			Path releaseFile = tmpDir.resolve(this.config.getVersionFile());
			updateRelease = new ReleaseInfo(FileUtils.readProps(releaseFile), Files.size(releaseFile));
			
			// Loguea la info del update y se indica si hay que reiniciar
			boolean mustUpdateServer = this.analyzeAndLogRelease(updateRelease);

			// Se recorren los archivos del update
			for(Path file : FileUtils.getRootPaths(tmpDir)) {
				String destName = file.getFileName().toString(); 
				
				// Si es el jar pero no se debe actualizar, se saltea la copia
				if(destName.equals(APP_JAR_NAME)) {
					if(!mustUpdateServer) continue;
					destName= TMP_JAR_NAME;
				}
				
				// Se reemplazan los directorios y archivos actuales con los del update
				FileUtils.replace(file, WORK_DIR.resolve(destName));
			}
			
			// Se borra el directorio temporal
			FileUtils.deleteDir(tmpDir);
			
			// Se llama al Replacer
			if(mustUpdateServer) {
				log.info("Se debe actualizar el server");
				
				log.info("Se hace backup de la base de datos");
				dbService.backUpWithDate(BackupType.DB);
				
//				log.info("Se descargan las nuevas dependencias");
//				downloader.downloadDependencies(WORK_DIR.resolve(APP_JAR_NAME), config.getRepoDir());

				log.info("Se llama a la aplicacion updater que reemplaza el archivo y reinicia la app");
				status.change(UpdateAction.RESTARTING, updateRelease);
				
				// Se ejecuta el replacer que reinicia el servicio
				this.executeReplacer();
				
				// Se cierra el contexto para apagar la aplicacion
				context.close();
			} else {
				// Si no se reinicia el server, se debe actualizar la version actual usando el nuevo properties
				this.loadCurrentVersion();
			}
		
		} catch(WebContabException e) {
			throw e;
		} catch(Exception e) {
			throw new UpdaterException("Error al realizar update", e);
		} finally {
			status.change(UpdateAction.IDLE, this.getPendingDownloadedUpdate());
		}
	}
	
	/**
	 * Ejecuta el proceso externo replacer, que reemplaza el jar y lo reinicia
	 */
	private void executeReplacer() throws IOException, InterruptedException {
		String cmd = String.format("java -cp . Replacer \"%s\" \"%s\" \"%s\"", WORK_DIR.resolve(APP_JAR_NAME).toAbsolutePath(), WORK_DIR.resolve(TMP_JAR_NAME).toAbsolutePath(), APP_START_CMD);
		ExecUtils.exec(cmd);
	}
	
	/**
	 * Loguea la info de una release e indica si se debe actualizar el server
	 * @param release
	 * @return
	 */
	private boolean analyzeAndLogRelease(ReleaseInfo release) {
		log.info("Informacion de la nueva release:");
		log.info("Version: {} - Fecha: {}", release.getReleaseVersion(), release.getReleaseDate());
		
		boolean mustUpdateServer = mustUpdate(status.getCurrentRelease().getServerVersion(), release.getServerVersion());
		String logMessage = mustUpdateServer ? "Se actualiza" : "NO se actualiza";
		log.info("Server: {} - {} (version actual: {})", release.getServerVersion(), logMessage, status.getCurrentRelease().getServerVersion());
		
		return mustUpdateServer;
	}
	
	/**
	 * Determina si es necesario descargar la version remota.
	 * <p>
	 * Localmente se tienen dos versiones: la actual y la ultima que se descargo.
	 * <br>Para descargar la remota, debe ser mas nueva que ambas.
	 * </p>
	 * @return
	 */
	public boolean mustDownloadVersion(SemVersion remoteVersion) {
		// Determina cual es la mas nueva entre las dos locales (actual y ultima descargada)
		SemVersion maxVersion = this.lastDownloadedUpdate != null && mustUpdate(status.getCurrentRelease().getReleaseVersion(), this.lastDownloadedUpdate.getReleaseVersion()) ? this.lastDownloadedUpdate.getReleaseVersion() : status.getCurrentRelease().getReleaseVersion();

		// Si la version local debe ser actualizada con la remota, se indica que hay que descargar
		if(mustUpdate(maxVersion, remoteVersion)) {
			log.info("Nueva version disponible: '{}' (actual: '{}' - Ult descargada: '{}')", remoteVersion, status.getCurrentRelease().getReleaseVersion(), this.lastDownloadedUpdate);
			return true;
			
		} else if(log.isDebugEnabled()) {
			log.debug("La ultima version disponible es '{}'. No es necesario descargar nueva actualizacion (actual: '{}' - Ult descargada: '{}')", remoteVersion, status.getCurrentRelease().getReleaseVersion(), this.lastDownloadedUpdate);
		}
		return false;
	}

	/**
	 * Determina si se debe actualizar version.
	 * <p>
	 * Se debe actualizar si:
	 * <ul>
	 * <li>"to" es mas nueva.</li>
	 * <li>Ambas son la misma version y "from" es SNAPSHOT (siempre se considera a la misma version como mas nueva)</li>
	 * <li>"from" es null.</li>
	 * </ul>
	 * </p>
	 */
	private boolean mustUpdate(SemVersion from, SemVersion to) {
		if(to == null) return false;
		if(from == null) return true;
		
		if(to.isGreaterThan(from)) return true;
		
		return to.equals(from) && from.isSnapshot();
	}
	
	/**
	 * Busca la ultima actualizacion disponible en el filesystem local (ya descargado)
	 * @return
	 * @throws IOException
	 */
	private ReleaseInfo findLastDownloadedUpdateinFileSystem() throws IOException {
		SemVersion lastVersion = null; 
		
		// Itera las actualizaciones
		try(DirectoryStream<Path> files = Files.newDirectoryStream(config.getBackupDir(), "webcontab-*.zip")) {
			for(Path file : files) {
				// Obtiene la version del nombre del archivo
				String v = file.getFileName().toString().replace("webcontab-", "").replace(".zip", "");
				SemVersion version = new SemVersion(v);
				
				// Si este archivo es una version mas nueva, es la nueva lastVersion
				if(lastVersion == null || mustUpdate(lastVersion, version)) lastVersion = version;
			}
		}
		// Si no hay, devuelve null
		if(lastVersion == null) return null;
		
		// Si hay, lee el archivo properties dentro del zip para obtener las versiones
		return getInfoFromFile(config.getBackupDir().resolve(this.getFilename(lastVersion)));
	}
	
	/**
	 * Obtiene info de la release leyendo el zip de la actualizacion
	 * @param file
	 * @return
	 */
	private ReleaseInfo getInfoFromFile(Path file) {
		try {
			log.debug("Leyendo el archivo properties de {}", file);
			Properties p = new Properties();
			p.load(new StringReader(ZipUtils.readTextFileFromZip(file, "version.properties")));
			
			ReleaseInfo info = new ReleaseInfo(p, Files.size(file));
			info.setRequiresRestart(mustUpdate(status.getCurrentRelease().getServerVersion(), info.getServerVersion()));
			
			return info;
		} catch(IOException e) {
			throw new UpdaterException("Error al leer archivo de update " + file, e);
		}
	}

	/**
	 * Obtiene info del update del servidor remoto
	 * @param uri
	 * @return
	 */
	private UpdateInfo getUpdateInfo(URI uri) {
		try {
			HttpRequest req = HttpRequest.newBuilder(uri).build();
			
			HttpResponse<String> response = HttpClient.newBuilder()
					.build()
					.send(req, HttpResponse.BodyHandlers.ofString());
			
			if(response.statusCode() != 200) throw new WebApplicationException(response.statusCode());
			
			try(StringReader r = new StringReader(response.body())) {
				Properties p = new Properties();
				p.load(r);
				return new UpdateInfo(p);
			}
			
		} catch(Exception e) {
			throw new UpdaterException("Error al obtener la informacion de update", e);
		}
	}
	
	/**
	 * Descarga la actualizacion de la URI especificada
	 * @param uri
	 * @param directory
	 * @param mimeType
	 * @return
	 */
	private Path downloadFile(URI uri, Path directory, String mimeType) {
		try {
			HttpRequest req = HttpRequest.newBuilder(uri)
					.header("Accept", mimeType)
					.build();
			
			String filename = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
			
			HttpResponse<Path> response = HttpClient.newBuilder()
					.build()
					.send(req, HttpResponse.BodyHandlers.ofFile(directory.resolve(filename)));
			
			return response.body();
			
		} catch(Exception e) {
			throw new UpdaterException("Error al obtener al descargar archivo: " + uri, e);
		}
	}

}
