package com.ldtteam.tableau.crowdin.extensions;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * The Crowdin extension.
 */
public abstract class CrowdinExtension {

    /**
     * Get the Crowdin extension for the given project.
     *
     * @param project The project to get the extension from.
     * @return The Crowdin extension.
     */
    public static CrowdinExtension get(Project project) {
        return TableauScriptingExtension.get(project, CrowdinExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "crowdin";

    @Inject
    public CrowdinExtension(Project project) {
        final ModExtension mod = ModExtension.get(project);

        getSourceFiles().convention(mod.getModId().map(id -> project.file("src/main/resources/assets/" + id + "/lang/en_us.json")));
        getTargetFiles().convention(mod.getModId().map(id -> project.file("src/main/resources/assets/" + id + "/lang/en_us.json")));

        getDownloadLocation().convention(mod.getModId().map(id -> project.getLayout().getProjectDirectory().dir("src/main/resources/assets/" + id + "/lang")));
        getSplitByBranch().convention(false);
    }

    /**
     * The source files that should be merged before uploading.
     *
     * @return The source files.
     */
    public abstract ConfigurableFileCollection getSourceFiles();

    /**
     * The target files that should be uploaded to Crowdin.
     *
     * @return The target files.
     */
    public abstract ConfigurableFileCollection getTargetFiles();

    /**
     * The download location for the translations.
     *
     * @return The download location.
     */
    public abstract DirectoryProperty getDownloadLocation();

    /**
     * Whether to split the translations by branch.
     *
     * @return Whether to split the translations by branch.
     */
    public abstract Property<Boolean> getSplitByBranch();

    /**
     * A regex pattern that needs to match the branch name for the build crowdin build to be triggered.
     * <p>
     *     If not specified, the build will be triggered on all branches.
     * </p>
     */
    public abstract Property<String> getOnlyBuildOnBranchMatching();

    /**
     * A regex pattern that needs to match the branch name for the upload to crowdin to be triggered.
     * <p>
     *     If not specified, the upload will be triggered on all branches.
     * </p>
     */
    public abstract Property<String> getOnlyUploadOnBranchMatching();
}
