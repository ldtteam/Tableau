package com.ldtteam.tableau.crowdin.tasks;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A task that merges translations generated or pre-existing by datagen.
 */
@CacheableTask
public abstract class MergeTranslations extends DefaultTask {

    public MergeTranslations() {
        setGroup("Crowdin");
        setDescription("Merges the source translations into one translation set, and then writes that set to all targets.");
    }

    /**
     * Creates a merged translation as the task action.
     *
     * @throws Exception when the merge fails, or when the merged translations could not be written.
     */
    @SuppressWarnings("unchecked")
    @TaskAction
    public void mergeTranslations() throws Exception {
        final Set<File> sources = getSourceFiles().getFiles();

        JsonSlurper slurper = new JsonSlurper();
        final Map<String, Object> merged = new HashMap<>();
        for (File source : sources) {
            Map<String, Object> sourceJson = (Map<String, Object>) slurper.parse(source);
            merged.putAll(sourceJson);
        }

        final Set<File> targets = getTargetFiles().getFiles();
        for (File target : targets) {
            Files.writeString(target.toPath(), JsonOutput.toJson(merged), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    /**
     * The source files from which the translations should be merged.
     * <p>
     *     The collection is processed in order.
     *     Translation keys which already exist in earlier files, will get overridden by values
     *     of later files.
     *
     * @return The source files which should be merged.
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getSourceFiles();

    /**
     * The files to which the merged results should be written.
     * <p>
     *     This is marked as an input, because multiple distinct outputs can not exist.
     *
     * @return The output files.
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getTargetFiles();
}
