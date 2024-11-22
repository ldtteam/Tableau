package com.ldtteam.tableau.jetbrains.annotations.utils;

import org.gradle.api.provider.Provider;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Utility class for providers.
 */
public class ProviderUtils {

    /**
     * Private constructor to hide the implicit public one.
     */
    private ProviderUtils() {
    }

    /**
     * Creates a conditional provider that returns the input provider if the condition is true, otherwise it returns the empty provider.
     *
     * @param input The input provider.
     * @param condition The condition provider.
     * @param empty The empty provider.
     * @return The conditional provider.
     * @param <T> The type of the collection elements.
     * @param <C> The type of the collection.
     */
    public static <T, C extends Collection<T>> Provider<C> conditionalCollection(Provider<C> input, Provider<Boolean> condition, Supplier<C> empty) {
        return input.zip(condition, (c, b) -> b ? c : empty.get());
    }
}
