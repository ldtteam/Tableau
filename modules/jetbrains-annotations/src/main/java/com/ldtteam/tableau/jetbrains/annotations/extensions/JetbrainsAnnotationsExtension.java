package com.ldtteam.tableau.jetbrains.annotations.extensions;

import com.ldtteam.tableau.jetbrains.annotations.utils.ProviderUtils;
import com.ldtteam.tableau.jetbrains.annotations.utils.ResourceUtils;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.dsl.Dependencies;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Set;

/**
 * Extension class that manages the JetBrains annotations.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class JetbrainsAnnotationsExtension implements Dependencies {

    /**
     * Gets the JetBrains annotations extension from the project.
     *
     * @param project The project to get the extension from.
     * @return The JetBrains annotations extension.
     */
    public static JetbrainsAnnotationsExtension get(final Project project) {
        return TableauScriptingExtension.get(project, JetbrainsAnnotationsExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "annotations";

    /**
     * Creates a new instance of the extension.
     *
     * @param project The project to create the extension for.
     */
    @Inject
    public JetbrainsAnnotationsExtension(final Project project) {
        final SourceSetExtension sourceSetExtension = SourceSetExtension.get(project);
        sourceSetExtension.getSourceSets().configureEach(sourceSetConfiguration -> {
            //Register a control extension on each source set to allow for the configuration of the jetbrains annotations per annotation.
            sourceSetConfiguration.getExtensions().create(JetbrainsAnnotationsSourceSetExtension.EXTENSION_NAME, JetbrainsAnnotationsSourceSetExtension.class);
        });

        //Read the jetbrains annotations version from the resources.
        //And create a default dependency for the jetbrains annotations.
        final Dependency defaultJetbrainsAnnotationsDependency = project.getDependencies().create("org.jetbrains:annotations:%s".formatted(ResourceUtils.getJetbrainsAnnotationsVersion()));

        //Configure all source sets to inject the jetbrains annotations if the extension is enabled.
        sourceSetExtension.getSourceSets().configureEach(sourceSetConfiguration -> {
            //Get the source sets extension to check whether it is enabled or not.
            final JetbrainsAnnotationsSourceSetExtension jaSourceSetExtension = JetbrainsAnnotationsSourceSetExtension.get(sourceSetConfiguration);
            final SourceSet sourceSet = project.getExtensions().getByType(SourceSetContainer.class).getByName(sourceSetConfiguration.getName());
            final Configuration implementation = project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName());

            //Wrap the dependency collector to add the default dependency if no other dependencies are present.
            final DependencyCollector collector = new DefaultDependencyAwareDependencyCollector(defaultJetbrainsAnnotationsDependency, getDependencies());

            //Add the dependencies and constraints to the implementation configuration.
            implementation.getDependencies().addAllLater(
                    ProviderUtils.conditionalCollection(collector.getDependencies(), jaSourceSetExtension.getInjectAnnotations(), Set::of)
            );
            implementation.getDependencyConstraints().addAllLater(
                    ProviderUtils.conditionalCollection(collector.getDependencyConstraints(), jaSourceSetExtension.getInjectAnnotations(), Set::of)
            );
        });
    }

    /**
     * The dependency collector that is used to collect the dependency for the jetbrains annotations centrally.
     *
     * @return The dependency collector.
     */
    public abstract DependencyCollector getDependencies();

    /**
     * The dependency collector that is used to collect the dependency for the jetbrains annotations centrally.
     * <p>
     *     This is a delegate which will return a default dependency if no other dependencies are added to it.
     *
     * @param alternative The alternative dependency to use if no other dependencies are present.
     * @param delegate The delegate dependency collector.
     */
    private record DefaultDependencyAwareDependencyCollector(Dependency alternative,
                                                             DependencyCollector delegate) implements DependencyCollector {

        @Override
            public void add(@NotNull CharSequence dependencyNotation) {
                delegate().add(dependencyNotation);
            }

            @Override
            public void add(@NotNull CharSequence dependencyNotation, @NotNull Action<? super ExternalModuleDependency> configuration) {
                delegate().add(dependencyNotation, configuration);
            }

            @Override
            public void add(@NotNull FileCollection files) {
                delegate().add(files);
            }

            @Override
            public void add(@NotNull FileCollection files, @NotNull Action<? super FileCollectionDependency> configuration) {
                delegate().add(files, configuration);
            }

            @Override
            public void add(@NotNull ProviderConvertible<? extends MinimalExternalModuleDependency> externalModule) {
                delegate().add(externalModule);
            }

            @Override
            public void add(@NotNull ProviderConvertible<? extends MinimalExternalModuleDependency> externalModule, @NotNull Action<? super ExternalModuleDependency> configuration) {
                delegate().add(externalModule, configuration);
            }

            @Override
            public void add(@NotNull Dependency dependency) {
                delegate().add(dependency);
            }

            @Override
            public <D extends Dependency> void add(@NotNull D dependency, @NotNull Action<? super D> configuration) {
                delegate().add(dependency, configuration);
            }

            @Override
            public void add(@NotNull Provider<? extends Dependency> dependency) {
                delegate().add(dependency);
            }

            @Override
            public <D extends Dependency> void add(@NotNull Provider<? extends D> dependency, @NotNull Action<? super D> configuration) {
                delegate().add(dependency, configuration);
            }

            @Override
            public void addConstraint(@NotNull DependencyConstraint dependencyConstraint) {
                delegate().addConstraint(dependencyConstraint);
            }

            @Override
            public void addConstraint(@NotNull DependencyConstraint dependencyConstraint, @NotNull Action<? super DependencyConstraint> configuration) {
                delegate().addConstraint(dependencyConstraint, configuration);
            }

            @Override
            public void addConstraint(@NotNull Provider<? extends DependencyConstraint> dependencyConstraint) {
                delegate().addConstraint(dependencyConstraint);
            }

            @Override
            public void addConstraint(@NotNull Provider<? extends DependencyConstraint> dependencyConstraint, @NotNull Action<? super DependencyConstraint> configuration) {
                delegate().addConstraint(dependencyConstraint, configuration);
            }

            @Override
            public <D extends Dependency> void bundle(@NotNull Iterable<? extends D> bundle) {
                delegate().bundle(bundle);
            }

            @Override
            public <D extends Dependency> void bundle(@NotNull Iterable<? extends D> bundle, @NotNull Action<? super D> configuration) {
                delegate().bundle(bundle, configuration);
            }

            @Override
            public <D extends Dependency> void bundle(@NotNull Provider<? extends Iterable<? extends D>> bundle) {
                delegate().bundle(bundle);
            }

            @Override
            public <D extends Dependency> void bundle(@NotNull Provider<? extends Iterable<? extends D>> bundle, @NotNull Action<? super D> configuration) {
                delegate().bundle(bundle, configuration);
            }

            @Override
            public <D extends Dependency> void bundle(@NotNull ProviderConvertible<? extends Iterable<? extends D>> bundle) {
                delegate().bundle(bundle);
            }

            @Override
            public <D extends Dependency> void bundle(@NotNull ProviderConvertible<? extends Iterable<? extends D>> bundle, @NotNull Action<? super D> configuration) {
                delegate().bundle(bundle, configuration);
            }

            @Override
            public @NotNull Provider<Set<Dependency>> getDependencies() {
                //We wrap the delegates provider in a new provider that will return the alternative dependency if the delegate returns an empty set.
                return delegate().getDependencies().map(dependencies -> {
                    if (dependencies.isEmpty()) {
                        return Set.of(alternative());
                    }
                    return dependencies;
                }).orElse(Set.of(alternative()));
            }

            @Override
            public @NotNull Provider<Set<DependencyConstraint>> getDependencyConstraints() {
                return delegate().getDependencyConstraints();
            }
        }
}
