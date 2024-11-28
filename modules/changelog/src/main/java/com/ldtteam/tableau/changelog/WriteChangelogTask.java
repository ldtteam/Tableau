package com.ldtteam.tableau.changelog;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Writes a given piece of text to a given changelog.
 * <p>
 *     The task will create the file and its parent directories if they don't exist.
 *     The exact text written is the given text component with a line separator.
 *     The text is appended to the file if it already exists.
 * <p>
 *     This task is not cacheable, as it is supposed to run against a different file
 *     every invocation.
 */
@DisableCachingByDefault(
        because = "Not work caching, also not really cacheable since this write to a different file every invocation."
)
public abstract class WriteChangelogTask extends DefaultTask {

    /**
     * Creates a new task instance.
     */
    public WriteChangelogTask() {
        setGroup("documentation");
        setDescription("Writes a changelog component to the changelog file.");
    }

    /**
     * Executes the task and write the given component to the file, creating it in the process if need be.
     *
     * @throws IOException when a failure occurs during the attempt to write the text to the file.
     */
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

    /**
     * The text component that should be written to the changelog.
     *
     * @return The text component to write.
     */
    @Input
    @Optional
    public abstract Property<String> getComponent();

    /**
     * The changelog file that the component should be written to.
     *
     * @return The changelog file to write to.
     */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getChangelogFile();
}
