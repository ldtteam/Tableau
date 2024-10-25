package com.ldtteam.tableau;

import com.ldtteam.tableau.common.CommonPlugin;
import com.ldtteam.tableau.crowdin.CrowdinPlugin;
import com.ldtteam.tableau.crowdin.translation.management.CrowdinTranslationManagementPlugin;
import com.ldtteam.tableau.curseforge.CurseForgePlugin;
import com.ldtteam.tableau.extensions.ModuleFeatures;
import com.ldtteam.tableau.features.FeaturePluginManager;
import com.ldtteam.tableau.git.GitPlugin;
import com.ldtteam.tableau.jarjar.JarjarPlugin;
import com.ldtteam.tableau.local.file.configuration.LocalFileConfigurationPlugin;
import com.ldtteam.tableau.neogradle.NeoGradlePlugin;
import com.ldtteam.tableau.parchment.ParchmentPlugin;
import com.ldtteam.tableau.shadowing.ShadowingPlugin;
import com.ldtteam.tableau.sourceset.management.SourcesetManagementPlugin;
import com.ldtteam.tableau.utilities.UtilitiesPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;
import org.sonarqube.gradle.SonarQubePlugin;

public class TableauProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(UtilitiesPlugin.class);
        target.getPlugins().apply(LocalFileConfigurationPlugin.class);

        FeaturePluginManager.applyFeaturePlugin(target, CrowdinPlugin.class, ModuleFeatures::usesCrowdin);
        FeaturePluginManager.applyFeaturePlugin(target, CrowdinTranslationManagementPlugin.class, ModuleFeatures::usesCrowdInTranslationManagement);
        FeaturePluginManager.applyFeaturePlugin(target, SonarQubePlugin.class, ModuleFeatures::usesSonarQube);

        target.getPlugins().apply(CommonPlugin.class);

        FeaturePluginManager.applyFeaturePlugin(target, ShadowingPlugin.class, ModuleFeatures::usesShadowing);
        FeaturePluginManager.applyFeaturePlugin(target, JarjarPlugin.class, ModuleFeatures::usesJarJar);
        FeaturePluginManager.applyFeaturePlugin(target, CurseForgePlugin.class, ModuleFeatures::usesCurse);

        target.getPlugins().apply(NeoGradlePlugin.class);

        FeaturePluginManager.applyFeaturePlugin(target, ParchmentPlugin.class, ModuleFeatures::usesParchment);
        FeaturePluginManager.applyFeaturePlugin(target, GitPlugin.class, ModuleFeatures::usesGit);

        target.getPlugins().apply(SourcesetManagementPlugin.class);
    }
}
