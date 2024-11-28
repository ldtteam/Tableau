/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.changelog;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Core project plugin for the changelog module.
 * <p>
 *     Ensures that the changelog processing tasks are properly registered.
 *     The output tasks will only run if there is a footer or header registered.
 */
public class ChangelogProjectPlugin implements Plugin<Project> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public ChangelogProjectPlugin() {
    }

    @Override
    public void apply(@NotNull Project target) {
        TableauScriptingExtension.register(target, ChangelogExtension.EXTENSION_NAME, ChangelogExtension.class);

        //Write the header
        target.getTasks().register("outputChangelogHeader", WriteChangelogTask.class, task -> {
            task.getComponent().set(ChangelogExtension.get(target).getHeader().map(header -> header + System.lineSeparator() + System.lineSeparator()));
            task.getChangelogFile().convention(target.getLayout().getBuildDirectory().file("changelog.md"));

            task.onlyIf(t -> ChangelogExtension.get(target).getHeader().isPresent());
        });

        //Write the footer
        target.getTasks().register("outputChangelogFooter", WriteChangelogTask.class, task -> {
            task.getComponent().set(ChangelogExtension.get(target).getFooter());
            task.getChangelogFile().convention(target.getLayout().getBuildDirectory().file("changelog.md"));

            task.onlyIf(t -> ChangelogExtension.get(target).getFooter().isPresent());
        });
    }
}
