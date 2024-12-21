package com.ldtteam.tableau.neoforge.metadata.components;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.neoforge.metadata.api.IMetadataComponent;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

/**
 * Metadata component that configures the license aspect of the mod jar in its metadata.
 */
public abstract class LicenseComponent implements IMetadataComponent {

    /**
     * The name of the license component in the metadata.
     */
    public static final String NAME = "license";

    /**
     * Creates a new metadata component.
     * 
     * @param project The project his belongs to.
     * @param sourceSet The sourceset that this belongs to.
     */
    @Inject
    public LicenseComponent(Project project, SourceSetConfiguration sourceSet) {
        //We default to MIT
        //TODO: Research LICENSE extraction from GitHub / Git
        getName().convention("MIT");
    }

    /**
     * The name license of the project that should be used.
     * 
     * @return The license
     */
    @Input
    @Optional
    public abstract Property<String> getName();

    @Override
    public void write(CommentedConfig config) {
        config.set(NAME, getName().get());
    }
}
