/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.local.file.configuration;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LocalFileConfigurationProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        if (target.file("gradle/configuration.gradle").exists()) {
            target.getLogger().lifecycle("Found configuration.gradle file, applying file configuration plugin.");
            target.apply(Map.of("from", "gradle/configuration.gradle"));
        }

        if (target.file("gradle/local.configuration.gradle").exists()) {
            target.getLogger().lifecycle("Found local.configuration.gradle file, applying local file configuration plugin.");
            target.apply(Map.of("from", "gradle/local.configuration.gradle"));
        }

        target.afterEvaluate(project -> {
            target.fileTree("gradle", spec -> spec.include("**/*.gradle")).visit(details -> {
                if (details.isDirectory())
                    return;

                // We load local files last.
                if (details.getFile().getName().contains("local"))
                    return;

                // We skip configuration files
                if (details.getFile().getName().contains("configuration"))
                    return;

                project.getLogger().lifecycle("Applying configuration file: " + details.getFile().getName());
                project.apply(Map.of("from", details.getFile()));
            });

            target.fileTree("gradle", spec -> spec.include("**/*.gradle")).visit(details -> {
                if (details.isDirectory())
                    return;

                // We load local files last.
                if (!details.getFile().getName().contains("local"))
                    return;

                // We skip configuration files
                if (details.getFile().getName().contains("configuration"))
                    return;

                project.getLogger().lifecycle("Applying configuration file: " + details.getFile().getName());
                project.apply(Map.of("from", details.getFile()));
            });
        });
    }


}
