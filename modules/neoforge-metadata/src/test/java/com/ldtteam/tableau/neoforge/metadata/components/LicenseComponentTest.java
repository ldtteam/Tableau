package com.ldtteam.tableau.neoforge.metadata.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;

class LicenseComponentTest {

    private Project project;
    private SourceSetConfiguration sourceSet;
    private LicenseComponent licenseComponent;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        sourceSet = mock(SourceSetConfiguration.class);
        when(sourceSet.getName()).thenReturn("main");
        licenseComponent = project.getObjects().newInstance(LicenseComponent.class, project, sourceSet);
    }

    @Test
    void testInitialization() {
        assertNotNull(licenseComponent);
        assertEquals("MIT", licenseComponent.getName().get());
    }

    @Test
    void testWrite() {
        licenseComponent.getName().set("Apache-2.0");

        CommentedConfig config = CommentedConfig.inMemory();
        licenseComponent.write(config);
        assertEquals("Apache-2.0", config.get("license"));
    }

    @Test
    void testDefaultWrite() {
        CommentedConfig config = CommentedConfig.inMemory();
        licenseComponent.write(config);
        assertEquals("MIT", config.get("license"));
    }
}