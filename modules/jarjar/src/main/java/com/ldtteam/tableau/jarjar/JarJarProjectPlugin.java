/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.jarjar;

import com.ldtteam.tableau.extensions.NeoGradleExtension;
import com.ldtteam.tableau.jarjar.extensions.JarJarExtension;
import com.ldtteam.tableau.neogradle.NeoGradlePlugin;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import net.neoforged.gradle.common.tasks.JarJar;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.bundling.Jar;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Project plugin for JarJar.
 * <p>
 *     Configures the contained configuration, the jar task and the jarjar task.
 *     Sets the jar task to have the slim classifier and the jarjar task to use the contained configuration.
 */
public class JarJarProjectPlugin implements Plugin<Project> {

    /**
     * Name of the configuration used by the jarjar plugin that contains the dependencies to be jar-in-jar'ed.
     */
    public static final String CONTAINED_CONFIGURATION_NAME = "contained";

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public JarJarProjectPlugin() {
    }

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(NeoGradlePlugin.class);

        NeoGradleExtension.get(target).getExtensions().create(JarJarExtension.EXTENSION_NAME, JarJarExtension.class);

        configureContainedConfiguration(target);
        configureJarTask(target);
        configureJarJarTask(target);
    }

    /**
     * Creates and configures the contained configuration.
     *
     * @param project the project to configure
     */
    private void configureContainedConfiguration(Project project) {
        final Configuration configuration = project.getConfigurations().maybeCreate(CONTAINED_CONFIGURATION_NAME);
        project.afterEvaluate(evaluatedProject -> {
            final JarJarExtension jarJar = JarJarExtension.get(evaluatedProject);
            configuration.setTransitive(!jarJar.getUsesNoneTransitiveJarJar().get());
        });
    }

    /**
     * Configures the jar task to use the slim classifier.
     * <p>
     *     This ensures that the jar-in-jar'ed jar is not overwritten by the original jar.
     * @param project the project to configure
     */
    private void configureJarTask(Project project) {
        project.getTasks().named("jar", Jar.class, jar -> {
            jar.getArchiveClassifier().set("slim");
        });
    }

    /**
     * Configures the jarjar task.
     * <p>
     *     Ensures that the primary sourcesets are included and the contained configuration is used.
     *
     * @param project the project to configure
     */
    private void configureJarJarTask(Project project) {
        final SourceSetExtension sourceSets = SourceSetExtension.get(project);
        final NeoGradleExtension neoGradle = NeoGradleExtension.get(project);

        project.getTasks().named("jarjar", JarJar.class, jar -> {
            jar.getArchiveClassifier().set(neoGradle.getPrimaryJarClassifier());

            sourceSets.getUniversalJarSourceSets().get().forEach(sourceSet -> {
                jar.from(sourceSet.getOutput());
            });

            jar.configuration(project.getConfigurations().getByName(CONTAINED_CONFIGURATION_NAME));
        });
    }
}
