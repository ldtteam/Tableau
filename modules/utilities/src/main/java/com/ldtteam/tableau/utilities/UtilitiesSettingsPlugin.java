package com.ldtteam.tableau.utilities;

import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Settings plugin for the utilities module.
 * <p>
 *     Configures the project with the utility functions.
 */
public class UtilitiesSettingsPlugin implements Plugin<Settings> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public UtilitiesSettingsPlugin() {
    }

    @Override
    public void apply(@NotNull Settings target) {
        target.getExtensions().create(UtilityFunctions.EXTENSION_NAME, UtilityFunctions.class);
    }
}
