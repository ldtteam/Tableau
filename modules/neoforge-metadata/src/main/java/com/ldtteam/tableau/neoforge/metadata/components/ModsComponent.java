package com.ldtteam.tableau.neoforge.metadata.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Project;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.ldtteam.tableau.extensions.NeoGradleSourceSetConfigurationExtension;
import com.ldtteam.tableau.neoforge.metadata.api.IMetadataComponent;
import com.ldtteam.tableau.neoforge.metadata.components.model.Mod;
import com.ldtteam.tableau.neoforge.metadata.extensions.MetadataExtension;
import com.ldtteam.tableau.sourceset.management.extensions.SourceSetExtension.SourceSetConfiguration;
import com.ldtteam.tableau.utilities.utils.DelegatingNamedDomainObjectContainer;

/**
 * Exposed API container for mods, converts the mods and their relevant 
 */
public abstract class ModsComponent extends DelegatingNamedDomainObjectContainer<Mod> implements IMetadataComponent {

    /**
     * The name of the component.
     */
    public static final String NAME = "mods";   

    /**
     * Creates a new instance of the mods component.
     * 
     * @param project The project it belongs to.
     * @param sourceSet The sourceset it belongs to.
     */
    @Inject
    public ModsComponent(Project project, final SourceSetConfiguration sourceSet) {
        super(project.getObjects().domainObjectContainer(Mod.class, name -> {
            //When a mod is added to a source set, that sourceset should be published.
            sourceSet.getIsPublished().convention(true);

            //As soon as a mod is added to the sourceset, we include the jar as a source in all runs.
            final NeoGradleSourceSetConfigurationExtension neogradleSourceSet = NeoGradleSourceSetConfigurationExtension.get(sourceSet);
            neogradleSourceSet.getIsModSource().convention(true);

            //When a mod gets added, we enable the metadata generation on the sourceset
            final MetadataExtension metadata = MetadataExtension.get(sourceSet);
            metadata.getIsEnabled().convention(true);

            //Create the mod object.
            return project.getObjects().newInstance(Mod.class, project, sourceSet, name);
        }));
    }

    @Override
    public void write(CommentedConfig config) {
        final List<CommentedConfig> mods = new ArrayList<>();
        final Map<String, List<CommentedConfig>> dependencies = new HashMap<>();

        forEach(mod -> {
            mods.add(mod.writeMetadata());
            dependencies.put(mod.getModId(), mod.writeDependencies());
        });

        config.set("mods", mods);
        dependencies.forEach((modId, dependencyConfig) -> config.set("dependencies.%s".formatted(modId), dependencyConfig));
    }
}
