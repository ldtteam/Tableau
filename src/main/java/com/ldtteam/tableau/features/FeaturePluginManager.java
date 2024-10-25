package com.ldtteam.tableau.features;

import com.ldtteam.tableau.extensions.ModuleFeatures;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import java.util.function.Function;

/**
 * Manager for features.
 */
public class FeaturePluginManager {

    /**
     * Apply a feature if it is enabled.
     *
     * @param project       The project to apply the feature to.
     * @param plugin        The plugin to apply.
     * @param featureEnabled The feature enabled property.
     */
    public static void applyFeaturePlugin(final Project project, final Class<? extends Plugin<?>> plugin, final Function<ModuleFeatures, Property<Boolean>> featureEnabled) {
        final ModuleFeatures features = ModuleFeatures.get(project);

        if (featureEnabled.apply(features).get()) {
            project.getPlugins().apply(plugin);
        }
    }
}
