package com.ldtteam.tableau.sourceset.management.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.Dependencies;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import com.ldtteam.tableau.common.extensions.ProjectExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.utilities.utils.DelegatingNamedDomainObjectContainer;

/**
 * Contains the base dsl for source set management.
 */
public abstract class SourceSetExtension extends DelegatingNamedDomainObjectContainer<SourceSetExtension.SourceSetConfiguration> {

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

    /**
     * Creates a new extension.
     *
     * @param project The project.
     */
    @SuppressWarnings("UnstableApiUsage")
    @Inject
    public SourceSetExtension(final Project project) {
        super(project.container(SourceSetConfiguration.class, new NamedDomainObjectFactory<>() {
            @Override
            public @NotNull SourceSetConfiguration create(@NotNull String name) {
                final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

                final SourceSet sourceSet = sourceSets.maybeCreate(name);

                return project.getObjects().newInstance(
                        SourceSetConfiguration.class,
                        project.getObjects(),
                        name,
                        sourceSet
                );
            }
        }));

        whenObjectAdded(configuration -> {
            final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            final SourceSet sourceSet = sourceSets.getByName(configuration.getName());
            final JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);

            final Configuration implementation = project.getConfigurations()
                    .maybeCreate(sourceSet.getImplementationConfigurationName());
            final Configuration api = project.getConfigurations()
                    .maybeCreate(sourceSet.getApiConfigurationName());

            final Configuration tableauImplementation = project.getConfigurations()
                    .maybeCreate(sourceSet.getImplementationConfigurationName() + "Tableau");
            final Configuration tableauApi = project.getConfigurations()
                    .maybeCreate(sourceSet.getApiConfigurationName() + "Tableau");

            tableauImplementation.setCanBeResolved(true);
            tableauApi.setCanBeResolved(true);

            tableauImplementation.setCanBeConsumed(false);
            tableauApi.setCanBeConsumed(false);

            implementation.extendsFrom(tableauImplementation);
            api.extendsFrom(tableauApi);

            java.registerFeature(sourceSet.getName(), feature -> {
                feature.usingSourceSet(sourceSet);
                feature.withSourcesJar();
                feature.withJavadocJar();
            });

            getUniversalJarSourceSets().addAll(
                    configuration.getIsPartOfPrimaryJar()
                            .map(isPartOfUniversalJar -> isPartOfUniversalJar ? sourceSet : null)
                            .filter(Objects::nonNull)
                            .map(Collections::singletonList)
                            .orElse(Collections.emptyList())
            );

            getPublishedSourceSets().addAll(
                    configuration.getIsPublished()
                            .map(isPublished -> isPublished ? sourceSet : null)
                            .filter(Objects::nonNull)
                            .map(Collections::singletonList)
                            .orElse(Collections.emptyList())
            );

            tableauImplementation.fromDependencyCollector(configuration.getDependencies().getImplementation());
            tableauApi.fromDependencyCollector(configuration.getDependencies().getApi());
        });

        final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        getUniversalJarSourceSets().add(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME));
        List<SourceSet> elements = new ArrayList<>();
        elements.add(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME));
        getPublishedSourceSets().convention(elements);

        project.afterEvaluate(p -> {
            //Run this in an afterEval, because we need a group configured, which is not available at apply and construction time.
            final SourceSetConfiguration main = maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME);

            main.getResources().srcDir(ProjectExtension.get(project).getModId().map("src/datagen/generated/%s"::formatted));
        });
    }

    /**
     * Adds and configures a sourceset for an API.
     *
     * @param action The configuration action.
     */
    public void api(final Action<SourceSetConfiguration> action) {
         create(JavaPlugin.API_CONFIGURATION_NAME, configuration -> {
            action.execute(configuration);
            configuration.getIsPartOfPrimaryJar().set(true);
         });
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
        private final SourceSet sourceSet;
        private final SourceDirectorySet java;
        private final SourceDirectorySet resources;

        private final SourceSetDependencies dependencies;

        /**
         * Creates a new source set configuration.
         *
         * @param objectFactory The object factory.
         * @param name          The name of the source set.
         * @param sourceSet     The source set.
         */
        @Inject
        public SourceSetConfiguration(ObjectFactory objectFactory, String name, SourceSet sourceSet) {
            this.name = name;
            this.sourceSet = sourceSet;
            this.java = sourceSet.getJava();
            this.resources = sourceSet.getResources();

            this.dependencies = objectFactory.newInstance(SourceSetDependencies.class);

            getIsPartOfPrimaryJar().convention(SourceSet.isMain(sourceSet));
            getIsPublished().convention(SourceSet.isMain(sourceSet));
        }

        /**
         * The gradle source set that this configuration is for.
         *
         * @return the source set.
         */
        public SourceSet getSourceSet() {
            return sourceSet;
        }

        /**
         * The name of the source set.
         *
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
        public abstract Property<Boolean> getIsPartOfPrimaryJar();

        /**
         * Indicates whether the source set is published.
         *
         * @return The property.
         */
        public abstract Property<Boolean> getIsPublished();

        /**
         * The dependencies for the source set.
         *
         * @return the dependencies for the source set.
         */
        public SourceSetDependencies getDependencies() {
            return dependencies;
        }

        /**
         * Configuration callback for the source sets dependencies
         *
         * @param action The configuration action.
         */
        public void dependencies(Action<SourceSetDependencies> action) {
            action.execute(getDependencies());
        }

        /**
         * The java source directory configuration.
         *
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
         * The resources source directory configuration.
         *
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
    public abstract static class SourceSetDependencies implements Dependencies, ExtensionAware {

        /**
         * Creates a new source set dependencies model.
         */
        @Inject
        public SourceSetDependencies() {
        }

        /**
         * The implementation dependencies.
         *
         * @return the implementation dependencies.
         */
        public abstract DependencyCollector getImplementation();

        /**
         * The api dependencies.
         *
         * @return the api dependencies.
         */
        public abstract DependencyCollector getApi();
    }
}
