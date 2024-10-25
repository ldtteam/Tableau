package com.ldtteam.tableau.extensions;

import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
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
        return project.getGradle().getExtensions().getByType(ModuleFeatures.class);
    }

    @Inject
    public ModuleFeatures(final Settings settings) {
        usesCrowdin().convention(UtilityFunctions.get(settings).getUsesProperty("crowdin"));
        usesCrowdInTranslationManagement().convention(UtilityFunctions.get(settings).getUsesProperty("crowdinTranslationManagement"));
        usesSonarQube().convention(UtilityFunctions.get(settings).getUsesProperty("sonarQube"));
        usesShadowing().convention(UtilityFunctions.get(settings).getUsesProperty("shadowing"));
        usesJarJar().convention(UtilityFunctions.get(settings).getUsesProperty("jarjar"));
        usesCurse().convention(UtilityFunctions.get(settings).getUsesProperty("curse"));
    }

    /**
     * Gets the property for whether the project uses crowdin.
     *
     * @return The property for whether the project uses crowdin.
     */
    public abstract Property<Boolean> usesCrowdin();

    /**
     * Gets the property for whether the project uses crowdin in translation management.
     *
     * @return The property for whether the project uses crowdin in translation management.
     */
    public abstract Property<Boolean> usesCrowdInTranslationManagement();

    /**
     * Gets the property for whether the project uses sonarqube.
     *
     * @return The property for whether the project uses sonarqube.
     */
    public abstract Property<Boolean> usesSonarQube();

    /**
     * Gets the property for whether the project uses shadowing.
     *
     * @return The property for whether the project uses shadowing.
     */
    public abstract Property<Boolean> usesShadowing();

    /**
     * Gets the property for whether the project uses jarjar.
     *
     * @return The property for whether the project uses jarjar.
     */
    public abstract Property<Boolean> usesJarJar();

    /**
     * Gets the property for whether the project uses curse.
     *
     * @return The property for whether the project uses curse.
     */
    public abstract Property<Boolean> usesCurse();

    /**
     * Gets the property for whether the project uses parchment.
     *
     * @return The property for whether the project uses parchment.
     */
    public abstract Property<Boolean> usesParchment();

    /**
     * Gets the property for whether the project uses git.
     *
     * @return The property for whether the project uses git.
     */
    public abstract Property<Boolean> usesGit();
}
