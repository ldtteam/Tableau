package com.ldtteam.tableau.extensions;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.resource.processing.ResourceProcessingProjectPlugin;
import com.ldtteam.tableau.resource.processing.extensions.ResourceProcessingExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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

    @Inject
    public NeoGradleResourceProcessingExtension(Project project, ResourceProcessingExtension resourceProcessing) {
        resourceProcessing.getProperties().putAll(
                getInterpolateVersions().map(interpolate -> {
                    if (!interpolate) {
                        return Map.of();
                    }

                    final ModExtension mod = ModExtension.get(project);
                    final NeoGradleExtension neoGradle = NeoGradleExtension.get(project);
                    final Provider<String> minecraftVersionRange =
                            getMinimalMinecraftVersion().zip(mod.getMinecraftVersion(), "[%s,%s])"::formatted)
                                    .orElse(mod.getMinecraftVersion().map("[%s]"::formatted));

                    final Provider<String> forgeVersionRange =
                            getMinimalForgeVersion().zip(neoGradle.getNeoForgeVersion(), "[%s,))"::formatted)
                                    .orElse(neoGradle.getNeoForgeVersion().map("[%s,)"::formatted));

                    return Map.of("mcVersion", minecraftVersionRange, "forgeVersion", forgeVersionRange);
                })
        );
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "neogradle";

    /**
     * @return Indicates whether to interpolate versions.
     */
    public abstract Property<Boolean> getInterpolateVersions();

    /**
     * @return The minimal Minecraft version.
     */
    public abstract Property<String> getMinimalMinecraftVersion();

    /**
     * @return The minimal Forge version.
     */
    public abstract Property<String> getMinimalForgeVersion();
}
