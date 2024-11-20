package com.ldtteam.tableau.neogradle.tasks;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.ldtteam.tableau.neogradle.model.ModsTomlDependency;
import com.ldtteam.tableau.neogradle.model.ParsedBasicModInfo;
import com.ldtteam.tableau.neogradle.model.ResolvedDependency;
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper;
import net.neoforged.neoforgespi.language.IConfigurable;
import net.neoforged.neoforgespi.language.IModInfo.Ordering;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

/**
 * Task for automatically generating the neoforge.mods.toml based on input data.
 */
public abstract class GenerateModsTomlTask extends DefaultTask
{
    public GenerateModsTomlTask()
    {
        setGroup("neogradle");
        setDescription("Generate the neoforge.mods.toml file");

        final Provider<RegularFile> outputFile = getProject().getLayout().getBuildDirectory().dir(getName()).map(dir -> dir.file("neoforge.mods.toml"));
        getOutputFile().convention(outputFile);
        getOutputs().file(outputFile);
    }

    @TaskAction
    public void generateModsToml() throws IOException
    {
        final File modsTomlFile = getOutputFile().get().getAsFile();
        if (modsTomlFile.exists())
        {
            if (!modsTomlFile.delete())
            {
                getLogger().warn("Could not delete old neoforge.mods.toml, will keep using old file.");
                return;
            }
        }

        try (final BufferedWriter writer = Files.newBufferedWriter(modsTomlFile.toPath(), StandardOpenOption.CREATE_NEW))
        {
            writeHeader(writer);
            writeLine(writer, "modLoader=\"javafml\"");
            writeLine(writer, String.format("loaderVersion=\"[%d,)\"", 4));
            writeLine(writer, String.format("license=\"%s\"", getLicense().get()));
            writeLine(writer, String.format("issueTrackerURL=\"%s\"", getIssueTrackerUrl().get()));
            writeLine(writer, "");
            writeLine(writer, "[[mods]]");
            writeLine(writer, String.format("modId=\"%s\"", getModId().get()));
            writeLine(writer, String.format("version=\"%s\"", getModVersion().get()));
            writeLine(writer, String.format("displayName=\"%s\"", getModName().get()));
            writeLine(writer, String.format("description='''%s'''", getModDescription().get()));
            writeLine(writer, String.format("logoFile=\"%s\"", getModLogo().get()));
            writeLine(writer, String.format("authors=\"%s\"", getPublisher().get()));
            writeLine(writer, String.format("displayURL=\"%s\"", getDisplayUrl().get()));
            writeLine(writer, "");

            final String mcVersion = getMinecraftVersion().get();
            final DefaultArtifactVersion artifactVersion = new DefaultArtifactVersion(mcVersion);
            final String nextMcMajorVersion = String.format("%s.%s", artifactVersion.getMajorVersion(), artifactVersion.getMinorVersion() + 1);

            writeDependency(writer, new ModsTomlDependency("neoforge", String.format("[%s,)", getNeoforgeVersion().get()), true, Ordering.NONE));
            writeDependency(writer, new ModsTomlDependency("minecraft", String.format("[%s,%s)", mcVersion, nextMcMajorVersion), true, Ordering.NONE));
            writeDependencies(writer, getRequiredDependencies().get());
            writeDependencies(writer, getOptionalDependencies().get());
        }
    }

    /**
     * Write the list of dependencies to the output of the neoforge.mods.toml.
     *
     * @param writer       The buffered writer.
     * @param dependencies The set of dependencies.
     * @throws IOException If an exception is thrown during writing.
     */
    private void writeDependencies(final BufferedWriter writer, final Set<ResolvedDependency> dependencies) throws IOException
    {
        for (final ResolvedDependency dependency : dependencies)
        {
            final List<ParsedBasicModInfo> modInfos = getModInfos(dependency.getFile());
            for (final ParsedBasicModInfo modInfo : modInfos)
            {
                writeDependency(writer, new ModsTomlDependency(modInfo.modId(), dependency.getVersionRange(), true, Ordering.AFTER));
            }
        }
    }

    /**
     * Write the header to the output of the neoforge.mods.toml.
     *
     * @param writer The buffered writer.
     * @throws IOException If an exception is thrown during writing.
     */
    private void writeHeader(final BufferedWriter writer) throws IOException
    {
        writeLine(writer, "# This file is automatically generated by Tableau.");
        writeLine(writer, "# If anything is wrong with this file and Tableau has no option to fix this,");
        writeLine(writer, "# you may disable automatic generation by writing:");
        writeLine(writer, "# ```");
        writeLine(writer, "# tableau {");
        writeLine(writer, "#     neogradle {");
        writeLine(writer, "#         autoGenerateModsToml = false");
        writeLine(writer, "#     }");
        writeLine(writer, "# }");
        writeLine(writer, "# ```");
        writeLine(writer, "# When you do this, you have to supply all the configuration manually.");
        writeLine(writer, "");
    }

    /**
     * Write a dependency block to the neoforge.mods.toml.
     *
     * @param writer The buffered writer.
     * @throws IOException If an exception is thrown during writing.
     */
    private void writeDependency(final BufferedWriter writer, final ModsTomlDependency dependency) throws IOException
    {
        writeLine(writer, String.format("[[dependencies.%s]]", getModId().get()));
        writeLine(writer, String.format("\tmodId=\"%s\"", dependency.modId()));
        writeLine(writer, String.format("\ttype=\"%s\"", dependency.required() ? "required" : "optional"));
        writeLine(writer, String.format("\tversionRange=\"%s\"", dependency.versionRange()));
        writeLine(writer, String.format("\tordering=\"%s\"", dependency.ordering()));
        writeLine(writer, "\tside=\"BOTH\"");
    }

    /**
     * Internal line write method.
     */
    private void writeLine(final BufferedWriter writer, final String content) throws IOException
    {
        writer.write(content);
        writer.newLine();
    }

    /**
     * Get a list of mod information classes for the given jar file.
     *
     * @param file The input jar file.
     * @return The list of mod info instances, or an empty list if no mods were found in the jar.
     */
    @NotNull
    private List<ParsedBasicModInfo> getModInfos(final File file)
    {
        try (final FileSystem fileSystem = FileSystems.newFileSystem(file.toPath()))
        {
            final Path path = fileSystem.getPath("META-INF/neoforge.mods.toml");
            if (!Files.exists(path))
            {
                return List.of();
            }

            final FileConfig fileConfig = FileConfig.builder(path).build();
            fileConfig.load();
            fileConfig.close();
            final TomlFormat format = TomlFormat.instance();
            final UnmodifiableCommentedConfig unmodifiableConfig = format.createParser().parse(format.createWriter().writeToString(fileConfig)).unmodifiable();
            final NightConfigWrapper configWrapper = new NightConfigWrapper(unmodifiableConfig);

            final List<ParsedBasicModInfo> modInfos = new ArrayList<>();
            for (final IConfigurable config : configWrapper.getConfigList("mods"))
            {
                final Optional<String> modId = config.getConfigElement("modId");
                final Optional<String> modVersion = config.getConfigElement("version");
                if (modId.isPresent() && modVersion.isPresent())
                {
                    getLogger().lifecycle("Obtained the mod info for jar \"{}\", found mod with modId \"{}\"", file.getName(), modId.get());
                    modInfos.add(new ParsedBasicModInfo(modId.get(), modVersion.get()));
                }
            }
            return Collections.unmodifiableList(modInfos);
        }
        catch (Exception ex)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().lifecycle("Failure obtaining the mod info for jar \"{}\", the provided jar doesn't seem to be a Neoforge mod file. Reason:", file.getName(), ex);
            }
            else
            {
                getLogger().lifecycle("Failure obtaining the mod info for jar \"{}\", the provided jar doesn't seem to be a Neoforge mod file.", file.getName());
            }
            return List.of();
        }
    }

    /**
     * @return The Neoforge version.
     */
    @Input
    public abstract Property<String> getNeoforgeVersion();

    /**
     * @return The Minecraft version.
     */
    @Input
    public abstract Property<String> getMinecraftVersion();

    /**
     * @return The mod id.
     */
    @Input
    public abstract Property<String> getModId();

    /**
     * @return The mod display name.
     */
    @Input
    public abstract Property<String> getModName();

    /**
     * @return The mod description.
     */
    @Input
    public abstract Property<String> getModDescription();

    /**
     * @return The mod logo.
     */
    @Input
    public abstract Property<String> getModLogo();

    /**
     * @return The version of the mod.
     */
    @Input
    public abstract Property<String> getModVersion();

    /**
     * @return The mod publisher.
     */
    @Input
    public abstract Property<String> getPublisher();

    /**
     * @return The website for the mod.
     */
    @Input
    public abstract Property<URI> getDisplayUrl();

    /**
     * @return The issue tracker url, where issues may be filed for the mod.
     */
    @Input
    public abstract Property<URI> getIssueTrackerUrl();

    /**
     * @return The license used for the mod.
     */
    @Input
    public abstract Property<String> getLicense();

    /**
     * @return The required mod dependencies.
     */
    @Nested
    public abstract SetProperty<ResolvedDependency> getRequiredDependencies();

    /**
     * @return The optional mod dependencies.
     */
    @Nested
    public abstract SetProperty<ResolvedDependency> getOptionalDependencies();

    /**
     * @return The destination file for the mods toml.
     */
    @OutputFile
    public abstract RegularFileProperty getOutputFile();
}
