/*
 * (C) Copyright 2018-2018, by CHEN Kui and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.graph.concurrent;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Create a synchronized (thread-safe) Graph backed by the specified Graph. This Graph is designed
 * to support concurrent reads which are mutually exclusive with writes. In order to guarantee
 * serial access, it is critical that <strong>all</strong> access to the backing Graph is
 * accomplished through the created Graph.
 *
 * <p>
 * Users need to manually synchronize on edge supplier (see {@link Graph#getEdgeSupplier()}) if
 * creating an edge needs to access shared resources. Failure to follow this advice may result in
 * non-deterministic behavior.
 * </p>
 *
 * <p>
 * For all methods returning a Set, the Graph guarantees that all operations on the returned Set do
 * not affect the backing Graph. For <code>edgeSet</code> and <code>vertexSet</code> methods, the
 * returned Set is backed by the underlying graph, but when a traversal over the set is started via
 * a method such as iterator(), a snapshot of the underlying Set is copied for iteration purposes.
 * For <code>edgesOf</code>, <code>incomingEdgesOf</code> and <code>outgoingEdgesOf</code> methods,
 * the returned Set is a unmodifiable copy of the result produced by the underlying Graph. Users can
 * control whether those copies should be cached; caching may significantly increase memory
 * requirements. If users decide to cache those copies and the backing graph's changes don't affect
 * them, those copies will be returned the next time the method is called. If the backing graph's
 * changes affect them, they will be removed from cache and re-created the next time the method is
 * called. If users decide to not cache those copies, the graph will create ephemeral copies every
 * time the method is called. For other methods returning a Set, the Set is just the backing Graph's
 * return.
 * </p>
 *
 * <p>
 * As an alternative, a <em>copyless mode</em> is supported. When enabled, no collection copies are
 * made at all (and hence the cache setting is ignored). This requires the caller to explicitly
 * synchronize iteration via the {@link #getLock} method. This approach requires quite a bit of care
 * on the part of the calling application, so it is disabled by default.
 * </p>
 *
 * <p>
 * Even though this graph implementation is thread-safe, callers should still be aware of potential
 * hazards from removal methods. If calling code obtains a reference to a vertex or edge from the
 * graph, and then calls another graph method to access information about that object, an
 * {@link IllegalArgumentException} may be thrown if another thread has concurrently removed that
 * object. Therefore, calling the remove methods concurrently with a typical algorithm is likely to
 * cause the algorithm to fail with an {@link IllegalArgumentException}. So really the main
 * concurrent read/write use case is add-only. <br>
 * eg: If threadA tries to get all edges touching a certain vertex after threadB removes the vertex,
 * the algorithm will be interrupted by {@link IllegalArgumentException}.
 * </p>
 * 
 * <pre>
 * Thread threadA = new Thread(() -&gt; {
 *     Set vertices = graph.vertexSet();
 *     for (Object v : vertices) {
 *         // {@link IllegalArgumentException} may be thrown since other threads may have removed
 *         // the vertex.
 *         Set edges = graph.edgesOf(v);
 *         doOtherThings();
 *     }
 * });
 * Thread threadB = new Thread(() -&gt; {
 *     Set vertices = graph.vertexSet();
 *     for (Object v : vertices) {
 *         if (someConditions) {
 *             graph.removeVertex(v);
 *         }
 *     }
 * });
 * </pre>
 *
 * <p>
 *
 * One way to avoid the hazard noted above is for the calling application to explicitly synchronize
 * all iterations using the {@link #getLock} method.
 *
 * </p>
 *
 * <p>
 * The created Graph's hashCode is equal to the backing set's hashCode. And the created Graph is
 * equal to another Graph if they are the same Graph or the backing Graph is equal to the other
 * Graph.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author CHEN Kui
 */
public class AsSynchronizedGraph<V, E>
    extends
    GraphDelegator<V, E>
    implements
    Graph<V, E>,
    Serializable
{
    private static final long serialVersionUID = 5144561442831050752L;

    private final ReentrantReadWriteLock readWriteLock;

    // A set encapsulating backing vertexSet.
    private transient CopyOnDemandSet<V> allVerticesSet;

    // A set encapsulating backing edgeSet.
    private transient CopyOnDemandSet<E> allEdgesSet;

    private CacheStrategy<V, E> cacheStrategy;

    /**
     * Constructor for AsSynchronizedGraph with default settings (cache disabled, non-fair mode, and
     * copyless mode disabled).
     *
     * @param g the backing graph (the delegate)
     */
    public AsSynchronizedGraph(Graph<V, E> g)
    {
        this(g, false, false, false);
    }

    /**
     * Constructor for AsSynchronizedGraph with specified properties.
     *
     * @param g the backing graph (the delegate)
     * @param cacheEnable a flag describing whether a cache will be used
     * @param fair a flag describing whether fair mode will be used
     * @param copyless a flag describing whether copyless mode will be used
     */
    private AsSynchronizedGraph(Graph<V, E> g, boolean cacheEnable, boolean fair, boolean copyless)
    {
        super(g);
        readWriteLock = new ReentrantReadWriteLock(fair);
        if (copyless) {
            cacheStrategy = new NoCopy();
        } else if (cacheEnable) {
            cacheStrategy = new CacheAccess();
        } else {
            cacheStrategy = new NoCache();
        }
        allEdgesSet = new CopyOnDemandSet<>(super.edgeSet(), readWriteLock, copyless);
        allVerticesSet = new CopyOnDemandSet<>(super.vertexSet(), readWriteLock, copyless);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        readWriteLock.readLock().lock();
        try {
            return super.getAllEdges(sourceVertex, targetVertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        readWriteLock.readLock().lock();
        try {
            return super.getEdge(sourceVertex, targetVertex);
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        readWriteLock.writeLock().lock();
        try {
            E e = cacheStrategy.addEdge(sourceVertex, targetVertex);
            if (e != null)
                edgeSetModified();
            return e;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        readWriteLock.writeLock().lock();
        try {
            if (cacheStrategy.addEdge(sourceVertex, targetVertex, e)) {
                edgeSetModified();
                return true;
            }
            return false;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        readWriteLock.writeLock().lock();
        try {
            if (super.addVertex(v)) {
                vertexSetModified();
                return true;
            }
            return false;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(V sourceVertex, V targetVertex)
    {
        readWriteLock.readLock().lock();
        try {
            return super.containsEdge(sourceVertex, targetVertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(E e)
    {
        readWriteLock.readLock().lock();
        try {
            return super.containsEdge(e);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsVertex(V v)
    {
        readWriteLock.readLock().lock();
        try {
            return super.containsVertex(v);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int degreeOf(V vertex)
    {
        readWriteLock.readLock().lock();
        try {
            return super.degreeOf(vertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgeSet()
    {
        return allEdgesSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        readWriteLock.readLock().lock();
        try {
            return cacheStrategy.edgesOf(vertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        readWriteLock.readLock().lock();
        try {
            return super.inDegreeOf(vertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        readWriteLock.readLock().lock();
        try {
            return cacheStrategy.incomingEdgesOf(vertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        readWriteLock.readLock().lock();
        try {
            return super.outDegreeOf(vertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        readWriteLock.readLock().lock();
        try {
            return cacheStrategy.outgoingEdgesOf(vertex);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAllEdges(Collection<? extends E> edges)
    {
        readWriteLock.writeLock().lock();
        try {
            return super.removeAllEdges(edges);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex)
    {
        readWriteLock.writeLock().lock();
        try {
            return super.removeAllEdges(sourceVertex, targetVertex);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices)
    {
        readWriteLock.writeLock().lock();
        try {
            return super.removeAllVertices(vertices);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeEdge(E e)
    {
        readWriteLock.writeLock().lock();
        try {
            if (cacheStrategy.removeEdge(e)) {
                edgeSetModified();
                return true;
            }
            return false;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        readWriteLock.writeLock().lock();
        try {
            E e = cacheStrategy.removeEdge(sourceVertex, targetVertex);
            if (e != null)
                edgeSetModified();
            return e;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeVertex(V v)
    {
        readWriteLock.writeLock().lock();
        try {
            if (cacheStrategy.removeVertex(v)) {
                edgeSetModified();
                vertexSetModified();
                return true;
            }
            return false;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        readWriteLock.readLock().lock();
        try {
            return super.toString();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> vertexSet()
    {
        return allVerticesSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E e)
    {
        readWriteLock.readLock().lock();
        try {
            return super.getEdgeSource(e);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E e)
    {
        readWriteLock.readLock().lock();
        try {
            return super.getEdgeTarget(e);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getEdgeWeight(E e)
    {
        readWriteLock.readLock().lock();
        try {
            return super.getEdgeWeight(e);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEdgeWeight(E e, double weight)
    {
        readWriteLock.writeLock().lock();
        try {
            super.setEdgeWeight(e, weight);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Return whether the graph uses cache for <code>edgesOf</code>, <code>incomingEdgesOf</code>
     * and <code>outgoingEdgesOf</code> methods.
     * 
     * @return <tt>true</tt> if cache is in use, <tt>false</tt> if cache is not in use.
     */
    public boolean isCacheEnabled()
    {
        readWriteLock.readLock().lock();
        try {
            return cacheStrategy.isCacheEnabled();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Return whether copyless mode is used for collection-returning methods.
     * 
     * @return <tt>true</tt> if the graph uses copyless mode, <tt>false</tt> otherwise
     */
    public boolean isCopyless()
    {
        return allVerticesSet.isCopyless();
    }

    /**
     * Set the cache strategy for <code>edgesOf</code>, <code>incomingEdgesOf</code> and
     * <code>outgoingEdgesOf</code> methods.
     *
     * @param cacheEnabled a flag whether to use cache for those methods, if <tt>true</tt>, cache
     *        will be used for those methods, otherwise cache will not be used.
     * @return the AsSynchronizedGraph
     */
    public AsSynchronizedGraph<V, E> setCache(boolean cacheEnabled)
    {
        readWriteLock.writeLock().lock();
        try {
            if (cacheEnabled == isCacheEnabled())
                return this;
            if (cacheEnabled)
                cacheStrategy = new CacheAccess();
            else
                cacheStrategy = new NoCache();
            return this;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        readWriteLock.readLock().lock();
        try {
            return getDelegate().hashCode();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        readWriteLock.readLock().lock();
        try {
            return getDelegate().equals(o);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Create a unmodifiable copy of the set.
     *
     * @param set the set to be copied.
     *
     * @return a unmodifiable copy of the set
     */
    private <C> Set<C> copySet(Set<C> set)
    {
        return Collections.unmodifiableSet(new LinkedHashSet<>(set));
    }

    /**
     * Inform allVerticesSet that the backing data has been modified.
     */
    private void vertexSetModified()
    {
        allVerticesSet.modified();
    }

    /**
     * Inform allEdgesSet that the backing data has been modified.
     */
    private void edgeSetModified()
    {
        allEdgesSet.modified();
    }

    /**
     * Return whether fair mode is used for synchronizing access to this graph.
     * 
     * @return <tt>true</tt> if the graph uses fair mode, <tt>false</tt> if non-fair mode
     */
    public boolean isFair()
    {
        return readWriteLock.isFair();
    }

    /**
     * Get the read/write lock used to synchronize all access to this graph. This can be used by
     * calling applications to explicitly synchronize compound sequences of graph accessses. The
     * lock is reentrant, so the locks acquired internally by AsSynchronizedGraph will not interfere
     * with the caller's acquired lock. However, write methods <strong>MUST NOT</strong> be called
     * while holding a read lock, otherwise a deadlock will occur.
     *
     * @return the reentrant read/write lock used to synchronize all access to this graph
     */
    public ReentrantReadWriteLock getLock()
    {
        return readWriteLock;
    }

    /**
     * Create a synchronized (thread-safe) and unmodifiable Set backed by the specified Set. In
     * order to guarantee serial access, it is critical that <strong>all</strong> access to the
     * backing Set is accomplished through the created Set.
     *
     * <p>
     * When a traversal over the set is started via a method such as iterator(), a snapshot of the
     * underlying set is copied for iteration purposes (unless copyless mode is enabled).
     * </p>
     *
     * <p>
     * The created Set's hashCode is equal to the backing Set's hashCode. And the created Set is
     * equal to another set if they are the same Set or the backing Set is equal to the other Set.
     * </p>
     *
     * <p>
     * The created set will be serializable if the backing set is serializable.
     * </p>
     *
     * @param <E> the class of the objects in the set
     *
     * @author CHEN Kui
     */
    private static class CopyOnDemandSet<E>
        implements
        Set<E>,
        Serializable
    {
        private static final long serialVersionUID = 5553953818148294283L;

        // Backing set.
        private Set<E> set;

        // When this flag is set, the backing set is used directly rather than
        // a copy.
        private final boolean copyless;

        // Backing set's unmodifiable copy. If null, needs to be recomputed on next access.
        volatile private transient Set<E> copy;

        final ReadWriteLock readWriteLock;

        private static final String UNMODIFIABLE = "this set is unmodifiable";

        /**
         * Constructor for CopyOnDemandSet.
         * 
         * @param s the backing set.
         * @param readWriteLock the ReadWriteLock on which to locked
         * @param copyless whether copyless mode should be used
         */
        private CopyOnDemandSet(Set<E> s, ReadWriteLock readWriteLock, boolean copyless)
        {
            set = Objects.requireNonNull(s, "s must not be null");
            copy = null;
            this.readWriteLock = readWriteLock;
            this.copyless = copyless;
        }

        /**
         * Return whether copyless mode is used for iteration.
         * 
         * @return <tt>true</tt> if the set uses copyless mode, <tt>false</tt> otherwise
         */
        public boolean isCopyless()
        {
            return copyless;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size()
        {
            readWriteLock.readLock().lock();
            try {
                return set.size();
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty()
        {
            readWriteLock.readLock().lock();
            try {
                return set.isEmpty();
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Object o)
        {
            readWriteLock.readLock().lock();
            try {
                return set.contains(o);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * Returns an iterator over the elements in the backing set's unmodifiable copy. The
         * elements are returned in the same order of the backing set.
         *
         * @return an iterator over the elements in the backing set's unmodifiable copy.
         */
        @Override
        public Iterator<E> iterator()
        {
            return getCopy().iterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] toArray()
        {
            readWriteLock.readLock().lock();
            try {
                return set.toArray();
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T[] toArray(T[] a)
        {
            readWriteLock.readLock().lock();
            try {
                return set.toArray(a);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean add(E e)
        {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object o)
        {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsAll(Collection<?> c)
        {
            readWriteLock.readLock().lock();
            try {
                return set.containsAll(c);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addAll(Collection<? extends E> c)
        {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear()
        {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * {@inheritDoc}
         */
        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action)
        {
            readWriteLock.readLock().lock();
            try {
                set.forEach(action);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeIf(Predicate<? super E> filter)
        {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * Creates a <Code>Spliterator</code> over the elements in the set's unmodifiable copy.
         *
         * @return a <code>Spliterator</code> over the elements in the backing set's unmodifiable
         *         copy.
         */
        @Override
        public Spliterator<E> spliterator()
        {
            return getCopy().spliterator();
        }

        /**
         * Return a sequential <code>Stream</code> with the backing set's unmodifiable copy as its
         * source.
         * 
         * @return a sequential <code>Stream</code> with the backing set's unmodifiable copy as its
         *         source.
         */
        @Override
        public Stream<E> stream()
        {
            return getCopy().stream();
        }

        /**
         * Return a possibly parallel <code>Stream</code> with the backing set's unmodifiable copy
         * as its source.
         * 
         * @return a possibly parallel <code>Stream</code> with the backing set's unmodifiable copy
         *         as its source.
         */
        @Override
        public Stream<E> parallelStream()
        {
            return getCopy().parallelStream();
        }

        /**
         * Compares the specified object with this set for equality.
         * 
         * @param o object to be compared for equality with this set.
         * @return <code>true</code> if o and this set are the same object or o is equal to the
         *         backing object, false otherwise.
         */
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            readWriteLock.readLock().lock();
            try {
                return set.equals(o);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * Return the backing set's hashcode.
         * 
         * @return the backing set's hashcode.
         */
        @Override
        public int hashCode()
        {
            readWriteLock.readLock().lock();
            try {
                return set.hashCode();
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * Return the backing set's toString result.
         * 
         * @return the backing set's toString result.
         */
        @Override
        public String toString()
        {
            readWriteLock.readLock().lock();
            try {
                return set.toString();
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * Get the backing set's unmodifiable copy, or a direct reference to the backing set if in
         * copyless mode.
         *
         * @return the backing set or its unmodifiable copy
         */
        private Set<E> getCopy()
        {
            if (copyless) {
                return set;
            }
            readWriteLock.readLock().lock();
            try {
                Set<E> tempCopy = copy;
                if (tempCopy == null) {
                    synchronized (this) {
                        tempCopy = copy;
                        if (tempCopy == null) {
                            copy = tempCopy = new LinkedHashSet<>(set);
                        }
                    }
                }
                return tempCopy;
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        /**
         * If the backing set is modified, call this method to let this set knows the backing set's
         * copy need to update.
         */
        private void modified()
        {
            copy = null;
        }
    }

    /**
     * An interface for cache strategy of AsSynchronizedGraph's <code>edgesOf</code>,
     * <code>incomingEdgesOf</code> and <code>outgoingEdgesOf</code> methods.
     */
    private interface CacheStrategy<V, E>
    {
        /**
         * Add an edge into AsSynchronizedGraph's backing graph.
         */
        E addEdge(V sourceVertex, V targetVertex);

        /**
         * Add an edge into AsSynchronizedGraph's backing graph.
         */
        boolean addEdge(V sourceVertex, V targetVertex, E e);

        /**
         * Get all edges touching the specified vertex in AsSynchronizedGraph's backing graph.
         */
        Set<E> edgesOf(V vertex);

        /**
         * Get a set of all edges in AsSynchronizedGraph's backing graph incoming into the specified
         * vertex.
         */
        Set<E> incomingEdgesOf(V vertex);

        /**
         * Get a set of all edges in AsSynchronizedGraph's backing graph outgoing from the specified
         * vertex.
         */
        Set<E> outgoingEdgesOf(V vertex);

        /**
         * Remove the specified edge from AsSynchronizedGraph's backing graph.
         */
        boolean removeEdge(E e);

        /**
         * Remove an edge from AsSynchronizedGraph's backing graph.
         */
        E removeEdge(V sourceVertex, V targetVertex);

        /**
         * Remove the specified vertex from AsSynchronizedGraph's backing graph.
         */
        boolean removeVertex(V v);

        /**
         * Return whether the graph uses cache for <code>edgesOf</code>,
         * <code>incomingEdgesOf</code> and <code>outgoingEdgesOf</code> methods.
         * 
         * @return <tt>true</tt> if cache is in use, <tt>false</tt> if cache is not in use.
         */
        boolean isCacheEnabled();
    }

    /**
     * Don't use cache for AsSynchronizedGraph's <code>edgesOf</code>, <code>incomingEdgesOf</code>
     * and <code>outgoingEdgesOf</code> methods.
     */
    private class NoCache
        implements
        CacheStrategy<V, E>,
        Serializable
    {
        private static final long serialVersionUID = 19246150051213471L;

        /**
         * {@inheritDoc}
         */
        @Override
        public E addEdge(V sourceVertex, V targetVertex)
        {
            return AsSynchronizedGraph.super.addEdge(sourceVertex, targetVertex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addEdge(V sourceVertex, V targetVertex, E e)
        {
            return AsSynchronizedGraph.super.addEdge(sourceVertex, targetVertex, e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> edgesOf(V vertex)
        {
            return copySet(AsSynchronizedGraph.super.edgesOf(vertex));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> incomingEdgesOf(V vertex)
        {
            return copySet(AsSynchronizedGraph.super.incomingEdgesOf(vertex));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> outgoingEdgesOf(V vertex)
        {
            return copySet(AsSynchronizedGraph.super.outgoingEdgesOf(vertex));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeEdge(E e)
        {
            return AsSynchronizedGraph.super.removeEdge(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E removeEdge(V sourceVertex, V targetVertex)
        {
            return AsSynchronizedGraph.super.removeEdge(sourceVertex, targetVertex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeVertex(V v)
        {
            return AsSynchronizedGraph.super.removeVertex(v);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCacheEnabled()
        {
            return false;
        }
    }

    /**
     * Disable cache as per <code>NoCache</code>, and also don't produce copies; instead, just
     * directly return the results from the underlying graph. This requires the caller to explicitly
     * synchronize iterations over these collections.
     */
    private class NoCopy
        extends
        NoCache
    {
        private static final long serialVersionUID = -5046944235164395939L;

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> edgesOf(V vertex)
        {
            return AsSynchronizedGraph.super.edgesOf(vertex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> incomingEdgesOf(V vertex)
        {
            return AsSynchronizedGraph.super.incomingEdgesOf(vertex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> outgoingEdgesOf(V vertex)
        {
            return AsSynchronizedGraph.super.outgoingEdgesOf(vertex);
        }
    }

    /**
     * Use cache for AsSynchronizedGraph's <code>edgesOf</code>, <code>incomingEdgesOf</code> and
     * <code>outgoingEdgesOf</code> methods.
     */
    private class CacheAccess
        implements
        CacheStrategy<V, E>,
        Serializable
    {
        private static final long serialVersionUID = -18262921841829294L;

        // A map caching for incomingEdges operation.
        private final transient Map<V, Set<E>> incomingEdgesMap = new ConcurrentHashMap<>();

        // A map caching for outgoingEdges operation.
        private final transient Map<V, Set<E>> outgoingEdgesMap = new ConcurrentHashMap<>();

        // A map caching for edgesOf operation.
        private final transient Map<V, Set<E>> edgesOfMap = new ConcurrentHashMap<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public E addEdge(V sourceVertex, V targetVertex)
        {
            E e = AsSynchronizedGraph.super.addEdge(sourceVertex, targetVertex);
            if (e != null)
                edgeModified(sourceVertex, targetVertex);
            return e;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addEdge(V sourceVertex, V targetVertex, E e)
        {
            if (AsSynchronizedGraph.super.addEdge(sourceVertex, targetVertex, e)) {
                edgeModified(sourceVertex, targetVertex);
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> edgesOf(V vertex)
        {
            Set<E> s = edgesOfMap.get(vertex);
            if (s != null)
                return s;
            s = copySet(AsSynchronizedGraph.super.edgesOf(vertex));
            edgesOfMap.put(vertex, s);
            return s;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> incomingEdgesOf(V vertex)
        {
            Set<E> s = incomingEdgesMap.get(vertex);
            if (s != null)
                return s;
            s = copySet(AsSynchronizedGraph.super.incomingEdgesOf(vertex));
            incomingEdgesMap.put(vertex, s);
            return s;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> outgoingEdgesOf(V vertex)
        {
            Set<E> s = outgoingEdgesMap.get(vertex);
            if (s != null)
                return s;
            s = copySet(AsSynchronizedGraph.super.outgoingEdgesOf(vertex));
            outgoingEdgesMap.put(vertex, s);
            return s;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeEdge(E e)
        {
            V sourceVertex = getEdgeSource(e);
            V targetVertex = getEdgeTarget(e);
            if (AsSynchronizedGraph.super.removeEdge(e)) {
                edgeModified(sourceVertex, targetVertex);
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E removeEdge(V sourceVertex, V targetVertex)
        {
            E e = AsSynchronizedGraph.super.removeEdge(sourceVertex, targetVertex);
            if (e != null)
                edgeModified(sourceVertex, targetVertex);
            return e;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean removeVertex(V v)
        {
            if (AsSynchronizedGraph.super.removeVertex(v)) {
                edgesOfMap.clear();
                incomingEdgesMap.clear();
                outgoingEdgesMap.clear();
                return true;
            }
            return false;
        }

        /**
         * Clear the copies which the edge to be added or removed can affect.
         *
         * @param sourceVertex source vertex of the modified edge.
         * @param targetVertex target vertex of the modified edge.
         */
        private void edgeModified(V sourceVertex, V targetVertex)
        {
            outgoingEdgesMap.remove(sourceVertex);
            incomingEdgesMap.remove(targetVertex);
            edgesOfMap.remove(sourceVertex);
            edgesOfMap.remove(targetVertex);
            if (!AsSynchronizedGraph.super.getType().isDirected()) {
                outgoingEdgesMap.remove(targetVertex);
                incomingEdgesMap.remove(sourceVertex);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCacheEnabled()
        {
            return true;
        }
    }

    /**
     * A builder for {@link AsSynchronizedGraph}.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @author CHEN Kui
     */
    public static class Builder<V, E>
    {
        private boolean cacheEnable;
        private boolean fair;
        private boolean copyless;

        /**
         * Construct a new Builder with non-fair mode, cache disabled, and copyless mode disabled.
         */
        public Builder()
        {
            cacheEnable = false;
            fair = false;
            copyless = false;
        }

        /**
         * Construct a new Builder matching the settings of an existing graph.
         *
         * @param graph the graph on which to base the builder
         */
        public Builder(AsSynchronizedGraph<V, E> graph)
        {
            this.cacheEnable = graph.isCacheEnabled();
            this.fair = graph.isFair();
            this.copyless = graph.isCopyless();
        }

        /**
         * Request a synchronized graph without caching.
         *
         * @return the Builder
         */
        public Builder<V, E> cacheDisable()
        {
            cacheEnable = false;
            return this;
        }

        /**
         * Request a synchronized graph with caching.
         *
         * @return the Builder
         */
        public Builder<V, E> cacheEnable()
        {
            cacheEnable = true;
            return this;
        }

        /**
         * Return whether a cache will be used for the synchronized graph being built.
         *
         * @return <tt>true</tt> if cache will be used, <tt>false</tt> if cache will not be used
         */
        public boolean isCacheEnable()
        {
            return cacheEnable;
        }

        /**
         * Request a synchronized graph which does not return collection copies.
         *
         * @return the Builder
         */
        public Builder<V, E> setCopyless()
        {
            copyless = true;
            return this;
        }

        /**
         * Request a synchronized graph which returns collection copies.
         *
         * @return the Builder
         */
        public Builder<V, E> clearCopyless()
        {
            copyless = false;
            return this;
        }

        /**
         * Return whether copyless mode will be used for the synchronized graph being built.
         *
         * @return <tt>true</tt> if constructed as copyless, <tt>false</tt> otherwise
         */
        public boolean isCopyless()
        {
            return copyless;
        }

        /**
         * Request a synchronized graph with fair mode.
         *
         * @return the SynchronizedGraphParams
         */
        public Builder<V, E> setFair()
        {
            fair = true;
            return this;
        }

        /**
         * Request a synchronized graph with non-fair mode.
         *
         * @return the SynchronizedGraphParams
         */
        public Builder<V, E> setNonfair()
        {
            fair = false;
            return this;
        }

        /**
         * Return whether fair mode will be used for the synchronized graph being built.
         *
         * @return <tt>true</tt> if constructed as fair mode, <tt>false</tt> if non-fair
         */
        public boolean isFair()
        {
            return fair;
        }

        /**
         * Build the AsSynchronizedGraph.
         *
         * @param graph the backing graph (the delegate)
         * @return the AsSynchronizedGraph
         */
        public AsSynchronizedGraph<V, E> build(Graph<V, E> graph)
        {
            return new AsSynchronizedGraph<>(graph, cacheEnable, fair, copyless);
        }
    }
}
