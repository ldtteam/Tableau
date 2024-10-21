package com.ldtteam.tableau;

import com.ldtteam.tableau.local.file.configuration.LocalFileConfigurationPlugin;
import com.ldtteam.tableau.utilities.UtilitiesPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class TableauProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(UtilitiesPlugin.class);
        target.getPlugins().apply(LocalFileConfigurationPlugin.class);
    }
}
