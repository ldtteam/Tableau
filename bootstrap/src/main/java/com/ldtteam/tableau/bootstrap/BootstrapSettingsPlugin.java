/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.bootstrap;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.plugin.use.PluginDependencySpec;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class BootstrapSettingsPlugin implements Plugin<Settings> {

    @Override
    public void apply(Settings target) {
        final String version = this.getClass().getPackage().getImplementationVersion();

        LoggerFactory.getLogger(BootstrapSettingsPlugin.class).warn("Setting up Bootstrap settings for {}", version);

        target.getPluginManagement().getRepositories().mavenLocal();
        target.getPluginManagement().getRepositories().maven(repo -> {
            repo.setUrl("https://ldtteam.jfrog.io/artifactory/tableau/");
            repo.setName("Tableau");
        });

        target.getPluginManagement().plugins(plugins -> {
            plugins.id("com.ldtteam.tableau").version(version);
        });
    }
}
