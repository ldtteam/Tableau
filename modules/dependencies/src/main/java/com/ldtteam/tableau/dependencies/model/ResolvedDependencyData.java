package com.ldtteam.tableau.dependencies.model;

import java.io.File;

/**
 * Dependency information container, contains the dependency identifier and the original jar file.
 *
 * @param versionRange The version range of the dependency.
 * @param file         The jar file.
 */
public record ResolvedDependencyData(String versionRange, File file) {
}
