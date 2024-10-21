package com.ldtteam.tableau.utilities.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.*;

/**
 * A collection of utility functions exposed to the build script through the "opc" extension.
 */
public abstract class UtilityFunctions implements ExtensionAware {

    private final Project project;

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "opc";

    /**
     * Gets the utility functions extension for a given project.
     *
     * @param project The project.
     * @return The utility functions extension.
     */
    public static UtilityFunctions get(Project project) {
        return (UtilityFunctions) ((ExtensionAware) project).getExtensions().getByName(EXTENSION_NAME);
    }

    @Inject
    public UtilityFunctions(Project project) {
        this.project = project;
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
     * Gets the environment property name for a given property name.
     * The property name is split by capital letters and joined with underscores.
     * The result is then converted to uppercase.
     * <p>
     * Example: "myPropertyName" -> "MY_PROPERTY_NAME"
     *
     * @param propertyName The property name.
     * @return The environment property name.
     */
    public String getEnvironmentPropertyName(final String propertyName) {
        final String[] parts = propertyName.split("(?=\\p{Upper})");
        return String.join("_", parts).toUpperCase();
    }

    /**
     * Gets a property from the project properties or environment variables.
     *
     * @param propertyName The name of the property.
     * @return The property value.
     */
    public Provider<String> getProperty(final String propertyName) {
        return project.getProviders().gradleProperty(propertyName)
                .orElse(project.getProviders().environmentVariable(getEnvironmentPropertyName(propertyName)));
    }

    /**
     * Gets all properties from the project properties or environment variables, prefixed by a given property name.
     *
     * @param propertyName The prefix of the properties.
     * @return The properties and their values.
     */
    public Provider<Map<String, String>> getPropertiesPrefixedBy(final String propertyName) {
        return project.getProviders().gradlePropertiesPrefixedBy(propertyName)
                .zip(project.getProviders().environmentVariablesPrefixedBy(getEnvironmentPropertyName(propertyName)), (gradle, env) -> {
                    final Map<String, String> result = new HashMap<>(gradle);
                    result.putAll(env);

                    return result;
                });
    }

    /**
     * Gets a property from the project properties or environment variables and converts it to an integer.
     *
     * @param propertyName The name of the property.
     * @return The property value as an integer.
     */
    public Provider<Integer> getIntegerProperty(final String propertyName) {
        return getProperty(propertyName).map(Integer::parseInt);
    }

    /**
     * Gets a list of strings from a property that is a semicolon-separated list.
     * If the property is not found, it will look for properties with the same prefix and an index.
     *
     * @param propertyName The name of the property.
     * @return The property value as a boolean.
     */
    public Provider<List<String>> getStringListProperty(final String propertyName) {
        return getProperty(propertyName)
                .map(value -> List.of(value.split(";")))
                .map(values -> {
                    values.removeIf(Objects::isNull);
                    values.removeIf(String::isBlank);
                    return values;
                }).orElse(
                        getPropertiesPrefixedBy(propertyName).map(properties -> {
                            final int max = properties.size();

                            if (max == 0) {
                                return List.<String>of();
                            }

                            final List<String> values = new ArrayList<>(max);
                            for (int i = 0; i < max; i++) {
                                final String property = properties.get("%s[%d]".formatted(propertyName, i));
                                if (properties.containsKey(property)) {
                                    values.add(property);
                                }
                            }
                            return values;
                        }).map(values -> {
                            values.removeIf(Objects::isNull);
                            values.removeIf(String::isBlank);
                            return values;
                        })
                );
    }

    /**
     * Gets the first string from a property that is a semicolon-separated list.
     * If the property is not found, it will look for properties with the same prefix and an index, and then return the first string.
     *
     * @param propertyName The name of the property.
     * @return The first string from the property value.
     */
    public Provider<String> getFirstStringListProperty(final String propertyName) {
        return getStringListProperty(propertyName).map(strings -> strings.isEmpty() ? "" : strings.getFirst());
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
