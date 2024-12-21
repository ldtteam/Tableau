package com.ldtteam.tableau.neoforge.metadata.conventions;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import com.ldtteam.tableau.neoforge.metadata.components.AccessTransformersComponent;
import com.ldtteam.tableau.neoforge.metadata.extensions.MetadataExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

/**
 * The class that configures the conventions for the tasks that are used to configure each access transformer.
 */
public class AccessTransformerTaskConventions {
    
    /**
     * Hidden constructor for utility class.
     */
    private AccessTransformerTaskConventions() {
        // Hide constructor
    }

    /**
     * Configures the conventions for the tasks that are used to configure each access transformer.
     * <p>
     * Creates the following tasks:
     * - For each mod metadata in any given source set, a task that configures the inclusion of the access transformers.
     * 
     * @param project The project to configure the conventions for.
     */
    public static void configure(Project project) {
        final SourceSetExtension sourceSetExtension = SourceSetExtension.get(project);
        sourceSetExtension.configureEach(sourceSet -> configureSourceSet(project, sourceSet));
    }

    private static void configureSourceSet(Project project, SourceSetConfiguration sourceSet) {
        configureMetadata(project, sourceSet, MetadataExtension.get(sourceSet));
    }

    private static void configureMetadata(Project project, SourceSetConfiguration sourceSet, MetadataExtension metadataExtension) {
        final AccessTransformersComponent accessTransformerComponent = metadataExtension.getExtensions().getByType(AccessTransformersComponent.class);
        
        //TODO: Currently the metadata writing in AccessTransformerComponent needs to use the same path as this task, we should consider changing this to a constant.
        //Configure the copy and process resources tasks
        final Provider<Directory> atDirectory = project.getLayout().getBuildDirectory()
                .dir("metadata")
                .map(metadata -> metadata.dir("accessTransformers"))
                .map(logos -> logos.dir(sourceSet.getName()));
        final TaskProvider<Copy> copyTransformers = project.getTasks()
                .register(sourceSet.getSourceSet().getTaskName("copy", "%sTransformers".formatted(sourceSet.getName())), Copy.class, copy -> {
                    copy.onlyIf(task -> !accessTransformerComponent.getFiles().isEmpty() && accessTransformerComponent.getFiles().stream().allMatch(File::exists));
                    copy.from(accessTransformerComponent);
                    copy.into(atDirectory);
                });
        project.getTasks().named(sourceSet.getSourceSet().getProcessResourcesTaskName(), ProcessResources.class,
                processResources -> {
                    processResources.from(project.fileTree(atDirectory));
                    processResources.into("META-INF/Tableau/AccessTransformers/%s".formatted(sourceSet.getName()));
                    processResources.dependsOn(copyTransformers);
                });
    }
}
