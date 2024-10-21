package com.ldtteam.tableau;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TableauPlugin implements Plugin<Object> {
    @Override
    public void apply(final Object target) {
        if (target instanceof Project project) {
            project.getPlugins().apply(TableauProjectPlugin.class);
        }

        throw new IllegalArgumentException("Unsupported target type: " + target.getClass().getName());
    }
}
