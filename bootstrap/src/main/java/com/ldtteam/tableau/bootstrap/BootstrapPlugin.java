/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.bootstrap;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * This defines the bootstrap system plugin for Tableau.
 * <p>
 *     Can be applied to any {@link org.gradle.api.plugins.PluginAware} will only operate on
 *     {@link Settings} however.
 */
@SuppressWarnings("unused") //Is a core entry point for a gradle plugin.
public class BootstrapPlugin implements Plugin<Object> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public BootstrapPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Settings settings) {
            settings.getPlugins().apply(BootstrapSettingsPlugin.class);
        }
    }
}
