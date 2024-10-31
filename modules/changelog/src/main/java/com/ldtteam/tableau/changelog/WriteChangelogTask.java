package com.ldtteam.tableau.changelog;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class WriteChangelogTask extends DefaultTask {

    public WriteChangelogTask() {
        setGroup("documentation");
        setDescription("Writes a changelog component to the changelog file.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @TaskAction
    public void writeChangelog() throws IOException {
        if (!getComponent().isPresent()) {
            setDidWork(false);
            return;
        }

        final File changelog = getChangelogFile().get().getAsFile();
        if (!changelog.exists()) {
            changelog.getParentFile().mkdirs();
            changelog.createNewFile();
        }

        final String component = getComponent().get();

        //Append the component to the file:
        Files.writeString(changelog.toPath(), component + System.lineSeparator(), java.nio.file.StandardOpenOption.APPEND);
    }

    @Input
    @Optional
    public abstract Property<String> getComponent();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getChangelogFile();
}
