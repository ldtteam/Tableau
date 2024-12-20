package com.ldtteam.tableau.neoforge.metadata.extensions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import com.ldtteam.tableau.neoforge.metadata.api.IMetadataComponent;
import com.ldtteam.tableau.neoforge.metadata.tasks.GenerateMetadataTask;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

/**
 * Defines an extension to manage the FML metadata needed for a given NeoForge project.
 */
public abstract class MetadataExtension implements ExtensionAware {

    /**
     * Gets the current metadata extensions for a given {@link SourceSetConfiguration source set}.
     * 
     * @param sourceSet The source set to get the metadata for.
     * @return The current metadata extension.
     */
    public static MetadataExtension get(final SourceSetConfiguration sourceSet) {
        return sourceSet.getExtensions().getByType(MetadataExtension.class);
    }

    /**
     * The name of the extension
     */
    public static final String EXTENSION_NAME = "metadata";

    //This is the path within the build directory to which we write our data, it represents a virtual jar root.
    private static final String GENERATION_ROOT_PATH = "metadata/neoforge";

    //This is the path within a jar where the metadata file needs to be written to.
    private static final String IN_JAR_PATH = "META-INF/neoforged.mods.toml";

    /**
     * Creates a new instance of the extension.
     * <p>
     * Will register the relevant generation tasks, so that the process resources task of the given source set will generate a properly located 
     * neoforge.mods.toml file.
     * 
     * @param project The project that the sourceset lives in.
     * @param sourceSetConfiguration The sourceSet for which this metadata is generated.
     */
    @Inject
    public MetadataExtension(final Project project, final SourceSetConfiguration sourceSetConfiguration) {
        //Get the raw sourceset so we can manipulate it.
        final SourceSet sourceSet = sourceSetConfiguration.getSourceSet();

        //Determine where to generate the file, and register the right directory as resources root, so that processResources picks it up.
        final Provider<Directory> generationDirectory = project.getLayout().getBuildDirectory()
            .map(buildDir -> buildDir.dir(GENERATION_ROOT_PATH))
            .map(generationRootDir -> generationRootDir.dir(sourceSet.getName()));

        sourceSet.getResources().srcDir(generationDirectory);

        final Provider<RegularFile> outputFile = generationDirectory.map(directory -> directory.file(IN_JAR_PATH));

        //Get process resources.
        final TaskProvider<ProcessResources> processResources = project.getTasks().named(sourceSet.getProcessResourcesTaskName(), ProcessResources.class);

        //Create and configure the tasks.
        final TaskProvider<GenerateMetadataTask> generateMetadata = project.getTasks().register(
            sourceSet.getTaskName("write", "NeoForgeMetadata"),
            GenerateMetadataTask.class,
            task -> {
                //Collect and register all the components.
                task.getComponents()
                    .set(task.getProject().provider(() ->  getMetadataComponents()));

                //Configure the output file.
                task.getOutput()
                    .set(outputFile);

                //Only run the task if we are enabled.
                task.onlyIf("Generation is enabled", ignored -> !getIsEnabled().isPresent() || getIsEnabled().get());
            }
        );

        //Make process resources depend on the generation.
        processResources.configure(task -> task.dependsOn(generateMetadata));

        //By default we only generate metadata for a source set that is actually the main source set, users need to enable all other source sets.
        getIsEnabled().convention(SourceSet.isMain(sourceSet));
    }

    private List<IMetadataComponent> getMetadataComponents() {
        final List<IMetadataComponent> result = new ArrayList<>();

        //We loop over the entire registered schema
        getExtensions().getExtensionsSchema().forEach(schema -> {
            //Get the objects for each schema element in our extension spec.
            final Object extension = getExtensions().findByName(schema.getName());
            if (extension instanceof IMetadataComponent metadataComponent) {
                //When we have a metadata component we add the component to the returned list, ensuring its further processing.
                result.add(metadataComponent);
            }
        });

        return result;
    }


    /**
     * Indicates whether the generation is enabled for this sourceset.
     * <p>
     * By default this is only the case for the main source set.
     * 
     * @return True when enabled, false when not.
     */
    public abstract Property<Boolean> getIsEnabled();
}
