package com.ldtteam.tableau.neoforge.metadata.components.model;

import java.util.ArrayList;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.Dependencies;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.problems.Problems;

import com.ldtteam.tableau.neoforge.metadata.utils.DependencyResolver;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;
import com.ldtteam.tableau.utilities.utils.DelegatingNamedDomainObjectContainer;

/**
 * Represents the dependencies of a mod.
 * <p>
 * Mod dependencies can either be added to the required or optional dependencies
 * collection, which is wired to the source sets implementation or api
 * configuration.
 * <p>
 * Alternatively, a dependency can directly be added as a model component to the
 * mod metadata.
 */
public abstract class ModDependencies extends DelegatingNamedDomainObjectContainer<ModDependency>
        implements Dependencies {

    /**
     * Creates a new ModDependencies instance.
     *
     * @param project                The project.
     * @param sourceSetConfiguration The source set configuration.
     */
    @Inject
    public ModDependencies(final Project project, final SourceSetConfiguration sourceSetConfiguration) {
        super(project.getObjects().domainObjectContainer(ModDependency.class, name -> {
            return project.getObjects().newInstance(ModDependency.class, name);
        }));

        this.addAllLater(getRequired().getDependencies().flatMap(dependencies -> {
            if (dependencies.isEmpty()) {
                return project.provider(() -> new ArrayList<>());
            }

            final Configuration resolveTarget = project.getConfigurations()
                    .detachedConfiguration(dependencies.toArray(Dependency[]::new));
            resolveTarget.getDependencyConstraints().addAllLater(getRequired().getDependencyConstraints());
            return DependencyResolver.resolveDependencies(project, getProblems(), resolveTarget, true);
        }));
        this.addAllLater(getOptional().getDependencies().flatMap(dependencies -> {
            if (dependencies.isEmpty()) {
                return project.provider(() -> new ArrayList<>());
            }

            final Configuration resolveTarget = project.getConfigurations()
                    .detachedConfiguration(dependencies.toArray(Dependency[]::new));
            resolveTarget.getDependencyConstraints().addAllLater(getOptional().getDependencyConstraints());
            return DependencyResolver.resolveDependencies(project, getProblems(), resolveTarget, false);
        }));

        final Configuration runtimeClasspath = project.getConfigurations()
                .maybeCreate(sourceSetConfiguration.getSourceSet().getRuntimeClasspathConfigurationName());
        final Configuration api = project.getConfigurations()
                .maybeCreate(sourceSetConfiguration.getSourceSet().getApiConfigurationName());

        runtimeClasspath.fromDependencyCollector(getOptional());
        api.fromDependencyCollector(getRequired());

    }

    /**
     * Problem reporter used to report dependency resolution and metadata extraction
     * issues.
     * 
     * @return the problem reporter
     */
    @Inject
    protected abstract Problems getProblems();

    /**
     * Get the required dependencies.
     *
     * @return The dependency collector for required mods.
     */
    public abstract DependencyCollector getRequired();

    /**
     * Get the optional dependencies.
     *
     * @return The dependency collector for optional mods.
     */
    public abstract DependencyCollector getOptional();
}
