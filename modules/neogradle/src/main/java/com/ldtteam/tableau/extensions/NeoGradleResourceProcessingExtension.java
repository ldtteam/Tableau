package com.ldtteam.tableau.extensions;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.resource.processing.extensions.ResourceProcessingExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Map;

/**
 * NeoGradle specific extension for resource processing.
 */
public abstract class NeoGradleResourceProcessingExtension {

    /**
     * Gets the NeoGradle resource processing extension for a given project.
     *
     * @param project The project.
     * @return The NeoGradle resource processing extension.
     */
    public static NeoGradleResourceProcessingExtension get(final Project project) {
        return ResourceProcessingExtension.get(project).getExtensions().findByType(NeoGradleResourceProcessingExtension.class);
    }

    /**
     * Creates a new instance of the NeoGradle resource processing extension.
     *
     * @param project            The project.
     * @param resourceProcessing The resource processing extension.
     */
    @Inject
    public NeoGradleResourceProcessingExtension(Project project, ResourceProcessingExtension resourceProcessing) {
        //Configure the interpolate versions flag.
        getInterpolateVersions().convention(true);

        //Configure the minimal versions.
        final ModExtension modExtension = ModExtension.get(project);
        getMinimalMinecraftVersion().convention(modExtension.getMinecraftVersion());

        //Configure the minimal forge version.
        getMinimalForgeVersion().convention(determineMinimalNeoforgeVersion(project));

        // Automatically interpolate resources if the interpolate versions flag is set.
        resourceProcessing.getProperties().putAll(
                //Check whether we should interpolate versions.
                getInterpolateVersions().map(interpolate -> {
                    if (!interpolate) {
                        //No interpolation needed.
                        return Map.of();
                    }

                    //Extract NeoGradle interpolation values.
                    final ModExtension mod = ModExtension.get(project);
                    final NeoGradleExtension neoGradle = NeoGradleExtension.get(project);

                    //Extract the minecraft version range.
                    final Provider<String> minecraftVersionRange =
                            getMinimalMinecraftVersion().zip(mod.getMinecraftVersion(), "[%s,%s])"::formatted)
                                    .orElse(mod.getMinecraftVersion().map("[%s]"::formatted));

                    //Extract the forge version range.
                    final Provider<String> forgeVersionRange =
                            getMinimalForgeVersion().zip(neoGradle.getNeoForgeVersion(), "[%s,))"::formatted)
                                    .orElse(neoGradle.getNeoForgeVersion().map("[%s,)"::formatted));

                    //Return the interpolation values.
                    return Map.of("mcVersion", minecraftVersionRange, "forgeVersion", forgeVersionRange);
                })
        );
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "neogradle";

    /**
     * Indicates whether to interpolate versions.
     *
     * @return Indicates whether to interpolate versions.
     */
    public abstract Property<Boolean> getInterpolateVersions();

    /**
     * The minimal supported minecraft version, is used for interpolation.
     *
     * @return The minimal Minecraft version.
     */
    public abstract Property<String> getMinimalMinecraftVersion();

    /**
     * The minimal supported forge version, is used for interpolation.
     *
     * @return The minimal Forge version.
     */
    public abstract Property<String> getMinimalForgeVersion();

    /**
     * Determines the minimal NeoForge version.
     * <p>
     * Resolves the minimal NeoForge version based on the configured NeoForge version.
     * If a hardcoded version is configured, it will be returned.
     * If a version range is configured, the latest version in the range will be returned.
     *
     * @param project The project.
     * @return The minimal NeoForge version.
     */
    private Provider<String> determineMinimalNeoforgeVersion(Project project) {
        final NeoGradleExtension neoGradleExtension = NeoGradleExtension.get(project);
        return neoGradleExtension.getNeoForgeVersion().map(neoForgeVersion -> {
            if (!neoForgeVersion.endsWith(".+")) {
                //We have a hard coded version.
                return neoForgeVersion;
            }

            final Dependency userdevDependency = project.getDependencies().create(
                    "net.neoforged:neoforged:%s:universal".formatted(neoForgeVersion)
            );
            final Configuration userdevResolveDependency = project.getConfigurations().detachedConfiguration(userdevDependency);
            final ResolvedConfiguration resolvedConfiguration = userdevResolveDependency.getResolvedConfiguration();
            final LenientConfiguration lenientConfiguration = resolvedConfiguration.getLenientConfiguration();

            final String defaultVersion = neoForgeVersion.substring(0, neoForgeVersion.length() - 2);

            return lenientConfiguration.getFirstLevelModuleDependencies()
                    .stream()
                    .filter(dep -> dep.getModuleGroup().equals("net.neoforged") && dep.getModuleName().equals("neoforged"))
                    .map(ResolvedDependency::getModuleVersion)
                    .findFirst()
                    .orElse(defaultVersion);
        });
    }
}
