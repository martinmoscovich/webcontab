package db.migration;

import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import com.mmoscovich.webcontab.model.Cuenta;

/**
 * Calcula los valores del campo alias y orden en la tabla Cuenta.
 * @author Martin
 *
 */
public class V2__Cuenta_orden_y_alias extends BaseJavaMigration {
	
	public void migrate(Context context) throws Exception {
		try (Statement select = context.getConnection().createStatement()) {
            try (ResultSet rows = select.executeQuery("SELECT id, codigo, legacy_codigo FROM CUENTA")) {
                while (rows.next()) {
                    Long id = rows.getLong("id");

                    // Si hay legacy Code, se usa como alias (quitando los ceros del final)
                    String legacyCode = rows.getString("legacy_codigo");
                    String alias = legacyCode != null ? "'" + legacyCode.replaceAll("0+$", "") + "'" : "null";
                    
                    // Se usa el codigo para generar el campo de orden
                    String codigo = rows.getString("codigo");
                    Cuenta c = new Cuenta();
                    c.setCodigo(codigo);
                    
                    try (Statement update = context.getConnection().createStatement()) {
                        update.execute("UPDATE CUENTA SET alias=" + alias + ", orden='" + c.getOrden() + "' WHERE id=" + id);
                    }
                }
            }
        }
    }

}
