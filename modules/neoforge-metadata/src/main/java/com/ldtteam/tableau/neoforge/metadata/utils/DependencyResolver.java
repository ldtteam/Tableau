package com.ldtteam.tableau.neoforge.metadata.utils;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.problems.Problems;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.Nullable;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.ldtteam.tableau.neoforge.metadata.components.model.ModDependency;

/**
 * Utility class for helping resolving dependencies data.
 */
public class DependencyResolver {

    /**
     * Utility class, not meant to be instantiated.
     */
    private DependencyResolver() {
    }

    /**
     * Find all the resolved dependencies.
     *
     * @param configuration The configuration to look for dependencies in.
     * @param required      Whether this dependency is required or not.
     * @return The set provider.
     */
    public static Provider<Set<ModDependency>> resolveDependencies(
            final Project project,
            final Problems problems,
            final Configuration configuration,
            final boolean required) {
        final Provider<CombinedDependencyData> combinedDependencyData = configuration.getIncoming().getArtifacts()
                .getResolvedArtifacts()
                .zip(configuration.getIncoming().getResolutionResult().getRootComponent(), CombinedDependencyData::new);

        final Provider<Set<ResolvedDependencyData>> resolvedDependencies = combinedDependencyData
                .map(dependency -> dependency.artifacts().stream()
                        .map(artifact -> resolveDependency(artifact, dependency.component()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));

        return resolvedDependencies.map(m -> m.stream()
                .flatMap(fm -> getModInfos(project, problems, fm, required).stream())
                .collect(Collectors.toSet()));
    }

    @Nullable
    private static ResolvedDependencyData resolveDependency(final ResolvedArtifactResult resolvedArtifact,
            final ResolvedComponentResult component) {
        final String versionRange = component.getDependencies().stream()
                .map(DependencyResult::getRequested)
                .filter(f -> f instanceof ModuleComponentSelector)
                .map(ModuleComponentSelector.class::cast)
                .filter(f -> componentMatches(f, resolvedArtifact))
                .map(m -> m.getVersionConstraint().getRequiredVersion())
                .findFirst()
                .orElse(null);
        if (versionRange == null) {
            return null;
        }

        return new ResolvedDependencyData(versionRange, resolvedArtifact.getFile());
    }

    private static boolean componentMatches(final ModuleComponentSelector selector,
            final ResolvedArtifactResult artifact) {
        if (artifact.getId().getComponentIdentifier() instanceof ModuleComponentIdentifier artifactIdentifier) {
            return selector.getModuleIdentifier().equals(artifactIdentifier.getModuleIdentifier());
        }
        return false;
    }

    private static List<ModDependency> getModInfos(final Project project, final Problems problems, final ResolvedDependencyData data, final boolean required) {
        try (final FileSystem fileSystem = FileSystems.newFileSystem(data.file().toPath())) {
            final Path path = fileSystem.getPath("META-INF/neoforge.mods.toml");
            if (!Files.exists(path)) {
                return List.of();
            }

            final FileConfig fileConfig = FileConfig.builder(path).build();
            fileConfig.load();
            
            final List<ModDependency> modInfos = new ArrayList<>();
            final List<CommentedConfig> mods = fileConfig.get("mods");  
            for (final CommentedConfig config : mods) {
                final String modId = config.get("modId");
                if (modId != null) {
                    final ModDependency modDependency = project.getObjects().newInstance(ModDependency.class, modId);
                    modDependency.getType().convention(required ? ModDependency.Type.REQUIRED : ModDependency.Type.OPTIONAL);
                    modDependency.getVersionRange().convention(data.versionRange());
                    modInfos.add(modDependency);
                }
            }
            return modInfos;
        } catch (final Exception e) {
            throw problems.forNamespace("tableau")
                .throwing(spec -> {
                    spec.id("metadata", "dependency-resolver");
                    spec.details("Failed to read the mod metadata from the file: " + data.file().getAbsolutePath());
                    spec.contextualLabel("Dependencies");
                    spec.fileLocation(data.file().getAbsolutePath());
                });
        }
    }

    private record CombinedDependencyData(Set<ResolvedArtifactResult> artifacts, ResolvedComponentResult component) {
    }

    private record ResolvedDependencyData(String versionRange, File file) {
    }
}
