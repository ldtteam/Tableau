package com.ldtteam.tableau.neogradle.extensions;

import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

import javax.inject.Inject;

/**
 * Extension that configures the NeoGradle plugin for a given source set.
 */
public abstract class NeoGradleSourceSetConfigurationExtension {

    /**
     * Gets the NeoGradle extension for the given source set configuration.
     *
     * @param sourceSetConfiguration the source set configuration to get the extension from
     * @return the NeoGradle extension
     */
    public static NeoGradleSourceSetConfigurationExtension get(SourceSetExtension.SourceSetConfiguration sourceSetConfiguration) {
        return sourceSetConfiguration.getExtensions().getByType(NeoGradleSourceSetConfigurationExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "neogradle";

    /**
     * Creates a new instance of the NeoGradle extension.
     *
     * @param project the project to create the extension for
     * @param sourceSetConfiguration the source set configuration to create the extension for
     */
    @Inject
    public NeoGradleSourceSetConfigurationExtension(Project project, SourceSetExtension.SourceSetConfiguration sourceSetConfiguration) {
        getIncludeInLibraries().convention(false);
        getIsModSource().convention(SourceSet.isMain(sourceSetConfiguration.getSourceSet()));
    }

    /**
     * Indicates whether the source set is a mod source.
     *
     * @return Indicates whether the source set is a mod source.
     */
    public abstract Property<Boolean> getIsModSource();

    /**
     * Indicates whether the source sets dependencies should be included in the libraries.
     *
     * @return Indicates whether the source sets dependencies should be included in the libraries.
     */
    public abstract Property<Boolean> getIncludeInLibraries();
}
