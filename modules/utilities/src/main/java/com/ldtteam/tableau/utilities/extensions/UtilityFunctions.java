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
        return providerFactory.gradleProperty(propertyName)
                .orElse(providerFactory.environmentVariable(getEnvironmentPropertyName(propertyName)));
    }

    /**
     * Gets a boolean property from the project properties or environment variables.
     *
     * @param propertyName The name of the property.
     * @return The property value.
     */
    public Provider<Boolean> getBooleanProperty(final String propertyName) {
        return getProperty(propertyName).map(Boolean::parseBoolean);
    }

    /**
     * Gets all properties from the project properties or environment variables, prefixed by a given property name.
     *
     * @param propertyName The prefix of the properties.
     * @return The properties and their values.
     */
    @SuppressWarnings("UnstableApiUsage")
    public Provider<Map<String, String>> getPropertiesPrefixedBy(final String propertyName) {
        return providerFactory.gradlePropertiesPrefixedBy(propertyName)
                .zip(providerFactory.environmentVariablesPrefixedBy(getEnvironmentPropertyName(propertyName)), (gradle, env) -> {
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
                ).orElse(List.of());
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

    /**
     * Gets a provider which indicates whether a feature with the given name is active.
     *
     * @param name The name of the feature.
     * @return The provider which indicates whether the feature is active.
     */
    public Provider<Boolean> getUsesProperty(final String name) {
        final String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
        return getBooleanProperty("uses%s".formatted(capitalized));
    }

    /**
     * Determines whether a property with the given name is present.
     *
     * @param property The name of the property.
     * @return Whether the property is present.
     */
    public boolean hasProperty(String property) {
        return providerFactory.gradleProperty(property).isPresent();
    }
}
