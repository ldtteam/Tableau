package com.ldtteam.tableau.common.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Versioning extension, handles the project configuration for versioning.
 */
public abstract class VersioningExtension {

    /**
     * Gets the versioning extension for a given project.
     *
     * @param project The project.
     * @return The versioning extension.
     */
    public static VersioningExtension get(final Project project) {
        return TableauScriptingExtension.get(project, VersioningExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "versioning";

    private final MinecraftBasedVersioning minecraft;
    private final ModExtension.Versioning mod;

    @Inject
    public VersioningExtension(final Project project) {
        minecraft = project.getObjects().newInstance(MinecraftBasedVersioning.class, project);
        mod = ModExtension.get(project).getVersioning();
    }

    /**
     * @return The minecraft based versioning configuration setup.
     */
    public MinecraftBasedVersioning getMinecraft() {
        return minecraft;
    }

    /**
     * Executes the given action on the minecraft based versioning configuration.
     *
     * @param action The action to execute.
     */
    public void minecraft(final Action<MinecraftBasedVersioning> action) {
        action.execute(minecraft);
    }

    /**
     * @return The mod versioning configuration setup.
     */
    public ModExtension.Versioning getMod() {
        return mod;
    }

    /**
     * Executes the given action on the mod versioning configuration.
     *
     * @param action The action to execute.
     */
    public void mod(final Action<ModExtension.Versioning> action) {
        action.execute(mod);
    }

    /**
     * Minecraft based versioning configuration.
     */
    public static abstract class MinecraftBasedVersioning {

        @Inject
        public MinecraftBasedVersioning(final Project project) {
            getEnabled().convention(false);
            getMinecraftVersion().convention(ModExtension.get(project).getMinecraftVersion());
            getMinecraftVersionElementIndex().convention(1);
            getSourceVersionElementIndex().convention(1);
            getSourceVersionName();
        }

        /**
         * @return Indicates whether the minecraft based versioning is enabled.
         */
        public abstract Property<Boolean> getEnabled();

        /**
         * @return The minecraft version.
         */
        public abstract Property<String> getMinecraftVersion();

        /**
         * @return The minecraft version element index.
         */
        public abstract Property<Integer> getMinecraftVersionElementIndex();

        /**
         * @return The source version element index.
         */
        public abstract Property<Integer> getSourceVersionElementIndex();

        /**
         * @return The source version name.
         */
        public abstract Property<String> getSourceVersionName();
    }


}
