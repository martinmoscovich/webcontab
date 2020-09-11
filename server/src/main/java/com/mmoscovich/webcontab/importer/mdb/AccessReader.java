package com.mmoscovich.webcontab.importer.mdb;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase encargada de leer los datos de la base MDB de WinContab
 */
@Slf4j
public class AccessReader implements AutoCloseable {
	private static final String openerClass = AccessOpener.class.getName();
	
	private Connection conn;
	private Statement st;
	
	@AllArgsConstructor
	public static class ImportedEjercicio {
		public String razonSocial;
		public String cuit;
		public LocalDate inicio;
		public LocalDate finalizacion;
		public int status;
	}
	
	@AllArgsConstructor
	public static class ImportedCuenta {
		public String codigo;
		public String descripcion;
		public BigDecimal saldoInicial;
		public boolean categoria;
		public Integer nivel;
		public boolean ajustable;
		// deudor?
		// saldoAjuste?
	}
	
	@AllArgsConstructor
	public static class ImportedAsiento {
		public short numero;
		public LocalDate fecha;
		public String detalle;
		public boolean apertura;
	}
	
	@AllArgsConstructor
	public static class ImportedMovimiento {
		public int numAsiento;
		// TODO Ver si usarlo
		public int numero;
		public String codigoCuenta;
		public BigDecimal monto;
		public String detalle;
	}
	
	/**
	 * Abre la conexion a la base de datos MDB especificada 
	 * @param path path del archivo MDB
	 * @throws SQLException
	 * @throws IOException
	 */
	public void open(Path path) throws SQLException, IOException {
		try {
			Connection conn = DriverManager.getConnection("jdbc:ucanaccess://" + path.toAbsolutePath().toString() + ";memory=true;skipIndexes=true;immediatelyReleaseResources=true;preventReloading=true;jackcessOpener=" + openerClass);
			st = conn.createStatement();
		} catch(Exception e) {
			this.close();
			throw e;
		}
	}
	
	/**
	 * Cierra la conexion a la base
	 */
	@Override
	public void close() throws IOException {
		try {
			try {
				if(st != null) st.close();
				st = null;
			} finally {
				if(conn != null) conn.close();
				conn = null;
			}
			
		} catch(SQLException e) {
			log.error("Error al importar de Access", e);
		}
	}
	
	
	private LocalDate dateToLocalDate(Date date) {
		return Instant.ofEpochMilli(date.getTime())
			      .atZone(ZoneId.systemDefault())
			      .toLocalDate();
	}
	
	/** Importa la organizacion y el ejercicio de la base de datos */
	public ImportedEjercicio getEjercicio() throws SQLException {
		try(ResultSet rs = st.executeQuery("SELECT * FROM Ejercicio")) {
			while(rs.next()) {
				return new ImportedEjercicio(rs.getString("Razon"), rs.getString("CUIT"), dateToLocalDate(rs.getDate("Apertura")), dateToLocalDate(rs.getDate("Cierre")), rs.getInt("Status"));
			}
		}
		return null;
	}
	
	/** Importa todas las cuentas de la base de datos */
	public List<ImportedCuenta> getCuentas() throws SQLException {
		List<ImportedCuenta> cuentas = new ArrayList<>();
		try(ResultSet rs = st.executeQuery("SELECT * FROM Cuentas ORDER BY CNum")) {
			while(rs.next()) {
				cuentas.add(new ImportedCuenta(rs.getString("CNum"), rs.getString("CDesc"), rs.getBigDecimal("CInicio"), rs.getBoolean("CRubro"), rs.getInt("CNivel"), rs.getBoolean("CAjuste")));
			}
		}
		return cuentas;
	}
	
	/** Importa todos los asientos de la base de datos */
	public List<ImportedAsiento> getAsientos() throws SQLException {
		List<ImportedAsiento> asientos = new ArrayList<>();
		try(ResultSet rs = st.executeQuery("SELECT * FROM Asientos ORDER BY ACtrl")) {
			while(rs.next()) {
				asientos.add(new ImportedAsiento(rs.getShort("ACtrl"), dateToLocalDate(rs.getDate("AFecha")), rs.getString("ALeyenda"), rs.getInt("ATipo") == 1));
			}
		}
		return asientos;
	}
	
	/** Importa todos los movimientos (imputaciones) de la base de datos */
	public List<ImportedMovimiento> getMovimientos() throws SQLException {
		List<ImportedMovimiento> movimientos = new ArrayList<>();
		try(ResultSet rs = st.executeQuery("SELECT * FROM Movimientos ORDER BY MACtrl, MNum")) {
			while(rs.next()) {
				ImportedMovimiento m = new ImportedMovimiento(rs.getInt("MACtrl"), rs.getInt("MNum"), rs.getString("MCNum"), rs.getBoolean("MDeudor") ? rs.getBigDecimal("MMonto") : rs.getBigDecimal("MMonto").negate(), rs.getString("MLeyenda"));
				movimientos.add(m);
			}
		}
		return movimientos;
	}

	
}
