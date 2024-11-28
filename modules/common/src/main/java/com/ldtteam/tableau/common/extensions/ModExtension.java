package com.ldtteam.tableau.common.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.net.URI;

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
        return TableauScriptingExtension.get(project, ModExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "mod";

    private final Versioning versioning;

    /**
     * Creates a new mod extension model.
     *
     * @param project The project for the model.
     */
    @Inject
    public ModExtension(final Project project) {
        versioning = project.getObjects().newInstance(Versioning.class);

        getPublisher().convention("ldtteam");
    }

    /**
     * The versioning model for this mod.
     *
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
     * The current mod id for the mod.
     * <p>
     *     Not configuring the mod id, will likely result in an error during the build.
     * </p>
     *
     * @return The mod id.
     */
    public abstract Property<String> getModId();

    /**
     * The mod group.
     * <p>
     *     Generally the mod group is the name of the modding team or the name of the modding group, in reverse DNS order.
     *     So, for example: com.ldtteam, or com.github.example-team
     * <p>
     *     Not configuring the mod group, will likely result in an error during the build.
     *
     * @return The mod group.
     */
    public abstract Property<String> getGroup();

    /**
     * The current main minecraft version against which the mod is build.
     *
     * @return The minecraft version.
     */
    public abstract Property<String> getMinecraftVersion();

    /**
     * The mod display name.
     *
     * @return The mod display name.
     */
    public abstract Property<String> getModName();

    /**
     * The full mod description.
     *
     * @return The mod description.
     */
    public abstract Property<String> getModDescription();

    /**
     * The mod logo file.
     *
     * @return The mod logo.
     */
    public abstract Property<String> getModLogo();

    /**
     * The display name of the team or contributor who publishes the mod.
     *
     * @return The mod publisher.
     */
    public abstract Property<String> getPublisher();

    /**
     * The url of the website where documentation can be found for the project.
     *
     * @return The website url, the location where documentation/support can be found.
     */
    public abstract Property<URI> getDisplayUrl();

    /**
     * The url of the website where the source code can be found for the project.
     *
     * @return The repository url, is the location where the source code can be found.
     */
    public abstract Property<URI> getRepositoryUrl();

    /**
     * The url of the website where the issue tracker can be found for the project.
     *
     * @return The issue tracker url, where issues may be filed for the mod.
     */
    public abstract Property<URI> getIssueTrackerUrl();

    /**
     * The license for the mod.
     *
     * @return The license used for the mod.
     */
    public abstract Property<String> getLicense();

    /**
     * Mod versioning configuration model.
     */
    public abstract static class Versioning {

        /**
         * Creates a new versioning model.
         */
        @Inject
        public Versioning() {
            getVersion().convention("0.0.0");
            getSuffix().convention("alpha");
        }

        /**
         * The semver or maven compatible version string.
         * <p>
         *     Mods should generally use maven versioning.
         * <p>
         *     The default value is 0.0.0
         *
         * @return The mod version.
         */
        public abstract Property<String> getVersion();

        /**
         * The version suffix.
         * <p>
         *     This should generally be something like -SNAPSHOT, -RELEASE or empty.
         * <p>
         *     Might also indicate the branch that was used as source to build this version of the mod.
         *
         * @return The mod version suffix.
         */
        public abstract Property<String> getSuffix();
    }
}
