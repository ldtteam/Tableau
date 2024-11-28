package com.ldtteam.tableau.dependencies.extensions;

import com.ldtteam.tableau.dependencies.model.ModDependency;
import com.ldtteam.tableau.dependencies.util.DependencyResolver;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.Dependencies;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Dependencies extension for the Tableau project.
 * <p>
 * Provides a way to register mod dependencies as project and Neogradle dependencies.
 */
public abstract class DependenciesExtension implements Dependencies {

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "modDependencies";

    /**
     * The project this extension is added to.
     */
    private final Project project;

    /**
     * Creates a new extension for the given project.
     *
     * @param project The project to create the extension for.
     */
    @Inject
    public DependenciesExtension(final Project project) {
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
    public static DependenciesExtension get(final Project project) {
        return TableauScriptingExtension.get(project, DependenciesExtension.class);
    }

    /**
     * Get the required dependencies.
     *
     * @return The dependency collector for required mods.
     */
    public abstract DependencyCollector getRequired();

    /**
     * Get the required runtime dependencies.
     *
     * @return A set property containing all the required runtime mods.
     */
    public abstract SetProperty<ModDependency> getRequiredRuntimeMods();

    /**
     * Add a required runtime dependency.
     *
     * @param modId        The mod id for the dependency.
     * @param versionRange The version range for the dependency.
     */
    public void requiredRuntime(final String modId, final String versionRange) {
        getRequiredRuntimeMods().add(new ModDependency(modId, versionRange, true));
    }

    /**
     * Get the optional dependencies.
     *
     * @return The dependency collector for optional mods.
     */
    public abstract DependencyCollector getOptional();

    /**
     * Get the optional runtime dependencies.
     *
     * @return A set property containing all the optional runtime mods.
     */
    public abstract SetProperty<ModDependency> getOptionalRuntimeMods();

    /**
     * Add an optional runtime dependency.
     *
     * @param modId        The mod id for the dependency.
     * @param versionRange The version range for the dependency.
     */
    public void optionalRuntime(final String modId, final String versionRange) {
        getOptionalRuntimeMods().add(new ModDependency(modId, versionRange, true));
    }

    /**
     * Get the configuration for the required dependencies.
     *
     * @return The configuration instance.
     */
    public Configuration getRequiredConfiguration() {
        final Configuration configuration = project.getConfigurations().detachedConfiguration();
        configuration.fromDependencyCollector(getRequired());
        return configuration;
    }

    /**
     * Get the configuration for the optional dependencies.
     *
     * @return The configuration instance.
     */
    public Configuration getOptionalConfiguration() {
        final Configuration configuration = project.getConfigurations().detachedConfiguration();
        configuration.fromDependencyCollector(getOptional());
        return configuration;
    }

    /**
     * Get all the required dependencies, combined {@link DependenciesExtension#getRequired()} and {@link DependenciesExtension#getRequiredRuntimeMods()}.
     *
     * @return The combined set of dependencies.
     */
    public Provider<Set<ModDependency>> getAllRequiredDependencies() {
        final Provider<Set<ModDependency>> requiredDependencies = DependencyResolver.resolveDependencies(getRequiredConfiguration(), true);

        return requiredDependencies.zip(getRequiredRuntimeMods(), (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet()));
    }

    /**
     * Get all the optional dependencies, combined {@link DependenciesExtension#getOptional()} and {@link DependenciesExtension#getOptionalRuntimeMods()}.
     *
     * @return The combined set of dependencies.
     */
    public Provider<Set<ModDependency>> getAllOptionalDependencies() {
        final Provider<Set<ModDependency>> optionalDependencies = DependencyResolver.resolveDependencies(getOptionalConfiguration(), false);

        return optionalDependencies.zip(getOptionalRuntimeMods(), (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet()));
    }
}
