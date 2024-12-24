package com.ldtteam.tableau.neoforge.metadata.components.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.neoforge.metadata.NeoforgeMetadataPlugin;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

class ModTest {

    private Project project;
    private SourceSetConfiguration sourceSetConfiguration;
    private SourceSet sourceSet;
    private Mod mod;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        var neoforgeMetadataPlugin = new NeoforgeMetadataPlugin();
        neoforgeMetadataPlugin.apply(project);

        sourceSetConfiguration = mock(SourceSetConfiguration.class);
        sourceSet = mock(SourceSet.class);
        when(sourceSetConfiguration.getSourceSet()).thenReturn(sourceSet);
        when(sourceSet.getRuntimeClasspathConfigurationName()).thenReturn("runtimeClasspath");
        when(sourceSet.getApiConfigurationName()).thenReturn("api");
        mod = project.getObjects().newInstance(Mod.class, project, sourceSetConfiguration, "testMod");
    }

    @Test
    void testWriteMetadata() {
        mod.getVersion().set("1.0.0");
        mod.getDisplayName().set("Test Mod");
        mod.getDescription().set("This is a test mod.");
        mod.getCredits().set("Test Developer");
        mod.getAuthors().set("Test Author");

        CommentedConfig config = mod.writeMetadata();

        assertEquals("testMod", config.get("modId"));
        assertEquals("1.0.0", config.get("version"));
        assertEquals("Test Mod", config.get("displayName"));
        assertEquals("This is a test mod.", config.get("description"));
        assertEquals("Test Developer", config.get("credits"));
        assertEquals("Test Author", config.get("authors"));
    }

    @Test
    void testWriteDependencies() {
        ModDependency dependency = project.getObjects().newInstance(ModDependency.class, "dependencyModId");
        dependency.getVersionRange().set("[1.0,2.0)");
        dependency.getType().set(ModDependency.Type.REQUIRED);
        dependency.getOrdering().set(ModDependency.Ordering.NONE);
        dependency.getSide().set(ModDependency.Side.BOTH);

        mod.getDependencies().add(dependency);

        List<CommentedConfig> configs = mod.writeDependencies();

        assertEquals(1, configs.size());
        CommentedConfig config = configs.get(0);
        assertEquals("dependencyModId", config.get("modId"));
        assertEquals("required", config.get("type"));
        assertEquals("[1.0,2.0)", config.get("version"));
        assertEquals("NONE", config.get("ordering"));
        assertEquals("BOTH", config.get("side"));
    }
}