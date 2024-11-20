package com.ldtteam.tableau.neogradle.model;

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
    /**
     * The version range of the dependency.
     */
    private final String versionRange;

    /**
     * The jar file.
     */
    private final File file;

    /**
     * Default constructor.
     *
     * @param versionRange The version range of the dependency.
     * @param file         The jar file.
     */
    public ResolvedDependency(final String versionRange, final File file)
    {
        this.versionRange = versionRange;
        this.file = file;
    }

    /**
     * Get the version range of the dependency.
     *
     * @return the maven version range.
     */
    @Input
    public String getVersionRange()
    {
        return versionRange;
    }

    /**
     * Get the jar file.
     *
     * @return the file in classpath where the jar is located.
     */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public File getFile()
    {
        return file;
    }
}
