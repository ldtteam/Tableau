package com.ldtteam.tableau.dependencies.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.Dependencies;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import javax.inject.Inject;

/**
 * The Dependencies extension for the Tableau project.
 * <p>
 * Provides a way to register mod dependencies as project and Neogradle dependencies.
 */
public abstract class DependenciesExtension implements Dependencies
{

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "modDependencies";
    private final Project project;

    @Inject
    public DependenciesExtension(final Project project)
    {
        this.project = project;

        final SourceSet mainSourceSet = project.getExtensions().getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        final Configuration apiConfiguration = project.getConfigurations().getByName(mainSourceSet.getApiConfigurationName());

        apiConfiguration.fromDependencyCollector(getRequired());
        apiConfiguration.fromDependencyCollector(getOptional());
    }

    /**
     * Gets the dependencies extension for a given project.
     *
     * @param project The project.
     * @return The git extension.
     */
    public static DependenciesExtension get(final Project project)
    {
        return TableauScriptingExtension.get(project, DependenciesExtension.class);
    }

    /**
     * Get the required dependencies.
     */
    public abstract DependencyCollector getRequired();

    /**
     * Get the optional dependencies.
     */
    public abstract DependencyCollector getOptional();

    /**
     * Get the configuration for the required dependencies.
     *
     * @return The configuration instance.
     */
    public Configuration getRequiredConfiguration()
    {
        final Configuration configuration = project.getConfigurations().detachedConfiguration();
        configuration.fromDependencyCollector(getRequired());
        return configuration;
    }

    /**
     * Get the configuration for the optional dependencies.
     *
     * @return The configuration instance.
     */
    public Configuration getOptionalConfiguration()
    {
        final Configuration configuration = project.getConfigurations().detachedConfiguration();
        configuration.fromDependencyCollector(getOptional());
        return configuration;
    }
}
