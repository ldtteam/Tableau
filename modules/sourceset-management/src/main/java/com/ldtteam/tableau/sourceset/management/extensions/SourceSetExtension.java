package com.ldtteam.tableau.sourceset.management.extensions;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.Dependencies;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Contains the base dsl for source set management.
 */
public abstract class SourceSetExtension {

    /**
     * Gets the extension from the project.
     *
     * @param project The project to get the extension from.
     * @return The extension.
     */
    public static SourceSetExtension get(final Project project) {
        return TableauScriptingExtension.get(project, SourceSetExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "sourceSets";

    private final NamedDomainObjectContainer<SourceSetConfiguration> sourceSets;

    @SuppressWarnings("UnstableApiUsage")
    @Inject
    public SourceSetExtension(final Project project) {
        this.sourceSets = project.container(SourceSetConfiguration.class, new NamedDomainObjectFactory<>() {
            @Override
            public @NotNull SourceSetConfiguration create(@NotNull String name) {
                final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

                final SourceSet sourceSet = sourceSets.maybeCreate(name);

                return project.getObjects().newInstance(
                        SourceSetConfiguration.class,
                        name,
                        sourceSet.getJava(),
                        sourceSet.getResources()
                );
            }
        });

        this.sourceSets.whenObjectAdded(configuration -> {
            final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            final SourceSet sourceSet = sourceSets.getByName(configuration.getName());
            final JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);

            final Configuration implementation = project.getConfigurations()
                    .maybeCreate(sourceSet.getImplementationConfigurationName());
            final Configuration api = project.getConfigurations()
                    .maybeCreate(sourceSet.getApiConfigurationName());

            java.registerFeature(sourceSet.getName(), feature -> {
                feature.usingSourceSet(sourceSet);
                feature.withSourcesJar();
                feature.withJavadocJar();
            });

            getUniversalJarSourceSets().add(
                    configuration.getIsPartOfUniversalJar()
                            .map(isPartOfUniversalJar -> isPartOfUniversalJar ? sourceSet : null)
            );

            getPublishedSourceSets().add(
                    configuration.getIsPublished()
                            .map(isPublished -> isPublished ? sourceSet : null)
            );

            implementation.fromDependencyCollector(configuration.getDependencies().getImplementation());
            api.fromDependencyCollector(configuration.getDependencies().getApi());
        });

        final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        getUniversalJarSourceSets().add(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME));

        sourceSet(SourceSet.MAIN_SOURCE_SET_NAME, sourceSet -> {
            final ModExtension modExtension = ModExtension.get(project);

            sourceSet.getResources().srcDir(modExtension.getModId().map("src/datagen/generated/%s"::formatted));
        });
    }

    /**
     * @return the source sets that are configured.
     */
    public NamedDomainObjectContainer<SourceSetConfiguration> getSourceSets() {
        return sourceSets;
    }

    /**
     * Adds and configures a sourceset for an API.
     *
     * @param action The configuration action.
     */
    public void api(final Action<SourceSetConfiguration> action) {
         sourceSet(JavaPlugin.API_CONFIGURATION_NAME, configuration -> {
            action.execute(configuration);
            configuration.getIsPartOfUniversalJar().set(true);
         });
    }

    public void sourceSet(final String name, final Action<SourceSetConfiguration> action) {
        this.sourceSets.create(name, action);
    }

    /**
     * Gets the source sets that are used to build the universal jar.
     *
     * @return The source sets.
     */
    public abstract ListProperty<SourceSet> getUniversalJarSourceSets();

    /**
     * Gets the source sets that are published, individually.
     *
     * @return The source sets.
     */
    public abstract ListProperty<SourceSet> getPublishedSourceSets();

    /**
     * Contains the configuration for a source set.
     */
    public abstract static class SourceSetConfiguration implements Named, ExtensionAware {

        private final String name;
        private final SourceDirectorySet java;
        private final SourceDirectorySet resources;

        @Inject
        public SourceSetConfiguration(String name, SourceDirectorySet java, SourceDirectorySet resources) {
            this.name = name;
            this.java = java;
            this.resources = resources;
        }

        /**
         * @return the name of the source set.
         */
        @Override
        public @NotNull String getName() {
            return name;
        }

        /**
         * Indicates whether the source set is part of the universal jar.
         *
         * @return The property.
         */
        public abstract Property<Boolean> getIsPartOfUniversalJar();

        /**
         * Indicates whether the source set is published.
         *
         * @return The property.
         */
        public abstract Property<Boolean> getIsPublished();

        /**
         * @return the dependencies for the source set.
         */
        @Inject
        public abstract SourceSetDependencies getDependencies();

        /**
         * @return the java source directory.
         */
        public SourceDirectorySet getJava() {
            return java;
        }

        /**
         * Configures the java source directory.
         *
         * @param action The configuration action.
         */
        public void java(final Action<SourceDirectorySet> action) {
            action.execute(java);
        }

        /**
         * @return the resources source directory.
         */
        public SourceDirectorySet getResources() {
            return resources;
        }

        /**
         * Configures the resources source directory.
         *
         * @param action The configuration action.
         */
        public void resources(final Action<SourceDirectorySet> action) {
            action.execute(resources);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SourceSetConfiguration that)) return false;
            return Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName());
        }
    }

    /**
     * Contains the dependencies for a source set.
     */
    @SuppressWarnings("UnstableApiUsage")
    public abstract static class SourceSetDependencies implements Dependencies {

        /**
         * @return the implementation dependencies.
         */
        public abstract DependencyCollector getImplementation();

        /**
         * @return the api dependencies.
         */
        public abstract DependencyCollector getApi();
    }
}
