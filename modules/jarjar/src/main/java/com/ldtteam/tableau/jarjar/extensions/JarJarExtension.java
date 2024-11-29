package com.ldtteam.tableau.jarjar.extensions;

import com.ldtteam.tableau.extensions.NeoGradleExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Extension that configures the JarJar plugin.
 */
public abstract class JarJarExtension {

    /**
     * Gets the JarJar extension for the given project.
     *
     * @param project the project to get the extension from
     * @return the JarJar extension
     */
    public static JarJarExtension get(final Project project) {
        return NeoGradleExtension.get(project).getExtensions().getByType(JarJarExtension.class);
    }

    /**
     * The name of the JarJar extension.
     */
    public static final String EXTENSION_NAME = "jarJar";

    /**
     * Creates a new extension for the jar-jar model.
     */
    @Inject
    public JarJarExtension() {
        getUsesNoneTransitiveJarJar().convention(true);
        getExtendImplementation().convention(true);
    }

    /**
     * Gets the property for whether the project uses none transitive jarjar.
     *
     * @return The property for whether the project uses none transitive jarjar.
     */
    public abstract Property<Boolean> getUsesNoneTransitiveJarJar();

    /**
     * Gets the property for whether the project should have its implementation configuration extended.
     *
     * @return The property for whether the project should have its implementation configuration extended.
     */
    public abstract Property<Boolean> getExtendImplementation();
}
