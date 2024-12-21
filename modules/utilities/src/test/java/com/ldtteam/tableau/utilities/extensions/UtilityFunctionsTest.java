package com.ldtteam.tableau.utilities.extensions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilityFunctionsTest {

    private final UtilityFunctions utilityFunctions = mock(UtilityFunctions.class);

    @Test
    public void testSplitVersionNumber() {
        when(utilityFunctions.splitVersionNumber("1.2.3")).thenCallRealMethod();

        String version = "1.2.3";
        String[] parts = utilityFunctions.splitVersionNumber(version);
        assertArrayEquals(new String[]{"1", "2", "3"}, parts);
    }

    @Test
    public void testBuildVersionNumberWithOffset() {
        when(utilityFunctions.buildVersionNumberWithOffset(
                "1.2.3", "1.2.5", "1.2.2", 2, 2)).thenCallRealMethod();
        when(utilityFunctions.splitVersionNumber(any())).thenCallRealMethod();

        String sourceVersion = "1.2.3";
        String currentVersion = "1.2.5";
        String relativeVersion = "1.2.2";
        int projectVersionElementIndex = 2;
        int sourceVersionElementIndex = 2;

        String result = utilityFunctions.buildVersionNumberWithOffset(
                sourceVersion, currentVersion, relativeVersion, projectVersionElementIndex, sourceVersionElementIndex);

        assertEquals("1.2.6", result);
    }

    @Test
    public void testBuildVersionNumberWithOffsetThrowsException() {
        when(utilityFunctions.buildVersionNumberWithOffset(
                "1.2.3", "1.2.1", "1.2.2", 2, 2)).thenCallRealMethod();
        when(utilityFunctions.splitVersionNumber(any())).thenCallRealMethod();

        String sourceVersion = "1.2.3";
        String currentVersion = "1.2.1";
        String relativeVersion = "1.2.2";
        int projectVersionElementIndex = 2;
        int sourceVersionElementIndex = 2;

        Executable executable = () -> utilityFunctions.buildVersionNumberWithOffset(
                sourceVersion, currentVersion, relativeVersion, projectVersionElementIndex, sourceVersionElementIndex);

        assertThrows(IllegalArgumentException.class, executable, "The current version is lower than the relative version.");
    }

    @Test
    public void testBuildVersionNumberWithOffsetDifferentIndices() {
        when(utilityFunctions.buildVersionNumberWithOffset(
                "1.2.3", "1.4.3", "1.3.3", 1, 1)).thenCallRealMethod();
        when(utilityFunctions.splitVersionNumber(any())).thenCallRealMethod();

        String sourceVersion = "1.2.3";
        String currentVersion = "1.4.3";
        String relativeVersion = "1.3.3";
        int projectVersionElementIndex = 1;
        int sourceVersionElementIndex = 1;

        String result = utilityFunctions.buildVersionNumberWithOffset(
                sourceVersion, currentVersion, relativeVersion, projectVersionElementIndex, sourceVersionElementIndex);

        assertEquals("1.3.3", result);
    }
}
