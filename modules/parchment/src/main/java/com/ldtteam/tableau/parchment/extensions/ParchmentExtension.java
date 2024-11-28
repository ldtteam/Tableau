package com.ldtteam.tableau.parchment.extensions;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import net.neoforged.gradle.dsl.common.extensions.subsystems.Subsystems;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Parchment configuration model extension
 */
public abstract class ParchmentExtension {

    /**
     * Gets the Parchment extension for the given project.
     *
     * @param project the project to get the extension from
     * @return the Parchment extension
     */
    public static ParchmentExtension get(Project project) {
        return TableauScriptingExtension.get(project, ParchmentExtension.class);
    }

    /**
     * The name of the Parchment extension.
     */
    public static final String EXTENSION_NAME = "parchment";

    /**
     * Creates a new instance of the Parchment extension.
     *
     * @param project the project to create the extension for
     */
    @Inject
    public ParchmentExtension(Project project) {

        final ModExtension modExtension = ModExtension.get(project);
        getMinecraftVersion().convention(modExtension.getMinecraftVersion());

        getParchmentVersion().convention("BLEEDING-SNAPSHOT");

        //Configure neogradle.
        final Subsystems neogradleSubsystems = project.getExtensions().getByType(Subsystems.class);
        neogradleSubsystems.getParchment().getMinecraftVersion().convention(getMinecraftVersion());
        neogradleSubsystems.getParchment().getMappingsVersion().convention(getParchmentVersion());
    }

    /**
     * Gets the minecraft version to get the parchment artifacts for.
     * <p>
     *     Parchment supports cross version compatibility, so this version is used to determine the
     *     version of the artifacts to use. If parchment has not released a new version yet for the
     *     minecraft version you configured in {@link com.ldtteam.tableau.common.extensions.ModExtension}
     *     you can configure a different version here.
     * <p>
     *     This value is defaulted to the minecraft version configured in the {@link com.ldtteam.tableau.common.extensions.ModExtension}.
     *
     * @return the minecraft version to get the parchment artifacts for
     */
    public abstract Property<String> getMinecraftVersion();

    /**
     * Gets the version of the parchment artifacts to use.
     * <p>
     *     The default version is configured to be the latest version of the parchment artifacts
     *
     * @return the version of the parchment artifacts to use.
     */
    public abstract Property<String> getParchmentVersion();
}
