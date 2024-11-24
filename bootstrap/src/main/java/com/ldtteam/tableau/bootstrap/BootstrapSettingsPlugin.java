/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.bootstrap;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Defines the core settings plugin for bootstrapping Tableau.
 * <p>
 *     This plugin is published to the Gradle Plugin repository.
 * <p>
 *     Its main job is to configure the plugin repositories in such a way that it
 *     then can load the relevant tableau modules from our maven.
 * <p>
 *     It will pull the version to load from its own published manifest.
 */
public class BootstrapSettingsPlugin implements Plugin<Settings> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public BootstrapSettingsPlugin() {
    }

    @Override
    public void apply(Settings target) {
        //Get the version to use.
        final String version = this.getClass().getPackage().getImplementationVersion();

        //Configure the repositories section.
        target.getPluginManagement().getRepositories().mavenLocal();
        target.getPluginManagement().getRepositories().maven(repo -> {
            repo.setUrl("https://ldtteam.jfrog.io/artifactory/tableau/");
            repo.setName("Tableau");
        });

        //Load the plugin, this mimics using the plugins block.
        target.getPluginManagement().plugins(plugins -> {
            plugins.id("com.ldtteam.tableau").version(version);
        });
    }
}