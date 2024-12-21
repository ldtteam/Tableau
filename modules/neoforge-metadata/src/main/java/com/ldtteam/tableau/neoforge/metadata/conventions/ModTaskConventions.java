package com.ldtteam.tableau.neoforge.metadata.conventions;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import com.ldtteam.tableau.neoforge.metadata.components.ModsComponent;
import com.ldtteam.tableau.neoforge.metadata.components.model.Mod;
import com.ldtteam.tableau.neoforge.metadata.extensions.MetadataExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

/**
 * Class that configures the conventions for the tasks that are used to configure each mod metadata.
 */
public class ModTaskConventions {
  
    /**
     * Hidden constructor for utility class.
     */
    private ModTaskConventions() {
        // Hide constructor
    }

    /**
     * Configures the conventions for the tasks that are used to configure each mod metadata.
     * <p>
     * Creates the following tasks:
     * - For each mod metadata in any given source set, a task that configures the inclusion of the logo.
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
        final ModsComponent modsComponent = project.getObjects().newInstance(ModsComponent.class, project, sourceSet);
        modsComponent.configureEach(mod -> configureMod(project, sourceSet, metadataExtension, mod));        
    }

    private static void configureMod(Project project, SourceSetConfiguration sourceSet, MetadataExtension metadataExtension, Mod mod) {
        //TODO: Currently the metadata writing in Mod needs to use the same path as this task, we should consider changing this to a constant.

        // Configure the automatic inclusion of the logo file on process resources.
        final String logoFileName = "%s.png".formatted(mod.getModId());
        final Provider<Directory> logoDirectory = project.getLayout().getBuildDirectory()
                .dir("metadata")
                .map(metadata -> metadata.dir("logos"))
                .map(logos -> logos.dir(mod.getModId()));
        final Provider<RegularFile> logoFile = logoDirectory.map(dir -> dir.file(logoFileName));
        final TaskProvider<Copy> copyLogo = project.getTasks()
                .register(sourceSet.getSourceSet().getTaskName("copy", "%sLogo".formatted(mod.getModId())), Copy.class, copy -> {
                    copy.onlyIf(task -> mod.getLogo().isPresent() && mod.getLogo().get().getAsFile().exists());
                    copy.from(mod.getLogo());
                    copy.into(logoFile);
                });
        project.getTasks().named(sourceSet.getSourceSet().getProcessResourcesTaskName(), ProcessResources.class,
                processResources -> {
                    processResources.from(logoFile);
                    processResources.into("META-INF/Tableau/Logos/%s".formatted(logoFileName));
                    processResources.dependsOn(copyLogo);
                });
    }
}
