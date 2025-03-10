/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.neoforge.metadata;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Module Plugin for handling NeoforgeMetadata.
 * <p>
 *     Can be applied to any {@link org.gradle.api.plugins.PluginAware} but will only operate on
 *     {@link Project projects}
 */
public class NeoforgeMetadataPlugin implements Plugin<Object> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public NeoforgeMetadataPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(NeoforgeMetadataProjectPlugin.class);
        }
    }
}
