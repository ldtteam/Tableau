package com.ldtteam.tableau.neoforge.metadata.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
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

    /**
     * The name of the component.
     */
    public static final String NAME = "accessTransformers";

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

        /*
        //The convention of our data is that it takes the sourcesets module data and looks for the relevant at-file in the META-INF directory.
        this.convention(sourceSet.getSourceSet().getResources().getSourceDirectories().getElements()
                .map(sourceDirectories -> {
                    return sourceDirectories.stream()
                    .filter(Directory.class::isInstance)
                    .map(Directory.class::cast)
                    .findFirst()
                    .orElseGet(() -> {
                        project.getLogger().warn("No source directory configured for source set %s. Skipping ATs.".formatted(sourceSet.getName()));
                        return project.getLayout().getProjectDirectory().dir(".tableau");
                    }); //Empty out the outer provider when there is no source directory configured.
                })
                .map(firstResourceDirectory -> {
                    return firstResourceDirectory.dir("META-INF").file("accesstransformer.cfg");
                }));*/
        
        //Register the usage of the in us contained ATs.
        final AccessTransformers accessTransformers = project.getExtensions().getByType(Minecraft.class).getAccessTransformers();
        accessTransformers.getFiles().from(this);
    }

    @Override
    public void write(CommentedConfig config) {
        final List<CommentedConfig> transformers = new ArrayList<>();

        if (!this.isEmpty()) {
            for (File file : getFiles()) {
                if (file.exists()) {
                    final CommentedConfig fileConfig = CommentedConfig.inMemory();
                    fileConfig.set("file", "META-INF/Tableau/AccessTransformers/%s/%s".formatted(sourceSet.getName(), file.getName()));
                    transformers.add(fileConfig);
                }
            }    
        }
        
        config.set("accessTransformers", transformers);
    }
}
