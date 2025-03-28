/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.resource.processing;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Defines the resource processing module plugin
 * <p>
 *     Can be applied to any {@link org.gradle.api.plugins.PluginAware}, but will only operate
 *     on a {@link Project}.
 */
public class ResourceProcessingPlugin implements Plugin<Object> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public ResourceProcessingPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(ResourceProcessingProjectPlugin.class);
        }
    }
}
