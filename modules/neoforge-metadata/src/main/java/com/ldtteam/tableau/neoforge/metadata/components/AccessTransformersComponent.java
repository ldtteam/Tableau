package com.ldtteam.tableau.neoforge.metadata.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.neoforge.metadata.api.IMetadataComponent;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;
import com.ldtteam.tableau.utilities.utils.DelegatingConfigurableFileCollection;

import net.neoforged.gradle.dsl.common.extensions.AccessTransformers;
import net.neoforged.gradle.dsl.common.extensions.Minecraft;

/**
 * The metadata component that describes the contained and applied access transformers for the source set.
 */
public class AccessTransformersComponent extends DelegatingConfigurableFileCollection implements IMetadataComponent {

    private final SourceSetConfiguration sourceSet;

    /**
     * Creates a new instance of the metadata component.
     * <p>
     * Preconfigures neogradle and automatically includes the given ATs in the jar.
     * 
     * @param project The project that this component belongs to.
     * @param sourceSet The source set that this component belongs to.
     */
    @Inject
    public AccessTransformersComponent(final Project project, final SourceSetConfiguration sourceSet) {
        super(project.getObjects().fileCollection());
        
        this.sourceSet = sourceSet;

        //Configure the copy and process resources tasks
        final Provider<Directory> atDirectory = project.getLayout().getBuildDirectory()
                .dir("metadata")
                .map(metadata -> metadata.dir("accessTransformers"))
                .map(logos -> logos.dir(sourceSet.getName()));
        final TaskProvider<Copy> copyTransformers = project.getTasks()
                .register(sourceSet.getSourceSet().getTaskName("copy", "%sTransformers".formatted(sourceSet.getName())), Copy.class, copy -> {
                    copy.onlyIf(task -> !getFiles().isEmpty() && getFiles().stream().allMatch(File::exists));
                    copy.from(this);
                    copy.into(atDirectory);
                });
        project.getTasks().named(sourceSet.getSourceSet().getProcessResourcesTaskName(), ProcessResources.class,
                processResources -> {
                    processResources.from(project.fileTree(atDirectory));
                    processResources.into("META-INF/Tableau/AccessTransformers/%s".formatted(sourceSet.getName()));
                    processResources.dependsOn(copyTransformers);
                });

        //The convention of our data is that it takes the sourcesets module data and looks for the relevant at-file in the META-INF directory.
        this.convention(sourceSet.getSourceSet().getResources().getSourceDirectories().getElements()
                .map(sourceDirectories -> {
                    return sourceDirectories.stream()
                    .filter(Directory.class::isInstance)
                    .map(Directory.class::cast)
                    .findFirst()
                    .orElse(null); //Empty out the outer provider when there is no source directory configured.
                })
                .map(firstResourceDirectory -> {
                    return firstResourceDirectory.dir("META-INF").file("accesstransformer.cfg");
                }));
        
        //Register the usage of the in us contained ATs.
        final AccessTransformers accessTransformers = project.getExtensions().getByType(Minecraft.class).getAccessTransformers();
        accessTransformers.getFiles().from(this);
    }

    @Override
    public void write(CommentedConfig config) {
        final List<CommentedConfig> transformers = new ArrayList<>();

        for (File file : getFiles()) {
            if (file.exists()) {
                final CommentedConfig fileConfig = CommentedConfig.inMemory();
                fileConfig.set("file", "META-INF/Tableau/AccessTransformers/%s/%s".formatted(sourceSet.getName(), file.getName()));
                transformers.add(fileConfig);
            }
        }

        config.set("accessTransformers", transformers);
    }
}
