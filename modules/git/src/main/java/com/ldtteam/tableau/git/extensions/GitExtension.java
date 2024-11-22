package com.ldtteam.tableau.git.extensions;

import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.*;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;

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
        return TableauScriptingExtension.get(project, GitExtension.class);
    }

    /**
     * The name of the extension.
     */
    public static final String EXTENSION_NAME = "git";

    /**
     * Creates a new git extension model.
     *
     * @param project The project for the model.
     */
    @Inject
    public GitExtension(@NotNull Project project) {
        this.getBranch().set(project.getProviders().of(CurrentBranchValueSource.class, noneValueSourceSpec -> {
        }));
        this.getGitUrl().set(project.getProviders().of(OriginRemoteUrlValueSource.class, noneValueSourceSpec -> {
        }));
        this.getGithubUrl().set(this.getGitUrl().map(it -> {
            //Remove the ".git" suffix
            if (it.endsWith(".git")) {
                return it.substring(0, it.length() - 4);
            }

            return it;
        }));
        this.getDevelopers().set(project.getProviders().of(DevelopersValueSource.class, noneValueSourceSpec -> {
        }));
        this.getInitialCommitYear().set(project.getProviders().of(FirstCommitYearValueSource.class, noneValueSourceSpec -> {
        }));
        this.getRepositoryName().set(this.getGithubUrl().map(it -> {
            //Extract the repository name from the url.
            final int lastSlash = it.lastIndexOf('/');
            return it.substring(lastSlash + 1);
        }));
    }

    /**
     * Gets the branch property.
     *
     * @return The branch property.
     */
    public abstract Property<String> getBranch();

    /**
     * Gets the GitHub url property.
     *
     * @return The GitHub url property.
     */
    public abstract Property<String> getGithubUrl();

    /**
     * Gets the git url property.
     *
     * @return The git url property.
     */
    public abstract Property<String> getGitUrl();

    /**
     * Gets the developers property.
     *
     * @return The developers property.
     */
    public abstract ListProperty<Developer> getDevelopers();

    /**
     * Gets the initial commit year property.
     *
     * @return The initial commit year property.
     */
    public abstract Property<Integer> getInitialCommitYear();

    /**
     * Gets the repository name property.
     *
     * @return The repository name property.
     */
    public abstract Property<String> getRepositoryName();

    /**
     * Lazy value source for the current branch.
     */
    public abstract static class CurrentBranchValueSource implements ValueSource<String, ValueSourceParameters.None> {

        /**
         * Creates a new value source.
         */
        @Inject
        public CurrentBranchValueSource() {
        }

        /**
         * The exec operations which are used to invoke git.
         * <p>
         *     Injected by the Gradle runtime.
         *
         * @return The exec operations.
         */
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

    /**
     * Lazy value source for the origin remote url.
     */
    public abstract static class OriginRemoteUrlValueSource implements ValueSource<String, ValueSourceParameters.None> {

        /**
         * Creates a new value source.
         */
        @Inject
        public OriginRemoteUrlValueSource() {
        }

        /**
         * The exec operations which are used to invoke git.
         * <p>
         *     Injected by the Gradle runtime.
         *
         * @return The exec operations.
         */
        @Inject
        abstract ExecOperations getExecOperations();

        public String obtain() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            getExecOperations().exec(it -> {
                it.setCommandLine("git", "config", "--get", "remote.origin.url");
                it.setStandardOutput(output);
            });

            return output.toString(Charset.defaultCharset());
        }
    }

    /**
     * A developer of the project.
     *
     * @param count The commit count of that developer.
     * @param name  The name of the developer.
     * @param email The email of the developer.
     */
    public record Developer(int count, String name, String email) {
    }

    /**
     * Lazy value source for the developers of the project.
     */
    public abstract static class DevelopersValueSource implements ValueSource<List<Developer>, ValueSourceParameters.None> {

        /**
         * Creates a new value source.
         */
        @Inject
        public DevelopersValueSource() {
        }

        /**
         * The exec operations which are used to invoke git.
         * <p>
         * Injected by the Gradle runtime.
         *
         * @return The exec operations.
         */
        @Inject
        abstract ExecOperations getExecOperations();

        public List<Developer> obtain() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            getExecOperations().exec(it -> {
                it.setCommandLine("git", "shortlog", "-sne");
                it.setStandardOutput(output);
            });

            final String outputString = output.toString(Charset.defaultCharset());
            //The output is formatted like:
            // 100 Author Name <email>
            //   2 Author Name <email>
            //We need to parse that into a list of developers.
            return outputString.lines().map(line -> {
                //First extract the email:
                final int emailStart = line.indexOf('<');
                final int emailEnd = line.lastIndexOf('>');
                final String email = line.substring(emailStart + 1, emailEnd).trim();

                //Then extract the name, it starts at the first none whitespace and numeric character and ends at the first '<' character.:
                final int nameStart = line.length() - line.replaceFirst("( |\\d)+", "").length();
                final int nameEnd = line.indexOf('<') - 1;
                final String name = line.substring(nameStart, nameEnd).trim();

                //Finally extract the count:
                final int count = Integer.parseInt(line.substring(0, nameStart).trim());

                return new Developer(count, name, email);
            }).toList();
        }
    }

    /**
     * Lazy value source for the year of the first commit.
     */
    public abstract static class FirstCommitYearValueSource implements ValueSource<Integer, ValueSourceParameters.None> {

        /**
         * Creates a new value source.
         */
        @Inject
        public FirstCommitYearValueSource() {
        }

        /**
         * The exec operations which are used to invoke git.
         * <p>
         *     Injected by the Gradle runtime.
         *
         * @return The exec operations.
         */
        @Inject
        abstract ExecOperations getExecOperations();

        public Integer obtain() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            getExecOperations().exec(it -> {
                it.setCommandLine("git", "log", "--reverse", "--pretty=format:%ad", "--date=format:%Y", "--max-count=1");
                it.setStandardOutput(output);
            });

            return Integer.parseInt(output.toString(Charset.defaultCharset()));
        }
    }

}
