package com.ldtteam.tableau.git.extensions;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.*;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * The Git extension for the Tableau project.
 * <p>
 * Provides access to git information.
 */
public abstract class GitExtension {

    /**
     * Gets the git extension for a given project.
     *
     * @param project The project.
     * @return The git extension.
     */
    public static GitExtension get(final Project project) {
        return project.getGradle().getExtensions().getByType(GitExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "git";

    @Inject
    public GitExtension(@NotNull Project project) {
        this.getBranch().set(project.getProviders().of(CurrentBranchValueSource.class, noneValueSourceSpec -> {
            //noop;
        }));
    }

    public abstract Property<String> getBranch();

    /**
     * Lazy value source for the current branch.
     */
    abstract static class CurrentBranchValueSource implements ValueSource<String, ValueSourceParameters.None> {

        @Inject
        abstract ExecOperations getExecOperations();

        public String obtain() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            getExecOperations().exec(it -> {
                it.setCommandLine("git", "rev-parse", "--abbrev-ref", "HEAD");
                it.setStandardOutput(output);
            });

            return output.toString(Charset.defaultCharset());
        }
    }


}
