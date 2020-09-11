package com.mmoscovich.webcontab.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Clase con metodos para ejecutar procesos externos.
 */
public class ExecUtils {

	/**
	 * Ejecuta el comando especificado
	 * @param command
	 */
	public static void exec(String command) throws IOException, InterruptedException {
		exec(command, null, false);
	}
	
	/**
	 * Ejecuta el comando especificado usando el directorio indicado como work dir.
	 * @param command comando a ejecutar
	 * @param cwd directorio de trabajo
	 * @param silent indica si debe imprimir en el stdout los mensajes del proceso
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void exec(String command, File cwd, boolean silent) throws IOException, InterruptedException {

		// Obtiene las variables de entorno para pasarselas al nuevo proceso
		List<String> env = CollectionUtils.map(System.getenv().entrySet(), e -> e.getKey() + "=" + e.getValue());
        
        // Ejecuta el comando y obtiene el proceso
        Process process = Runtime.getRuntime().exec(command, env.toArray(new String[] {}), cwd);

        String s = null;

        // Si no esta en modo silencioso, imprimir los mensajes
        if (!silent) {
            // Read the output from the
            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }
            }
        }

        // Mostrar cualquier tipo de error que se haya generado
        try (BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        }

        int exitVal = process.waitFor();
        if (exitVal != 0) {
            throw new RuntimeException("Error al ejecutar '" + command + "'");
        }
    }

}
