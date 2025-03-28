package com.ldtteam.tableau.neoforge.metadata.components;

import javax.inject.Inject;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.neoforge.metadata.api.IMetadataComponent;

/**
 * The file header component that is automatically added to all metadata toml files generated by this module.
 */
public abstract class HeaderComponent implements IMetadataComponent {

    /**
     * The name of the component.
     */
    public static final String NAME = "header";

    private static final String HEADER = """
            NeoForge Mod Metadata file.
            This file is generated by Tableau.
            Any changes you make to it in a Tableau workspace will be overriden.
            """;

    /**
     * Creates a new component instance.
     */
    @Inject
    public HeaderComponent() {
    }

    @Override
    public void write(CommentedConfig config) {
        config.setComment("", HEADER);
    }
}
