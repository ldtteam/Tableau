package com.ldtteam.tableau.neoforge.metadata.components.model;

import java.util.Locale;

import javax.inject.Inject;

import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import com.electronwill.nightconfig.core.CommentedConfig;

/**
 * Model for a dependency of a mod.
 */
public abstract class ModDependency implements Named {

    private final String modId;

    /**
     * Creates a new dependency model
     * 
     * @param modId   The mod id of the dependency.
     */
    @Inject
    public ModDependency(String modId) {
        this.modId = modId;

        this.getType().convention(Type.REQUIRED);
        this.getSide().convention(Side.BOTH);
    }

    /**
     * Writes the current model into a config that can be written to disk.
     * 
     * @return The 
     */
    CommentedConfig writeDependency() {
        final CommentedConfig config = CommentedConfig.inMemory();

        config.set("modId", getModId());
        config.set("type", getType().get().name().toLowerCase(Locale.ROOT));
        config.set("version", getVersionRange().get());

        if (getReason().isPresent()) {
            config.set("reason", getReason().get());
        }

        config.set("ordering", getOrdering().getOrElse(Ordering.NONE).name().toUpperCase(Locale.ROOT));
        config.set("side", getSide().getOrElse(Side.BOTH).name().toUpperCase(Locale.ROOT));

        return config;
    }

    /**
     * The mod id of the dependency that this models.
     * 
     * @return The mod id.
     */
    public String getModId() {
        return modId;
    }

    @Override
    public String getName() {
        return getModId();
    }

    /**
     * The type of the dependency that this defines.
     * <p>
     * This defaults to a REQUIRED dependency.
     * 
     * @return The dependency type.
     */
    @Input
    public abstract Property<Type> getType();

    /**
     * Indicates the reason for the dependency configuration.
     * <p>
     * Needs to be supplied for an INCOMPATIBLE, or DISCOURAGED dependency.
     * 
     * @return The reason for the dependency configuration.
     */
    @Input
    @Optional
    public abstract Property<String> getReason();

    /**
     * The version range that is supported by this dependency.
     * <p>
     * This field is optional.
     * 
     * @return The supported or not supported version range.
     */
    @Input
    @Optional
    public abstract Property<String> getVersionRange();

    /**
     * An optional ordering flag that determines mod loading order of this
     * dependency configuration.
     * <p>
     * This field is optional.
     * 
     * @return The ordering of the dependency.
     */
    @Input
    @Optional
    public abstract Property<Ordering> getOrdering();

    /**
     * An optional distribution indicator that determines on what distribution the
     * dependency should be considered.
     * <p>
     * Defaults to BOTH.
     * 
     * @return The side.
     */
    @Input
    @Optional
    public abstract Property<Side> getSide();

    /**
     * Defines the available dependency types.
     */
    public enum Type {
        /**
         * Marks the dependency as required, erroring out when it is not installed.
         */
        REQUIRED,

        /**
         * Marks the dependency as optional, allowing the game to run when it is not
         * installed.
         * <p>
         * Usefull for when you need to declare your mod to optionally run after
         * something else to configure compatibility.
         */
        OPTIONAL,

        /**
         * Hard incompatibility, the game does not start.
         */
        INCOMPATIBLE,

        /**
         * Soft incompatibility, the game will start with a big warning.
         */
        DISCOURAGED;
    }

    /**
     * Defines whether this mod needs to load before or after the defined
     * dependency.
     */
    public enum Ordering {
        /**
         * This mod is loaded before the dependency.
         */
        BEFORE,

        /**
         * This mod is loaded after the dependency.
         */
        AFTER,

        /**
         * Indicates that this mod has no ordering preference towards its dependency
         */
        NONE;
    }

    /**
     * Indicates whether the dependency should be considered in what distribution.
     */
    public enum Side {
        /**
         * The dependency needs to be considered both on the client as well as on the
         * dedicated server.
         */
        BOTH,

        /**
         * Indicates that this dependency needs only to be considered on the client,
         * usefull for depending on rendering mods.
         */
        CLIENT,

        /**
         * Indicates that this dependency needs only to be considered on the server,
         * usefull for depending on server plugin mods.
         */
        SERVER
    }
}