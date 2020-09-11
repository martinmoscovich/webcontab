package com.mmoscovich.webcontab.json;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mmoscovich.webcontab.model.PersistentEntity;

/**
 * Serializer de Jackson que permite solo incluir el id cuando se converte un {@link PersistentEntity} a JSON.
 *
 */
public class OnlyIdSerializer extends StdSerializer<PersistentEntity> {
	private static final long serialVersionUID = 1L;

	public OnlyIdSerializer() {
	        this(null);
	    }
	 
	    public OnlyIdSerializer(Class<PersistentEntity> t) {
	        super(t);
	    }
	 
	    @Override
	    public void serialize( PersistentEntity entity, JsonGenerator generator, SerializerProvider provider) 
	    		throws IOException, JsonProcessingException {

	        generator.writeObject(Map.of("id", entity.getId()));
	    }
	}