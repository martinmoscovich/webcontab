package com.mmoscovich.webcontab.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Helper para el manejo de archivos Zip
 *
 */
public class ZipUtils {

	/**
	 * Crea un zip con la lista de archivos y directorios indicados
	 * @param zipFile path al zip resultante
	 * @param sources archivos o directorios a comprimir
	 * @throws IOException
	 */
	public static void zip(Path zipFile, List<Path> sources) throws IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFile.toFile()));
				ZipOutputStream zipOut = new ZipOutputStream(bos)) {

			for (Path source : sources) {
				File f = source.toFile();
				zipFile(f, f.getName(), zipOut);
			}
		}
	}

	/**
	 * Crea una entrada dentro de un archivo zip, con el contenido de un archivo o un directorio (recursivamente).
	 * 
	 * @param fileToZip archivo o directorio a comprimir
	 * @param fileName nombre del archivo dentro del zip
	 * @param zipOut stream del zip
	 * @throws IOException
	 */
	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			// Si es un directorio, agrega una entrada para el mismo
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			// Recorre los archivos y subdirectorios y los agrega
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		
		// Si es un archivo, lo agrega
		zipOut.putNextEntry(new ZipEntry(fileName));
		Files.copy(fileToZip.toPath(), zipOut);
		zipOut.closeEntry();
	}
	
	/**
	 * Descomprime un archivo zip en el directorio indicado
	 * @param zipFile archivo a descomprimir
	 * @param dirDestination directorio destino
	 * @throws IOException
	 */
	public static void unzip(Path zipFile, Path dirDestination) throws IOException {
        try(ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile.toFile())))) {
        	
	        ZipEntry zipEntry = zis.getNextEntry();
	        
	        while (zipEntry != null) {
	        	// Obtiene la ruta donde copiar el archivo o directorio
	            File newFile = newFile(dirDestination.toFile(), zipEntry);
	            
	            if (!zipEntry.isDirectory()) {
	                // Si es un directorio se copia el contenido 
	            	try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile))) {
	            		IOUtils.copy(zis, bos);
	            	}
	            } else {
	                // Si es un directorio, simplemente se crea
	            	if(!newFile.exists()) newFile.mkdirs();
	            }
	            
	            zis.closeEntry();
	            zipEntry = zis.getNextEntry();
	        }
        }
    }
	
	/**
	 * Descomprime una entrada dentro del archivo zip.
	 * @param zipFile archivo zip
	 * @param dirDestination directorio base donde descomprimir
	 * @param entryPath entrada a descomprimir
	 * @return Path donde se descomprimio la entrada
	 * @throws IOException
	 */
	public static Path unzipEntry(Path zipFile, Path dirDestination, String entryPath) throws IOException {
        try(ZipFile zip = new ZipFile(zipFile.toFile())) {
	        ZipEntry entry = zip.getEntry(entryPath);
	        if(entry == null) throw new IllegalArgumentException("No se encontro la entrada '" + entryPath + "' en el archivo '" + zipFile + "'");
	        if (entry.isDirectory()) throw new IllegalArgumentException("La entrada '" + entryPath + "' es un directorio en el archivo '" + zipFile + "'");
	        
            // if the entry is a file, extracts it
	        File newFile = newFile(dirDestination.toFile(), entry);
        	try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile)); InputStream is = zip.getInputStream(entry)) {
        		IOUtils.copy(is, bos);
        	}
        	return newFile.toPath();
        }
	}
	
	/**
	 * Lee el contenido de un archivo de texto dentro de un Zip
	 * @param zipFile archivo zip
	 * @param entryPath ruta del archivo dentro del zip
	 * @return contenido del archivo de texto
	 * @throws IOException
	 */
	public static String readTextFileFromZip(Path zipFile, String entryPath) throws IOException {
        try(ZipFile zip = new ZipFile(zipFile.toFile())) {
	        ZipEntry entry = zip.getEntry(entryPath);
	        if(entry == null) throw new IllegalArgumentException("No se encontro la entrada '" + entryPath + "' en el archivo '" + zipFile + "'");

	        try(InputStream is = zip.getInputStream(entry)) {
	        	return IOUtils.toString(is, "UTF-8");
	        }
        }
	}
	
	/**
	 * Obtiene la ruta donde se debe crear el archivo o directorio, usando un directorio base y la ruta de la entrada 
	 * @param destinationDir directorio base
	 * @param zipEntry entrada dentro archivo zip
	 * @return
	 * @throws IOException
	 */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
         
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
         
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
         
        return destFile;
    }
}
