package com.ldtteam.tableau.neoforge.metadata.api;

import com.electronwill.nightconfig.core.CommentedConfig;

/**
 * Defines a component that is part of the metadata.
 */
public interface IMetadataComponent {

    /**
     * Writes the current component into the given config.
     * 
     * @param config The config to write into.
     */
    public void write(CommentedConfig config);
}
