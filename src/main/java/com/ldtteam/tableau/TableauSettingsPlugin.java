package com.ldtteam.tableau;

import com.ldtteam.tableau.extensions.ModuleFeatures;
import com.ldtteam.tableau.utilities.UtilitiesPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * The core tableau plugin for {@link Settings}.
 * <p>
 *     Configures the feature module management for the entire gradle project tree.
 * <p>
 *     Also applies the {@link TableauPlugin} to all projects.
 */
public class TableauSettingsPlugin implements Plugin<Settings> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public TableauSettingsPlugin() {
    }

    @Override
    public void apply(@NotNull Settings target) {
        target.getPlugins().apply(UtilitiesPlugin.class);

        ModuleFeatures moduleFeatures = target.getGradle().getExtensions().create(ModuleFeatures.EXTENSION_NAME, ModuleFeatures.class);
        target.getExtensions().add(ModuleFeatures.EXTENSION_NAME, moduleFeatures);

        target.getGradle().beforeProject(project -> {
            project.getPlugins().apply(TableauPlugin.class);
        });
    }
}
