/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.jarjar;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Module plugin for JarJar.
 * <p>
 *     Can be applied to any {@link org.gradle.api.plugins.PluginAware} but will only interact with {@link Project projects}
 */
public class JarJarPlugin implements Plugin<Object> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public JarJarPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(JarJarProjectPlugin.class);
        }
    }
}