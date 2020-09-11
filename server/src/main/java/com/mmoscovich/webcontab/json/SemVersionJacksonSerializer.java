
package com.mmoscovich.webcontab.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mmoscovich.webcontab.updater.SemVersion;

/**
 * Serializador de {@link SemVersion}.
 * <p>Utilizado para el actualizador.</p> 
 */
public class SemVersionJacksonSerializer extends JsonSerializer<SemVersion> {

	@Override
	public void serialize(SemVersion value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		
		gen.writeObject(value != null ? value.toString() : null);
	}

}
