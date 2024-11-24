package com.ldtteam.tableau.maven.publishing;

import com.ldtteam.tableau.common.extensions.ModExtension;
import com.ldtteam.tableau.git.extensions.GitExtension;
import com.ldtteam.tableau.scripting.extensions.TableauScriptingExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.XmlProvider;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.*;
import org.gradle.internal.instrumentation.api.annotations.ToBeReplacedByLazyProperty;

import javax.inject.Inject;
import java.util.*;

/**
 * Extension that configures the Maven publishing plugin.
 */
public class MavenPublishingExtension {

    /**
     * Gets the Maven publishing extension for the given project.
     *
     * @param project the project to get the extension from
     * @return the Maven publishing extension
     */
    public static MavenPublishingExtension get(final Project project) {
        return TableauScriptingExtension.get(project, MavenPublishingExtension.class);
    }

    /**
     * The name of the Maven publishing extension.
     */
    public static final String EXTENSION_NAME = "maven";

    private enum PublishingMode {
        UNKNOWN,
        LDTTEAM,
        GITHUB,
        CUSTOM,
        LOCAL;

        public boolean includedInMaven() {
            return this == UNKNOWN || this == LOCAL;
        }
    }

    private final Project project;
    private final LinkedList<Action<? super POM>> configurators;

    private PublishingMode publishingMode = PublishingMode.UNKNOWN;

    /**
     * Creates a new Maven publishing extension.
     *
     * @param project the project to create the extension for
     */
    @Inject
    public MavenPublishingExtension(final Project project) {
        this.project = project;
        this.configurators = new LinkedList<>();
    }

    /**
     * Configures the POM.
     *
     * @param configure the configuration action
     */
    public void pom(Action<? super POM> configure) {
        configurators.add(configure);
    }

    /**
     * Configures the publishing system to publish the project to the LDTTeam Maven repository.
     * <p>
     *     Also configures the POM to publish to the LDTTeam Maven repository.
     */
    public void publishToLDTTeam() {
        //LDTTeam always overrides configured POM settings
        pom(POM::distributeOnLDTTeamMaven);

        final PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        final Provider<String> username = project.getProviders().environmentVariable("LDTTeamJfrogUsername");
        final Provider<String> password = project.getProviders().environmentVariable("LDTTeamJfrogPassword");

        if (username.isPresent() && password.isPresent()) {
            publishing.repositories(mavenRepositories -> {
                mavenRepositories.maven(mavenRepository -> {
                    mavenRepository.setUrl("https://ldtteam.jfrog.io/ldtteam/mods-maven");
                    mavenRepository.credentials(credentials -> {
                        credentials.setUsername(username.get());
                        credentials.setPassword(password.get());
                    });
                    mavenRepository.setName("LDTTeam Maven");
                });
            });
        }
    }

    /**
     * Configures the publishing system to publish the project to a custom Maven repository.
     * <p>
     *     Also configures the POM to publish to the custom Maven repository.
     *
     * @param repositoryName the name of the repository
     * @param repositoryUrl the URL of the repository
     */
    public void publishTo(String repositoryName, String repositoryUrl) {
        if (publishingMode.includedInMaven()) {
            publishingMode = PublishingMode.CUSTOM;
            pom(pom -> pom.distributeOnCustomRepo(repositoryUrl));
        }

        final PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        final Provider<String> username = project.getProviders().environmentVariable("%s_USERNAME".formatted(repositoryName.toUpperCase(Locale.ROOT)));
        final Provider<String> password = project.getProviders().environmentVariable("%S_TOKEN".formatted(repositoryName.toUpperCase(Locale.ROOT)));

        if (username.isPresent() && password.isPresent()) {
            publishing.repositories(mavenRepositories -> {
                mavenRepositories.maven(mavenRepository -> {
                    mavenRepository.setUrl(repositoryUrl);
                    mavenRepository.credentials(credentials -> {
                        credentials.setUsername(username.get());
                        credentials.setPassword(password.get());
                    });
                    mavenRepository.setName(repositoryName);
                });
            });
        }
    }

    /**
     * Configures the publishing system to publish the project to the local Maven repository.
     * <p>
     *     Also configures the POM to publish to the local Maven repository, if it has not already been set to the LDTTeam Maven repository.
     */
    public void publishToGithub() {
        if (publishingMode.includedInMaven()) {
            publishingMode = PublishingMode.GITHUB;
            pom(POM::distributeOnGithubPackages);
        }

        final PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        final Provider<String> username = project.getProviders().environmentVariable("GITHUB_USERNAME");
        final Provider<String> password = project.getProviders().environmentVariable("GITHUB_TOKEN");

        if (username.isPresent() && password.isPresent()) {
            publishing.repositories(mavenRepositories -> {
                mavenRepositories.maven(mavenRepository -> {
                    final GitExtension git = GitExtension.get(project);
                    mavenRepository.setUrl(git.getRepositoryName().map("https://maven.pkg.github.com/%s"::formatted).get());
                    mavenRepository.credentials(credentials -> {
                        credentials.setUsername(username.get());
                        credentials.setPassword(password.get());
                    });
                    mavenRepository.setName("GitHub Packages");
                });
            });
        }
    }

    /**
     * Configures the publishing system to publish the project to the local Maven repository.
     * <p>
     *     Does **not** configure the POM to publish to the local Maven repository.
     */
    public void publishLocally() {
        if (publishingMode.includedInMaven()) {
            publishingMode = PublishingMode.LOCAL;
        }

        final PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        publishing.repositories(mavenRepositories -> {
            mavenRepositories.maven(mavenRepository -> {
                mavenRepository.setUrl("file:///" + project.getRootProject().file("repo").getAbsolutePath());
                mavenRepository.setName("Local Maven Repository");
            });
        });
    }

    /**
     * Configures the POM.
     *
     * @param pom the POM to configure
     */
    void configure(MavenPom pom) {
        for (Action<? super POM> configurator : configurators) {
            configurator.execute(new POM(project, pom));
        }

        final ModExtension mod = ModExtension.get(project);

        pom.getUrl().set(mod.getUrl());

        pom.organization(organization -> {
            organization.getName().set(mod.getPublisher());
        });

        pom.setPackaging("jar");
    }

    /**
     * The {@link MavenPom} implementation that delegates to the actual POM and applies additional configuration.
     *
     * @param project the project to which this POM belongs
     * @param delegate the actual {@link MavenPom} to delegate to
     */
    public record POM(Project project, MavenPom delegate) implements MavenPom {

        @ToBeReplacedByLazyProperty
        @Override
        public String getPackaging() {
            return delegate().getPackaging();
        }

        @Override
        public void setPackaging(String packaging) {
            delegate().setPackaging(packaging);
        }

        @Override
        public Property<String> getName() {
            return delegate().getName();
        }

        @Override
        public Property<String> getDescription() {
            return delegate().getDescription();
        }

        @Override
        public Property<String> getUrl() {
            return delegate().getUrl();
        }

        @Override
        public Property<String> getInceptionYear() {
            return delegate().getInceptionYear();
        }

        @Override
        public void licenses(Action<? super MavenPomLicenseSpec> action) {
            delegate().licenses(action);
        }

        @Override
        public void organization(Action<? super MavenPomOrganization> action) {
            delegate().organization(action);
        }

        @Override
        public void developers(Action<? super MavenPomDeveloperSpec> action) {
            delegate().developers(action);
        }

        @Override
        public void contributors(Action<? super MavenPomContributorSpec> action) {
            delegate().contributors(action);
        }

        @Override
        public void scm(Action<? super MavenPomScm> action) {
            delegate().scm(action);
        }

        @Override
        public void issueManagement(Action<? super MavenPomIssueManagement> action) {
            delegate().issueManagement(action);
        }

        @Override
        public void ciManagement(Action<? super MavenPomCiManagement> action) {
            delegate().ciManagement(action);
        }

        @Override
        public void distributionManagement(Action<? super MavenPomDistributionManagement> action) {
            delegate().distributionManagement(action);
        }

        @Override
        public void mailingLists(Action<? super MavenPomMailingListSpec> action) {
            delegate().mailingLists(action);
        }

        @Override
        public MapProperty<String, String> getProperties() {
            return delegate().getProperties();
        }

        @Override
        public void withXml(Action<? super XmlProvider> action) {
            delegate().withXml(action);
        }

        /**
         * Configures the POM to use the GNU Lesser General Public License v3.0.
         */
        public void usingGnu3License() {
            licenses(licenses -> {
                licenses.license(license -> {
                    license.getName().set("GNU Lesser General Public License v3.0");
                    license.getUrl().set("https://www.gnu.org/licenses/lgpl-3.0.html");
                });
            });
        }

        /**
         * Configures the POM to use the MIT License.
         */
        public void usingMitLicense() {
            licenses(licenses -> {
                licenses.license(license -> {
                    license.getName().set("MIT License");
                    license.getUrl().set("https://opensource.org/licenses/MIT");
                });
            });
        }

        /**
         * Configures distribution to happen via the LDTTeam Maven repository.
         */
        public void distributeOnLDTTeamMaven() {
            distributionManagement(distributionManagement -> {
                distributionManagement.getDownloadUrl().set("https://ldtteam.jfrog.io/artifactory/modding/");
            });
        }

        /**
         * Configures distribution to happen via the GitHub Packages repository.
         */
        public void distributeOnGithubPackages() {
            distributionManagement(distributionManagement -> {
                final GitExtension git = GitExtension.get(project);
                distributionManagement.getDownloadUrl().set(git.getRepositoryName().map("https://maven.pkg.github.com/%s"::formatted));
            });
        }

        /**
         * Configures distribution to happen via a custom repository.
         *
         * @param repositoryUrl the URL of the repository
         */
        public void distributeOnCustomRepo(String repositoryUrl) {
            distributionManagement(distributionManagement -> {
                distributionManagement.getDownloadUrl().set(repositoryUrl);
            });
        }

        /**
         * Configures the POM using information stored in git.
         */
        public void usingGit() {
            final GitExtension git = GitExtension.get(project);

            scm(scm -> {
                scm.getConnection().set(git.getGitUrl());
                scm.getDeveloperConnection().set(git.getGitUrl());
                scm.getUrl().set(git.getGithubUrl());
            });

            developers(developers -> {
                git.getDevelopers().get().stream().sorted(Comparator.comparing(GitExtension.Developer::count).reversed()).forEach(developer -> {
                    developers.developer(mavenPomDeveloper -> {
                        mavenPomDeveloper.getId().set(developer.email());
                        mavenPomDeveloper.getName().set(developer.name());
                        mavenPomDeveloper.getEmail().set(developer.email());
                    });
                });
            });

            contributors(contributors -> {
                List<GitExtension.Developer> developerList = new ArrayList<>(git.getDevelopers().get());

                developerList.sort(Comparator.comparing(GitExtension.Developer::count).reversed());

                if (developerList.size() > 5) {
                    developerList = developerList.subList(0, 5);
                }

                developerList.stream().sorted(Comparator.comparing(GitExtension.Developer::count).reversed()).forEach(developer -> {
                    contributors.contributor(mavenPomContributor -> {
                        mavenPomContributor.getName().set(developer.name());
                        mavenPomContributor.getEmail().set(developer.email());
                    });
                });
            });

            issueManagement(issueManagement -> {
                issueManagement.getUrl().set(git.getGithubUrl() + "/issues");
                issueManagement.getSystem().set("GitHub");
            });

            getInceptionYear().set(git.getInitialCommitYear().map(String::valueOf));

            ciManagement(ciManagement -> {
                ciManagement.getSystem().set("GitHub Actions");
                ciManagement.getUrl().set(git.getGithubUrl() + "/actions");
            });

            organization(organization -> {
                organization.getName().set(git.getOrganizationName());
                organization.getUrl().set(git.getOrganizationUrl());
            });
        }
    }
}
