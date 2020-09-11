package com.mmoscovich.webcontab.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.dao.DBRepository;
import com.mmoscovich.webcontab.exception.ConflictException;
import com.mmoscovich.webcontab.exception.ServerException;
import com.mmoscovich.webcontab.util.FileUtils;
import com.mmoscovich.webcontab.util.ZipUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que administra los backups de base de datos
 */
@Slf4j
@Service
public class DBService {
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH_mm_ss");
	
	private static final String NAME_IN_ZIP = "db.sql";
	private static final String SQL_BACKUP_EXTENSION = ".sql.zip";
	
	private Path backupDir;
	private Path tmpDir;
	
	private DBRepository db;
	
	/**
	 * Formato del backup de base de datos
	 *
	 */
	public static enum BackupType { 
		/** Backup binario: una copia de la base de datos que se puede usar para reemplazar la existente. */
		DB, 
		
		/** Script SQL comprimido como ZIP que contiene todas las queries para crear tablas, indices y datos. */
		SQL 
	}

	@Inject
	public DBService(DBRepository db, @Value("${webcontab.db.backup.dir}") String backupDir) throws IOException {
		this.db = db;
		this.backupDir = Path.of(backupDir);
		this.tmpDir = this.backupDir.resolve("tmp");
		
		// Si no existe el directorio de backup, lo crea
		if(Files.notExists(this.backupDir)) {
			log.info("No existe el directorio de backup de base de datos, se crea");
			Files.createDirectories(this.backupDir);
		}
		
		// Si no existe el directorio temporal, lo crea
		if(Files.notExists(this.tmpDir)) Files.createDirectory(this.tmpDir);
	}
	
	/**
	 * Crea un backup usando la fecha como nombre.
	 * @param type tipo de backup (SCRIPT comprimido o BINARIO)
	 * @return info del backup creado
	 * @throws IOException
	 */
	public BackupItem backUpWithDate(BackupType type) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		return this.backup(now.format(formatter), type);
	}
	
	/**
	 * Crea un backup con el nombre indicado.
 	 * <p>
	 * @param name nombre del backup
	 * @param type tipo de backup (SCRIPT comprimido o BINARIO)
	 * @return info del backup creado
	 * @throws IOException
	 */
	public BackupItem backup(String name, BackupType type) throws IOException {
		Path backupFile = null;
		try {
			if(type == BackupType.DB) {
				// Backup binario
				backupFile = backupDir.resolve(name + ".zip");
				if(Files.exists(backupFile)) throw new ConflictException("Ya existe backup (tipo DB) con nombre '" + name + "'");
				
				// Se pide el backup a la base de datos
				log.debug("Creando backup binario de la base de datos con nombre '{}'", name);
				db.backupDB(backupFile);
				return new BackupItem(name, type, LocalDateTime.now());
				
			} else {
				// Backup Script SQL
				backupFile = backupDir.resolve(name + SQL_BACKUP_EXTENSION);
				if(Files.exists(backupFile)) throw new ConflictException("Ya existe backup (tipo DB) con nombre '" + name + "'");
				
				// Se crea un dir temporal para guardar el script
				Path bupTempDir = getTempDir();
				Path sqlPath = bupTempDir.resolve(NAME_IN_ZIP);
				
				log.debug("Creando backup SQL (comprimido) de la base de datos con nombre '{}'", name);
				
				try {
					// Se pide el script a la base de datos
					db.backupSQL(sqlPath);
					
					// Se comprime
					ZipUtils.zip(backupFile, Arrays.asList(sqlPath));
					return new BackupItem(name, type, LocalDateTime.now());
					
				} finally {
					// Se borran los archivos temporales
					FileUtils.deleteDir(bupTempDir);
				}
			}
		} catch(Exception e) {
			if(backupFile != null) Files.deleteIfExists(backupFile);
			throw e;
		}
	}
	
//	public void restoreSQL(String name) throws EntityNotFoundException, IOException {
//		Path zipFile = backupDir.resolve(name + SQL_BACKUP_EXTENSION);
//		if(Files.notExists(zipFile)) throw new EntityNotFoundException("No se encontro el backup con nombre '" + name + "'");
//		
//		log.debug("Recuperando backup SQL (comprimido) de la base de datos con nombre '{}'", name);
//		
//		Path outDir = getTempDir();
//		try { 
//			ZipUtils.unzip(zipFile, outDir);
//			
//			Path sqlPath = outDir.resolve(NAME_IN_ZIP);
//			if(Files.notExists(sqlPath)) throw new ServerException("Se encontro el backup '" + name + "' pero no tiene el contenido esperado");
//			
//			db.runSQL(sqlPath);
//			
//		} finally {
//			FileUtils.deleteDir(outDir.toFile());
//		}
//	}
	
	/**
	 * Devuelve la lista de backups disponibles en el filesystem
	 * @return
	 * @throws IOException
	 */
	public List<BackupItem> list() throws IOException {
		return getBackupStream().collect(Collectors.toList());
	}
	
	/**
	 * Devuelve el ultimo backup disponible en el filesystem
	 * @return
	 * @throws IOException
	 */
	public Optional<BackupItem> getLastBackup() throws IOException {
		return getBackupStream().max((b1, b2) -> b1.getTs().compareTo(b2.getTs()));
	}
	
	/**
	 * Lee el directorio de backup y obtiene la lista de backups disponibles.
	 * @return
	 * @throws IOException
	 */
	private Stream<BackupItem> getBackupStream() throws IOException {
		return Files.list(this.backupDir).filter(file -> file.toString().endsWith(".zip")).map(path -> new BackupItem(path, formatter));
	}
	
	private Path getTempDir() throws IOException {
		return Files.createTempDirectory(tmpDir, "db-backup");
	}
	
	/**
	 * DTO que representa un item de backup.
	 */
	@Data
	@AllArgsConstructor
	public static class BackupItem {
		/** Nombre del backup */
		private String nombre;
		
		/** Tipo de backup */
		private BackupType tipo;
		
		/** Fecha del backup */
		private LocalDateTime ts;
		
		/**
		 * Dado un archivo, extrae la informacion del backup.
		 * @param path
		 * @param formatter
		 */
		public BackupItem(Path path, DateTimeFormatter formatter) {
			String filename = path.getFileName().toString();
			if(filename.endsWith(SQL_BACKUP_EXTENSION)) {
				this.nombre = filename.replace(SQL_BACKUP_EXTENSION, "");
				this.tipo = BackupType.SQL;
			} else {
				this.nombre = filename.replace(".zip", "");
				this.tipo = BackupType.DB;
			}
			try {
				// La fecha se intenta obtener del nombre del archivo
				this.ts = LocalDateTime.parse(filename.replace(".zip", ""), formatter);
			} catch(DateTimeParseException e) {
				try {
					// Si el nombre no es una fecha, se busca la fecha de modificacion del archivo.
					this.ts = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.UTC);
				} catch(IOException ioe) {
					throw new ServerException("Error al obtener fecha del backup", ioe);
				}
			}
		}
	}
		
}
