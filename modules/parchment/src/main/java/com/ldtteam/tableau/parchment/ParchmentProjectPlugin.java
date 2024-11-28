/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.parchment;

import com.ldtteam.tableau.common.CommonPlugin;
import com.ldtteam.tableau.neogradle.NeoGradlePlugin;
import com.ldtteam.tableau.parchment.extensions.ParchmentExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * The project plugin for the Parchment module.
 * <p>
 *     Configures the project to use parchment for parameter naming and javadoc insertion
 */
public class ParchmentProjectPlugin implements Plugin<Project> {

    /**
     * Creates a new instance of the Parchment project plugin.
     */
    @Inject
    public ParchmentProjectPlugin() {
    }

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(CommonPlugin.class);
        target.getPlugins().apply(NeoGradlePlugin.class);

        TableauScriptingExtension.register(target, ParchmentExtension.EXTENSION_NAME, ParchmentExtension.class, target);
    }
}