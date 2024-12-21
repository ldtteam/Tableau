package com.ldtteam.tableau.utilities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.ExtensionContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ldtteam.tableau.utilities.extensions.UtilityFunctions;

public class UtilitiesSettingsPluginTest {

    private Settings settings;
    private UtilitiesSettingsPlugin plugin;

    @BeforeEach
    public void setup() {
        settings = mock(Settings.class);
        plugin = new UtilitiesSettingsPlugin();
    }

    @Test
    public void testPluginApply() {
        ExtensionContainer extensions = mock(ExtensionContainer.class);
        when(settings.getExtensions()).thenReturn(extensions);

        plugin.apply(settings);

        verify(extensions).create(UtilityFunctions.EXTENSION_NAME, UtilityFunctions.class);
    }
}
