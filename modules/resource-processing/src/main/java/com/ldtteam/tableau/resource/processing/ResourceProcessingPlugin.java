/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.resource.processing;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

public class ResourceProcessingPlugin implements Plugin<Object> {

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(ResourceProcessingProjectPlugin.class);
        }
    }
}
