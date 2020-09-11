package com.mmoscovich.webcontab.jaxrs;

import javax.ws.rs.ext.Provider;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.mmoscovich.webcontab.SessionContext;

import lombok.AllArgsConstructor;

/**
 * Permite inyectar el contexto de session en Resources JAX-RS
 */
@AllArgsConstructor
@Provider
public class WebContabJaxRsBinder extends AbstractBinder {
	
	private SessionContext session;
	
	@Override
	protected void configure() {
		bindFactory(new SessionContextFactory(session)).to(SessionContext.class).proxy(true).proxyForSameScope(false).in(RequestScoped.class);
	}
	
	public static class SessionContextFactory implements Factory<SessionContext> {
		private SessionContext session;
		
		public SessionContextFactory(SessionContext session) {
			this.session = session;
		}

		@Override
		public SessionContext provide() {
			return session;
		}

		@Override
		public void dispose(SessionContext instance) {
		}
		
	}

}
