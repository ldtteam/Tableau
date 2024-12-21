package com.ldtteam.tableau.neoforge.metadata.components;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoaderVersionComponentTest {

    private Project project;
    private SourceSetConfiguration sourceSetConfiguration;
    private SourceSet sourceSet;
    private Configuration compileClasspath;
    private ResolvableDependencies incoming;
    private ArtifactCollection incomingArtifacts;
    private LoaderVersionComponent loaderVersionComponent;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        sourceSetConfiguration = mock(SourceSetConfiguration.class);
        sourceSet = mock(SourceSet.class);
        compileClasspath = mock(ConfigurationInternal.class);
        incoming = mock(ResolvableDependencies.class);
        incomingArtifacts = mock(ArtifactCollection.class);

        Provider<Set<ResolvedArtifactResult>> emptyArtifacts = project.provider(() -> Collections.emptySet());
        when(incomingArtifacts.getResolvedArtifacts()).thenReturn(emptyArtifacts);
        when(incoming.getArtifacts()).thenReturn(incomingArtifacts);
        when(compileClasspath.getIncoming()).thenReturn(incoming);
        when(compileClasspath.getName()).thenReturn("someClassPathConfiguration");
        project.getConfigurations().add(compileClasspath);
        
        when(sourceSet.getName()).thenReturn("testTarget");
        when(sourceSet.getCompileClasspathConfigurationName()).thenReturn("someClassPathConfiguration");


        when(sourceSetConfiguration.getName()).thenReturn("testTarget");
        when(sourceSetConfiguration.getSourceSet()).thenReturn(sourceSet);
        loaderVersionComponent = project.getObjects().newInstance(LoaderVersionComponent.class, project, sourceSetConfiguration);
    }

    @Test
    void testInitialization() {
        assertNotNull(loaderVersionComponent);
    }

    @Test
    void testWrite() {
        loaderVersionComponent.getRange().set("1.0.0");

        CommentedConfig config = CommentedConfig.inMemory();
        loaderVersionComponent.write(config);

        assertEquals("1.0.0", config.get("loaderVersion"));
    }

    @Test
    void testDefaultRange() throws IOException {
        ResolvedArtifactResult artifact = mock(ResolvedArtifactResult.class);
        ModuleComponentIdentifier identifier = mock(ModuleComponentIdentifier.class);
        ComponentArtifactIdentifier componentIdentifier = mock(ComponentArtifactIdentifier.class);
        when(artifact.getId()).thenReturn(componentIdentifier);
        when(identifier.getGroup()).thenReturn("net.neoforged");
        when(identifier.getModule()).thenReturn("neoforge");
        when(componentIdentifier.getComponentIdentifier()).thenReturn(identifier);
        
        File tempNeoForgeJar = File.createTempFile("neoforge", ".jar");
        tempNeoForgeJar.delete();
        when(artifact.getFile()).thenReturn(tempNeoForgeJar);

        CommentedConfig config = CommentedConfig.inMemory();
        config.set("loaderVersion", "1.2.3");

        Path neoforgeJarPath = tempNeoForgeJar.toPath();
        URI uri = URI.create("jar:" + neoforgeJarPath.toUri());
        FileSystem jarFs = FileSystems.newFileSystem(uri, Map.of("create", "true"));
        Path metaInfDir = jarFs.getPath("META-INF");
        Files.createDirectories(metaInfDir);
        Path modsTomlPath = jarFs.getPath("META-INF", "neoforged.mods.toml");
        Writer writer = Files.newBufferedWriter(modsTomlPath);
        TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.write(config, writer);
        writer.close();
        jarFs.close();

        Set<ResolvedArtifactResult> artifacts = Collections.singleton(artifact);
        Provider<Set<ResolvedArtifactResult>> resolvedArtifacts = project.provider(() -> artifacts);
        when(incomingArtifacts.getResolvedArtifacts()).thenReturn(resolvedArtifacts);

        loaderVersionComponent = project.getObjects().newInstance(LoaderVersionComponent.class, project, sourceSetConfiguration);

        assertNotNull(loaderVersionComponent.getRange().get());
        assertEquals("1.2.3", loaderVersionComponent.getRange().get());
    }
}
