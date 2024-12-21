package com.ldtteam.tableau.neoforge.metadata.components;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.extensions.NeoGradleSourceSetConfigurationExtension;
import com.ldtteam.tableau.neoforge.metadata.NeoforgeMetadataPlugin;
import com.ldtteam.tableau.neoforge.metadata.components.model.Mod;
import com.ldtteam.tableau.neoforge.metadata.extensions.MetadataExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModsComponentTest {

    private Project project;
    private SourceSetConfiguration sourceSetConfiguration;
    private SourceSet sourceSet;
    private ModsComponent modsComponent;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        var neoforgeMetadataPlugin = new NeoforgeMetadataPlugin();
        neoforgeMetadataPlugin.apply(project);
        
        var neoGradleSourceSetConfiguration = mock(NeoGradleSourceSetConfigurationExtension.class);
        when(neoGradleSourceSetConfiguration.getIsModSource()).thenReturn(project.getObjects().property(Boolean.class));
        when(neoGradleSourceSetConfiguration.getIncludeInLibraries()).thenReturn(project.getObjects().property(Boolean.class));

        var extensionContainer = mock(ExtensionContainer.class);
        when(extensionContainer.getByType(NeoGradleSourceSetConfigurationExtension.class)).thenReturn(neoGradleSourceSetConfiguration);

        var metadataExtension = mock(MetadataExtension.class);
        when(metadataExtension.getIsEnabled()).thenReturn(project.getObjects().property(Boolean.class));

        when(extensionContainer.getByType(MetadataExtension.class)).thenReturn(metadataExtension);

        sourceSetConfiguration = mock(SourceSetConfiguration.class);
        sourceSet = project.getExtensions().getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        when(sourceSetConfiguration.getName()).thenReturn(sourceSet.getName());
        when(sourceSetConfiguration.getSourceSet()).thenReturn(sourceSet);
        when(sourceSetConfiguration.getIsPublished()).thenReturn(project.getObjects().property(Boolean.class));
        when(sourceSetConfiguration.getExtensions()).thenReturn(extensionContainer);

        modsComponent = project.getObjects().newInstance(ModsComponent.class, project, sourceSetConfiguration);
    }

    @Test
    void testInitialization() {
        assertNotNull(modsComponent);
    }

    @Test
    void testWrite() {
        Mod mod = modsComponent.create("testMod");
        mod.getVersion().set("1.0.0");
        mod.getDisplayName().set("Test Mod");
        mod.getDescription().set("This is a test mod.");
        mod.getCredits().set("Test Developer");
        mod.getAuthors().set("Test Author");

        CommentedConfig config = CommentedConfig.inMemory();
        modsComponent.write(config);

        List<CommentedConfig> mods = config.get("mods");
        assertNotNull(mods);
        assertEquals(1, mods.size());

        CommentedConfig modConfig = mods.get(0);
        assertEquals("testMod", modConfig.get("modId"));
        assertEquals("1.0.0", modConfig.get("version"));
        assertEquals("Test Mod", modConfig.get("displayName"));
        assertEquals("This is a test mod.", modConfig.get("description"));
        assertEquals("Test Developer", modConfig.get("credits"));
        assertEquals("Test Author", modConfig.get("authors"));

        CommentedConfig dependencies = config.get("dependencies");
        assertNotNull(dependencies);
        assertTrue(dependencies.contains("testMod"));
    }
}