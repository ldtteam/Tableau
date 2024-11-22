/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.common;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.common.extensions.VersioningExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.problems.Problems;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Map;

/**
 * The common project plugin.
 * <p>
 *     This plugin is applied to all projects and configures the project with the common settings.
 */
@SuppressWarnings("UnstableApiUsage")
public class CommonProjectPlugin implements Plugin<Project> {


    private final Problems problems;

    /**
     * Creates a new plugin instance.
     *
     * @param problems The problems gradle subsystem to report problems to if found.
     */
    @Inject
    public CommonProjectPlugin(Problems problems) {
        this.problems = problems;
    }

    @Override
    public void apply(@NotNull Project target) {
        //Register all base plugins.
        target.getPlugins().apply("base");
        target.getPlugins().apply("jacoco");
        target.getPlugins().apply("idea");
        target.getPlugins().apply("eclipse");

        //The DSL Extension.
        TableauScriptingExtension.register(target, ModExtension.EXTENSION_NAME, ModExtension.class, target);

        //Configure processing.
        configureVersioning(target);
        configureRepositories(target);
        configureBase(target);

        //Set global duplication strategy
        target.getTasks().withType(Copy.class).configureEach(task -> task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE));

        //Deal with API javadoc management
        target.getTasks()
                .withType(Javadoc.class)
                .matching(task -> task.getName().contains("api"))
                .configureEach(task -> ((StandardJavadocDocletOptions) task.getOptions()).addStringOption("Xdoclint:none", "-quiet"));

        //Set jar duplication strategies
        target.getTasks().withType(Jar.class).configureEach(jar -> jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE));

        //Validate that the user configured a mod id, error out if not set.
        target.afterEvaluate(ignored -> {
            if (!ModExtension.get(target).getModId().isPresent()) {
                throw problems.forNamespace("tableau").throwing(spec -> {
                    //TODO: Configure documentation link.
                    spec.id("missing-mod-id", "Mod id is not configured.")
                            .details("Without a specified mod id a lot of systems can not be configured.")
                            .solution("Configure the mod id, in tableau's mod block.");
                });
            }
        });
    }

    /**
     * Configures all repositories for the project.
     *
     * @param target The target project.
     */
    private void configureRepositories(@NotNull Project target) {
        target.getRepositories().maven(maven -> {
            maven.setUrl("https://ldtteam.jfrog.io/ldtteam/modding/");
            maven.setName("LDT Team Modding");
        });
        target.getRepositories().mavenLocal();
        target.getRepositories().mavenCentral();
        target.getRepositories().flatDir(dir -> {
            dir.dirs("libs");
            dir.setName("Local Libraries");
        });
    }

    /**
     * Configures the versioning for the project.
     * <p>
     *     This will set the version of the project to the version of the mod.
     *     If the minecraft based versioning is enabled, the version will be set to the minecraft based version.
     *     If the minecraft based versioning is disabled, the version will be set to the version of the mod.
     *
     * @param target The target project.
     */
    private void configureVersioning(@NotNull Project target) {
        final VersioningExtension versioning = TableauScriptingExtension.register(target, VersioningExtension.EXTENSION_NAME, VersioningExtension.class, target);

        //Default mod version string build from the configured version and suffix.
        final Provider<String> versionString = versioning.getMod().getVersion().zip(versioning.getMod().getSuffix(), (version, suffix) -> {
            if (suffix.trim().isEmpty()) {
                return version;
            }
            return version + "-" + suffix;
        });

        //Minecraft based versioning provider.
        final Provider<String> minecraftBasedVersion = versionString.flatMap(version ->
                versioning.getMinecraft().getMinecraftVersion().flatMap(minecraftVersion ->
                        versioning.getMinecraft().getSourceVersionName().flatMap(sourceVersionName ->
                                versioning.getMinecraft().getSourceVersionElementIndex().flatMap(sourceVersionIndex ->
                                        versioning.getMinecraft().getMinecraftVersionElementIndex().map(minecraftVersionIndex -> UtilityFunctions.get(target).buildVersionNumberWithOffset(
                                                version,
                                                minecraftVersion,
                                                sourceVersionName,
                                                sourceVersionIndex,
                                                minecraftVersionIndex
                                        ))))));

        //Create a toString() capable object for the project version.
        final ProjectVersion projectVersion = new ProjectVersion(versioning.getMinecraft().getEnabled().zip(versionString, (minecraftBasedVersioningEnabled, version) -> {
            if (minecraftBasedVersioningEnabled) {
                return null;
            }

            return version;
        }).orElse(minecraftBasedVersion));

        //Set the version of the project.
        target.setVersion(projectVersion.toString());
    }

    /**
     * Configures the {@link BasePluginExtension} plugin for the project.
     *
     * @param project The project.
     */
    private void configureBase(final Project project) {
        final BasePluginExtension base = project.getExtensions().getByType(BasePluginExtension.class);
        base.getArchivesName().set(ModExtension.get(project).getModId());
    }

    /**
     * A record to store the project version, returning only the project version when {@link Object#toString()} is called.
     */
    private record ProjectVersion(Provider<String> versionProvider) {
        @Override
        public String toString() {
            return versionProvider().get();
        }
    }
}
