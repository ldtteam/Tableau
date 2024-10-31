package com.ldtteam.tableau.utilities.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.*;

/**
 * A collection of utility functions exposed to the build script through the "opc" extension.
 */
public abstract class UtilityFunctions implements ExtensionAware {

    private final ProviderFactory providerFactory;

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "utilities";

    /**
     * Gets the utility functions extension for a given extensible object.
     *
     * @param extensionAware The extension aware object.
     * @return The utility functions extension.
     */
    public static UtilityFunctions get(ExtensionAware extensionAware) {
        if (extensionAware instanceof Project project) {
            return TableauScriptingExtension.get(project, UtilityFunctions.class);
        }
        return (UtilityFunctions) extensionAware.getExtensions().getByName(EXTENSION_NAME);
    }

    @Inject
    public UtilityFunctions(Object object) {
        switch (object) {
            case ProviderFactory factory -> this.providerFactory = factory;
            case Project project -> this.providerFactory = project.getProviders();
            case Settings settings -> this.providerFactory = settings.getProviders();
            case null, default ->
                    throw new IllegalArgumentException("The object must be a ProviderFactory, a Project or a Settings.");
        }
    }

    /**
     * Splits a version number into its parts.
     *
     * @param version The version number to split.
     * @return The parts of the version number.
     */
    public String[] splitVersionNumber(final String version) {
        return version.split("\\.");
    }

    /**
     * Builds a version number with an offset based on the current version number and a relative version number.
     *
     * @param sourceVersionNumber        The version number to use as a source.
     * @param currentVersionNumber       The current version number.
     * @param relativeVersionNumber      The relative version number.
     * @param projectVersionElementIndex The index of the version element to update.
     * @param sourceVersionElementIndex  The index of the version element to use as a source.
     * @return The version number with the offset applied.
     */
    public String buildVersionNumberWithOffset(
            final String sourceVersionNumber,
            final String currentVersionNumber,
            final String relativeVersionNumber,
            int projectVersionElementIndex,
            int sourceVersionElementIndex
    ) {
        final String[] sourceVersionParts = splitVersionNumber(sourceVersionNumber);
        final String[] currentVersionParts = splitVersionNumber(currentVersionNumber);
        final String[] relativeVersionParts = splitVersionNumber(relativeVersionNumber);

        int currentVersion = Integer.parseInt(currentVersionParts[sourceVersionElementIndex]);
        int relativeVersion = Integer.parseInt(relativeVersionParts[sourceVersionElementIndex]);
        int offset = currentVersion - relativeVersion;

        if (offset < 0) {
            throw new IllegalArgumentException("The current version is lower than the relative version.");
        }

        final StringBuilder versionBuilder = new StringBuilder();
        for (int i = 0; i < sourceVersionParts.length; i++) {
            if (i != projectVersionElementIndex) {
                versionBuilder.append(sourceVersionParts[i]);
            } else {
                versionBuilder.append(Integer.parseInt(sourceVersionParts[i]) + offset);
            }

            if (i < sourceVersionParts.length - 1) {
                versionBuilder.append(".");
            }
        }

        return versionBuilder.toString();
    }

    /**
     * Determines the next major version based on the current version.
     *
     * @param currentVersion The current version.
     * @return The next major version.
     */
    public String determineNextMajorVersion(final String currentVersion) {
        final String[] parts = splitVersionNumber(currentVersion);
        final int major = Integer.parseInt(parts[0]);
        return "%d.0.0".formatted(major + 1);
    }

    /**
     * Builds a supported version range based on the current version.
     *
     * @param version The current version.
     * @return The supported version range.
     */
    public String buildSupportedVersionRange(final String version) {
        final String nextMajorVersion = determineNextMajorVersion(version);
        return "[%s, %s)".formatted(version, nextMajorVersion);
    }
}
