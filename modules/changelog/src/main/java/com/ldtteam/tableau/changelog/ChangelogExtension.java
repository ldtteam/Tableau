package com.ldtteam.tableau.changelog;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

/**
 * The changelog extension, configures properties related to how the changelog is generated.
 */
public abstract class ChangelogExtension {

    /**
     * Gets the changelog extension for the given project.
     *
     * @param project The project to get the extension for.
     * @return The changelog extension.
     */
    public static ChangelogExtension get(final Project project) {
        return TableauScriptingExtension.get(project, ChangelogExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "changelog";

    /**
     * The header to use for the changelog.
     */
    public abstract Property<String> getHeader();

    /**
     * The footer to use for the changelog.
     */
    public abstract Property<String> getFooter();
}
