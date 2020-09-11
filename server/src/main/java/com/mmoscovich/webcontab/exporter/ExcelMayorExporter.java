package com.mmoscovich.webcontab.exporter;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;

import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;
import com.mmoscovich.webcontab.services.InformeService.MayorExporterContext;

/**
 * Clase que genera el reporte Mayor (imputaciones de una o mas cuentas) en Excel.
 * 
 * <p>El item es de tipo {@link Imputacion} y el contexto es el {@link MayorExporterContext},
 * que contiene el {@link Ejercicio} y los saldos anteriores.</p>
 */
public class ExcelMayorExporter extends ExcelReportBase<Imputacion, MayorExporterContext> {
	/** Id de la cuenta que se esta procesando actualmente */
	private Long cuentaId = null;
	
	@Override
	protected void writeHeader() {
	    
	    CellStyle style = this.createCellStyle(this.createFont("Arial", 12, IndexedColors.BLACK, true, false), HorizontalAlignment.CENTER);
		
	    // Fila Organizacion
		this.addRow();
		
		Cell cell = this.setCell(0, this.context.getEjercicio().getOrganizacion().getNombre() + " (" + this.context.getEjercicio().getOrganizacion().getCuit() + ")");
		cell.setCellStyle(style);
        
		this.mergeCells(0, this.getMaxColumnIndex());
        
		// Fila Ejercicio
        this.addRow();
        cell = this.setCell(0, this.context.getEjercicio().getInicio().format(formatter) + " - " + this.context.getEjercicio().getFinalizacion().format(formatter));
        cell.setCellStyle(style);
        
        this.mergeCells(0, this.getMaxColumnIndex());
	}
	
	@Override
	protected void processRow(Imputacion imputacion) {
		// Flag que indica si es la primera fila de esta cuenta
		boolean isFirst = false;
		
		// Se fija si la imputacion actual es la primera de una nueva cuenta
		if(!imputacion.getCuenta().getId().equals(cuentaId)) {
			// Se cambia el id para reflejar la actual
			cuentaId = imputacion.getCuenta().getId();
			// Se indica que es la primera
			isFirst = true;

			// Se agrega el header de cuenta
			this.addCuentaHeader(imputacion.getCuenta());
			
			// Se chequea si hay saldo anterior para la cuenta
		    if(context.getSaldosAnteriores() != null) {
		    	BigDecimal saldoAnterior = context.getSaldosAnteriores().get(imputacion.getCuenta().getId());
		    	if(saldoAnterior != null) {
		    		// Si hay saldo anterior, se agrega una fila con el valor correspondiente (como si fuera imputacion)
		    		this.addRow();
		    		this.setCell(2, "Saldo Anterior");
		    		this.setCellCurrency(5, saldoAnterior, imputacion.getCuenta().getMoneda().getId());
		    		
		    		// En este caso, la fila actual ya no sera la primera
		    		isFirst = false;
		    	}
		    }
		}
		
		// Se agrega la fila
		XSSFRow row = this.addRow();
		 
        int columnCount = 0;

        // Se incluye numero y fecha del asiento, detalle de la imputacion, debe/haber y saldo parcial
        Asiento asiento = imputacion.getAsiento();
        this.setCell(columnCount++, asiento.getNumero());
        this.setCell(columnCount++, asiento.getFecha(), "dd/MM/yyyy");
        
		this.setCell(columnCount++, imputacion.getDetalle());
        
		// Si es HABER, se debe saltear una columna
        if(imputacion.getImporte().signum() < 0) columnCount++;
        
        Long monedaId = imputacion.getCuenta().getMoneda().getId();

        this.setCellCurrency(columnCount++, imputacion.getImporte().abs(), monedaId);
        
        // Si es DEBE, se debe saltear una columna
        if(imputacion.getImporte().signum() >= 0) columnCount++;
        
        // Celda calculada de saldo parcial
        XSSFCell cell = row.createCell(columnCount, CellType.FORMULA);
    	cell.setCellStyle(this.buildCurrencyStyle(monedaId));
    	this.colNumberToLetter(columnCount);
    	
    	// Si es la primera fila, no debe considerar el saldo de la fila anterior
        if(isFirst) {
        	cell.setCellFormula(String.format("%s-%s", this.cellNumberToLetter(rowNumber, columnCount - 2), this.cellNumberToLetter(rowNumber, columnCount - 1)));
        } else {
        	cell.setCellFormula(String.format("%s+%s-%s", this.cellNumberToLetter(rowNumber - 1, columnCount), this.cellNumberToLetter(rowNumber, columnCount - 2), this.cellNumberToLetter(rowNumber, columnCount - 1)));
        }
	}
	
	private void addCuentaHeader(Cuenta cuenta) {
		// Se dejan dos filas libres antes de iniciar una cuenta
        this.addEmptyRow();
        this.addEmptyRow();
        
		// Fila Organizacion
		this.addRow();
		
		XSSFFont font = this.createFont("Arial", 10, IndexedColors.BLACK, true, false);
		Cell cell = this.setCell(0, "Cuenta");
		cell.setCellStyle(this.createCellStyle(font));
		
		// Datos de la cuenta (codigo - descripcion)
		this.setCell(1, cuenta.getCodigo() + " - " + cuenta.getDescripcion());
		this.mergeCells(1, this.getMaxColumnIndex());
				
		// Se deja una libre entre header y tabla
		this.addEmptyRow();
		
		// Fila Header Tabla
		this.addRow();
		
		CellStyle style = this.createCellStyle(font, HorizontalAlignment.CENTER);

		this.setCell(0, "Asiento").setCellStyle(style);
	    this.setCell(1, "Fecha").setCellStyle(style);
	    this.setCell(2, "Detalle").setCellStyle(style);
	    this.setCell(3, "Debe").setCellStyle(style);
	    this.setCell(4, "Haber").setCellStyle(style);
	    this.setCell(5, "Saldo").setCellStyle(style);
	    
	}

	@Override
	protected String getTempFilePrefix() {
		return "mayor";
	}

	@Override
	protected String getSheetName() {
		return "Ejercicio";
	}

	@Override
	protected int getMaxColumnIndex() {
		return 5;
	}
}
