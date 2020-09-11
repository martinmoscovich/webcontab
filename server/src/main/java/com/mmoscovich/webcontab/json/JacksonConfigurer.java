package com.mmoscovich.webcontab.json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mmoscovich.webcontab.updater.SemVersion;

/**
 * Configuracion del {@link ObjectMapper}.
 */
@Configuration
public class JacksonConfigurer {

	@Autowired
    public void customize(ObjectMapper mapper) {
		// Soporte para lazy de Hibernate
    	Hibernate5Module m = new Hibernate5Module();
    	m.enable(Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        mapper.registerModule(m);
        
        // Remover los atributos null
        mapper.setSerializationInclusion(Include.NON_NULL);
        
        // Soporte para LocalDate, Instant
        mapper.registerModule(new JavaTimeModule());
        
        // Soporte para procesar SemVersion
        SimpleModule mod = new SimpleModule();
        mod.addSerializer(SemVersion.class, new SemVersionJacksonSerializer());
        mapper.registerModule(mod);
    }
}