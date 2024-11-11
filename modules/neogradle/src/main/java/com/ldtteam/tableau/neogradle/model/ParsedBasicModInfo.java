package com.ldtteam.tableau.neogradle.model;

/**
 * Basic mod information parsed directly from the jar.
 *
 * @param modId   The mod identifier.
 * @param version The version of the mod.
 */
public record ParsedBasicModInfo(String modId, String version)
{
}
