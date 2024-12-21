package com.ldtteam.tableau.utilities;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ldtteam.tableau.scripting.ScriptingPlugin;
import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;

public class UtilitiesProjectPluginTest {

    private Project project;
    private UtilitiesProjectPlugin plugin;

    @BeforeEach
    public void setup() {
        project = ProjectBuilder.builder().build();
        plugin = new UtilitiesProjectPlugin();
    }

    @Test
    public void testPluginApply() {
        plugin.apply(project);
        assertNotNull(UtilityFunctions.get(project));
    }

    @Test
    public void testScriptingPluginApplied() {
        plugin.apply(project);
        assertTrue(project.getPlugins().hasPlugin(ScriptingPlugin.class));
    }

    @Test
    public void testUtilityFunctionsRegistered() {
        plugin.apply(project);
        assertNotNull(UtilityFunctions.get(project));
    }
}