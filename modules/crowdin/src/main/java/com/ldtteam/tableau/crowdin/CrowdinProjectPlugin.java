/*
 * This source file was generated by the Gradle 'init' task
 */
package com.ldtteam.tableau.crowdin;

import com.ldtteam.tableau.common.CommonPlugin;
import com.ldtteam.tableau.crowdin.extensions.CrowdinExtension;
import com.ldtteam.tableau.crowdin.tasks.MergeTranslations;
import com.ldtteam.tableau.git.GitPlugin;
import com.ldtteam.tableau.git.extensions.GitExtension;
import com.ldtteam.tableau.scripting.ScriptingPlugin;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.file.Directory;
import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ProblemId;
import org.gradle.api.problems.Problems;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;
import org.zaproxy.gradle.crowdin.CrowdinPlugin;
import org.zaproxy.gradle.crowdin.tasks.BuildProjectTranslation;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Project plugin for handling crowdin translation management.
 */
@SuppressWarnings("UnstableApiUsage")
public class CrowdinProjectPlugin implements Plugin<Project> {

    private static final ProblemGroup CROWDIN_GROUP = TableauScriptingExtension.problemGroup("crowdin", "Crowdin");

    private final Problems problems;

    /**
     * Creates a new plugin instance.
     *
     * @param problems The {@link Problems} handler to use.
     */
    @Inject
    public CrowdinProjectPlugin(Problems problems) {
        this.problems = problems;
    }

    @Override
    public void apply(@NotNull Project target) {
        //We need these plugins as base, note the crowdin plugin is not our module plugin but the external one.
        target.getPlugins().apply(CrowdinPlugin.class);
        target.getPlugins().apply(ScriptingPlugin.class);
        target.getPlugins().apply(CommonPlugin.class);
        target.getPlugins().apply(GitPlugin.class);

        //Register the DSL extension.
        TableauScriptingExtension.register(target, CrowdinExtension.EXTENSION_NAME, CrowdinExtension.class);

        target.afterEvaluate(ignored -> {
            final CrowdinExtension crowdinExtension = CrowdinExtension.get(target);

            if (!crowdinExtension.getProjectId().isPresent()) {
                target.getLogger().debug("Crowdin project id not set, skipping Crowdin configuration");
                return;
            }

            final org.zaproxy.gradle.crowdin.CrowdinExtension crowdin = target.getExtensions().getByType(org.zaproxy.gradle.crowdin.CrowdinExtension.class);

            if (crowdinExtension.getSourceFiles().isEmpty()) {
                throw problems.getReporter().throwing(
                        new InvalidUserDataException("No source files specified for Crowdin"),
                        ProblemId.create("crowdin-no-source-files", "Missing source files for Crowdin", CROWDIN_GROUP),
                        spec -> {
                            spec.details("No source files specified for Crowdin, please specify at least one source file to manage")
                                    .solution("Add at least one source file to the crowdin block of the Tableau DSL")
                                    .documentedAt("https://tableau.ldtteam.com/docs/guides/translate-crowdin");
                        });
            }

            boolean mergesTranslations = false;
            if (crowdinExtension.getSourceFiles().getFiles().size() != 1) {
                if (crowdinExtension.getTargetFiles().isEmpty()) {
                    throw problems.getReporter().throwing(
                            new InvalidUserDataException("No target files specified for Crowdin"),
                            ProblemId.create("crowdin-no-target-files", "Missing target files for Crowdin", CROWDIN_GROUP),
                            spec -> {
                                spec.details("No target files specified for Crowdin, if you specify more then one source file, they need to be merged into at least one target file.")
                                .solution("Add at least one target file to the crowdin block of the Tableau DSL")
                                        .documentedAt("https://tableau.ldtteam.com/docs/guides/translate-crowdin");
                    });
                }

                final TaskProvider<MergeTranslations> mergeTranslations = target.getTasks().register("mergeTranslations", MergeTranslations.class, task -> {
                    task.getSourceFiles().from(crowdinExtension.getSourceFiles());
                    task.getTargetFiles().from(crowdinExtension.getTargetFiles());
                    task.setGroup("Crowdin");
                    task.setDescription("Merges the translations from the source files into the target files.");
                });
                mergesTranslations = true;

                target.getTasks().named("processResources", processResources -> {
                    processResources.dependsOn(mergeTranslations);
                });
            }

            if (target.getProviders().environmentVariable("CROWDIN_API_KEY").isPresent()) {
                crowdin.credentials(credentials -> {
                    credentials.getToken().set(target.getProviders().environmentVariable("CROWDIN_API_KEY"));
                });

                crowdin.configuration(configuration -> {
                    final File configFile;
                    try {
                        configFile = writeConfigurationFile(target);
                    } catch (IOException e) {
                        throw new InvalidUserDataException("Failed to write crowdin configuration file", e);
                    }

                    configuration.getFile().set(configFile);
                    configuration.getTokens().put("%crowdin_download_path%", crowdinExtension.getDownloadLocation().map(Directory::getAsFile).map(File::getAbsolutePath));
                });

                target.getTasks().named(CrowdinPlugin.BUILD_PROJECT_TRANSLATION_TASK_NAME, BuildProjectTranslation.class, task -> {
                    task.getWaitForBuilds().set(true);
                    task.getExportApprovedOnly().set(crowdinExtension.getExportApprovedOnly());
                    task.getSkipUntranslatedFiles().set(crowdinExtension.getSkipUntranslatedFiles());
                    task.getSkipUntranslatedStrings().set(crowdinExtension.getSkipUntranslatedStrings());
                });

                boolean willBuildTranslations = false;
                if (!crowdinExtension.getOnlyBuildOnBranchMatching().isPresent() || Pattern.matches(crowdinExtension.getOnlyBuildOnBranchMatching().get(), GitExtension.get(target).getBranch().get())) {
                    target.getTasks().named("processResources", processResources -> {
                        processResources.dependsOn(CrowdinPlugin.BUILD_PROJECT_TRANSLATION_TASK_NAME);
                    });
                    willBuildTranslations = true;
                }

                if (!crowdinExtension.getOnlyUploadOnBranchMatching().isPresent() || Pattern.matches(crowdinExtension.getOnlyUploadOnBranchMatching().get(), GitExtension.get(target).getBranch().get())) {
                    target.getTasks().named("processResources", processResources -> {
                        processResources.dependsOn(CrowdinPlugin.UPLOAD_SOURCE_FILES_TASK_NAME);
                    });
                    if (willBuildTranslations) {
                        target.getTasks().named(CrowdinPlugin.BUILD_PROJECT_TRANSLATION_TASK_NAME, task -> {
                            task.dependsOn(CrowdinPlugin.UPLOAD_SOURCE_FILES_TASK_NAME);
                        });
                    }

                    if (mergesTranslations) {
                        target.getTasks().named(CrowdinPlugin.UPLOAD_SOURCE_FILES_TASK_NAME, task -> {
                            task.dependsOn("mergeTranslations");
                        });
                    }
                }

                final TaskProvider<Delete> deleteTranslationFilesInBuildDir = target.getTasks().register("deleteTranslationFilesInBuildDir", Delete.class, task -> {
                    task.setGroup("Crowdin");
                    task.setDescription("Deletes the translation files from the build directory.");
                    task.dependsOn(target.getTasks().named(CrowdinPlugin.COPY_PROJECT_TRANSLATIONS_TASK_NAME));
                    task.delete(target.getLayout().getBuildDirectory().dir("temp").map(dir -> dir.dir("translations")));
                    task.setFollowSymlinks(true);
                });

                final TaskProvider<Copy> normalizeTranslationFilesInBuildDir = target.getTasks().register("normalizeTranslationFilesInBuildDir", Copy.class, task -> {
                    task.setGroup("Crowdin");
                    task.setDescription("Normalizes the translation files in the build directory.");
                    task.dependsOn(deleteTranslationFilesInBuildDir);
                    task.from(crowdinExtension.getDownloadLocation());
                    task.into(target.getLayout().getBuildDirectory().dir("temp").map(dir -> dir.dir("translations")));
                    task.rename(s -> s.toLowerCase(Locale.ROOT));
                });

                final TaskProvider<Delete> deleteTranslationFilesInRuntimeDir = target.getTasks().register("deleteTranslationFilesInRuntimeDir", Delete.class, task -> {
                    task.setGroup("Crowdin");
                    task.setDescription("Deletes the translation files from the runtime directory.");
                    task.dependsOn(normalizeTranslationFilesInBuildDir);
                    task.delete(crowdinExtension.getDownloadLocation());
                    task.setFollowSymlinks(true);
                });

                final TaskProvider<Copy> normalizeTranslationFilesInRuntimeDir = target.getTasks().register("normalizeTranslationFilesInRuntimeDir", Copy.class, task -> {
                    task.setGroup("Crowdin");
                    task.setDescription("Normalizes the translation files in the runtime directory.");
                    task.dependsOn(deleteTranslationFilesInRuntimeDir);
                    task.from(target.getLayout().getBuildDirectory().dir("temp").map(dir -> dir.dir("translations")));
                    task.into(crowdinExtension.getDownloadLocation());
                    task.rename(s -> s.toLowerCase(Locale.ROOT));
                });

                target.getTasks().named(CrowdinPlugin.COPY_PROJECT_TRANSLATIONS_TASK_NAME, task -> {
                    task.dependsOn(CrowdinPlugin.UPLOAD_SOURCE_FILES_TASK_NAME);
                });
                target.getTasks().named("processResources", processResources -> {
                    processResources.dependsOn(normalizeTranslationFilesInRuntimeDir);
                });
                target.getTasks().named("sourcesJar", sourcesJar -> {
                    sourcesJar.dependsOn(normalizeTranslationFilesInRuntimeDir);
                });
            }
        });
    }

    private File writeConfigurationFile(Project project) throws IOException {
        final File file = project.getLayout().getBuildDirectory().dir("crowdin").map(dir -> dir.file("crowdin.yml")).get().getAsFile();
        final int projectId = CrowdinExtension.get(project).getProjectId().get();

        if (file.exists()) {
            if (Files.readString(file.toPath()).contains("  - id: %s".formatted(projectId))) {
                return file;
            }

            if (!file.delete()) {
                throw new InvalidUserDataException("Failed to delete existing crowdin configuration file");
            }
        }

        if (!file.exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new InvalidUserDataException("Failed to create crowdin configuration file parent directories");
            }
        }

        //noinspection MalformedFormatString
        Files.writeString(file.toPath(), """
                projects:
                  - id: %s
                    sources:
                      - dir: "%crowdin_download_path%"
                        crowdinPath:
                          dir: "/lang"
                          filename: "en_us.json"
                        exportPattern:
                          dir: "/lang"
                          filename: "%locale_with_underscore%.json"
                        includes:
                          - pattern: "en_us.json"
                """.formatted(projectId));

        return file;
    }
}
