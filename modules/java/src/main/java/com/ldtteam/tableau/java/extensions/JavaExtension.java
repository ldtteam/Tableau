package com.ldtteam.tableau.java.extensions;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

import javax.inject.Inject;

/**
 * Configures the Java plugin.
 */
public abstract class JavaExtension {

    /**
     * Gets the Java extension for the given project.
     *
     * @param project the project to get the extension from
     * @return the Java extension
     */
    public static JavaExtension get(final Project project) {
        return TableauScriptingExtension.get(project, JavaExtension.class);
    }

    /**
     * The name of the Java extension.
     */
    public static final String EXTENSION_NAME = "java";

    @Inject
    public JavaExtension(final Project project) {
        final JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
        java.getToolchain().getLanguageVersion().set(getJavaVersion().map(JavaLanguageVersion::of));

        getAutomaticModuleName().convention(ModExtension.get(project).getModId());
    }

    /**
     * @return the Java version
     */
    public abstract Property<Integer> getJavaVersion();

    /**
     * @return the automatic module name
     */
    public abstract Property<String> getAutomaticModuleName();
}
