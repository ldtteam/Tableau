package com.ldtteam.tableau;

import com.ldtteam.tableau.changelog.ChangelogPlugin;
import com.ldtteam.tableau.common.CommonPlugin;
import com.ldtteam.tableau.crowdin.CrowdinPlugin;
import com.ldtteam.tableau.curseforge.CurseForgePlugin;
import com.ldtteam.tableau.extensions.ModuleFeatures;
import com.ldtteam.tableau.features.FeaturePluginManager;
import com.ldtteam.tableau.git.GitPlugin;
import com.ldtteam.tableau.jarjar.JarJarPlugin;
import com.ldtteam.tableau.java.JavaPlugin;
import com.ldtteam.tableau.jetbrains.annotations.JetbrainsAnnotationsPlugin;
import com.ldtteam.tableau.local.file.configuration.LocalFileConfigurationPlugin;
import com.ldtteam.tableau.maven.publishing.MavenPublishingPlugin;
import com.ldtteam.tableau.neogradle.NeoGradlePlugin;
import com.ldtteam.tableau.parchment.ParchmentPlugin;
import com.ldtteam.tableau.resource.processing.ResourceProcessingPlugin;
import com.ldtteam.tableau.scripting.ScriptingPlugin;
import com.ldtteam.tableau.shadowing.ShadowingPlugin;
import com.ldtteam.tableau.sourceset.management.SourcesetManagementPlugin;
import com.ldtteam.tableau.utilities.UtilitiesPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Core plugin which will apply all modules and features when needed to a {@link Project}
 */
public class TableauProjectPlugin implements Plugin<Project> {

    /**
     * Creates a new plugin instance.
     */
    @Inject
    public TableauProjectPlugin() {
    }

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(ScriptingPlugin.class);
        target.getPlugins().apply(UtilitiesPlugin.class);
        target.getPlugins().apply(LocalFileConfigurationPlugin.class);
        target.getPlugins().apply(CommonPlugin.class);
        target.getPlugins().apply(JavaPlugin.class);
        target.getPlugins().apply(SourcesetManagementPlugin.class);
        target.getPlugins().apply(ResourceProcessingPlugin.class);
        target.getPlugins().apply(NeoGradlePlugin.class);
        target.getPlugins().apply(JetbrainsAnnotationsPlugin.class);
        target.getPlugins().apply(MavenPublishingPlugin.class);
        target.getPlugins().apply(ChangelogPlugin.class);

        FeaturePluginManager.applyFeaturePlugin(target, GitPlugin.class, ModuleFeatures::getUsesGit);
        FeaturePluginManager.applyFeaturePlugin(target, ParchmentPlugin.class, ModuleFeatures::getUsesParchment);
        FeaturePluginManager.applyFeaturePlugin(target, ShadowingPlugin.class, ModuleFeatures::getUsesShadowing);
        FeaturePluginManager.applyFeaturePlugin(target, JarJarPlugin.class, ModuleFeatures::getUsesJarJar);
        FeaturePluginManager.applyFeaturePlugin(target, CurseForgePlugin.class, ModuleFeatures::getUsesCurse);
        FeaturePluginManager.applyFeaturePlugin(target, CrowdinPlugin.class, ModuleFeatures::getUsesCrowdin);

    }
}
