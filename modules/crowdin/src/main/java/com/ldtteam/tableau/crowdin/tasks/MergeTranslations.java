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

@CacheableTask
public abstract class MergeTranslations extends DefaultTask {

    public MergeTranslations() {
        setGroup("Crowdin");
        setDescription("Merges the source translations into one translation set, and then writes that set to all targets.");
    }

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

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getSourceFiles();

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getTargetFiles();
}
