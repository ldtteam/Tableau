package com.ldtteam.tableau.jetbrains.annotations.extensions;

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

    @Inject
    public JetbrainsAnnotationsExtension(final Project project) {
        final SourceSetExtension sourceSetExtension = SourceSetExtension.get(project);
        sourceSetExtension.getSourceSets().configureEach(sourceSetConfiguration -> {
            sourceSetConfiguration.getExtensions().create(JetbrainsAnnotationsSourceSetExtension.EXTENSION_NAME, JetbrainsAnnotationsSourceSetExtension.class);
        });

        final Dependency defaultJetbrainsAnnotationsDependency = project.getDependencies().create("org.jetbrains:annotations:%s".formatted(ResourceUtils.getJetbrainsAnnotationsVersion()));

        project.afterEvaluate(p -> {
            sourceSetExtension.getSourceSets().all(sourceSetConfiguration -> {
                final JetbrainsAnnotationsSourceSetExtension jaSourceSetExtension = JetbrainsAnnotationsSourceSetExtension.get(sourceSetConfiguration);
                if (jaSourceSetExtension.getInjectAnnotations().get()) {
                    final SourceSet sourceSet = project.getExtensions().getByType(SourceSetContainer.class).getByName(sourceSetConfiguration.getName());
                    final Configuration implementation = project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName());

                    implementation.fromDependencyCollector(new DefaultDependencyAwareDependencyCollector(defaultJetbrainsAnnotationsDependency, getDependencies()));
                }
            });
        });
    }

    public abstract DependencyCollector getDependencies();

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
