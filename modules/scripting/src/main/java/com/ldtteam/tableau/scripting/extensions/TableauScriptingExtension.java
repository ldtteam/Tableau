package com.ldtteam.tableau.scripting.extensions;

import com.ldtteam.tableau.scripting.ScriptingPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

/**
 * Core class that defines the root of the tableau scripting DSL.
 * This class does not provide any functionality, other than being the root of the DSL.
 * <p>
 *     Statically this class provides an API to register an retrieve DSL extensions for a given project.
 * </p>
 *
 */
public abstract class TableauScriptingExtension implements ExtensionAware {

    /**
     * Gets the TableauScripting extension for the given project.
     *
     * @param project the project to get the extension from
     * @return the TableauScripting extension
     */
    public static TableauScriptingExtension get(final Project project) {
        return project.getExtensions().getByType(TableauScriptingExtension.class);
    }

    /**
     * Gets the DSL component with the given type for the given project.
     *
     * @param project the project to get the extension from
     * @param type the type of the extension
     * @return the extension
     * @param <T> the type of the extension
     */
    public static <T> T get(final Project project, final Class<T> type) {
        return get(project).getExtensions().getByType(type);
    }

    /**
     * Registers a new DSL component with the given name and type for the given project.
     *
     * @param project the project to register the extension to
     * @param name the name of the extension
     * @param type the type of the extension
     * @param args the arguments to pass to the extension
     * @param <T> the type of the extension
     */
    public static <T> T register(final Project project, final String name, final Class<T> type, final Object... args) {
        if (!project.getPlugins().hasPlugin(ScriptingPlugin.class))
            project.getPlugins().apply(ScriptingPlugin.class);

        return get(project).getExtensions().create(name, type, args);
    }

    /**
     * The name of the TableauScripting extension.
     */
    public static final String EXTENSION_NAME = "tableau";
}
