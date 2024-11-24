/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.parchment;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Module plugin for handling parchment registration.
 * <p>
 *     Can be applied to any {@link org.gradle.api.plugins.PluginAware} but will only operate on
 *     {@link Project projects}
 */
public class ParchmentPlugin implements Plugin<Object> {

    /**
     * Creates a new instance of the Parchment plugin.
     */
    @Inject
    public ParchmentPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(ParchmentProjectPlugin.class);
        }
    }
}