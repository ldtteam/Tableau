package com.ldtteam.tableau.utilities;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UtilitiesPluginTest {

    @Test
    void testApplyWithProject() {
        Project project = ProjectBuilder.builder().build();
        UtilitiesPlugin plugin = new UtilitiesPlugin();
        plugin.apply(project);

        Assertions.assertTrue(project.getPlugins().hasPlugin(UtilitiesProjectPlugin.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testApplyWithSettings() {
        Settings settings = mock(Settings.class);
        PluginContainer plugins = mock(PluginContainer.class);
        List<Class<?>> pluginClasses = new ArrayList<>();

        when(settings.getPlugins()).thenReturn(plugins);
        when(plugins.apply(any(Class.class))).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                if (args.length > 0 && args[0] instanceof Class<?> pluginClass) {
                    pluginClasses.add(pluginClass);
                }
                return null;
            }
        });
        
        UtilitiesPlugin plugin = new UtilitiesPlugin();
        plugin.apply(settings);
        
        Assertions.assertTrue(pluginClasses.contains(UtilitiesSettingsPlugin.class));
    }


    @Test
    void testApplyWithInvalidObject() {
        Object invalidObject = new Object();
        UtilitiesPlugin plugin = new UtilitiesPlugin();
        assertThrows(IllegalArgumentException.class, () -> plugin.apply(invalidObject));
    }
}