/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.scripting;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Cores scripting logic plugin.
 * <p>
 *     Can be applied to any object, however will internally bounce to the project specific plugin when it is applied to a project.
 * <p>
 *     If it is applied to an object {@link org.gradle.api.plugins.PluginAware} type, then the apply method will noop.
 */
public class ScriptingPlugin implements Plugin<Object> {

    /**
     * Creates a new scripting plugin.
     */
    @Inject
    public ScriptingPlugin() {
    }

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(ScriptingProjectPlugin.class);
        }
    }
}