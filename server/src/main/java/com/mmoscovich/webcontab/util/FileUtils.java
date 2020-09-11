package com.mmoscovich.webcontab.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helpers para el filesystem.
 *
 */
public class FileUtils {
	
	/**
	 * Escribe un archivo de texto en el filesystem
	 * @param path path al archivo a crear
	 * @param content texto a escribir
	 * @throws IOException
	 */
	public static void writeFile(Path path, String content) throws IOException {
		try (FileWriter writer = new FileWriter(path.toFile())) {
			writer.write(content);
		}
	}
	
	/**
	 * Reemplaza el contenido del destino con el del source.
	 * <p>Pueden ser ambos archivos o directorios.</p>
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void replace(Path src, Path dest) throws IOException {
		if(!Files.exists(src)) throw new IOException("Source file does not exist");
		
		if(Files.isDirectory(src)) {
			if(Files.exists(dest) && !Files.isDirectory(dest)) throw new IOException("Source is a directory but destination is a file");
			replaceDir(src, dest);
		} else {
			if(Files.exists(dest) && Files.isDirectory(dest)) throw new IOException("Source is a file but destination is a directory");
			replaceFile(src, dest);
		}
	}
	
	/**
	 * Reemplaza un archivo, borrando el existente
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void replaceFile(Path src, Path dest) throws IOException {
		Files.deleteIfExists(dest);
		Files.copy(src, dest);
	}
	
	/**
	 * Reemplaza un directorio, borrando el existente y copiando el nuevo recursivamente
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void replaceDir(Path src, Path dest) throws IOException {
		deleteDir(dest);
		copyDir(src, dest);
	}
	
	/** 
	 * Obtiene el contenido raiz de un directorio (no recursivo).
	 * @param dir directorio a leer
	 * @return lista de archivos y directorios dentro del directorio especificado
	 * @throws IOException
	 */
	public static List<Path> getRootPaths(Path dir) throws IOException {
		if(!Files.isDirectory(dir)) throw new IOException("Path " + dir.toString() + " is not a directory");
		try(Stream<Path> ps = Files.list(dir)) {
			return ps.collect(Collectors.toList());
		}
	}
	
	/**
	 * Elimina un directorio completo, recursivamente
	 * @param path
	 * @throws IOException
	 */
	public static void deleteDir(Path path) throws IOException {
		if(Files.exists(path)) Files.walkFileTree(path, new DeleteFileVisitor());
	}

	/**
	 * Copia un directorio completo, recursivamente
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void copyDir(Path src, Path dest) throws IOException {
		Files.walkFileTree(src, new CopyFileVisitor(dest));
	}

	/**
	 * Visitor para copiar un directorio entero jerarquicamente, incluyendo archivos y subdirectorios
	 */
	public static class CopyFileVisitor extends SimpleFileVisitor<Path> {
		private final Path targetPath;
		private Path sourcePath = null;

		public CopyFileVisitor(Path targetPath) {
			this.targetPath = targetPath;
		}

		@Override
		public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
			if (sourcePath == null) {
				sourcePath = dir;
			} else {
				Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
			return FileVisitResult.CONTINUE;
		}
	}

	/**
	 * Visitor para borrar un directorio entero jerarquicamente, incluyendo archivos y subdirectorios
	 */
	public static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
	}
	
	/**
	 * Lee un archivo properties del filesystem
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Properties readProps(Path file) throws IOException {
		Properties p = new Properties();
		try (InputStream is = Files.newInputStream(file)) {
			p.load(is);
			return p;
		}
	}

}
