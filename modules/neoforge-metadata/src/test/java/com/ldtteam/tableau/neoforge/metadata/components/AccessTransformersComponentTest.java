package com.ldtteam.tableau.neoforge.metadata.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstructionWithAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.neoforge.metadata.NeoforgeMetadataPlugin;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

class AccessTransformersComponentTest {

    private Project project;
    private SourceSetConfiguration sourceSetConfiguration;
    private SourceSet sourceSet;
    private AccessTransformersComponent component;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        var neoforgeMetadataPlugin = new NeoforgeMetadataPlugin();
        neoforgeMetadataPlugin.apply(project);

        sourceSetConfiguration = mock(SourceSetConfiguration.class);
        sourceSet = project.getExtensions().getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        when(sourceSetConfiguration.getName()).thenReturn(sourceSet.getName());
        when(sourceSetConfiguration.getSourceSet()).thenReturn(sourceSet);
        component = new AccessTransformersComponent(project, sourceSetConfiguration);
    }

    @Test
    void testInitialization() {
        assertNotNull(component);
        assertEquals("main", sourceSetConfiguration.getName());
    }

    @Test
    void testWrite() throws IOException {
        File mockFile = File.createTempFile("accesstransformer", ".cfg");
        component.from(mockFile);

        CommentedConfig config = CommentedConfig.inMemory();
        component.write(config);

        List<CommentedConfig> transformers = config.get("accessTransformers");
        assertNotNull(transformers);
        assertEquals(1, transformers.size());
        assertEquals("META-INF/Tableau/AccessTransformers/main/%s".formatted(mockFile.getName()), transformers.get(0).get("file"));

        mockFile.delete();
    }

    @Test
    void testWriteEmpty() {
        CommentedConfig config = CommentedConfig.inMemory();
        component.write(config);

        List<CommentedConfig> transformers = config.get("accessTransformers");
        assertNotNull(transformers);
        assertEquals(0, transformers.size());
    }

    @Test
    void testWriteNonExistentFile() {
        File mockFile = new File("nonexistentfile.cfg");
        component.from(mockFile);

        CommentedConfig config = CommentedConfig.inMemory();
        component.write(config);

        List<CommentedConfig> transformers = config.get("accessTransformers");
        assertNotNull(transformers);
        assertEquals(0, transformers.size());
    }
}