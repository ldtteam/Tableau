package com.ldtteam.tableau.dependencies.model;

/**
 * A container class for holding a runtime dependency that can be used by other modules.
 *
 * @param modId        The mod identifier.
 * @param versionRange The version range for the mod.
 * @param required     If the dependency is required or optional.
 */
public record ModDependency(String modId, String versionRange, boolean required) {
}
