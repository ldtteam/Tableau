package com.ldtteam.tableau.jetbrains.annotations.extensions;

import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import org.gradle.api.provider.Property;

/**
 * Extension class that manages the JetBrains annotations for a given source set.
 */
public abstract class JetbrainsAnnotationsSourceSetExtension {

    /**
     * Gets the JetBrains annotations extension from the project.
     *
     * @param configuration The source set configuration to get the extension from.
     * @return The JetBrains annotations extension.
     */
    public static JetbrainsAnnotationsSourceSetExtension get(final SourceSetExtension.SourceSetConfiguration configuration) {
        return configuration.getExtensions().getByType(JetbrainsAnnotationsSourceSetExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "jetbrains";

    public JetbrainsAnnotationsSourceSetExtension() {
        getInjectAnnotations().convention(true);
    }

    /**
     * @return Indicates whether the JetBrains annotations are enabled for the source set.
     */
    public abstract Property<Boolean> getInjectAnnotations();
}
