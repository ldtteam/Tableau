package com.ldtteam.tableau.neoforge.metadata.tasks;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.ldtteam.tableau.neoforge.metadata.api.IMetadataComponent;
import com.ldtteam.tableau.neoforge.metadata.toml.AutomaticConfig;

/**
 * Defines a task that can write the metadata supplied to it via the components.
 */
@CacheableTask
public abstract class GenerateMetadataTask extends DefaultTask {

    /**
     * Creates a new task instance.
     */
    @Inject
    public GenerateMetadataTask() {
    }

    /**
     * Invoked to actually write the toml metadata file from the given components.
     * <p>
     * If the output file does not exist it will be created
     * <p>
     * If no components exist, an empty metadata file will be created.
     * 
     * @throws Exception When a failure occurs during the writing phase.
     */
    @TaskAction
    public void WriteToml() throws Exception {
        try(final CommentedFileConfig config = AutomaticConfig.create(getOutput().get().getAsFile())) {
            getComponents().get().forEach(component -> component.write(config));
        }
    }
    
    /**
     * All components that make up the metadata to write.
     * 
     * @return The metadata.
     */
    @Nested
    public abstract ListProperty<IMetadataComponent> getComponents();

    /**
     * The file to write the metadata to.
     * 
     * @return The metadata file.
     */
    @OutputFile
    public abstract RegularFileProperty getOutput();
}
