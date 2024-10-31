package com.ldtteam.tableau.extensions;

import com.ldtteam.tableau.common.extensions.VersioningExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.bundling.Jar;

import javax.inject.Inject;
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

    @Inject
    public NeoGradleExtension(final Project project) {
        if (project.file("src/main/resources/META-INF/accesstransformer.cfg").exists()) {
            getAccessTransformers().from(project.file("src/main/resources/META-INF/accesstransformer.cfg"));
        }

        final VersioningExtension versioning = VersioningExtension.get(project);

        getPrimaryJarClassifier().convention("universal");
        getNeoForgeVersion().convention(
            versioning.getMinecraft().getEnabled().flatMap(enabled -> {
                if (enabled) {
                    return versioning.getMinecraft().getMinecraftVersion().map(version -> {
                        //The minecraft version is formatted like: a.b.c
                        //The NeoForge version is formatted like: b.c.+
                        final String[] parts = version.split("\\.");
                        return "%s.%s.+".formatted(parts[1], parts[2]);
                    });
                } else {
                    return null;
                }
            }).orElse("+")
        );

        getUseRandomPlayerNames().convention(false);

        project.getTasks().named("jar", Jar.class, jar -> {
            jar.getArchiveClassifier().set(getPrimaryJarClassifier());

            jar.manifest(manifest -> {
                manifest.attributes(Map.of("FMLModType", getIsFmlLibrary().map(isFmlLibrary -> isFmlLibrary ? "LIBRARY" : "MOD")));
            });
        });
    }

    /**
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
     * @return The version of NeoForge to use.
     */
    public abstract Property<String> getNeoForgeVersion();

    /**
     * @return The classifier for the primary jar.
     */
    public abstract Property<String> getPrimaryJarClassifier();

    /**
     * @return Indicates whether the project should use random player names.
     */
    public abstract Property<Boolean> getUseRandomPlayerNames();

    /**
     * @return The additional data gen mods to use.
     */
    public abstract ListProperty<String> getAdditionalDataGenMods();

    /**
     * Adds a data gen mod to the project.
     *
     * @param mod the additional data gen mod
     */
    public void dataGenMod(String mod) {
        getAdditionalDataGenMods().add(mod);
    }

    /**
     * @return Indicates whether the project is an FML library.
     */
    public abstract Property<Boolean> getIsFmlLibrary();
}
