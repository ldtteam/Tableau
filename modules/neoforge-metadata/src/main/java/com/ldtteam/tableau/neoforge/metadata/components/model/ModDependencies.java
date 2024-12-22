package com.ldtteam.tableau.neoforge.metadata.components.model;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.Dependencies;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.problems.Problems;

import com.ldtteam.tableau.neoforge.metadata.utils.DependencyResolver;
import com.ldtteam.tableau.utilities.utils.DelegatingNamedDomainObjectContainer;

public abstract class ModDependencies extends DelegatingNamedDomainObjectContainer<ModDependency> implements Dependencies {
    
    public ModDependencies(final Project project) {
        super(project.getObjects().domainObjectContainer(ModDependency.class, name -> {
            return project.getObjects().newInstance(ModDependency.class, name);
        }));

        this.addAllLater(getRequired().getDependencies().flatMap(dependencies -> {
            final Configuration resolveTarget = project.getConfigurations().detachedConfiguration(dependencies.toArray(Dependency[]::new));
            return DependencyResolver.resolveDependencies(project, getProblems(), resolveTarget, true);
        }));
        this.addAllLater(getOptional().getDependencies().flatMap(dependencies -> {
            final Configuration resolveTarget = project.getConfigurations().detachedConfiguration(dependencies.toArray(Dependency[]::new));
            return DependencyResolver.resolveDependencies(project, getProblems(), resolveTarget, false);
        }));
    }

    /**
     * Problem reporter used to report dependency resolution and metadata extraction issues.
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
