/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.curseforge;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.curseforge.extensions.CurseForgeExtension;
import com.ldtteam.tableau.extensions.NeoGradleExtension;
import com.ldtteam.tableau.jarjar.JarJarPlugin;
import com.ldtteam.tableau.neogradle.NeoGradlePlugin;
import com.ldtteam.tableau.shadowing.ShadowingPlugin;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension;
import net.darkhax.curseforgegradle.Constants;
import net.darkhax.curseforgegradle.TaskPublishCurseForge;
import net.darkhax.curseforgegradle.UploadArtifact;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.internal.project.IProjectFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class CurseForgeProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        target.getPlugins().apply(NeoGradlePlugin.class);
    }

    private TaskProvider<? extends Jar> getMainJar(Project project) {
        if (project.getPlugins().hasPlugin(JarJarPlugin.class)) {
            return project.getTasks().named("jarJar", Jar.class);
        }

        if (project.getPlugins().hasPlugin(ShadowingPlugin.class)) {
            return project.getTasks().named("shadowJar", Jar.class);
        }

        return project.getTasks().named("jar", Jar.class);
    }

    private boolean mainJarIsSlim(Project project) {
        return project.getPlugins().hasPlugin(ShadowingPlugin.class) || project.getPlugins().hasPlugin(JarJarPlugin.class);
    }

    private void configureUploadTask(final Project project) {
        final TaskProvider<? extends Jar> mainJar = getMainJar(project);
        final CurseForgeExtension curse = CurseForgeExtension.get(project);
        final SourceSetExtension sourceSets = SourceSetExtension.get(project);
        final ModExtension mod = ModExtension.get(project);

        final TaskProvider<TaskPublishCurseForge> curseforge = project.getTasks().register("curseforge", TaskPublishCurseForge.class, task -> {
            task.apiToken = project.getProviders().environmentVariable("CURSE_API_KEY");

            final UploadArtifact artifact = task.upload(curse.getId(), mainJar);
            task.dependsOn(mainJar);

            curse.getAdditionalMinecraftVersions().get()
                    .forEach(artifact::addGameVersion);

            artifact.releaseType = curse.getReleaseType();

            artifact.addModLoader("NeoForge");
            artifact.addGameVersion(mod.getMinecraftVersion());

            if (curse.getUsesFancyDisplayName().get()) {
                final String displayName = curse.getArtifactName().isPresent() ?
                        curse.getArtifactName().get() :
                        project.getRootProject().getName();

                artifact.displayName = "%s - %s - %s".formatted(displayName,
                        project.getVersion(),
                        curse.getReleaseType().get().toString().toLowerCase(Locale.ROOT));
            }

            artifact.changelog = project.getRootProject().file("changelog.md");
            artifact.changelogType = "markdown";

            sourceSets.getPublishedSourceSets().get().forEach(sourceSet -> {
                artifact.withAdditionalFile(project.getTasks().named(sourceSet.getJavadocJarTaskName()));
                task.dependsOn(project.getTasks().named(sourceSet.getJavadocJarTaskName()));

                artifact.withAdditionalFile(project.getTasks().named(sourceSet.getSourcesJarTaskName()));
                task.dependsOn(project.getTasks().named(sourceSet.getSourcesJarTaskName()));

                if (!SourceSet.isMain(sourceSet) || mainJarIsSlim(project)) {
                    artifact.withAdditionalFile(project.getTasks().named(sourceSet.getJarTaskName()));
                    task.dependsOn(project.getTasks().named(sourceSet.getJarTaskName()));
                }
            });

            curse.getRelationships().get().forEach((slug, relationship) -> {
                if (!Constants.VALID_RELATION_TYPES.contains(relationship)) {
                    throw new InvalidUserDataException("Invalid relationship type: %s for project: %s".formatted(relationship, slug));
                }
                artifact.addRelation(slug, relationship);
            });
        });

        project.getTasks().named("publish").configure(task -> task.dependsOn(curseforge));
    }
}
