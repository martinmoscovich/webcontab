package com.mmoscovich.webcontab;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mmoscovich.webcontab.updater.UpdaterConfig;

/**
 * Punto de entrada de la aplicacion
 *
 */
@EnableScheduling
@EnableCaching 
@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({UpdaterConfig.class})
public class WebContab {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(WebContab.class, args);
	}
}
