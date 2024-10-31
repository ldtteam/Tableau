/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.maven.publishing;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

public class MavenPublishingPlugin implements Plugin<Object> {

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(MavenPublishingProjectPlugin.class);
        }
    }
}