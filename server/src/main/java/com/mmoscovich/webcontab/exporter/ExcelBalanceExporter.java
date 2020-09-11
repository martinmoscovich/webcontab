package com.mmoscovich.webcontab.exporter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.mmoscovich.webcontab.dto.informes.BalanceCuenta;
import com.mmoscovich.webcontab.model.Ejercicio;

/**
 * Clase que genera el reporte del Balance (saldo de cada cuenta) en Excel.
 * 
 * <p>El item es de tipo {@link BalanceCuenta} y el contexto es el {@link Ejercicio}.</p>
 */
public class ExcelBalanceExporter extends ExcelReportBase<BalanceCuenta, Ejercicio> {

	@Override
	protected void writeHeader() {
		CellStyle style = this.createCellStyle(this.createFont("Arial", 12, IndexedColors.BLACK, true, false), HorizontalAlignment.CENTER);
		
	    // Fila Organizacion
		this.addRow();
		
		Cell cell = this.setCell(0, this.context.getOrganizacion().getNombre() + " (" + this.context.getOrganizacion().getCuit() + ")");
		cell.setCellStyle(style);
        
		this.mergeCells(0, this.getMaxColumnIndex() + 3);
        
		// Fila Ejercicio
        this.addRow();
        cell = this.setCell(0, this.context.getInicio().format(formatter) + " - " + this.context.getFinalizacion().format(formatter));
        cell.setCellStyle(style);
        
        this.mergeCells(0, this.getMaxColumnIndex() + 3);
		 
        // Se deja una libre entre header y tabla
        this.addEmptyRow();

        // Fila Header Tabla
        this.addRow();
 
        this.setCell(0, "Código");
        this.setCell(1, "Descripción");
        this.setCell(2, "Saldo");
	}

	@Override
	protected void processRow(BalanceCuenta row) {
		this.addRow();
		 
        int columnCount = 0;

        // Por cada fila se pone el codigo y descipcion de la cuenta, y su saldo.
        this.setCell(columnCount++, row.getCodigo());
        this.setCell(columnCount++, row.getDescripcion());
        this.setCellCurrency(columnCount++, row.getSaldo(), row.getMonedaId());
	}

	@Override
	protected String getTempFilePrefix() {
		return "balance";
	}

	@Override
	protected String getSheetName() {
		return "Ejercicio";
	}

	@Override
	protected int getMaxColumnIndex() {
		return 2;
	}

}
