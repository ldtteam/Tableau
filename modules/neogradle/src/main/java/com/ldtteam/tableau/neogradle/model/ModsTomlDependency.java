package com.ldtteam.tableau.neogradle.model;

import net.neoforged.neoforgespi.language.IModInfo.Ordering;

/**
 * A container class for holding a dependency that can be written to the neoforge.mods.toml file.
 *
 * @param modId        The mod identifier.
 * @param versionRange The version range for the mod.
 * @param required     If the dependency is required or optional.
 * @param ordering     How the dependency should be ordered compared to the current mod.
 */
public record ModsTomlDependency(String modId, String versionRange, boolean required, Ordering ordering)
{
}
