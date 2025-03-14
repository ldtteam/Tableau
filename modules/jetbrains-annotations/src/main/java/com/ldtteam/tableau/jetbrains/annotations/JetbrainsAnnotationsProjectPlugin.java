/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.jetbrains.annotations;

import com.ldtteam.tableau.jetbrains.annotations.extensions.JetbrainsAnnotationsExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * The project plugin for the Jetbrains Annotations module.
 */
public class JetbrainsAnnotationsProjectPlugin implements Plugin<Project> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public JetbrainsAnnotationsProjectPlugin() {
    }

    @Override
    public void apply(@NotNull Project target) {
        TableauScriptingExtension.register(target, JetbrainsAnnotationsExtension.EXTENSION_NAME, JetbrainsAnnotationsExtension.class, target);
    }
}
