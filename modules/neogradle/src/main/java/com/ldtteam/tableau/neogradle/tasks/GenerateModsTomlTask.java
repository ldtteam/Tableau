package com.ldtteam.tableau.neogradle.tasks;

import com.ldtteam.tableau.dependencies.model.ModDependency;
import com.ldtteam.tableau.neogradle.model.ModsTomlDependency;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;

/**
 * Task for automatically generating the neoforge.mods.toml based on input data.
 */
public abstract class GenerateModsTomlTask extends DefaultTask {

    /**
     * Creates a new task instance.
     */
    public GenerateModsTomlTask() {
        setGroup("neogradle");
        setDescription("Generate the neoforge.mods.toml file");

        final Provider<RegularFile> outputFile = getProject().getLayout().getBuildDirectory().dir(getName()).map(dir -> dir.file("neoforge.mods.toml"));
        getOutputFile().convention(outputFile);
        getOutputs().file(outputFile);
    }

    /**
     * Executes the task and generates the neoforge.mods.toml file.
     *
     * @throws IOException when a failure occurs during the attempt to write the text to the file.
     */
    @TaskAction
    public void generateModsToml() throws IOException {
        final File modsTomlFile = getOutputFile().get().getAsFile();
        if (modsTomlFile.exists()) {
            if (!modsTomlFile.delete()) {
                getLogger().warn("Could not delete old neoforge.mods.toml, will keep using old file.");
                return;
            }
        }

        try (final BufferedWriter writer = Files.newBufferedWriter(modsTomlFile.toPath(), StandardOpenOption.CREATE_NEW)) {
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
            writeDependencies(writer, getRequiredDependencies().get(), true);
            writeDependencies(writer, getOptionalDependencies().get(), false);
        }
    }

    /**
     * Write the list of dependencies to the output of the neoforge.mods.toml from runtime dependencies.
     *
     * @param writer       The buffered writer.
     * @param dependencies The set of dependencies.
     * @param required     Whether these are required or optional dependencies.
     * @throws IOException If an exception is thrown during writing.
     */
    private void writeDependencies(final BufferedWriter writer, final Set<ModDependency> dependencies, final boolean required) throws IOException {
        for (final ModDependency dependency : dependencies) {
            writeDependency(writer, new ModsTomlDependency(dependency.modId(), dependency.versionRange(), required, Ordering.AFTER));
        }
    }

    /**
     * Write the header to the output of the neoforge.mods.toml.
     *
     * @param writer The buffered writer.
     * @throws IOException If an exception is thrown during writing.
     */
    private void writeHeader(final BufferedWriter writer) throws IOException {
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
    private void writeDependency(final BufferedWriter writer, final ModsTomlDependency dependency) throws IOException {
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
    private void writeLine(final BufferedWriter writer, final String content) throws IOException {
        writer.write(content);
        writer.newLine();
    }

    /**
     * Get the Neoforge version.
     *
     * @return The Neoforge version.
     */
    @Input
    public abstract Property<String> getNeoforgeVersion();

    /**
     * Get the Minecraft version.
     *
     * @return The Minecraft version.
     */
    @Input
    public abstract Property<String> getMinecraftVersion();

    /**
     * Get the mod id.
     *
     * @return The mod id.
     */
    @Input
    public abstract Property<String> getModId();

    /**
     * Get the mod display name.
     *
     * @return The mod display name.
     */
    @Input
    public abstract Property<String> getModName();

    /**
     * Get the mod description.
     *
     * @return The mod description.
     */
    @Input
    public abstract Property<String> getModDescription();

    /**
     * Get the mod logo.
     *
     * @return The mod logo.
     */
    @Input
    public abstract Property<String> getModLogo();

    /**
     * Get the version of the mod.
     *
     * @return The version of the mod.
     */
    @Input
    public abstract Property<String> getModVersion();

    /**
     * Get the mod publisher.
     *
     * @return The mod publisher.
     */
    @Input
    public abstract Property<String> getPublisher();

    /**
     * Get the website for the mod.
     *
     * @return The website for the mod.
     */
    @Input
    public abstract Property<URI> getDisplayUrl();

    /**
     * Get the issue tracker url, where issues may be filed for the mod.
     *
     * @return The issue tracker url, where issues may be filed for the mod.
     */
    @Input
    public abstract Property<URI> getIssueTrackerUrl();

    /**
     * Get the license used for the mod.
     *
     * @return The license used for the mod.
     */
    @Input
    public abstract Property<String> getLicense();

    /**
     * Get the required mod dependencies.
     *
     * @return The required mod dependencies.
     */
    @Nested
    public abstract SetProperty<ModDependency> getRequiredDependencies();

    /**
     * Get the optional mod dependencies.
     *
     * @return The optional mod dependencies.
     */
    @Nested
    public abstract SetProperty<ModDependency> getOptionalDependencies();

    /**
     * Get the destination file for the mods toml.
     *
     * @return The destination file for the mods toml.
     */
    @OutputFile
    public abstract RegularFileProperty getOutputFile();
}
