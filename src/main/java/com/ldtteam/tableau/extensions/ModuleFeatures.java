package com.ldtteam.tableau.extensions;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Represents a feature extension.
 */
public abstract class ModuleFeatures {

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "features";

    /**
     * Gets the feature extension for a given project.
     *
     * @param project The project.
     * @return The feature extension.
     */
    public static ModuleFeatures get(Project project) {
        //In case the settings plugin is not loaded.
        if (project.getGradle().getExtensions().findByType(ModuleFeatures.class) == null) {
            return project.getObjects().newInstance(ModuleFeatures.class);
        }

        return project.getGradle().getExtensions().getByType(ModuleFeatures.class);
    }

    @Inject
    public ModuleFeatures() {
        getUsesCrowdin().convention(false);
        getUsesSonarQube().convention(false);
        getUsesShadowing().convention(false);
        getUsesJarJar().convention(false);
        getUsesCurse().convention(false);
        getUsesParchment().convention(false);
        getUsesGit().convention(false);
        getUsesTesting().convention(false);
    }

    /**
     * Gets the property for whether the project uses crowdin.
     *
     * @return The property for whether the project uses crowdin.
     */
    public abstract Property<Boolean> getUsesCrowdin();

    /**
     * Gets the property for whether the project uses sonarqube.
     *
     * @return The property for whether the project uses sonarqube.
     */
    public abstract Property<Boolean> getUsesSonarQube();

    /**
     * Gets the property for whether the project uses shadowing.
     *
     * @return The property for whether the project uses shadowing.
     */
    public abstract Property<Boolean> getUsesShadowing();

    /**
     * Gets the property for whether the project uses jarjar.
     *
     * @return The property for whether the project uses jarjar.
     */
    public abstract Property<Boolean> getUsesJarJar();

    /**
     * Gets the property for whether the project uses curse.
     *
     * @return The property for whether the project uses curse.
     */
    public abstract Property<Boolean> getUsesCurse();

    /**
     * Gets the property for whether the project uses parchment.
     *
     * @return The property for whether the project uses parchment.
     */
    public abstract Property<Boolean> getUsesParchment();

    /**
     * Gets the property for whether the project uses git.
     *
     * @return The property for whether the project uses git.
     */
    public abstract Property<Boolean> getUsesGit();

    /**
     * Gets the property for whether the project uses testing.
     *
     * @return The property for whether the project uses testing.
     */
    public abstract Property<Boolean> getUsesTesting();
}
