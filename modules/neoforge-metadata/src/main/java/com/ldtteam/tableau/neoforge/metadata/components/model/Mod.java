package com.ldtteam.tableau.neoforge.metadata.components.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.git.extensions.GitExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

/**
 * Represents the metadata component for a single mod.
 */
public abstract class Mod implements Named {

    private final String modId;
    private final ModDependencies dependencies;

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
        this.dependencies = project.getObjects().newInstance(ModDependencies.class, project, sourceSet);

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

    @Override
    public String getName() {
        return getModId();
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
    public final ModDependencies getDependencies() {
        return dependencies;
    }

    /**
     * Configures the dependencies block in the mod.
     * 
     * @param depAction The dependency configuration action.
     */
    public void dependencies(Action<ModDependencies> depAction) {
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
}
