/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.crowdin;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

public class CrowdinPlugin implements Plugin<Object> {

    @Override
    public void apply(@NotNull Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(CrowdinProjectPlugin.class);
        }
    }
}
