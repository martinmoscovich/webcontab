package com.mmoscovich.webcontab.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.exception.ServerException;

/**
 * Clase que se encarga de leer el archivo de log para poder mostrarlo en pantalla
 */
@Service
public class LogReader {
	
	@Value("${logging.file}")
	private Path logFile;
	
	/**
	 * Lee las ultimas n lineas del archivo de log
	 * @param numLines lineas a leer
	 * 
	 * @return string del log con las lineas (en orden cronologico)
	 * @throws InvalidRequestException si el numero de lineas es menor a 1
	 */
	public String read(int numLines) throws InvalidRequestException {
		if(numLines < 1) throw new InvalidRequestException("Debe especificar un numero de lineas mayor a cero");
		
		// Une las lineas usando new line
		return String.join("\n", this.readLines(numLines));
	}
	
	/**
	 * Lee las ultimas n lineas del archivo de log
	 * @param numLines lineas a leer
	 * @return lista de lineas (en orden cronologico)
	 */
	private List<String> readLines(int numLines) {
		List<String> lines = new ArrayList<String>();
		
		// Lee el archivo
		try(ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile, Charset.forName("UTF8"))) {
			
			// Agrega al resultado como maximo las ultimas n lineas (si son menos, devuelve las que hay) 
			for(int i = 0; i < numLines; i++) {
				String line = reader.readLine();
				
				// Si no hay mas lineas, cortar el loop
				if(line == null) break;
				
				lines.add(line);
			}
			
			// Las invierte para que queden en orden cronologico 
			Collections.reverse(lines);
			return lines;
		} catch(IOException e) {
			throw new ServerException("Error al leer log", e);
		}
	}

}
