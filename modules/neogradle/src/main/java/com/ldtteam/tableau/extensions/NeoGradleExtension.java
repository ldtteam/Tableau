package com.ldtteam.tableau.extensions;

import com.ldtteam.tableau.common.extensions.ProjectExtension;
import com.ldtteam.tableau.common.extensions.VersioningExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.bundling.Jar;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Extension that configures the NeoGradle plugin.
 */
public abstract class NeoGradleExtension implements ExtensionAware {

    /**
     * Gets the NeoGradle extension for the given project.
     *
     * @param project the project to get the extension from
     * @return the NeoGradle extension
     */
    public static NeoGradleExtension get(final Project project) {
        return TableauScriptingExtension.get(project, NeoGradleExtension.class);
    }

    /**
     * The name of the NeoGradle extension.
     */
    public static final String EXTENSION_NAME = "neogradle";

    /**
     * Creates a new instance of the NeoGradle extension.
     *
     * @param project the project to create the extension for
     */
    @Inject
    public NeoGradleExtension(final Project project) {
        //Automatically add the access transformer file if it exists.
        if (project.file("src/main/resources/META-INF/accesstransformer.cfg").exists()) {
            getAccessTransformers().from(project.file("src/main/resources/META-INF/accesstransformer.cfg"));
        }

        //Automatically add the interface injection file if it exists.
        if (project.file("src/main/resources/META-INF/interface-injection.json").exists()) {
            getAccessTransformers().from(project.file("src/main/resources/META-INF/interface-injection.json"));
        }

        final VersioningExtension versioning = VersioningExtension.get(project);
        final ProjectExtension projectExtension = ProjectExtension.get(project);

        //Set the default values for the extension.
        getPrimaryJarClassifier().convention("universal");

        //By default, we extract the latest neoforge version from the minecraft version.
        getNeoForgeVersion().convention(
                project.getProviders().gradleProperty("neoforge.version")
                        .orElse(projectExtension.getMinecraftVersion().flatMap(enabled -> versioning.getMinecraft().getMinecraftVersion().map(version -> {
                            //The minecraft version is formatted like: a.b.c
                            //The NeoForge version is formatted like: b.c.+
                            final String[] parts = version.split("\\.");
                            return "%s.%s.+".formatted(parts[1], parts[2]);
                        })).orElse("+"))
        );

        //Always default to a normal username.
        getUseRandomPlayerNames().convention(false);

        //If we are a library we need to configure the manifest to be a library.
        project.getTasks().named("jar", Jar.class, jar -> {
            jar.getArchiveClassifier().set(getPrimaryJarClassifier());

            jar.manifest(manifest -> {
                manifest.attributes(Map.of("FMLModType", getIsLibrary().map(isFmlLibrary -> isFmlLibrary ? "LIBRARY" : "MOD")));
            });
        });

        //Default to no additional data gen mods.
        getAdditionalDataGenMods().convention(new ArrayList<>());

        //Default to the data generation runs defined in the property, if not set default to the current modern Minecraft run configuration.
        getDataGenerationRuns().convention(
            project.getProviders().gradleProperty("neoforge.data.runs").map(
                s -> Arrays.stream(s.split(",")).toList()
            ).orElse(List.of("clientData", "serverData"))
        );
    }

    /**
     * Contains the access transformers to apply to the project.
     *
     * @return The access transformers to apply to the project.
     */
    public abstract ConfigurableFileCollection getAccessTransformers();

    /**
     * Adds an access transformer to the project.
     *
     * @param file the access transformer file
     */
    public void accessTransformer(Object file) {
        getAccessTransformers().from(file);
    }

    /**
     * Contains the interface injections to apply to the project.
     *
     * @return The access transformers to apply to the project.
     */
    public abstract ConfigurableFileCollection getInterfaceInjections();

    /**
     * Adds an access transformer to the project.
     *
     * @param file the access transformer file
     */
    public void interfaceInjection(Object file) {
        getInterfaceInjections().from(file);
    }

    /**
     * The neoforge version to use.
     *
     * @return The version of NeoForge to use.
     */
    public abstract Property<@NotNull String> getNeoForgeVersion();

    /**
     * The classifier to use for the primary jar.
     *
     * @return The classifier for the primary jar.
     */
    public abstract Property<@NotNull String> getPrimaryJarClassifier();

    /**
     * Whether, to use random player names, when starting the client.
     *
     * @return Indicates whether the project should use random player names.
     */
    public abstract Property<@NotNull Boolean> getUseRandomPlayerNames();

    /**
     * The additional data gen mods to use.
     *
     * @return The additional data gen mods to use.
     */
    public abstract ListProperty<@NotNull String> getAdditionalDataGenMods();

    /**
     * Adds a data gen mod to the project.
     *
     * @param mod the additional data gen mod
     */
    public void additionalDataGenMod(String mod) {
        getAdditionalDataGenMods().add(mod);
    }

    /**
     * Whether the project is an FML library.
     *
     * @return Indicates whether the project is an FML library.
     */
    public abstract Property<@NotNull Boolean> getIsLibrary();

    /**
     * Defines the data generation runs to configure.
     * Generally match up with the run types exposed by neoforge.
     * <p>
     *     This is by default read from a gradle property `neoforge.runs.data`, if that is not supplied it is currently by default to clientData and serverData.
     * </p>
     * @return The data generation run.
     */
    public abstract ListProperty<@NotNull String> getDataGenerationRuns();
}
