package com.ldtteam.tableau.neoforge.metadata.components.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.git.extensions.GitExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

/**
 * Represents the metadata component for a single mod.
 */
public abstract class Mod {

    private final String modId;
    private final NamedDomainObjectContainer<Dependency> dependencies;

    /**
     * Creates a new instace of the metadata component.
     * 
     * @param project The project.
     * @param sourceSet The source set.
     * @param modId   The mod id for the metadata.
     */
    @Inject
    public Mod(final Project project, final SourceSetConfiguration sourceSet, final String modId) {
        this.modId = modId;
        this.dependencies = project.getObjects().domainObjectContainer(Dependency.class, name -> {
            return project.getObjects().newInstance(Dependency.class, name);
        });

        final GitExtension gitExtension = GitExtension.get(project);

        this.getVersion().convention(project.getVersion().toString());
        this.getDescription().convention("The %s mod".formatted(modId));
        this.getDisplayName().convention(project.getName());
        this.getLogo().convention(project.getLayout().getProjectDirectory().file("logo.png"));
        this.getCredits().convention(gitExtension.getDevelopers().map(developers -> developers.stream()
                .sorted(Comparator.comparing(GitExtension.Developer::count).reversed())
                .map(developer -> developer.name())
                .collect(Collectors.joining(", "))));
        this.getAuthors().convention(gitExtension.getDevelopers().map(developers -> developers.stream()
                .filter(developer -> developer.count() >= 5)
                .sorted(Comparator.comparing(GitExtension.Developer::count).reversed())
                .map(developer -> developer.name())
                .collect(Collectors.joining(", "))));

        // Configure the automatic inclusion of the logo file on process resources.
        final String logoFileName = "%s.png".formatted(modId);
        final Provider<Directory> logoDirectory = project.getLayout().getBuildDirectory()
                .dir("metadata")
                .map(metadata -> metadata.dir("logos"))
                .map(logos -> logos.dir(modId));
        final Provider<RegularFile> logoFile = logoDirectory.map(dir -> dir.file(logoFileName));
        final TaskProvider<Copy> copyLogo = project.getTasks()
                .register(sourceSet.getSourceSet().getTaskName("copy", "%sLogo".formatted(modId)), Copy.class, copy -> {
                    copy.onlyIf(task -> getLogo().isPresent() && getLogo().get().getAsFile().exists());
                    copy.from(getLogo());
                    copy.into(logoFile);
                });
        project.getTasks().named(sourceSet.getSourceSet().getProcessResourcesTaskName(), ProcessResources.class,
                processResources -> {
                    processResources.from(logoFile);
                    processResources.into("META-INF/Tableau/Logos/%s".formatted(logoFileName));
                    processResources.dependsOn(copyLogo);
                });

        /*
         * //Validate the uniqueness of Access Transformer file names in project
         * project.afterEvaluate(ignored -> {
         * final long distinctFileNameCount = getAccessTransformers().getFiles()
         * .stream()
         * .map(File::getName)
         * .distinct()
         * .count();
         * 
         * final long fileCount = getAccessTransformers().getFiles().size();
         * 
         * if (fileCount != distinctFileNameCount) {
         * throw getProblems().forNamespace("tableau")
         * .throwing(spec -> {
         * spec.id("metadata", "noneUniqueAtFileNames");
         * spec.
         * details("The project needs to have all unique access transformer file names. No duplicates are allowed!"
         * );
         * spec.solution("Rename one of the duplicate access transformer files.");
         * });
         * }
         * });
         * 
         * //Configure the automatic inclusion of access transformer files
         * final Provider<Directory> logoDirectory =
         * project.getLayout().getBuildDirectory()
         * .dir("metadata")
         * .map(metadata -> metadata.dir("logos"))
         * .map(logos -> logos.dir(modId));
         */
    }

    /**
     * The id of the mod that this metadata defines.
     * 
     * @return The mod id.
     */
    @Input
    public String getModId() {
        return modId;
    }

    /**
     * The current version of the mod.
     * <p>
     * Defaults to the project version.
     * 
     * @return The version of the mod.
     */
    @Input
    public abstract Property<String> getVersion();

    /**
     * The display name of the project.
     * <p>
     * Defaults to the current projects name.
     * 
     * @return The display name of the project.
     */
    @Input
    public abstract Property<String> getDisplayName();

    /**
     * The url where Forge can find the update json.
     * <p>
     * This is an optional field.
     * 
     * @return The update file url.
     */
    @Input
    @Optional
    public abstract Property<String> getUpdateUrl();

    /**
     * The url where Forge can send a user to for documentation or help.
     * 
     * @return The display Url.
     */
    @Input
    @Optional
    public abstract Property<String> getDisplayUrl();

    /**
     * A file that should be included as a logo into the main jar.
     * <p>
     * This is optional.
     * <p>
     * This file will automatically be included in your mod resources when it is
     * compiled.
     * <p>
     * Defaults to the logo.png file in the project directory.
     * 
     * @return The logo file.
     */
    @Input
    @Optional
    public abstract RegularFileProperty getLogo();

    /**
     * Defines the credits given to all people that worked on the mod.
     * <p>
     * Defaults to all contributors from the git repository.
     * 
     * @return The credits.
     */
    @Input
    @Optional
    public abstract Property<String> getCredits();

    /**
     * Defines the main authors of the project.
     * <p>
     * Defaults to all contributors from the git repository with more then 5
     * commits.
     * 
     * @return The main authors of the project.
     */
    @Input
    @Optional
    public abstract Property<String> getAuthors();

    /**
     * Defines the project description.
     * <p>
     * This field is optional.
     * 
     * @return The project description.
     */
    @Input
    public abstract Property<String> getDescription();

    /**
     * Gives access to all dependencies of this mod.
     * 
     * @return The dependencies of this mod.
     */
    @Nested
    public final NamedDomainObjectContainer<Dependency> getDependencies() {
        return dependencies;
    }

    /**
     * Configures the dependencies block in the mod.
     * 
     * @param depAction The dependency configuration action.
     */
    public void dependencies(Action<NamedDomainObjectContainer<Dependency>> depAction) {
        depAction.execute(getDependencies());
    }

    /**
     * Writes the metadata described in this model to a config.
     * <p>
     * Does not include the dependencies.
     * 
     * @return The metadata in config form.
     */
    public CommentedConfig writeMetadata() {
        final CommentedConfig config = CommentedConfig.inMemory();

        config.set("modId", getModId());
        config.set("version", getVersion().get());
        config.set("displayName", getDisplayName().get());

        if (getUpdateUrl().isPresent()) {
            config.set("updateJSONURL", getUpdateUrl().get());
        }

        if (getDisplayUrl().isPresent()) {
            config.set("displayURL", getDisplayUrl().get());
        }

        if (getLogo().isPresent()) {
            final String logoFileName = "%s.png".formatted(modId);
            config.set("logoFile", "META-INF/Tableau/Logos/%s".formatted(logoFileName));
        }

        if (getCredits().isPresent()) {
            config.set("credits", getCredits().get());
        }

        if (getAuthors().isPresent()) {
            config.set("authors", getAuthors().get());
        }

        config.set("description", getDescription().get());

        return config;
    }

    /**
     * Writes the dependencies described in this model to a config.
     * <p>
     * Does not include the metadata.
     * 
     * @return The dependencies in config form.
     */
    public List<CommentedConfig> writeDependencies() {
        final List<CommentedConfig> configs = new ArrayList<>();

        getDependencies().forEach(dependency -> configs.add(dependency.writeDependency()));

        return configs;
    }

    /**
     * Model for a dependency of a mod.
     */
    public static abstract class Dependency implements Named {

        private final String modId;

        /**
         * Creates a new dependency model
         * 
         * @param modId   The mod id of the dependency.
         */
        @Inject
        public Dependency(String modId) {
            this.modId = modId;

            this.getType().convention(Type.REQUIRED);
            this.getSide().convention(Side.BOTH);
        }

        /**
         * Writes the current model into a config that can be written to disk.
         * 
         * @return The 
         */
        private CommentedConfig writeDependency() {
            final CommentedConfig config = CommentedConfig.inMemory();

            config.set("modId", getModId());
            config.set("type", getType().get().name().toLowerCase(Locale.ROOT));
            config.set("version", getVersionRange().get());

            if (getReason().isPresent()) {
                config.set("reason", getReason().get());
            }

            config.set("ordering", getOrdering().getOrElse(Ordering.NONE).name().toUpperCase(Locale.ROOT));
            config.set("side", getSide().getOrElse(Side.BOTH).name().toUpperCase(Locale.ROOT));

            return config;
        }

        /**
         * The mod id of the dependency that this models.
         * 
         * @return The mod id.
         */
        public String getModId() {
            return modId;
        }

        @Override
        public String getName() {
            return getModId();
        }

        /**
         * The type of the dependency that this defines.
         * <p>
         * This defaults to a REQUIRED dependency.
         * 
         * @return The dependency type.
         */
        @Input
        public abstract Property<Type> getType();

        /**
         * Indicates the reason for the dependency configuration.
         * <p>
         * Needs to be supplied for an INCOMPATIBLE, or DISCOURAGED dependency.
         * 
         * @return The reason for the dependency configuration.
         */
        @Input
        @Optional
        public abstract Property<String> getReason();

        /**
         * The version range that is supported by this dependency.
         * <p>
         * This field is optional.
         * 
         * @return The supported or not supported version range.
         */
        @Input
        @Optional
        public abstract Property<String> getVersionRange();

        /**
         * An optional ordering flag that determines mod loading order of this
         * dependency configuration.
         * <p>
         * This field is optional.
         * 
         * @return The ordering of the dependency.
         */
        @Input
        @Optional
        public abstract Property<Ordering> getOrdering();

        /**
         * An optional distribution indicator that determines on what distribution the
         * dependency should be considered.
         * <p>
         * Defaults to BOTH.
         * 
         * @return The side.
         */
        @Input
        @Optional
        public abstract Property<Side> getSide();

        /**
         * Defines the available dependency types.
         */
        public enum Type {
            /**
             * Marks the dependency as required, erroring out when it is not installed.
             */
            REQUIRED,

            /**
             * Marks the dependency as optional, allowing the game to run when it is not
             * installed.
             * <p>
             * Usefull for when you need to declare your mod to optionally run after
             * something else to configure compatibility.
             */
            OPTIONAL,

            /**
             * Hard incompatibility, the game does not start.
             */
            INCOMPATIBLE,

            /**
             * Soft incompatibility, the game will start with a big warning.
             */
            DISCOURAGED;
        }

        /**
         * Defines whether this mod needs to load before or after the defined
         * dependency.
         */
        public enum Ordering {
            /**
             * This mod is loaded before the dependency.
             */
            BEFORE,

            /**
             * This mod is loaded after the dependency.
             */
            AFTER,

            /**
             * Indicates that this mod has no ordering preference towards its dependency
             */
            NONE;
        }

        /**
         * Indicates whether the dependency should be considered in what distribution.
         */
        public enum Side {
            /**
             * The dependency needs to be considered both on the client as well as on the
             * dedicated server.
             */
            BOTH,

            /**
             * Indicates that this dependency needs only to be considered on the client,
             * usefull for depending on rendering mods.
             */
            CLIENT,

            /**
             * Indicates that this dependency needs only to be considered on the server,
             * usefull for depending on server plugin mods.
             */
            SERVER
        }
    }
}
