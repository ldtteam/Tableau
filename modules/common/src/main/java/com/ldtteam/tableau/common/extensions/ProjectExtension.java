package com.ldtteam.tableau.common.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ProblemId;
import org.gradle.api.problems.Problems;
import org.gradle.api.problems.Severity;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

/**
 * Mod extension, handles the project configuration for the mod.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class ProjectExtension {

    private static final ProblemGroup COMMON_GROUP = TableauScriptingExtension.problemGroup("common", "Common");

    /**
     * Gets the mod extension for a given project.
     *
     * @param project The project.
     * @return The mod extension.
     */
    public static ProjectExtension get(final Project project) {
        return TableauScriptingExtension.get(project, ProjectExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "project";

    private final Versioning versioning;

    /**
     * Creates a new mod extension model.
     *
     * @param project The project for the model.
     */
    @Inject
    public ProjectExtension(final Project project) {
        versioning = project.getObjects().newInstance(Versioning.class, project);

        getPublisher().convention("ldtteam");

        getModId().convention(project.provider(() -> {
            throw getProblems().getReporter().throwing(new IllegalStateException("Mod id is not configured."),
                    ProblemId.create("missing-mod-id", "Mod id is not configured.", COMMON_GROUP),
                    spec -> {
                        spec.details("Without a specified mod id a lot of systems can not be configured.")
                                .solution("Configure the mod id, in tableau's project block.")
                                .documentedAt("https://tableau.ldtteam.com/docs/getting-started#configuring-the-basics");
                    });
        }));

        getGroup().convention(project.provider(() -> {
            throw getProblems().getReporter().throwing(
                    new IllegalStateException("Project group is not configured."),
                    ProblemId.create("missing-project-group", "Project group is not configured.", COMMON_GROUP),
                    spec -> {
                        spec.details("Without a specified project group a lot of systems can not be configured.")
                                .solution("Configure the projects group, in tableau's project block.")
                                .documentedAt("https://tableau.ldtteam.com/docs/getting-started#configuring-the-basics");
                    });
        }));

        getMinecraftVersion().convention(project.getProviders().gradleProperty("minecraft.version"));
    }

    /**
     * The gradle problem API for handling configuration issues.
     *
     * @return The problem API.
     */
    @Inject
    protected abstract Problems getProblems();

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
     * Not configuring the mod id, will likely result in an error during the build.
     *
     * @return The mod id.
     */
    public abstract Property<String> getModId();

    /**
     * The mod group.
     * <p>
     * Generally the mod group is the name of the modding team or the name of the modding group, in reverse DNS order.
     * So, for example: com.ldtteam, or com.github.example-team
     * <p>
     * Not configuring the mod group, will likely result in an error during the build.
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
     * The display name of the team or contributor who publishes the mod.
     *
     * @return The mod publisher.
     */
    public abstract Property<String> getPublisher();

    /**
     * The url of the website where documentation or source code can be found for the project.
     *
     * @return The mod url, is the location where documentation and support can be found.
     */
    public abstract Property<String> getUrl();

    /**
     * Mod versioning configuration model.
     */
    public abstract static class Versioning {

        private final Project project;

        /**
         * Creates a new versioning model.
         *
         * @param project The project for the model.
         */
        @Inject
        public Versioning(Project project) {
            this.project = project;
            getVersion().convention(environmentVariable("VERSION")
                    .orElse(project.getProviders().gradleProperty("modVersion"))
                    .orElse(project.getProviders().gradleProperty("mod.version"))
                    .orElse(project.getProviders().gradleProperty("local.version"))
                    .orElse(project.getProviders().gradleProperty("localVersion"))
                    .orElse("0.0.0"));
            getSuffix().convention(project.getProviders().gradleProperty("modVersionSuffix")
                    .orElse(project.getProviders().gradleProperty("local.suffix"))
                    .orElse(project.getProviders().gradleProperty("localSuffix"))
                    .orElse(""));
        }

        /**
         * The semver or maven compatible version string.
         * <p>
         * Mods should generally use maven versioning.
         * <p>
         * The default value is 0.0.0
         *
         * @return The mod version.
         */
        public abstract Property<String> getVersion();

        /**
         * The version suffix.
         * <p>
         * This should generally be something like -SNAPSHOT, -RELEASE or empty.
         * <p>
         * Might also indicate the branch that was used as source to build this version of the mod.
         *
         * @return The mod version suffix.
         */
        public abstract Property<String> getSuffix();

        /**
         * Creates a provider for a value of a given environment variable.
         *
         * @param key The key of the environment variable.
         * @return The provider for the value of the environment variable.
         */
        public Provider<String> environmentVariable(final String key) {
            return project.getProviders().environmentVariable(key);
        }

        /**
         * Creates a provider for a value of a given gradle property.
         *
         * @param key The key of the system property.
         * @return The provider for the value of the gradle property.
         */
        public Provider<String> property(final String key) {
            return project.getProviders().gradleProperty(key);
        }
    }
}
