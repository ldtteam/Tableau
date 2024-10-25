package com.ldtteam.tableau;

import com.ldtteam.tableau.extensions.ModuleFeatures;
import com.ldtteam.tableau.utilities.UtilitiesPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

public class TableauSettingsPlugin implements Plugin<Settings> {

    @Override
    public void apply(@NotNull Settings target) {
        target.getPlugins().apply(UtilitiesPlugin.class);

        ModuleFeatures moduleFeatures = target.getGradle().getExtensions().create(ModuleFeatures.EXTENSION_NAME, ModuleFeatures.class);
        target.getExtensions().add(ModuleFeatures.EXTENSION_NAME, moduleFeatures);
    }
}
