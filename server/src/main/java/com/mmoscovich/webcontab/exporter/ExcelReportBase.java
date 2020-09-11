package com.mmoscovich.webcontab.exporter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mmoscovich.webcontab.exception.ServerException;
import com.mmoscovich.webcontab.model.Moneda;
import com.mmoscovich.webcontab.util.JpaUtils;

/**
 * Clase base para los reportes en Excel.
 * 
 * <p>Contiene la logica comun para todos los reportes y metodos utiles para simplificar la creacion de los mismos.</p>
 * <p>Utiliza el patron "template method" para que las subclases solo tengan que indicar como crear las secciones.</p>
 *
 * @param <E> Tipo del item que se procesa
 * @param <C> Tipo del contexto global que se usa durante la creacion.
 */
public abstract class ExcelReportBase<E, C> {
	
	protected DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	protected XSSFWorkbook workbook;
	protected XSSFSheet sheet;
	protected C context;
	protected EntityManager em;
	
	/** Index de la fila actual */
	protected int rowNumber = -1;
	
	/** Metodo llamado para crear el Header de la hoja */
	protected abstract void writeHeader();
	
	/**
	 * Metodo llamado por cada item
	 * @param row item
	 */
	protected abstract void processRow(E row);
	
	/**
	 * Metodo que devuelve el prefijo del archivo temporal
	 */
	protected abstract String getTempFilePrefix();
	
	/**
	 * Metodo que devuelve el nombre que tendra la hoja.
	 */
	protected abstract String getSheetName();
	
	/**
	 * Metodo que indica cual es el indice maximo de columna (empezando de 0) del reporte.
	 */
	protected abstract int getMaxColumnIndex();
	
	/**
	 * Genera el reporte, lo guarda en un archivo y devuelve su path.
	 * 
	 * @param em Entity Manager utilizado para hacer la query
	 * @param context contexto global del reporte, necesario para su creacion
	 * @param rows Stream de items a exportar
	 * 
	 * @return Path donde se guardo el reporte.
	 */
	public Path exportar(EntityManager em, C context, Stream<E> rows) {
		try {
			// Crea el archivo temporal
			Path file = Files.createTempFile(this.getTempFilePrefix(), ".xlsx");
			
			this.em = em;
			this.context = context;
			// Crea el libro y la hoja
			this.workbook = new XSSFWorkbook();
			this.sheet = workbook.createSheet(this.getSheetName());

			// Escribe el header
			this.writeHeader();
			
			// Recorre los items (al ser un stream, se van buscando en la base a medida que se necesitan)
			rows.forEach(row -> {
				// Procesa un item
				this.processRow(row);
				
				// Si el item esta dentro de la sesion, se lo remueve para liberar memoria
				if(JpaUtils.isEntity(em, row.getClass())) {
					em.detach(row);
				}
			});
			
			// Evalua las formulas para obtener los anchos finales de columna
			XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
			
			// Ajusta el ancho de la columna segun el valor con mayor longitud de cada una
			for(int i = 0; i <= this.getMaxColumnIndex(); i++) sheet.autoSizeColumn(i);
			
			// Escribe el libro en el archivo temporal
			try(OutputStream os = new BufferedOutputStream(new FileOutputStream(file.toFile()))){
				workbook.write(os);
				return file;
			}
		} catch (IOException e) {
			throw new ServerException("Error al exportar a Excel", e);
		} finally {
			try {
				this.close();
			} catch (IOException e) {
				throw new ServerException("Error al exportar a Excel", e);
			}
		}
	}
	
	/** Cierra el libro */
	private void close() throws IOException  {
		this.em = null;
		if(this.workbook != null) {
			this.workbook.close();
			this.workbook = null;
		}
	}
	
	/**
	 * Agrega una fila a la hoja.
	 * 
	 * @return la fila creada
	 */
	protected XSSFRow addRow() {
		rowNumber++;
		if(this.sheet == null) throw new IllegalArgumentException("No hay hoja activa");
		return this.sheet.createRow(rowNumber);
	}
	
	/** 
	 * Agrega una fila vacia a la hoja (para dejar un espacio)
	 */
	protected void addEmptyRow() {
		rowNumber++;
	}
	
	/**
	 * Mergea celdas de la fila actual
	 * @param colStart indice de la columna donde comenzar
	 * @param colEnd indice de la columna donde terminar
	 */
	protected void mergeCells(int colStart, int colEnd) {
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, colStart, colEnd));
	}
	
	/**
	 * Asigna un texto en una celda de la fila actual
	 * @param colNumber numero de columna
	 * @param text texto
	 * @return La celda modificada
	 */
	protected XSSFCell setCell(int colNumber, String text) {
		XSSFCell cell = sheet.getRow(rowNumber).createCell(colNumber);
		cell.setCellValue(text);
		return cell;
	}
	
	/**
	 * Asigna un numero decimal en una celda de la fila actual
	 * 
	 * @param colNumber numero de columna
	 * @param value numero
	 * 
	 * @return La celda modificada
	 */
	protected XSSFCell setCell(int colNumber, double value) {
		XSSFCell cell = sheet.getRow(rowNumber).createCell(colNumber);
		cell.setCellValue(value);
		return cell;
	}
	
	
	/**
	 * Asigna un valor mometario en una celda de la fila actual
	 * 
	 * @param colNumber numero de columna
	 * @param value numero
	 * @param monedaId id de la moneda a utilizar (para el simbolo)
	 * 
	 * @return La celda modificada
	 */
	protected XSSFCell setCellCurrency(int colNumber, BigDecimal value, Long monedaId) {
		XSSFCell cell = this.setCell(colNumber, value.doubleValue());
		cell.setCellStyle(this.buildCurrencyStyle(monedaId));
		
		return cell;
	}
	
	/**
	 * Crea el estilo de tipo currency usando el simbolo de la moneda indicada 
	 * @param monedaId id de la moneda a utilizar (para el simbolo)
	 * @return
	 */
	protected CellStyle buildCurrencyStyle(Long monedaId) {
		// Se busca la moneda en la base (o la cache) y se obtiene el simbolo
		Moneda moneda = em.find(Moneda.class, monedaId);
		String simbolo = moneda != null ? moneda.getSimbolo() : "";
		
		CellStyle cellStyle = workbook.createCellStyle();
    	CreationHelper creationHelper = workbook.getCreationHelper();
    	// Formato que pone el simbolo y en caso de ser valor negativo, lo pone en rojo
    	cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("\"" + simbolo + "\" #,##0.00_);[Red]\"" + simbolo + "\" -#,##0.00"));
    	
    	return cellStyle;
	}
	
	/**
	 * Asigna una fecha en una celda de la fila actual
	 * 
	 * @param colNumber numero de columna
	 * @param date fecha
	 * 
	 * @return La celda modificada
	 */
	protected XSSFCell setCell(int colNumber, LocalDate date) {
		XSSFCell cell = sheet.getRow(rowNumber).createCell(colNumber);
		cell.setCellValue(date);
		return cell;
	}
	
	/**
	 * Asigna una fecha en una celda de la fila actual, aplicando el formato especificado
	 * 
	 * @param colNumber numero de columna
	 * @param date fecha
	 * @param format formato a aplicar
	 * 
	 * @return La celda modificada
	 */
	protected XSSFCell setCell(int colNumber, LocalDate date, String format) {
		XSSFCell cell = sheet.getRow(rowNumber).createCell(colNumber);
		
		// Crea el estilo para agregar el formato
		CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(format));
        cell.setCellStyle(cellStyle);
		
		cell.setCellValue(date);
		return cell;
	}
	
	/**
	 * Devuelve un Font que se puede aplicar en una celda, usando los parametros especificados
	 * @param name nombre del font
	 * @param size tamanio
	 * @param color
	 * @param bold
	 * @param italic
	 * @return el font creado
	 */
	protected XSSFFont createFont(String name, int size, IndexedColors color, boolean bold, boolean italic) {
		if(this.workbook == null) throw new IllegalArgumentException("No hay libro activo");
		
		XSSFFont font = workbook.createFont();
	    font.setFontName(name);
	    font.setFontHeightInPoints((short)size);
	    font.setColor(color.getIndex());
	    font.setBold(bold);
	    font.setItalic(italic);
	    
	    return font;
	}
	
	/**
	 * Crea un estilo para el font especificado.
	 * @param font
	 * @return estilo
	 */
	protected CellStyle createCellStyle(XSSFFont font) {
		return this.createCellStyle(font, null);
	}
	
	/**
	 * Crea un estilo para el font y la alineacion especificados.
	 * @param font
	 * @param alignment
	 * @return estilo
	 */
	protected CellStyle createCellStyle(XSSFFont font, HorizontalAlignment alignment) {
		CellStyle style = workbook.createCellStyle();
	    style.setFont(font);
	    if(alignment != null) style.setAlignment(HorizontalAlignment.CENTER);
	    return style;
	}
	
	/** Convierte unas cordenadas numericas en la representacion en texto (Ej: A2) */
	protected String cellNumberToLetter(int rowNumber, int colNumber) {
		return this.colNumberToLetter(colNumber) + (rowNumber + 1);
	}
	
	/**
	 * Convierte un numero de columna en la letra correspondiente (A, B, C, etc)
	 * @param colNumber
	 * @return
	 */
	protected String colNumberToLetter(int colNumber) {
		return CellReference.convertNumToColString(colNumber);
	}
	
	

}
