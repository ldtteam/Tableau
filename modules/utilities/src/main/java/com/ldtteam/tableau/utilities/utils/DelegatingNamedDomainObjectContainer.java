package com.ldtteam.tableau.utilities.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Namer;
import org.gradle.api.Rule;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import groovy.lang.Closure;

/**
 * A delegating implementation of a {@link NamedDomainObjectContainer}
 * 
 * @param <N> The type of the contained named element.
 */
public class DelegatingNamedDomainObjectContainer<N> implements NamedDomainObjectContainer<N> {

    private final NamedDomainObjectContainer<N> delegate;
    
    /**
     * Creates a new delegating container.
     * 
     * @param delegate The delegate to call when a method of the contract interface is called.
     */
    public DelegatingNamedDomainObjectContainer(NamedDomainObjectContainer<N> delegate) {
        this.delegate = delegate;
    }

    public boolean add(N e) {
        return delegate.add(e);
    }

    public boolean addAll(Collection<? extends N> c) {
        return delegate.addAll(c);
    }

    public void addAllLater(Provider<? extends Iterable<N>> provider) {
        delegate.addAllLater(provider);
    }

    public void addLater(Provider<? extends N> provider) {
        delegate.addLater(provider);
    }

    public Rule addRule(Rule rule) {
        return delegate.addRule(rule);
    }

    public Rule addRule(String description, Closure ruleAction) {
        return delegate.addRule(description, ruleAction);
    }

    public Rule addRule(String description, Action<String> ruleAction) {
        return delegate.addRule(description, ruleAction);
    }

    public void all(Action<? super N> action) {
        delegate.all(action);
    }

    public void all(Closure action) {
        delegate.all(action);
    }

    public void forEach(Consumer<? super N> action) {
        delegate.forEach(action);
    }

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return delegate.toArray(generator);
    }

    public void clear() {
        delegate.clear();
    }

    public NamedDomainObjectContainer<N> configure(Closure configureClosure) {
        return delegate.configure(configureClosure);
    }

    public void configureEach(Action<? super N> action) {
        delegate.configureEach(action);
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public Iterator<N> iterator() {
        return delegate.iterator();
    }

    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    public N create(String name) throws InvalidUserDataException {
        return delegate.create(name);
    }

    public N create(String name, Closure configureClosure) throws InvalidUserDataException {
        return delegate.create(name, configureClosure);
    }

    public N create(String name, Action<? super N> configureAction) throws InvalidUserDataException {
        return delegate.create(name, configureAction);
    }

    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    public boolean removeIf(Predicate<? super N> filter) {
        return delegate.removeIf(filter);
    }

    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public Set<N> findAll(Closure spec) {
        return delegate.findAll(spec);
    }

    public N findByName(String arg0) {
        return delegate.findByName(arg0);
    }

    public SortedMap<String, N> getAsMap() {
        return delegate.getAsMap();
    }

    public N getAt(String name) throws UnknownDomainObjectException {
        return delegate.getAt(name);
    }

    public N getByName(String name) throws UnknownDomainObjectException {
        return delegate.getByName(name);
    }

    public N getByName(String name, Closure configureClosure) throws UnknownDomainObjectException {
        return delegate.getByName(name, configureClosure);
    }

    public N getByName(String name, Action<? super N> configureAction) throws UnknownDomainObjectException {
        return delegate.getByName(name, configureAction);
    }

    public NamedDomainObjectCollectionSchema getCollectionSchema() {
        return delegate.getCollectionSchema();
    }

    public Namer<N> getNamer() {
        return delegate.getNamer();
    }

    public SortedSet<String> getNames() {
        return delegate.getNames();
    }

    public List<Rule> getRules() {
        return delegate.getRules();
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public NamedDomainObjectSet<N> matching(Spec<? super N> spec) {
        return delegate.matching(spec);
    }

    public NamedDomainObjectSet<N> matching(Closure spec) {
        return delegate.matching(spec);
    }

    public N maybeCreate(String name) {
        return delegate.maybeCreate(name);
    }

    public NamedDomainObjectSet<N> named(Spec<String> nameFilter) {
        return delegate.named(nameFilter);
    }

    public NamedDomainObjectProvider<N> named(String name) throws UnknownDomainObjectException {
        return delegate.named(name);
    }

    public NamedDomainObjectProvider<N> named(String name, Action<? super N> configurationAction)
            throws UnknownDomainObjectException {
        return delegate.named(name, configurationAction);
    }

    public <S extends N> NamedDomainObjectProvider<S> named(String name, Class<S> type)
            throws UnknownDomainObjectException {
        return delegate.named(name, type);
    }

    public <S extends N> NamedDomainObjectProvider<S> named(String name, Class<S> type,
            Action<? super S> configurationAction) throws UnknownDomainObjectException {
        return delegate.named(name, type, configurationAction);
    }

    public Spliterator<N> spliterator() {
        return delegate.spliterator();
    }

    public Stream<N> stream() {
        return delegate.stream();
    }

    public Stream<N> parallelStream() {
        return delegate.parallelStream();
    }

    public NamedDomainObjectProvider<N> register(String name) throws InvalidUserDataException {
        return delegate.register(name);
    }

    public NamedDomainObjectProvider<N> register(String name, Action<? super N> configurationAction)
            throws InvalidUserDataException {
        return delegate.register(name, configurationAction);
    }

    public Action<? super N> whenObjectAdded(Action<? super N> action) {
        return delegate.whenObjectAdded(action);
    }

    public void whenObjectAdded(Closure action) {
        delegate.whenObjectAdded(action);
    }

    public Action<? super N> whenObjectRemoved(Action<? super N> action) {
        return delegate.whenObjectRemoved(action);
    }

    public void whenObjectRemoved(Closure action) {
        delegate.whenObjectRemoved(action);
    }

    public <S extends N> NamedDomainObjectSet<S> withType(Class<S> type) {
        return delegate.withType(type);
    }

    public <S extends N> DomainObjectCollection<S> withType(Class<S> type, Action<? super S> configureAction) {
        return delegate.withType(type, configureAction);
    }

    public <S extends N> DomainObjectCollection<S> withType(Class<S> arg0, Closure arg1) {
        return delegate.withType(arg0, arg1);
    }
}
