/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.utilities;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Plugin for the utilities module.
 * <p>
 *     Can be applied to any {@link org.gradle.api.plugins.PluginAware} object
 *     but will only operate on {@link Project} and {@link Settings} instances.
 */
public class UtilitiesPlugin implements Plugin<Object> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public UtilitiesPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(UtilitiesProjectPlugin.class);
        } else if (target instanceof Settings settings) {
            settings.getPlugins().apply(UtilitiesSettingsPlugin.class);
        } else {
            throw new IllegalArgumentException("The object must be a Project or a Settings.");
        }
    }
}