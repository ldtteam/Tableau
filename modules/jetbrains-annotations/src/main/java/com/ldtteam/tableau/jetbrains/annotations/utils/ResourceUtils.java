package com.ldtteam.tableau.jetbrains.annotations.utils;

import com.ldtteam.tableau.jetbrains.annotations.JetbrainsAnnotationsPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Utility class for resources.
 */
public class ResourceUtils {

    /**
     * Private constructor to hide the implicit public one.
     */
    private ResourceUtils() {
    }

    /**
     * Gets the JetBrains annotations version.
     *
     * @return The JetBrains annotations version.
     */
    public static @NotNull String getJetbrainsAnnotationsVersion() {
        final String neogradleVersion;
        try(final InputStream stream = Objects.requireNonNull(JetbrainsAnnotationsPlugin.class.getClassLoader().getResource("annotations-version.tableau")).openStream()) {
            neogradleVersion = new String(stream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read NeoGradle version", e);
        }
        return neogradleVersion;
    }
}
