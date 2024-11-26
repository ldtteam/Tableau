package com.ldtteam.tableau.sourceset.management.extensions;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import groovy.lang.Closure;
import org.gradle.api.*;
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
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Contains the base dsl for source set management.
 */
public abstract class SourceSetExtension implements NamedDomainObjectContainer<SourceSetExtension.SourceSetConfiguration> {

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

    /**
     * Creates a new extension.
     *
     * @param project The project.
     */
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
                        project.getObjects(),
                        name,
                        sourceSet
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
        getPublishedSourceSets().convention(List.of(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)));

        project.afterEvaluate(p -> {
            //Run this in an afterEval, because we need a group configured, which is not available at apply and construction time.
            final SourceSetConfiguration main = maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME);

            main.getResources().srcDir(ModExtension.get(project).getModId().map("src/datagen/generated/%s"::formatted));
        });
    }

    /**
     * The source sets which are configured for use with Tableau.
     *
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

    @Override
    public @NotNull SourceSetConfiguration create(@NotNull String name) throws InvalidUserDataException {
        return sourceSets.create(name);
    }

    @Override
    public @NotNull SourceSetConfiguration maybeCreate(@NotNull String name) {
        return sourceSets.maybeCreate(name);
    }

    @Override
    public @NotNull SourceSetConfiguration create(@NotNull String name, @NotNull Closure configureClosure) throws InvalidUserDataException {
        return sourceSets.create(name, configureClosure);
    }

    @Override
    public @NotNull SourceSetConfiguration create(@NotNull String name, @NotNull Action<? super SourceSetConfiguration> configureAction) throws InvalidUserDataException {
        return sourceSets.create(name, configureAction);
    }

    @Override
    public @NotNull NamedDomainObjectContainer<SourceSetConfiguration> configure(@NotNull Closure configureClosure) {
        return sourceSets.configure(configureClosure);
    }

    @Override
    public @NotNull NamedDomainObjectProvider<SourceSetConfiguration> register(@NotNull String name, @NotNull Action<? super SourceSetConfiguration> configurationAction) throws InvalidUserDataException {
        return sourceSets.register(name, configurationAction);
    }

    @Override
    public @NotNull NamedDomainObjectProvider<SourceSetConfiguration> register(@NotNull String name) throws InvalidUserDataException {
        return sourceSets.register(name);
    }

    @Override
    public <S extends SourceSetConfiguration> @NotNull NamedDomainObjectSet<S> withType(@NotNull Class<S> type) {
        return sourceSets.withType(type);
    }

    @Override
    public @NotNull NamedDomainObjectSet<SourceSetConfiguration> named(@NotNull Spec<String> nameFilter) {
        return sourceSets.named(nameFilter);
    }

    @Override
    public @NotNull NamedDomainObjectSet<SourceSetConfiguration> matching(@NotNull Spec<? super SourceSetConfiguration> spec) {
        return sourceSets.matching(spec);
    }

    @Override
    public @NotNull NamedDomainObjectSet<SourceSetConfiguration> matching(@NotNull Closure spec) {
        return sourceSets.matching(spec);
    }

    @Override
    public @NotNull Set<SourceSetConfiguration> findAll(@NotNull Closure spec) {
        return sourceSets.findAll(spec);
    }

    @Override
    public boolean add(@NotNull SourceSetConfiguration e) {
        return sourceSets.add(e);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends SourceSetConfiguration> c) {
        return sourceSets.addAll(c);
    }

    @Override
    public @NotNull Namer<SourceSetConfiguration> getNamer() {
        return sourceSets.getNamer();
    }

    @Override
    public @NotNull SortedMap<String, SourceSetConfiguration> getAsMap() {
        return sourceSets.getAsMap();
    }

    @Override
    public @NotNull SortedSet<String> getNames() {
        return sourceSets.getNames();
    }

    @Nullable
    @Override
    public SourceSetConfiguration findByName(@NotNull String name) {
        return sourceSets.findByName(name);
    }

    @Override
    public @NotNull SourceSetConfiguration getByName(@NotNull String name) throws UnknownDomainObjectException {
        return sourceSets.getByName(name);
    }

    @Override
    public @NotNull SourceSetConfiguration getByName(@NotNull String name, @NotNull Closure configureClosure) throws UnknownDomainObjectException {
        return sourceSets.getByName(name, configureClosure);
    }

    @Override
    public @NotNull SourceSetConfiguration getByName(@NotNull String name, @NotNull Action<? super SourceSetConfiguration> configureAction) throws UnknownDomainObjectException {
        return sourceSets.getByName(name, configureAction);
    }

    @Override
    public @NotNull SourceSetConfiguration getAt(@NotNull String name) throws UnknownDomainObjectException {
        return sourceSets.getAt(name);
    }

    @Override
    public @NotNull Rule addRule(@NotNull Rule rule) {
        return sourceSets.addRule(rule);
    }

    @Override
    public @NotNull Rule addRule(@NotNull String description, @NotNull Closure ruleAction) {
        return sourceSets.addRule(description, ruleAction);
    }

    @Override
    public @NotNull Rule addRule(@NotNull String description, @NotNull Action<String> ruleAction) {
        return sourceSets.addRule(description, ruleAction);
    }

    @Override
    public @NotNull List<Rule> getRules() {
        return sourceSets.getRules();
    }

    @Override
    public @NotNull NamedDomainObjectProvider<SourceSetConfiguration> named(@NotNull String name) throws UnknownDomainObjectException {
        return sourceSets.named(name);
    }

    @Override
    public @NotNull NamedDomainObjectProvider<SourceSetConfiguration> named(@NotNull String name, @NotNull Action<? super SourceSetConfiguration> configurationAction) throws UnknownDomainObjectException {
        return sourceSets.named(name, configurationAction);
    }

    @Override
    public <S extends SourceSetConfiguration> @NotNull NamedDomainObjectProvider<S> named(@NotNull String name, @NotNull Class<S> type) throws UnknownDomainObjectException {
        return sourceSets.named(name, type);
    }

    @Override
    public <S extends SourceSetConfiguration> @NotNull NamedDomainObjectProvider<S> named(@NotNull String name, @NotNull Class<S> type, @NotNull Action<? super S> configurationAction) throws UnknownDomainObjectException {
        return sourceSets.named(name, type, configurationAction);
    }

    @Internal
    @Override
    public @NotNull NamedDomainObjectCollectionSchema getCollectionSchema() {
        return sourceSets.getCollectionSchema();
    }

    @Override
    public void addLater(@NotNull Provider<? extends SourceSetConfiguration> provider) {
        sourceSets.addLater(provider);
    }

    @Override
    public void addAllLater(@NotNull Provider<? extends Iterable<SourceSetConfiguration>> provider) {
        sourceSets.addAllLater(provider);
    }

    @Override
    public <S extends SourceSetConfiguration> @NotNull DomainObjectCollection<S> withType(@NotNull Class<S> type, @NotNull Action<? super S> configureAction) {
        return sourceSets.withType(type, configureAction);
    }

    @Override
    public <S extends SourceSetConfiguration> @NotNull DomainObjectCollection<S> withType(@NotNull Class<S> type, @NotNull Closure configureClosure) {
        return sourceSets.withType(type, configureClosure);
    }

    @Override
    public @NotNull Action<? super SourceSetConfiguration> whenObjectAdded(@NotNull Action<? super SourceSetConfiguration> action) {
        return sourceSets.whenObjectAdded(action);
    }

    @Override
    public void whenObjectAdded(@NotNull Closure action) {
        sourceSets.whenObjectAdded(action);
    }

    @Override
    public @NotNull Action<? super SourceSetConfiguration> whenObjectRemoved(@NotNull Action<? super SourceSetConfiguration> action) {
        return sourceSets.whenObjectRemoved(action);
    }

    @Override
    public void whenObjectRemoved(@NotNull Closure action) {
        sourceSets.whenObjectRemoved(action);
    }

    @Override
    public void all(@NotNull Action<? super SourceSetConfiguration> action) {
        sourceSets.all(action);
    }

    @Override
    public void all(@NotNull Closure action) {
        sourceSets.all(action);
    }

    @Override
    public void configureEach(@NotNull Action<? super SourceSetConfiguration> action) {
        sourceSets.configureEach(action);
    }

    @Override
    public int size() {
        return sourceSets.size();
    }

    @Override
    public boolean isEmpty() {
        return sourceSets.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return sourceSets.contains(o);
    }

    @Override
    public @NotNull Iterator<SourceSetConfiguration> iterator() {
        return sourceSets.iterator();
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return sourceSets.toArray();
    }

    @Override
    public @NotNull <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return sourceSets.toArray(a);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return sourceSets.toArray(generator);
    }

    @Override
    public boolean remove(Object o) {
        return sourceSets.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return sourceSets.containsAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return sourceSets.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super SourceSetConfiguration> filter) {
        return sourceSets.removeIf(filter);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return sourceSets.retainAll(c);
    }

    @Override
    public void clear() {
        sourceSets.clear();
    }

    @Override
    public Spliterator<SourceSetConfiguration> spliterator() {
        return sourceSets.spliterator();
    }

    @Override
    public Stream<SourceSetConfiguration> stream() {
        return sourceSets.stream();
    }

    @Override
    public Stream<SourceSetConfiguration> parallelStream() {
        return sourceSets.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super SourceSetConfiguration> action) {
        sourceSets.forEach(action);
    }

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
    public abstract static class SourceSetDependencies implements Dependencies {

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
