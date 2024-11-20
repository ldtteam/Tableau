package com.ldtteam.tableau.neogradle.model;

import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;

import java.util.Set;

/**
 * Container class for combining the set of resolved artifacts and the resolved component result, turning them into a single provider.
 *
 * @param artifacts The set of the resolved artifacts.
 * @param component The resolved component result.
 */
public record CombinedDependency(Set<ResolvedArtifactResult> artifacts, ResolvedComponentResult component)
{
}
