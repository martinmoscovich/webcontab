package com.mmoscovich.webcontab.exporter;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Organizacion;

/**
 * Clase que genera el reporte de Plan de cuentas en Excel.
 * 
 * <p>El item es de tipo {@link CuentaBase} y el contexto es el {@link Organizacion}.</p>
 */
public class ExcelPlanDeCuentasExporter extends ExcelReportBase<CuentaBase, Organizacion> {
	
	/** Indica el nivel inicial de este reporte (ya que no necesariamente empieza en el raiz */
	private Integer rootLevel;

	@Override
	protected void writeHeader() {
		CellStyle style = this.createCellStyle(this.createFont("Arial", 12, IndexedColors.BLACK, true, false), HorizontalAlignment.CENTER);
		
	    // Fila Organizacion
		this.addRow();
		
		Cell cell = this.setCell(0, this.context.getNombre() + " (" + this.context.getCuit() + ")");
		cell.setCellStyle(style);
        
		this.mergeCells(0, this.getMaxColumnIndex() + 3);
		 
        // Se deja una libre entre header y tabla
        this.addEmptyRow();

        // Fila Header Tabla
        this.addRow();
 
        this.setCell(0, "Código");
        this.setCell(1, "Descripción");
        this.setCell(2, "Nivel");
        this.setCell(3, "Moneda");
	}

	@Override
	protected void processRow(CuentaBase row) {
		this.addRow();
		 
        int columnCount = 0;
        
        // Obtiene el nivel de la categoria o cuenta y si es la primera, lo asigna como nivel inicial
        int nivel = row.getCodigo().split("\\.").length;
        if(rootLevel == null) rootLevel = nivel;

        // Calcula la indentacion de la descripcion segun el nivel de la cuenta y el inicial
        // Esto permite que se visualice mas parecido a un arbol
        String indentacion = StringUtils.repeat(" ", (nivel - rootLevel) * 4);
        
        // Se incluye codigo, descripcion, nivel y si es imputable
        this.setCell(columnCount++, row.getCodigo());
        this.setCell(columnCount++, indentacion + row.getDescripcion());
        this.setCell(columnCount++, nivel);

        if(row instanceof Cuenta) {
        	this.setCell(columnCount++, ((Cuenta)row).getMoneda().getCodigo() );
        }
	}

	@Override
	protected String getTempFilePrefix() {
		return "plan";
	}

	@Override
	protected String getSheetName() {
		return "Plan de Cuentas";
	}

	@Override
	protected int getMaxColumnIndex() {
		return 3;
	}

}
