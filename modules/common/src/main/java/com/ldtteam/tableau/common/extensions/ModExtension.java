package com.ldtteam.tableau.common.extensions;

import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Mod extension, handles the project configuration for the mod.
 */
public abstract class ModExtension {

    /**
     * Gets the mod extension for a given project.
     *
     * @param project The project.
     * @return The mod extension.
     */
    public static ModExtension get(final Project project) {
        return project.getExtensions().getByType(ModExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "mod";

    private final Versioning versioning;

    @Inject
    public ModExtension(final Project project) {
        versioning = project.getObjects().newInstance(Versioning.class);
        getModId().convention(UtilityFunctions.get(project).getProperty("modId"));
    }

    /**
     * @return The versioning configuration.
     */
    public Versioning getVersioning() {
        return versioning;
    }

    /**
     * Executes the given action on the versioning configuration.
     *
     * @param action The action to execute.
     */
    public void versioning(final Action<Versioning> action) {
        action.execute(versioning);
    }

    /**
     * @return The mod id.
     */
    public abstract Property<String> getModId();

    /**
     * Mod versioning configuration.
     */
    public abstract static class Versioning {

        @Inject
        public Versioning(final Project project) {
            getVersion().convention(UtilityFunctions.get(project).getProperty("modVersion").orElse("0.0.1"));
            getSuffix().convention(UtilityFunctions.get(project).getProperty("modVersionSuffix").orElse("alpha"));
        }

        /**
         * @return The mod version.
         */
        public abstract Property<String> getVersion();

        /**
         * @return The mod version suffix.
         */
        public abstract Property<String> getSuffix();
    }
}
