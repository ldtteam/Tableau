package com.ldtteam.tableau;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

public class TableauPlugin implements Plugin<Object> {

    @Override
    public void apply(final @NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(TableauProjectPlugin.class);
        }

        if (target instanceof Settings settings) {
            settings.getPlugins().apply(TableauSettingsPlugin.class);
        }

        throw new IllegalArgumentException("Unsupported target type: " + target.getClass().getName());
    }
}
