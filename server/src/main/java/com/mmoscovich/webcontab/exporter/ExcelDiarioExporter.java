package com.mmoscovich.webcontab.exporter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;

/**
 * Clase que genera el reporte Diario (asientos con sus imputaciones) en Excel.
 * 
 * <p>El item es de tipo {@link Asiento} y el contexto es el {@link Ejercicio}.</p>
 */
public class ExcelDiarioExporter extends ExcelReportBase<Asiento, Ejercicio> {
	
	@Override
	protected void writeHeader() {
	    
	    CellStyle style = this.createCellStyle(this.createFont("Arial", 12, IndexedColors.BLACK, true, false), HorizontalAlignment.CENTER);
		
	    // Fila Organizacion
		this.addRow();
		
		Cell cell = this.setCell(0, this.context.getOrganizacion().getNombre() + " (" + this.context.getOrganizacion().getCuit() + ")");
		cell.setCellStyle(style);
        
		this.mergeCells(0, this.getMaxColumnIndex());
        
		// Fila Ejercicio
        this.addRow();
        cell = this.setCell(0, this.context.getInicio().format(formatter) + " - " + this.context.getFinalizacion().format(formatter));
        cell.setCellStyle(style);
        
        this.mergeCells(0, this.getMaxColumnIndex());
		 
        // Se deja una libre entre header y tabla
        this.addEmptyRow();

        // Fila Header Tabla
        this.addRow();
 
        this.setCell(0, "Número");
        this.setCell(1, "Fecha");
        this.setCell(2, "Cuenta");
        this.mergeCells(2, 3);
        this.setCell(4, "Detalle");
        this.setCell(5, "Debe");
        this.setCell(6, "Haber");
	}

	@Override
	protected void processRow(Asiento asiento) {
		this.addRow();
		 
        int columnCount = 0;

        // Cada asiento tiene su propio header con numero y fecha
        this.setCell(columnCount++, asiento.getNumero());
        this.setCell(columnCount++, asiento.getFecha(), "dd/MM/yyyy");
        
        // Luego se escribe cada imputacion del asiento
        for(Imputacion imp : asiento.getImputaciones()) {
        	this.writeImputacion(imp);
        }
	}

	@Override
	protected String getTempFilePrefix() {
		return "diario";
	}

	@Override
	protected String getSheetName() {
		return "Ejercicio";
	}

	@Override
	protected int getMaxColumnIndex() {
		return 6;
	}
	
	/**
	 * Escribe la fila de cada imputacion
	 * @param imputacion
	 */
	private void writeImputacion(Imputacion imputacion)  {
		int columnCount = 2;
		
		this.addRow();
		
		// Cada imputacion tiene codigo y descripcion de la cuenta, el detalle y el debe/haber
		this.setCell(columnCount++, imputacion.getCuenta().getCodigo());
		this.setCell(columnCount++, imputacion.getCuenta().getDescripcion());
		this.setCell(columnCount++, imputacion.getDetalle());
        
		// Si es HABER, se debe saltear una columna
        if(imputacion.getImporte().signum() < 0) columnCount++;
        
        this.setCellCurrency(columnCount++, imputacion.getImporte().abs(), imputacion.getCuenta().getMoneda().getId());
	}


}
