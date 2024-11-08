/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.changelog;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

public class ChangelogProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        TableauScriptingExtension.register(target, ChangelogExtension.EXTENSION_NAME, ChangelogExtension.class);

        target.getTasks().register("outputChangelogHeader", WriteChangelogTask.class, task -> {
            task.getComponent().set(ChangelogExtension.get(target).getHeader().map(header -> header + System.lineSeparator() + System.lineSeparator()));
            task.getChangelogFile().convention(target.getLayout().getBuildDirectory().file("changelog.md"));

            task.onlyIf(t -> ChangelogExtension.get(target).getHeader().isPresent());
        });

        target.getTasks().register("outputChangelogFooter", WriteChangelogTask.class, task -> {
            task.getComponent().set(ChangelogExtension.get(target).getFooter());
            task.getChangelogFile().convention(target.getLayout().getBuildDirectory().file("changelog.md"));

            task.onlyIf(t -> ChangelogExtension.get(target).getFooter().isPresent());
        });
    }
}