package com.mmoscovich.webcontab.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provee el ObjectMapper del contexto de Spring a JAX-RS
 *
 */
@Provider
public class JacksonJaxRsProvider implements ContextResolver<ObjectMapper> {

	@Inject
	private ObjectMapper mapper;
	
    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return mapper;
    }
}