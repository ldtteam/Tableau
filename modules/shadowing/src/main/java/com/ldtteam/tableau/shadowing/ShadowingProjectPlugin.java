/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.shadowing;

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin;
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.ldtteam.tableau.extensions.NeoGradleExtension;
import com.ldtteam.tableau.neogradle.NeoGradlePlugin;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.shadowing.extensions.ShadowingExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Project plugin for the shadowing module.
 * <p>
 *     Configures the shadow plugin to include the correct dependencies and to shadow them.
 */
public class ShadowingProjectPlugin implements Plugin<Project> {

    /**
     * Name of the configuration used by the shadow plugin that contains the dependencies to be shadowed.
     */
    public static final String CONTAINED_CONFIGURATION_NAME = "contained";

    /**
     * Creates a new instance of the plugin.
     */
    @Inject
    public ShadowingProjectPlugin() {
    }

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(NeoGradlePlugin.class);
        target.getPlugins().apply(ShadowPlugin.class);

        TableauScriptingExtension.register(target, ShadowingExtension.EXTENSION_NAME, ShadowingExtension.class);

        configureContainedConfiguration(target);
        configureJarTask(target);
        configureShadowJarTask(target);
    }

    /**
     * Creates and configures the contained configuration.
     *
     * @param project the project to configure
     */
    @SuppressWarnings("UnstableApiUsage")
    private void configureContainedConfiguration(Project project) {
        final Configuration configuration = project.getConfigurations().maybeCreate(CONTAINED_CONFIGURATION_NAME).setTransitive(false);


        final Configuration implementation = project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);

        final ShadowingExtension shadow = ShadowingExtension.get(project);
        project.afterEvaluate(ignored -> {
            configuration.setTransitive(!shadow.getUsesNoneTransitiveShadow().get());

            if (shadow.getExtendImplementation().get()) {
                implementation.extendsFrom(configuration);
            }
        });

        //For now register the extension only on the main source set.
        final SourceSetExtension sourceSets = SourceSetExtension.get(project);

        //Needs to go through matching to ensure that the source set is lazily looked up.
        sourceSets.matching(sourceSet -> SourceSet.isMain(sourceSet.getSourceSet()))
                .configureEach(sourceSet -> {
                    final SourceSetExtension.SourceSetConfiguration main = sourceSets.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME);
                    final DependencyCollector dependencies = project.getObjects().dependencyCollector();
                    main.getDependencies().getExtensions().add(CONTAINED_CONFIGURATION_NAME, dependencies);

                    configuration.fromDependencyCollector(dependencies);
                });
    }

    /**
     * Configures the jar task to use the slim classifier.
     * <p>
     *     This ensures that the shadowed jar is not overwritten by the original jar.
     *
     * @param project the project to configure
     */
    private void configureJarTask(Project project) {
        project.getTasks().named("jar", Jar.class, jar -> {
            jar.getArchiveClassifier().set("slim");
        });
    }

    /**
     * Configures the shadowJar task.
     * <p>
     *     Ensures that the shadowed jar is created with the correct classifier and contains the correct dependencies.
     *
     * @param project the project to configure
     */
    private void configureShadowJarTask(Project project) {
        final NeoGradleExtension neoGradle = NeoGradleExtension.get(project);
        final SourceSetExtension sourceSets = SourceSetExtension.get(project);
        final ShadowingExtension shadowing = ShadowingExtension.get(project);

        project.getTasks().named(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME, ShadowJar.class, shadowJar -> {
            shadowJar.getArchiveClassifier().set(neoGradle.getPrimaryJarClassifier());

            sourceSets.getUniversalJarSourceSets().get().forEach(sourceSet -> {
                shadowJar.from(sourceSet.getOutput());
            });

            List<FileCollection> configurations = new ArrayList<>();
            configurations.add(project.getConfigurations().getByName(CONTAINED_CONFIGURATION_NAME));
            shadowJar.setConfigurations(configurations);

            shadowing.getRenamedNamespaces().get().forEach(shadowJar::relocate);
        });
    }
}
