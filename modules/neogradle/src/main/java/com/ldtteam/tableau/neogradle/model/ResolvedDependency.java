package com.ldtteam.tableau.neogradle.model;

import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.io.File;

/**
 * Dependency information container, contains the dependency identifier and the original jar file.
 */
@SuppressWarnings("ClassCanBeRecord") // Because of Gradle configuration cache this file cannot be converted to a record due to missing getters.
public class ResolvedDependency
{
    private final String id;

    private final File file;

    /**
     * Default constructor.
     *
     * @param id   The dependency identifier.
     * @param file The jar file.
     */
    public ResolvedDependency(final String id, final File file)
    {
        this.id = id;
        this.file = file;
    }

    @Input
    public String getId()
    {
        return id;
    }

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public File getFile()
    {
        return file;
    }
}
