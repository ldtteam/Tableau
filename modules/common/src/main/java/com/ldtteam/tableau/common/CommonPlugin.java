/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.common;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Plugin for the common module.
 * <p>
 *     Can be applied to any {@link org.gradle.api.plugins.PluginAware} but will only
 *     operate on {@link Project projects}
 */
public class CommonPlugin implements Plugin<Object> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public CommonPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(CommonProjectPlugin.class);
        }
    }
}