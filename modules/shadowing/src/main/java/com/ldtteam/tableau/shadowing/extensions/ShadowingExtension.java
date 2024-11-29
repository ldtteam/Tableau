package com.ldtteam.tableau.shadowing.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Represents the shadowing extension.
 */
public abstract class ShadowingExtension {

    /**
     * Gets the shadowing extension for a given project.
     *
     * @param project The project.
     * @return The shadowing extension.
     */
    public static ShadowingExtension get(Project project) {
        return TableauScriptingExtension.get(project, ShadowingExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "shadowing";

    /**
     * Creates a new shadowing extension.
     */
    @Inject
    public ShadowingExtension() {
    }

    /**
     * Gets the renamed namespaces.
     *
     * @return The renamed namespaces.
     */
    public abstract MapProperty<String, String> getRenamedNamespaces();

    /**
     * Gets the property for whether the project uses none transitive shadowing.
     *
     * @return The property for whether the project uses none transitive shadowing.
     */
    public abstract Property<Boolean> getUsesNoneTransitiveShadow();

    /**
     * Gets the property for whether the project should have its implementation configuration extended.
     *
     * @return The property for whether the project should have its implementation configuration extended.
     */
    public abstract Property<Boolean> getExtendImplementation();
}
