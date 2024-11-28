package com.ldtteam.tableau;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * The core tableau plugin
 * <p>
 *     This can be applied to any {@link org.gradle.api.plugins.PluginAware} however it will only
 *     operate on {@link Project projects} and {@link Settings settings} types.
 */
public class TableauPlugin implements Plugin<Object> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public TableauPlugin() {
    }

    @Override
    public void apply(final @NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(TableauProjectPlugin.class);
        } else if (target instanceof Settings settings) {
            settings.getPlugins().apply(TableauSettingsPlugin.class);
        }
    }
}
