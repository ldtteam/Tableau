package com.ldtteam.tableau.neoforge.metadata.toml;

import java.io.File;
import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

/**
 * A try-with-resources aware and automatically loading and saving commented file config that can be used 
 * to trivially process TOML files.
 */
public class AutomaticConfig extends CommentedConfigWrapper<CommentedFileConfig> implements CommentedFileConfig {

    /**
     * Creates a new instance of the config for the given file.
     * <p>
     * This will create the file on save if it is missing.
     * 
     * @param file The file to read and write to.
     * @return The file config.
     */
    public static CommentedFileConfig create(File file) {
        final CommentedFileConfig config = CommentedFileConfig.builder(file)
            .onFileNotFound(FileNotFoundAction.CREATE_EMPTY)
            .sync()
            .build();

        return new AutomaticConfig(config);
    }

    private final CommentedFileConfig fileConfig;

    private AutomaticConfig(CommentedFileConfig config) {
		super(config);
		this.fileConfig = config;

        //Immediatly load the existing config.
        config.load();
	}

	@Override
	public File getFile() {
		return fileConfig.getFile();
	}

	@Override
	public Path getNioPath() {
		return fileConfig.getNioPath();
	}

	@Override
	public void save() {
		fileConfig.save();
	}

	@Override
	public void load() {
		fileConfig.load();
	}

	@Override
	public void close() {
        //First save then close.
        fileConfig.save();
		fileConfig.close();
	}
}
