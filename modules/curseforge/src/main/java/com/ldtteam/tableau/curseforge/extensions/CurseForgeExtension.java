package com.ldtteam.tableau.curseforge.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import net.darkhax.curseforgegradle.Constants;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Extension that configures the CurseForge plugin.
 */
public abstract class CurseForgeExtension {

    /**
     * Gets the CurseForge extension for the given project.
     *
     * @param project the project to get the extension from
     * @return the CurseForge extension
     */
    public static CurseForgeExtension get(final Project project) {
        return TableauScriptingExtension.get(project, CurseForgeExtension.class);
    }

    /**
     * The name of the CurseForge extension.
     */
    public static final String EXTENSION_NAME = "curse";

    private final Project project;

    /**
     * Creates a new extension for the given project.
     *
     * @param project The project to create the extension for.
     */
    @Inject
    public CurseForgeExtension(final Project project) {
        this.project = project;

        getReleaseType().convention(project.getProviders().environmentVariable("CURSE_RELEASE_TYPE").map(value -> {
            try {
                return ReleaseType.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                project.getLogger().warn("Invalid release type: {}, falling back to ALPHA", value);
                return ReleaseType.ALPHA;
            }
        }).orElse(ReleaseType.RELEASE));

        getUsesFancyDisplayName().convention(true);

        getDebug().convention(false);
    }

    /**
     * The id of the project un CurseForge.
     * <p>
     *     Is a numeric.
     * @return The id of the project on CurseForge.
     */
    public abstract Property<Integer> getId();

    /**
     * The possible release types for a project on CurseForge.
     */
    public enum ReleaseType {
        /**
         * Indicates that this build is an ALPHA build.
         * <p>
         *     Does not show up in normal search results.
         */
        ALPHA,

        /**
         * Indicates that this is a pre-release build.
         * <p>
         *     Shows up in normal search results
         */
        BETA,

        /**
         * Indicates that this build is a full release build
         * <p>
         *     Shows up in normal search results
         * <p>
         *     Is the default value if not specified
         */
        RELEASE;

        /**
         * Creates a lowercase string representation of the name of this release type.
         *
         * @return The name of the release type.
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * The release type to use for this build.
     *
     * @return The release type of the project on CurseForge.
     */
    public abstract Property<ReleaseType> getReleaseType();

    /**
     * The minecraft versions which this build additionally supports.
     *
     * @return The additional Minecraft versions to support, will automatically add the NeoGradle minimal minecraft version.
     */
    public abstract SetProperty<String> getAdditionalMinecraftVersions();

    /**
     * Whether to use the fancy display name for the projects files.
     * <p>
     *     This will make the files have a display name that includes the project version and the release type.
     * @return whether to use the fancy display name for project files.
     */
    public abstract Property<Boolean> getUsesFancyDisplayName();

    /**
     * The name of the artifact that this project produces.
     *
     * @return The name of the artifact to upload to CurseForge.
     */
    public abstract Property<String> getArtifactName();

    /**
     * The relationships of the project on CurseForge.
     * <p>
     *     The key is the slug of the project in the relationship and the value is the kind of relationship.
     *     As values, you can use:
     *     <ul>
     *         <li>embeddedLibrary</li>
     *         <li>incompatible</li>
     *         <li>optionalDependency</li>
     *         <li>requiredDependency</li>
     *         <li>tool</li>
     *     </ul>
     * @return the relationships this project has with others
     */
    public abstract MapProperty<String, String> getRelationships();

    /**
     * Configures the relationships of the project on CurseForge.
     *
     * @param action the action to configure the relationships
     */
    public void relationships(Action<? super Relationships> action) {
        action.execute(project.getObjects().newInstance(Relationships.class, getRelationships()));
    }

    /**
     * Whether to enable debug mode for the CurseForge plugin.
     *
     * @return whether to enable debug mode for the CurseForge plugin.
     */
    public abstract Property<Boolean> getDebug();

    /**
     * The DSL for configuring the relationships of the project on CurseForge.
     */
    public abstract static class Relationships {

        private final MapProperty<String, String> relationships;

        /**
         * Creates a new relationships model.
         *
         * @param relationships The backing data map.
         */
        @Inject
        public Relationships(MapProperty<String, String> relationships) {
            this.relationships = relationships;
        }

        /**
         * The relationships of the project on CurseForge.
         * <p>
         *     The key is the slug of the project in the relationship and the value is the kind of relationship.
         *     As values, you can use:
         *     <ul>
         *         <li>embeddedLibrary</li>
         *         <li>incompatible</li>
         *         <li>optionalDependency</li>
         *         <li>requiredDependency</li>
         *         <li>tool</li>
         *     </ul>
         * @return the relationships this project has with other published projects on CurseForge.
         */
        public MapProperty<String, String> getRelationships() {
            return this.relationships;
        }

        /**
         * Adds the given project as a tool that can be used with this project.
         *
         * @param slug the slug of the project
         */
        public void tool(String slug) {
            getRelationships().put(slug, Constants.RELATION_TOOL);
        }

        /**
         * Adds the given project as an embedded library that is included in this project.
         *
         * @param slug the slug of the project
         */
        public void embedded(String slug) {
            getRelationships().put(slug, Constants.RELATION_EMBEDDED);
        }

        /**
         * Marks the given project as being incompatible with sodium.
         *
         * @param slug the slug of the project
         */
        public void incompatible(String slug) {
            getRelationships().put(slug, Constants.RELATION_INCOMPATIBLE);
        }

        /**
         * Marks the given project as an optional dependency.
         *
         * @param slug the slug of the project
         */
        public void optional(String slug) {
            getRelationships().put(slug, Constants.RELATION_OPTIONAL);
        }

        /**
         * Marks the given project as a required dependency.
         *
         * @param slug the slug of the project
         */
        public void required(String slug) {
            getRelationships().put(slug, Constants.RELATION_REQUIRED);
        }
    }
}
