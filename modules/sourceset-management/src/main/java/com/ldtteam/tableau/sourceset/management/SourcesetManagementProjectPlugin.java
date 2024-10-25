/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.sourceset.management;

import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SourcesetManagementProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        final UtilityFunctions utilityFunctions = UtilityFunctions.get(target);

        final Set<String> publishableSourceSets = new HashSet<>();
        final boolean projectHasApi = utilityFunctions.getBooleanProperty("projectHasApi").orElse(true).get();

        if (projectHasApi) {
            publishableSourceSets.add("api");
        }
        publishableSourceSets.add("main");
        if (utilityFunctions.hasProperty("publishableSourceSets")) {
            publishableSourceSets.clear();
            publishableSourceSets.addAll(utilityFunctions.getStringListProperty("publishableSourceSets").get());
        }

        final Set<String> availableSourceSets = new LinkedHashSet<>();
        final Map<String, Set<String>> sourceSetConfigurationDependencies = new HashMap<>();
        final Map<String, Set<String>> sourceSetSourceSetDependencies = new HashMap<>();
        final Map<String, Set<String>> sourceSetAdditionalResourceDirectories = new HashMap<>();
        final Map<String, Set<String>> sourceSetExcludedResourceDefinitions = new HashMap<>();
        final Map<String, Set<String>> sourceSetIncludedResourceDefinitions = new HashMap<>();
        final Map<String, String> sourceSetClassifiers = new HashMap<>();

        configureSourceSetDefaults(projectHasApi,
                availableSourceSets,
                sourceSetClassifiers,
                sourceSetSourceSetDependencies,
                sourceSetAdditionalResourceDirectories,
                sourceSetExcludedResourceDefinitions,
                sourceSetIncludedResourceDefinitions);
        configureSourceSetModifications(target,
                utilityFunctions,
                availableSourceSets,
                sourceSetConfigurationDependencies,
                sourceSetSourceSetDependencies,
                sourceSetAdditionalResourceDirectories,
                sourceSetExcludedResourceDefinitions,
                sourceSetIncludedResourceDefinitions,
                sourceSetClassifiers,
                projectHasApi);

        configureSourceSets(target,
                availableSourceSets,
                sourceSetConfigurationDependencies,
                sourceSetSourceSetDependencies,
                sourceSetIncludedResourceDefinitions,
                sourceSetExcludedResourceDefinitions,
                sourceSetAdditionalResourceDirectories);

        configureMainSourceSet(target);


    }

    private static void configureMainSourceSet(@NotNull Project target) {
        final JavaPluginExtension java = target.getExtensions().getByType(JavaPluginExtension.class);
        java.withSourcesJar();
        java.withJavadocJar();
    }

    private static void configureSourceSets(@NotNull Project target, Set<String> availableSourceSets, Map<String, Set<String>> sourceSetConfigurationDependencies, Map<String, Set<String>> sourceSetSourceSetDependencies, Map<String, Set<String>> sourceSetIncludedResourceDefinitions, Map<String, Set<String>> sourceSetExcludedResourceDefinitions, Map<String, Set<String>> sourceSetAdditionalResourceDirectories) {
        for (String sourceSetName : availableSourceSets) {
            configureSourceSet(target,
                    sourceSetName,
                    sourceSetConfigurationDependencies,
                    sourceSetSourceSetDependencies,
                    sourceSetIncludedResourceDefinitions,
                    sourceSetExcludedResourceDefinitions,
                    sourceSetAdditionalResourceDirectories);
        }
    }

    private static void configureSourceSet(@NotNull Project target, String sourceSetName, Map<String, Set<String>> sourceSetConfigurationDependencies, Map<String, Set<String>> sourceSetSourceSetDependencies, Map<String, Set<String>> sourceSetIncludedResourceDefinitions, Map<String, Set<String>> sourceSetExcludedResourceDefinitions, Map<String, Set<String>> sourceSetAdditionalResourceDirectories) {
        final SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
        final SourceSet sourceSet = sourceSets.maybeCreate(sourceSetName);

        sourceSet.getJava().srcDir("src/%s/java".formatted(sourceSetName));
        sourceSet.getResources().srcDir("src/%s/resources".formatted(sourceSetName));

        if (!SourceSet.isMain(sourceSet)) {
            final JavaPluginExtension java = target.getExtensions().getByType(JavaPluginExtension.class);
            java.registerFeature(sourceSetName, feature -> {
                feature.usingSourceSet(sourceSet);
                feature.withJavadocJar();
                feature.withSourcesJar();
            });
        }

        final Configuration implementation = target.getConfigurations().getByName(sourceSet.getImplementationConfigurationName());
        final Configuration api = target.getConfigurations().getByName(sourceSet.getApiConfigurationName());

        if (sourceSetConfigurationDependencies.containsKey(sourceSetName)) {
            for (String configuration : sourceSetConfigurationDependencies.get(sourceSetName)) {
                final Configuration sourceSetConfiguration = target.getConfigurations().getByName(configuration);
                implementation.extendsFrom(sourceSetConfiguration);
            }
        }

        if (sourceSetSourceSetDependencies.containsKey(sourceSetName)) {
            for (String sourceSetDependency : sourceSetSourceSetDependencies.get(sourceSetName)) {
                final SourceSet otherSourceSet = sourceSets.maybeCreate(sourceSetDependency);

                final Dependency otherSourceSetOutputDependency = target.getDependencies().create(otherSourceSet.getOutput());
                api.getDependencies().add(otherSourceSetOutputDependency);
            }
        }

        if (sourceSetIncludedResourceDefinitions.containsKey(sourceSetName)) {
            sourceSet.getResources().include(sourceSetIncludedResourceDefinitions.get(sourceSetName).toArray(String[]::new));
        }

        if (sourceSetExcludedResourceDefinitions.containsKey(sourceSetName)) {
            sourceSet.getResources().exclude(sourceSetExcludedResourceDefinitions.get(sourceSetName).toArray(String[]::new));
        }

        if (sourceSetAdditionalResourceDirectories.containsKey(sourceSetName)) {
            for (String additionalResourceDirectory : sourceSetAdditionalResourceDirectories.get(sourceSetName)) {
                sourceSet.getResources().srcDir(additionalResourceDirectory);
            }
        }
    }

    private void configureSourceSetModifications(@NotNull Project target, UtilityFunctions utilityFunctions, Set<String> availableSourceSets, Map<String, Set<String>> sourceSetConfigurationDependencies, Map<String, Set<String>> sourceSetSourceSetDependencies, Map<String, Set<String>> sourceSetAdditionalResourceDirectories, Map<String, Set<String>> sourceSetExcludedResourceDefinitions, Map<String, Set<String>> sourceSetIncludedResourceDefinitions, Map<String, String> sourceSetClassifiers, boolean projectHasApi) {
        if (utilityFunctions.getBooleanProperty("extendDefaultSourceSetConfiguration").get()) {
            final List<String> additionalSourceSets = utilityFunctions.getStringListProperty("additionalSourceSets").get();
            availableSourceSets.addAll(additionalSourceSets);

            for (final String sourceSetName : additionalSourceSets) {
                loadSourceSetConfiguration(target,
                        sourceSetName,
                        sourceSetConfigurationDependencies,
                        sourceSetSourceSetDependencies,
                        sourceSetAdditionalResourceDirectories,
                        sourceSetExcludedResourceDefinitions,
                        sourceSetIncludedResourceDefinitions,
                        sourceSetClassifiers);
            }

            if (projectHasApi) {
                loadSourceSetConfiguration(target,
                        "api",
                        sourceSetConfigurationDependencies,
                        sourceSetSourceSetDependencies,
                        sourceSetAdditionalResourceDirectories,
                        sourceSetExcludedResourceDefinitions,
                        sourceSetIncludedResourceDefinitions,
                        sourceSetClassifiers);
            }

            loadSourceSetConfiguration(target,
                    "main",
                    sourceSetConfigurationDependencies,
                    sourceSetSourceSetDependencies,
                    sourceSetAdditionalResourceDirectories,
                    sourceSetExcludedResourceDefinitions,
                    sourceSetIncludedResourceDefinitions,
                    sourceSetClassifiers);

            loadSourceSetConfiguration(target,
                    "test",
                    sourceSetConfigurationDependencies,
                    sourceSetSourceSetDependencies,
                    sourceSetAdditionalResourceDirectories,
                    sourceSetExcludedResourceDefinitions,
                    sourceSetIncludedResourceDefinitions,
                    sourceSetClassifiers);
        }
    }

    private static void configureSourceSetDefaults(boolean projectHasApi, Set<String> availableSourceSets, Map<String, String> sourceSetClassifiers, Map<String, Set<String>> sourceSetSourceSetDependencies, Map<String, Set<String>> sourceSetAdditionalResourceDirectories, Map<String, Set<String>> sourceSetExcludedResourceDefinitions, Map<String, Set<String>> sourceSetIncludedResourceDefinitions) {
        if (projectHasApi) {
            availableSourceSets.add("api");
        }
        availableSourceSets.add("main");
        availableSourceSets.add("test");

        sourceSetClassifiers.put("api", "api");
        sourceSetClassifiers.put("main", "");
        sourceSetClassifiers.put("test", "test");

        if (projectHasApi) {
            sourceSetSourceSetDependencies.putIfAbsent("main", new HashSet<>(Set.of("api")));
            sourceSetSourceSetDependencies.putIfAbsent("test", new HashSet<>(Set.of("main")));
        }

        sourceSetAdditionalResourceDirectories.put("main", new HashSet<>(Set.of("src/datagen/generated/${project.modId}")));
        sourceSetExcludedResourceDefinitions.put("main", new HashSet<>(Set.of(".cache")));
        sourceSetIncludedResourceDefinitions.put("main", new HashSet<>(Set.of("**/**")));
    }

    private void loadSourceSetConfiguration(Project project,
                                            String sourceSetName,
                                            Map<String, Set<String>> sourceSetConfigurationDependencies,
                                            Map<String, Set<String>> sourceSetSourceSetDependencies,
                                            Map<String, Set<String>> sourceSetAdditionalResourceDirectories,
                                            Map<String, Set<String>> sourceSetExcludedResourceDefinitions,
                                            Map<String, Set<String>> sourceSetIncludedResourceDefinitions,
                                            Map<String, String> sourceSetClassifiers) {
        final String configurationDependenciesPropertyName = sourceSetName + "ConfigurationDependencies";
        final String sourceSetDependenciesPropertyName = sourceSetName + "SourceSetDependencies";
        final String additionalResourcesPropertyName = sourceSetName + "ResourceDirectories";
        final String excludeResourcesPropertyName = sourceSetName + "ExcludedResources";
        final String includeResourcesPropertyName = sourceSetName + "IncludedResources";
        final String customClassifierPropertyName = sourceSetName + "Classifier";

        final UtilityFunctions utilityFunctions = UtilityFunctions.get(project);

        sourceSetConfigurationDependencies.putIfAbsent(sourceSetName, new HashSet<>());
        sourceSetConfigurationDependencies.get(sourceSetName).addAll(utilityFunctions.getStringListProperty(configurationDependenciesPropertyName).get());

        sourceSetSourceSetDependencies.putIfAbsent(sourceSetName, new HashSet<>());
        sourceSetSourceSetDependencies.get(sourceSetName).addAll(utilityFunctions.getStringListProperty(sourceSetDependenciesPropertyName).get());

        sourceSetAdditionalResourceDirectories.putIfAbsent(sourceSetName, new HashSet<>());
        sourceSetAdditionalResourceDirectories.get(sourceSetName).addAll(utilityFunctions.getStringListProperty(additionalResourcesPropertyName).get());

        sourceSetExcludedResourceDefinitions.putIfAbsent(sourceSetName, new HashSet<>());
        sourceSetExcludedResourceDefinitions.get(sourceSetName).addAll(utilityFunctions.getStringListProperty(excludeResourcesPropertyName).get());

        sourceSetIncludedResourceDefinitions.putIfAbsent(sourceSetName, new HashSet<>());
        sourceSetIncludedResourceDefinitions.get(sourceSetName).addAll(utilityFunctions.getStringListProperty(includeResourcesPropertyName).get());

        if (utilityFunctions.hasProperty(customClassifierPropertyName)) {
            sourceSetClassifiers.put(sourceSetName, utilityFunctions.getProperty(customClassifierPropertyName).get());
        }
    }
}
