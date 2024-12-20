package com.ldtteam.tableau.utilities.utils;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SupportsConvention;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;

import groovy.lang.Closure;

/**
 * A {@link ConfigurableFileCollection} that delegates to an inner collection, allowing for partial overriding of functionality,
 * or when desired extension of custom functionality onto them.
 */
public class DelegatingConfigurableFileCollection implements ConfigurableFileCollection {

    private final ConfigurableFileCollection delegate;

    /**
     * Creates a new delegating collection.
     * <p>
     * All calls to methods of {@link ConfigurableFileCollection} are by default delegated to the given collection.
     * 
     * @param delegate The collection to delegate all method calls to by default.
     */
    public DelegatingConfigurableFileCollection(ConfigurableFileCollection delegate) {
        this.delegate = delegate;
    }

    public Object addToAntBuilder(Object builder, String nodeName) {
        return delegate.addToAntBuilder(builder, nodeName);
    }

    public void addToAntBuilder(Object builder, String nodeName, AntType type) {
        delegate.addToAntBuilder(builder, nodeName, type);
    }

    public ConfigurableFileCollection builtBy(Object... tasks) {
        return delegate.builtBy(tasks);
    }

    public boolean contains(File file) {
        return delegate.contains(file);
    }

    public ConfigurableFileCollection convention(Iterable<?> arg0) {
        return delegate.convention(arg0);
    }

    public ConfigurableFileCollection convention(Object... arg0) {
        return delegate.convention(arg0);
    }

    public void disallowChanges() {
        delegate.disallowChanges();
    }

    public void disallowUnsafeRead() {
        delegate.disallowUnsafeRead();
    }

    public FileCollection filter(Closure filterClosure) {
        return delegate.filter(filterClosure);
    }

    public FileCollection filter(Spec<? super File> filterSpec) {
        return delegate.filter(filterSpec);
    }

    public void finalizeValue() {
        delegate.finalizeValue();
    }

    public void finalizeValueOnRead() {
        delegate.finalizeValueOnRead();
    }

    public void forEach(Consumer<? super File> action) {
        delegate.forEach(action);
    }

    public ConfigurableFileCollection from(Object... paths) {
        return delegate.from(paths);
    }

    public FileTree getAsFileTree() {
        return delegate.getAsFileTree();
    }

    public String getAsPath() {
        return delegate.getAsPath();
    }

    public TaskDependency getBuildDependencies() {
        return delegate.getBuildDependencies();
    }

    public Set<Object> getBuiltBy() {
        return delegate.getBuiltBy();
    }

    public Provider<Set<FileSystemLocation>> getElements() {
        return delegate.getElements();
    }

    public Set<File> getFiles() {
        return delegate.getFiles();
    }

    public Set<Object> getFrom() {
        return delegate.getFrom();
    }

    public File getSingleFile() throws IllegalStateException {
        return delegate.getSingleFile();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Iterator<File> iterator() {
        return delegate.iterator();
    }

    public FileCollection minus(FileCollection collection) {
        return delegate.minus(collection);
    }

    public FileCollection plus(FileCollection collection) {
        return delegate.plus(collection);
    }

    public ConfigurableFileCollection setBuiltBy(Iterable<?> tasks) {
        return delegate.setBuiltBy(tasks);
    }

    public void setFrom(Iterable<?> paths) {
        delegate.setFrom(paths);
    }

    public void setFrom(Object... paths) {
        delegate.setFrom(paths);
    }

    public Spliterator<File> spliterator() {
        return delegate.spliterator();
    }

    public SupportsConvention unset() {
        return delegate.unset();
    }

    public SupportsConvention unsetConvention() {
        return delegate.unsetConvention();
    }
}
