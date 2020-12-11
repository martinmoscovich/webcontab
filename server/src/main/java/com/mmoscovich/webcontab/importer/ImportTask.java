package com.mmoscovich.webcontab.importer;

import java.nio.file.Path;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Datos de una tarea de importacion.
 * <p>
 * Es necesario porque la importacion es una tarea de 2 pasos, primero se sube el archivo y se lo analiza y luego 
 * el usuario confirma.
 * <br>Ademas es un proceso async, y mientras se ejecuta el usuario puede requerir informacion del progreso.
 * </p>
 * <p>
 * Por lo tanto, se requiere este modelo para guardar temporalmente la tarea asociada a un ID.</p>
 *
 */
@Data
@AllArgsConstructor
public class ImportTask {
	private UUID uuid;
	
	/** Path de donde se guardo el MDB a importar */
	@JsonIgnore
	private Path file;
	
	/** 
	 * Organizacion asociada al MDB.
	 * Puede ser nueva o existente 
	 * */
	private Organizacion organizacion;
	
	/**
	 * Ejercicio asociado al MDB.
	 * Puede ser nuevo o existente.
	 */
	private Ejercicio ejercicio;
	
	/** Status de la tarea*/
	private ImportStatus status = ImportStatus.PENDING;
	
	/** Mensaje de error, cuando ocurre uno */
	private String error;
	
	/** Estadisticas de la importacion */
	private ImportTaskSummary summary;
	
	/** Usuario que pidio la importacion, se usa para el usuario de creacion */
	private User user;
	
	public ImportTask(UUID uuid, Path file, Organizacion org, Ejercicio ej) {
		this.uuid = uuid;
		this.file = file;
		this.organizacion = org;
		this.ejercicio = ej;
		this.summary = new ImportTaskSummary();
	}
	public void error(String error) {
		this.error = error;
		this.status = ImportStatus.ERROR;
	}
	public void success() {
		this.status = ImportStatus.FINISHED;
	}
	
	/**
	 * Indica si esta tarea puede importar el ejercicio (o solo org y cuentas)
	 * @return
	 */
	@JsonProperty("puedeImportarEjercicio")
	public boolean puedeImportarEjercicio() {
		// Si el ejercicio es null significa que generaba conflicto (solapamiento) con 1 o mas existentes. No se puede importar
		// Si no es null pero tiene id significa que el ejercicio ya existe. No se permiten cargar asientos en ej existentes
		return this.ejercicio != null && this.ejercicio.getId() == null;
	}
	
	/** Posible status de la tarea */
	public static enum ImportStatus {
		PENDING, IMPORTING, FINISHED, ERROR
	}
	
	
	@Data
	public static class ImportTaskSummary {
		/** Cantidad de organizaciones importadas */
		private int organizacionesImportadas;
		
		/** Cantidad de ejercicios importados */
		private int ejerciciosImportados;
		
		/**
		 * Cantidad total de cuentas en el MDB.
		 * <br>Incluye las importadas y las que ya existian. 
		 */
		private int cuentasTotales;
		
		/** Cantidad de cuentas importadas */
		private int cuentasImportadas;
		
		/** 
		 * Cantidad total de categorias en el MDB.
		 * <br>Incluye las importadas y las que ya existian. 
		 */
		private int categoriasTotales;
		
		/** Cantidad de categorias importadas */
		private int categoriasImportadas;
		
		/** Cantidad de asientos importados */
		private int asientosImportados;
		
		/** Cantidad de imputaciones importadas */
		private int imputacionesImportadas;
		
		public void addOrganizacion() {
			this.organizacionesImportadas++;
		}
		public void addEjercicio() {
			this.ejerciciosImportados++;
		}
		
		public void addCuenta() {
			this.cuentasTotales++;
		}
		public void addCuentaImportada() {
			this.cuentasImportadas++;
		}
		public void addCategoria() {
			this.categoriasTotales++;
		}
		public void addCategoriaImportada() {
			this.categoriasImportadas++;
		}
		public void addAsiento() {
			this.asientosImportados++;
		}
		public void addImputaciones(int count) {
			this.imputacionesImportadas += count;
		}
	}
}


