package com.ldtteam.tableau.dependencies.util;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.ldtteam.tableau.dependencies.model.CombinedDependencyData;
import com.ldtteam.tableau.dependencies.model.ModDependency;
import com.ldtteam.tableau.dependencies.model.ResolvedDependencyData;
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper;
import net.neoforged.neoforgespi.language.IConfigurable;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for helping resolving dependencies data.
 */
public class DependencyResolver {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyResolver.class);

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
    public static Provider<Set<ModDependency>> resolveDependencies(final Configuration configuration, final boolean required) {
        final Provider<CombinedDependencyData> combinedDependencyData = configuration.getIncoming().getArtifacts().getResolvedArtifacts()
                .zip(configuration.getIncoming().getResolutionResult().getRootComponent(), CombinedDependencyData::new);

        final Provider<Set<ResolvedDependencyData>> resolvedDependencies = combinedDependencyData.map(dependency -> dependency.artifacts().stream()
                .map(artifact -> resolveDependency(artifact, dependency.component()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        return resolvedDependencies.map(m -> m.stream()
                .flatMap(fm -> getModInfos(fm, required).stream())
                .collect(Collectors.toSet()));
    }

    @Nullable
    private static ResolvedDependencyData resolveDependency(final ResolvedArtifactResult resolvedArtifact, final ResolvedComponentResult component) {
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

    /**
     * Check whether the given component selector matches the provided resolved artifact.
     *
     * @param selector The input component selector.
     * @param artifact The resolved artifact.
     * @return True if the artifact identifier matches the component selector.
     */
    private static boolean componentMatches(final ModuleComponentSelector selector, final ResolvedArtifactResult artifact) {
        if (artifact.getId().getComponentIdentifier() instanceof ModuleComponentIdentifier artifactIdentifier) {
            return selector.getModuleIdentifier().equals(artifactIdentifier.getModuleIdentifier());
        }
        return false;
    }

    /**
     * Get a list of mod information classes for the given jar file.
     *
     * @param data     The input dependency data.
     * @param required Whether this dependency is required or not.
     * @return The list of mod info instances, or an empty list if no mods were found in the jar.
     */
    @NotNull
    private static List<ModDependency> getModInfos(final ResolvedDependencyData data, final boolean required) {
        try (final FileSystem fileSystem = FileSystems.newFileSystem(data.file().toPath())) {
            final Path path = fileSystem.getPath("META-INF/neoforge.mods.toml");
            if (!Files.exists(path)) {
                return List.of();
            }

            final FileConfig fileConfig = FileConfig.builder(path).build();
            fileConfig.load();
            fileConfig.close();
            final TomlFormat format = TomlFormat.instance();
            final UnmodifiableCommentedConfig unmodifiableConfig = format.createParser().parse(format.createWriter().writeToString(fileConfig)).unmodifiable();
            final NightConfigWrapper configWrapper = new NightConfigWrapper(unmodifiableConfig);

            final List<ModDependency> modInfos = new ArrayList<>();
            for (final IConfigurable config : configWrapper.getConfigList("mods")) {
                final Optional<String> modId = config.getConfigElement("modId");
                if (modId.isPresent()) {
                    LOGGER.info("Obtained the mod info for jar \"{}\", found mod with modId \"{}\"", data.file().getName(), modId.get());
                    modInfos.add(new ModDependency(modId.get(), data.versionRange(), required));
                }
            }
            return Collections.unmodifiableList(modInfos);
        } catch (Exception ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn("Failure obtaining the mod info for jar \"{}\", the provided jar doesn't seem to be a Neoforge mod file. Reason:", data.file().getName(), ex);
            } else {
                LOGGER.warn("Failure obtaining the mod info for jar \"{}\", the provided jar doesn't seem to be a Neoforge mod file.", data.file().getName());
            }
            return List.of();
        }
    }
}
