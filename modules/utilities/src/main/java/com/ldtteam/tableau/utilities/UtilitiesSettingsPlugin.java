package com.ldtteam.tableau.utilities;

import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

public class UtilitiesSettingsPlugin implements Plugin<Settings> {

    @Override
    public void apply(@NotNull Settings target) {
        target.getExtensions().create(UtilityFunctions.EXTENSION_NAME, UtilityFunctions.class, target);
    }
}
