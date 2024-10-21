/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.utilities;

import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.NotNull;

public class UtilitiesProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getExtensions().create(UtilityFunctions.EXTENSION_NAME, UtilityFunctions.class, target);
    }
}
