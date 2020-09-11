package com.mmoscovich.webcontab;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

/**
 * Filtro para permitir utilizar la aplicacion cliente como SPA.
 * <p>Toda llamada no reconocida se envia al index (y se rutea en el cliente).</p>
 * @author Martin
 *
 */
@Component
@WebFilter(value = "/*")
public class SpaFilter implements Filter {
	
	private UrlPathHelper helper = new UrlPathHelper();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;

		String path = helper.getPathWithinApplication(req);
		
		// Si es un archivo especifico (.html, .css, .js, etc) o es parte de la api, se deja pasar
		if(path.contains(".") || path.startsWith("/api") || path.startsWith("/h2")) {
			chain.doFilter(request, response);
		} else {
			// Si no, al index
			req.getRequestDispatcher("/index.html").forward(request, response);
		}
		
		
	}

}