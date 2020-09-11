package com.mmoscovich.webcontab.updater;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;
import org.springframework.boot.loader.thin.DependencyResolver;
import org.springframework.boot.loader.thin.PathResolver;

public class DependenciesDownloader {

	public List<Archive> downloadDependencies(Path jarFile, Path rootDir) throws IOException {
		PathResolver resolver = getResolver(rootDir);
		return resolver.resolve(null, new JarFileArchive(jarFile.toFile()), "thin", new String[] {});
	}
	
	private PathResolver getResolver(Path rootDir) {
		PathResolver resolver = new PathResolver(DependencyResolver.instance());
		resolver.setRoot(rootDir.toAbsolutePath().toString());
		return resolver;
//		if (StringUtils.hasText(locations)) {
//			resolver.setLocations(locations.split(","));
//		}
//		if (!"false".equals(offline)) {
//			resolver.setOffline(true);
//		}
//		if (!"false".equals(force)) {
//			resolver.setForce(true);
//		}
//		resolver.setOverrides(getSystemProperties());
	}
}
