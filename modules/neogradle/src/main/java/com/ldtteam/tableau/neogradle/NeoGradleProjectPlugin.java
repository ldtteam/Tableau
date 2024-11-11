/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.neogradle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.dependencies.extensions.DependenciesExtension;
import com.ldtteam.tableau.extensions.NeoGradleExtension;
import com.ldtteam.tableau.extensions.NeoGradleResourceProcessingExtension;
import com.ldtteam.tableau.extensions.NeoGradleSourceSetConfigurationExtension;
import com.ldtteam.tableau.neogradle.tasks.GenerateModsTomlTask;
import com.ldtteam.tableau.neogradle.model.ResolvedDependency;
import com.ldtteam.tableau.resource.processing.extensions.ResourceProcessingExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import net.neoforged.gradle.dsl.common.extensions.AccessTransformers;
import net.neoforged.gradle.dsl.common.extensions.InterfaceInjections;
import net.neoforged.gradle.dsl.common.extensions.Minecraft;
import net.neoforged.gradle.dsl.common.runs.run.RunManager;
import net.neoforged.gradle.userdev.UserDevPlugin;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Rule;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NeoGradleProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(UserDevPlugin.class);

        TableauScriptingExtension.register(target, NeoGradleExtension.EXTENSION_NAME, NeoGradleExtension.class, target);

        configureLibraryConfigurations(target);
        configureSourceSets(target);
        configureRuns(target);
        configureResourceProcessing(target);
        configureAccessTransformers(target);
        configureInterfaceInjections(target);
        configureModsTomlGeneration(target);
    }

    /**
     * Configures the source sets for the given project.
     */
    private void configureSourceSets(@NotNull Project target) {
        final SourceSetExtension sourceSetExtension = SourceSetExtension.get(target);
        sourceSetExtension.getSourceSets().configureEach(sourceSetConfig -> {
            final NeoGradleSourceSetConfigurationExtension extension = target.getObjects()
                    .newInstance(NeoGradleSourceSetConfigurationExtension.class, target, sourceSetConfig);

            sourceSetConfig.getExtensions().add(NeoGradleSourceSetConfigurationExtension.EXTENSION_NAME, extension);

            final SourceSet sourceSet = target.getExtensions().getByType(SourceSetContainer.class).getByName(sourceSetConfig.getName());
            final Configuration implementation = target.getConfigurations().maybeCreate(sourceSet.getImplementationConfigurationName());
            final NeoGradleExtension neoGradleExtension = NeoGradleExtension.get(target);

            //Register the neogradle dependency for the source set.
            implementation.getDependencies().addLater(
                    neoGradleExtension.getNeoForgeVersion().map("net.neoforged:neoforge:%s"::formatted)
                            .map(target.getDependencies()::create)
            );
        });
    }

    private void configureLibraryConfigurations(@NotNull final Project target) {
        target.getConfigurations().addRule(new Rule() {
            @Override
            public @NotNull String getDescription() {
                return "Creates a library configuration when a source set is marked to be included in the libraries, and it is requested.";
            }

            @Override
            public void apply(@NotNull String domainObjectName) {
                //The configuration in question is shaped as follows: "sourceSetNameLibrary".
                //Important is the point that for the main source set, the configuration is named "library".
                if (!domainObjectName.endsWith("Library") && !domainObjectName.equals("library")) {
                    return;
                }

                final SourceSetExtension sourceSetExtension = SourceSetExtension.get(target);

                //Check if the source set is marked to be included in the libraries.
                if (domainObjectName.equals("library")) {
                    final SourceSetExtension.SourceSetConfiguration mainSourceSetConfig = sourceSetExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    final NeoGradleSourceSetConfigurationExtension extension = NeoGradleSourceSetConfigurationExtension.get(mainSourceSetConfig);
                    final SourceSet mainSourceSet = target.getExtensions().getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME);

                    if (!extension.getIncludeInLibraries().get()) {
                        //Not included in libraries. Ignore.
                        return;
                    }

                    //Create the configuration and extend the implementation configuration with it.
                    final Configuration libraries = target.getConfigurations().create(domainObjectName);
                    final Configuration implementation = target.getConfigurations().getByName(mainSourceSet.getImplementationConfigurationName());

                    implementation.extendsFrom(libraries);
                    return;
                }

                final String sourceSetName = domainObjectName.substring(0, domainObjectName.length() - "Library".length());

                if (sourceSetExtension.getSourceSets().findByName(sourceSetName) == null) {
                    //The source set configuration does not exist. Ignore.
                    return;
                }

                //We know that a source set configuration exists for the given source set name.
                //So we also know a neogradle extension exists for the source set configuration.
                //And we know that the source set itself will exist.
                final SourceSetExtension.SourceSetConfiguration sourceSetConfig = sourceSetExtension.getSourceSets().getByName(sourceSetName);
                final NeoGradleSourceSetConfigurationExtension extension = NeoGradleSourceSetConfigurationExtension.get(sourceSetConfig);
                final SourceSet sourceSet = target.getExtensions().getByType(SourceSetContainer.class).getByName(sourceSetName);

                if (!extension.getIncludeInLibraries().get()) {
                    //Not included in libraries. Ignore.
                    return;
                }

                //Create the configuration and extend the implementation configuration with it.
                final Configuration libraries = target.getConfigurations().create(domainObjectName);
                final Configuration implementation = target.getConfigurations().getByName(sourceSet.getImplementationConfigurationName());

                implementation.extendsFrom(libraries);
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private void configureRuns(@NotNull Project target) {
        final RunManager runManager = target.getExtensions().getByType(RunManager.class);
        final NeoGradleExtension extension = NeoGradleExtension.get(target);
        final ModExtension modExtension = ModExtension.get(target);
        final SourceSetExtension sourceSetExtension = SourceSetExtension.get(target);
        final SourceSetContainer sourceSetContainer = target.getExtensions().getByType(SourceSetContainer.class);

        //Ensure a client run is created.
        runManager.maybeCreate("client");

        //Configure the client run to use random player names if the extension is set to do so.
        runManager.named("client", run -> {
            run.getArguments().addAll(
                    extension.getUseRandomPlayerNames().map(useRandomPlayerNames -> {
                        if (useRandomPlayerNames) {
                            final String randomAppendix = String.valueOf((Math.abs(new Random().nextInt() % 600) + 1));
                            return List.of("--username", "Dev%s".formatted(randomAppendix));
                        } else {
                            //When not using random player names, we don't need to add any arguments.
                            return List.of();
                        }
                    })
            );
        });

        //Ensure a server run is created.
        runManager.maybeCreate("server");

        //Ensure a data gen run is created.
        runManager.maybeCreate("data");

        //Configure the data run to have the correct arguments.
        runManager.named("data", run -> {
            //Add the arguments for the data gen run.
            //By default, these are the arguments for the main mod, its output directory, and the default existing resources' directory.
            run.getArguments().addAll(
                    modExtension.getModId().map(modId -> List.of(
                            "--mod", modId,
                            "--all",
                            "--output", target.file("src/datagen/generated/%s".formatted(modId)).getAbsolutePath(),
                            "--existing", target.file("src/main/resources/").getAbsolutePath()
                    ))
            );

            //Add the arguments for the additional data gen mods.
            run.getArguments().addAll(
                    extension.getAdditionalDataGenMods().map(mods -> {
                        if (mods.isEmpty()) {
                            //When no additional data gen mods are set, we don't need to add any arguments.
                            return List.of();
                        }

                        //When additional data gen mods are set, we need to add the arguments for each mod.
                        //Per mod, we need to add the "--existing-mod" argument followed by the mod id.
                        return mods.stream()
                                .flatMap(modId -> Stream.of("--existing-mod", modId))
                                .toList();
                    })
            );
        });

        //Ensure a game test server run is created.
        runManager.maybeCreate("gameTestServer");

        //Configure the game test server run to have the correct arguments.
        runManager.named("gameTestServer", run -> {
            //Ensure that our mods have their tests run.
            run.getSystemProperties().putAll(
                    modExtension.getModId().map(modId -> Map.of("forge.enabledGameTestNamespaces", modId))
            );
        });

        //Configure all runs to be compatible with our configuration.
        runManager.configureEach(run -> {
            //Configure logging.
            run.systemProperty("forge.logging.markers", "");
            run.systemProperty("forge.logging.console.level", "info");

            //Add the mod sources to the run.
            run.getModSources().addAllLater(
                    modExtension.getModId().map(modId -> {
                        final List<SourceSet> sourceSets = sourceSetExtension.getSourceSets()
                                .stream()
                                .filter(sourceSet -> NeoGradleSourceSetConfigurationExtension.get(sourceSet).getIsModSource().get())
                                .map(sourceSet -> sourceSetContainer.getByName(sourceSet.getName()))
                                .toList();

                        final Multimap<String, SourceSet> modSources = HashMultimap.create();
                        modSources.putAll(modId, sourceSets);

                        return modSources;
                    })
            );

            //After evaluation, add the library configurations to the run.
            sourceSetExtension.getSourceSets()
                    .stream()
                    .filter(config -> NeoGradleSourceSetConfigurationExtension.get(config).getIncludeInLibraries().get())
                    .map(config -> sourceSetContainer.getByName(config.getName()))
                    .map(sourceSet -> getLibraryConfiguration(target, sourceSet))
                    .forEach(config -> run.getDependencies().getRuntime().add(config));
        });
    }

    /**
     * Gets the library configuration for the given source set.
     * <p>
     * If the source set is the main source set, the configuration is named "library".
     * If the source set is not the main source set, the configuration is named "%sLibrary".formatted(sourceSet.getName()).
     * If the source set is missing, then an exception is thrown.
     * </p>
     *
     * @param target    The project to get the configuration from.
     * @param sourceSet The source set to get the configuration for.
     * @return The library configuration.
     */
    private Configuration getLibraryConfiguration(@NotNull final Project target, @NotNull final SourceSet sourceSet) {
        if (SourceSet.isMain(sourceSet)) {
            return target.getConfigurations().getByName("library");
        }

        return target.getConfigurations().getByName("%sLibrary".formatted(sourceSet.getName()));
    }

    /**
     * Configures the resource processing for the given project.
     */
    private void configureResourceProcessing(final Project project) {
        final ResourceProcessingExtension resourceProcessing = ResourceProcessingExtension.get(project);
        resourceProcessing.getExtensions().create(NeoGradleResourceProcessingExtension.EXTENSION_NAME, NeoGradleResourceProcessingExtension.class, project, resourceProcessing);
    }

    /**
     * Configures the access transformers for the given project.
     */
    private void configureAccessTransformers(final Project project) {
        final NeoGradleExtension extension = NeoGradleExtension.get(project);

        final Minecraft minecraft = project.getExtensions().getByType(Minecraft.class);
        final AccessTransformers accessTransformers = minecraft.getAccessTransformers();

        accessTransformers.files(extension.getAccessTransformers());

        //TODO: Consider how and when to expose the access transformers as artifacts.
    }

    /**
     * Configures the interface injections for the given project.
     */
    private void configureInterfaceInjections(final Project project) {
        final NeoGradleExtension extension = NeoGradleExtension.get(project);

        final Minecraft minecraft = project.getExtensions().getByType(Minecraft.class);
        final InterfaceInjections interfaceInjections = minecraft.getInterfaceInjections();

        interfaceInjections.files(extension.getInterfaceInjections());

        //TODO: Consider how and when to expose the interface injections as artifacts.
    }

    /**
     * Configures the automatic mods toml generation for the given project.
     */
    private void configureModsTomlGeneration(final Project project) {
        final ModExtension mod = ModExtension.get(project);
        final NeoGradleExtension neogradle = NeoGradleExtension.get(project);
        final DependenciesExtension dependencies = DependenciesExtension.get(project);

        final TaskProvider<GenerateModsTomlTask> generationTask = project.getTasks().register("generateModsToml", GenerateModsTomlTask.class, (task) -> {
            task.getNeoforgeVersion().set(neogradle.getNeoForgeVersion());
            task.getMinecraftVersion().set(mod.getMinecraftVersion());
            task.getModId().set(mod.getModId());
            task.getModName().set(mod.getModName());
            task.getModDescription().set(mod.getModDescription());
            task.getModLogo().set(mod.getModLogo());
            task.getModVersion().set(project.getVersion().toString());
            task.getPublisher().set(mod.getPublisher());
            task.getDisplayUrl().set(mod.getDisplayUrl());
            task.getIssueTrackerUrl().set(mod.getIssueTrackerUrl());
            task.getLicense().set(mod.getLicense());

            final Configuration requiredConfiguration = dependencies.getRequiredConfiguration();
            final Configuration optionalConfiguration = dependencies.getOptionalConfiguration();
            task.getRequiredDependencies().set(resolveDependencies(requiredConfiguration));
            task.getOptionalDependencies().set(resolveDependencies(optionalConfiguration));
            task.getRequiredResolvedComponents().set(requiredConfiguration.getIncoming().getResolutionResult().getRootComponent());
            task.getOptionalResolvedComponents().set(optionalConfiguration.getIncoming().getResolutionResult().getRootComponent());
        });

        if (neogradle.getAutoGenerateModsToml().get()) {
            project.getTasks().named("processResources", ProcessResources.class).configure(task -> {
                task.from(generationTask.get().getOutputFile(), (it) -> {
                    it.into("META-INF");
                });
            });
        }
    }

    /**
     * Find all the resolved dependencies.
     *
     * @param configuration The configuration to look for dependencies in.
     * @return The set provider.
     */
    private Provider<Set<ResolvedDependency>> resolveDependencies(final Configuration configuration)
    {
        return configuration.getIncoming()
                 .getArtifacts()
                 .getResolvedArtifacts()
                 .map(m -> m.stream().map(m2 -> new ResolvedDependency(m2.getId().getComponentIdentifier().getDisplayName(), m2.getFile())).collect(Collectors.toSet()));
    }
}
