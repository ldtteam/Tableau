package com.ldtteam.tableau.neoforge.metadata.components;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.problems.Problems;
import org.gradle.api.problems.Severity;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.ldtteam.tableau.neoforge.metadata.api.IMetadataComponent;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

/**
 * The metadata component that defines the supported loader version of the source set that this component belongs to.
 */
public abstract class LoaderVersionComponent implements IMetadataComponent {

    /**
     * The name of the component extension and toml entry.
     */
    public static final String NAME = "loaderVersion";

    /**
     * Creates a new metadata component.
     * <p>
     * Preconfigures the supported range to be the loader version of the neoforge version that it depends upon.
     * 
     * @param project The project that it belongs to.
     * @param sourceSetConfiguration The sourceset that it belongs to.
     */
    @Inject
    public LoaderVersionComponent(final Project project, final SourceSetConfiguration sourceSetConfiguration) {
        final Configuration compileClasspath = project.getConfigurations().getByName(sourceSetConfiguration.getSourceSet().getCompileClasspathConfigurationName());
        getRange().convention(compileClasspath.getIncoming()
            .getArtifacts()
            .getResolvedArtifacts()
            .map(resolvedArtifacts -> {
                return resolvedArtifacts.stream()
                .filter(artifact -> {
                    if (!(artifact.getId().getComponentIdentifier() instanceof ModuleComponentIdentifier identifier)) {
                        return false;
                    }

                    return identifier.getGroup().equals("net.neoforged") && identifier.getModule().equals("neoforge");
                })
                .map(artifact -> artifact.getFile().toPath())
                .map(jarPath -> openFileSystem(jarPath))
                .map(jarFs -> jarFs.getPath("META-INF", "neoforged.mods.toml"))
                .filter(modsTomlPath -> Files.exists(modsTomlPath))
                .map(modsTomlPath -> FileConfig.of(modsTomlPath))
                .peek(config -> config.load())
                .map(modsToml -> modsToml.<String>get(NAME))
                .findFirst()
                .orElseThrow();
            }));
    }

    /**
     * The gradle problem API for handling file system read problems
     * 
     * @return The problem API.
     */
    @Inject 
    protected abstract Problems getProblems();

    @SuppressWarnings("UnstableApiUsage")
    private FileSystem openFileSystem(Path path) {
        try {
            return FileSystems.newFileSystem(path);
        } catch (IOException e) {
            throw getProblems().getReporter()
                .throwing(spec -> {
                    spec.contextualLabel("Metadata generation")
                        .details("Tableau was not able to properly open the NeoForge jar to search for the currently supported loader range!")
                        .solution("Configure the supported loader range your self, validate your dependencies, or try again later")
                        .withException(new GradleException("Failed to open the NeoForge jar to search for the currently supported loader range!", e))
                        .severity(Severity.ERROR);
                });
        }
    }

    /**
     * The supported loader version range.
     * <p>
     * Preconfigured with the loader version of the neoforge version that this source set depends on.
     * 
     * @return The loader version.
     */
    @Input
    public abstract Property<String> getRange();

    @Override
    public void write(CommentedConfig config) {
        config.set(NAME, getRange().get());
    }
}
