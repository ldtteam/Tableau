/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.java;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.java.extensions.JavaExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;
import java.util.Map;

public class JavaProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply("java-library");

        TableauScriptingExtension.register(target, JavaExtension.EXTENSION_NAME, JavaExtension.class, target);

        configureJarTask(target);
        configurePublishedJars(target);
    }

    /**
     * Configures the jar task.
     * <p>
     *     <ul>
     *         <li>Adds the output of the universal source sets to the jar task.</li>
     *     </ul>
     * </p>
     *
     * @param project the project to configure
     */
    private void configureJarTask(final Project project) {
        project.getTasks().named("jar", Jar.class, jar -> {
            final SourceSetExtension sourceSets = SourceSetExtension.get(project);
            sourceSets.getUniversalJarSourceSets().get().forEach(sourceSet -> {
                jar.from(sourceSet.getOutput());
            });

            jar.manifest(manifest -> {
                final ModExtension mod = ModExtension.get(project);
                final JavaExtension java = JavaExtension.get(project);

                manifest.attributes(Map.of(
                        "Maven-Artifact", "%s:%s:%s".formatted(project.getGroup(), project.getName(), project.getVersion()),
                        "Specification-Title", mod.getModId(),
                        "Specification-Vendor", mod.getPublisher(),
                        "Specification-Version", project.getVersion().toString().split("\\.")[0],
                        "Implementation-Title", project.getName(),
                        "Implementation-Vendor", mod.getPublisher(),
                        "Implementation-Version", project.getVersion().toString(),
                        "Automatic-Module-Name", java.getAutomaticModuleName()
                ));
            });
        });

        project.getTasks().named("sourcesJar", Jar.class, jar -> {
            jar.getArchiveClassifier().set("sources");
            jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

            final SourceSetExtension sourceSets = SourceSetExtension.get(project);
            sourceSets.getUniversalJarSourceSets().get().forEach(sourceSet -> {
                jar.from(sourceSet.getOutput());
            });
        });

        project.getTasks().named("javadoc", Javadoc.class, javadoc -> {
            final SourceSetExtension sourceSets = SourceSetExtension.get(project);
            sourceSets.getUniversalJarSourceSets().get().forEach(sourceSet -> {
                javadoc.source(sourceSet.getAllJava());
            });
        });
    }

    /**
     * Configures the published jars.
     *
     * @param project the project to configure
     */
    @SuppressWarnings("deprecation")
    public void configurePublishedJars(final Project project) {
        project.afterEvaluate(ignored -> {
            final SourceSetExtension sourceSets = SourceSetExtension.get(project);

            sourceSets.getPublishedSourceSets().get().forEach(sourceSet -> {
                var javadocTask = project.getTasks().maybeCreate(sourceSet.getJavadocTaskName(), Javadoc.class);
                project.getTasks().named(sourceSet.getJavadocTaskName(), Javadoc.class, javadoc -> {
                    javadoc.source(sourceSet.getAllJava());
                    javadoc.setGroup("documentation");
                    javadoc.setClasspath(sourceSet.getCompileClasspath());
                    javadoc.setDestinationDir(project.getBuildDir().toPath().resolve("additional").resolve("javadoc").resolve(sourceSet.getName()).toFile());
                });

                var javadocJarTask = project.getTasks().maybeCreate(sourceSet.getJavadocJarTaskName(), Jar.class);
                project.getTasks().named(sourceSet.getJavadocJarTaskName(), Jar.class, jar -> {
                    jar.dependsOn(javadocTask);
                    jar.setGroup("packaging");
                    jar.from(javadocTask);
                });

                var outputJarTask = project.getTasks().maybeCreate(sourceSet.getJarTaskName(), Jar.class);
                project.getTasks().named(sourceSet.getJarTaskName(), Jar.class, jar -> {
                    jar.setGroup("build");
                    jar.from(sourceSet.getOutput());
                    jar.getArchiveClassifier().set(sourceSet.getName().toLowerCase(Locale.ROOT));
                });

                var sourcesJarTask = project.getTasks().maybeCreate(sourceSet.getSourcesJarTaskName(), Jar.class);
                project.getTasks().named(sourceSet.getSourcesJarTaskName(), Jar.class, jar -> {
                    jar.setGroup("sources");
                    jar.from(sourceSet.getAllSource());
                    jar.getArchiveClassifier().set("sources");
                    jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
                });

                //TODO: Wire up test tasks once we need testing for individual source sets.

                var sourceSetBuildTaskName = sourceSet.getTaskName("build", null);
                var sourceSetBuildTask = project.getTasks().maybeCreate(sourceSetBuildTaskName);
                project.getTasks().named(sourceSetBuildTaskName, task -> {
                    task.setGroup("build");
                    task.dependsOn(outputJarTask, sourcesJarTask, javadocJarTask);
                });

                project.getTasks().named("build", task -> {
                    task.dependsOn(sourceSetBuildTask);
                });

                project.getTasks().named("assemble", task -> {
                    task.dependsOn(outputJarTask, sourcesJarTask, javadocJarTask);
                });
            });
        });
    }
}
