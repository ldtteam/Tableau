package com.ldtteam.tableau.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

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
        return project.getExtensions().getByType(NeoGradleExtension.class);
    }

    /**
     * The name of the NeoGradle extension.
     */
    public static final String EXTENSION_NAME = "neogradle";

    @Inject
    public NeoGradleExtension(final Project project) {
    }
}
