package com.mmoscovich.webcontab;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configura Flyway para migraciones.
 * 
 * <br>Se usa este en lugar de la integracion con Spring Boot para que ejecute luego de que Hibernate actualiza el esquema 
 * 
 *
 */
@Configuration
@ConditionalOnProperty("webcontab.db.migration.enabled")
public class DBMigrationConfig {
	
	@Value("${webcontab.db.migration.repair}")
	private boolean repair;

	@Autowired
    public DBMigrationConfig(DataSource dataSource) {
        Flyway f = Flyway.configure().baselineOnMigrate(true).dataSource(dataSource).load();
        
        if(this.repair) f.repair();
        f.migrate();
    }
}
