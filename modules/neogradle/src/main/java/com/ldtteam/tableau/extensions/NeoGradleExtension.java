package com.ldtteam.tableau.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

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
}
