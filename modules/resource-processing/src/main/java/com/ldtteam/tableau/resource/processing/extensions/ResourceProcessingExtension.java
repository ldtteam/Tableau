package com.ldtteam.tableau.resource.processing.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.language.jvm.tasks.ProcessResources;

import javax.inject.Inject;

/**
 * Extension that configures the resource processing.
 */
public abstract class ResourceProcessingExtension implements ExtensionAware {

    /**
     * Gets the resource processing extension for a given project.
     *
     * @param project The project.
     * @return The resource processing extension.
     */
    public static ResourceProcessingExtension get(final Project project) {
        return TableauScriptingExtension.get(project, ResourceProcessingExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "resourceProcessing";

    private final Project project;

    /**
     * Creates a new plugin instance.
     *
     * @param project The project to create the instance in.
     */
    @Inject
    public ResourceProcessingExtension(final Project project) {
        this.project = project;

        getIsEnabled().convention(true);

        project.afterEvaluate(ignored -> {
            //Get all source sets.
            final SourceSetContainer container = project.getExtensions().getByType(SourceSetContainer.class);
            container.all(sourceSet -> {

                //For each source set get its process resources task
                project.getTasks().named(sourceSet.getProcessResourcesTaskName(), ProcessResources.class, task -> {
                    //Configure interpolation. The following .get() calls is why this is in an afterEvaluate block.
                    getMatching().get().forEach(match -> {
                        task.filesMatching(match, fileCopyDetails -> {
                            fileCopyDetails.expand(getProperties().get());
                        });
                    });

                    task.getInputs().properties(getProperties().get());
                });
            });
        });
    }

    /**
     * Indicates whether resource processing is enabled or not.
     * <p>
     *     When resource processing is enabled, then it needs to be ran before a run.
     *
     * @return True when enabled, false when not.
     */
    public abstract Property<Boolean> getIsEnabled();

    /**
     * The properties that should be interpolated during resource processing.
     *
     * @return The properties to extend the resource processing.
     */
    public abstract MapProperty<String, Object> getProperties();

    /**
     * Uses the project properties as interpolation values.
     */
    public void fromProject() {
        project.getProperties().forEach((key, value) -> {
            if (value instanceof String || value instanceof Number) {
                getProperties().put(key, value);
            }
        });

        getProperties().put("version", project.getVersion().toString());
    }

    /**
     * A list of glob patterns that match file paths in the project which should receive interpolation.
     *
     * @return The matching patterns, which select the files to interpolate.
     */
    public abstract ListProperty<String> getMatching();

    /**
     * Interpolates the mods.toml files.
     * Regardless of whether this is neoforge or mcf.
     */
    public void modsToml() {
        getMatching().add("**/**.mods.toml");
        getMatching().add("**/mods.toml");
    }

    /**
     * Interpolates all files.
     */
    public void all() {
        getMatching().add("**/**");
    }
}
