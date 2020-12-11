package com.mmoscovich.webcontab;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.mmoscovich.webcontab.exception.mapper.AccessDeniedExceptionMapper;
import com.mmoscovich.webcontab.exception.mapper.BeanValidationExceptionMapper;
import com.mmoscovich.webcontab.exception.mapper.JacksonMapppingExceptionMapper;
import com.mmoscovich.webcontab.exception.mapper.JacksonParseExceptionMapper;
import com.mmoscovich.webcontab.exception.mapper.UnhandledExceptionMapper;
import com.mmoscovich.webcontab.exception.mapper.WebApplicationExceptionMapper;
import com.mmoscovich.webcontab.exception.mapper.WebContabExceptionMapper;
import com.mmoscovich.webcontab.jaxrs.JacksonJaxRsProvider;
import com.mmoscovich.webcontab.jaxrs.WebContabJaxRsBinder;
import com.mmoscovich.webcontab.jaxrs.WebContabParamConverterProvider;
import com.mmoscovich.webcontab.resources.FirstTimeResource;
import com.mmoscovich.webcontab.resources.ImportResource;
import com.mmoscovich.webcontab.resources.InflacionResource;
import com.mmoscovich.webcontab.resources.MonedaResource;
import com.mmoscovich.webcontab.resources.OrganizacionResource;
import com.mmoscovich.webcontab.resources.ProvinciaResource;
import com.mmoscovich.webcontab.resources.SessionResource;
import com.mmoscovich.webcontab.resources.UserResource;
import com.mmoscovich.webcontab.resources.admin.AdminResource;
import com.mmoscovich.webcontab.resources.admin.AdminUserResource;
import com.mmoscovich.webcontab.resources.ejercicio.AsientoResource;
import com.mmoscovich.webcontab.resources.ejercicio.InformeResource;
import com.mmoscovich.webcontab.resources.organizacion.CategoriaResource;
import com.mmoscovich.webcontab.resources.organizacion.CuentaResource;
import com.mmoscovich.webcontab.resources.organizacion.EjercicioResource;

import lombok.AllArgsConstructor;

/**
 * Integracion Spring - Jersey
 * 
 */
@Component
@AllArgsConstructor
public class RestConfig extends ResourceConfig {
	
	@Inject
	private SessionContext session;

	@PostConstruct
	private void setup() {
		// Resources
        register(AsientoResource.class);
        register(ProvinciaResource.class);
        register(MonedaResource.class);
        register(CategoriaResource.class);
        register(CuentaResource.class);
        register(OrganizacionResource.class);
        register(EjercicioResource.class);
        register(InformeResource.class);
        register(ImportResource.class);
        register(SessionResource.class);
        register(AdminResource.class);
        register(AdminUserResource.class);
        register(UserResource.class);
        register(FirstTimeResource.class);
        register(InflacionResource.class);
        
        // Providers
        register(WebContabParamConverterProvider.class);
        register(JacksonJaxRsProvider.class);
        register(new WebContabJaxRsBinder(session));
    	
        // Exception Mappers
        register(WebContabExceptionMapper.class);
        
        register(JacksonMapppingExceptionMapper.class);
        register(JacksonParseExceptionMapper.class);
        register(BeanValidationExceptionMapper.class);
        register(WebApplicationExceptionMapper.class);
        register(AccessDeniedExceptionMapper.class);
        register(UnhandledExceptionMapper.class);
        
        // Multipart
        register(MultiPartFeature.class);
    }

}