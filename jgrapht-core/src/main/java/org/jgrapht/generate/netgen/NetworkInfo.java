/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.generate.netgen;

import java.util.*;

/**
 * Represents network auxiliary information. This information is produced by the
 * {@link NetworkGenerator}.
 * <p>
 * Using the network information instance, you can find out:
 * <ul>
 * <li>Which network vertices belong to which class.</li>
 * <li>Which network arcs belong to the skeleton network.</li>
 * </ul>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see NetworkGenerator
 */
public class NetworkInfo<V, E>
{
    /**
     * Network configuration.
     */
    NetworkGeneratorConfig config;
    /**
     * List of network vertices.
     */
    List<V> vertices;
    /**
     * List of network skeleton arcs.
     */
    List<E> skeletonArcs;

    /**
     * Creates a new network information instance.
     *
     * @param config network configuration.
     */
    NetworkInfo(NetworkGeneratorConfig config)
    {
        this.config = config;
        this.vertices = new ArrayList<>();
        this.skeletonArcs = new ArrayList<>();
    }

    /**
     * Saves information about the arc {@code chainArc}.
     *
     * @param chainArc chain arc.
     */
    void registerChainArc(E chainArc)
    {
        skeletonArcs.add(chainArc);
    }

    /**
     * Returns a list containing network pure sources.
     *
     * @return a list containing network pure sources.
     */
    public List<V> getPureSources()
    {
        return Collections.unmodifiableList(vertices.subList(0, config.getPureSourceNum()));
    }

    /**
     * Returns a list containing network t-sources.
     *
     * @return a list containing network t-sources.
     */
    public List<V> getTransshipmentSources()
    {
        return Collections
            .unmodifiableList(vertices.subList(config.getPureSourceNum(), config.getSourceNum()));
    }

    /**
     * Returns a list containing network sources (pure sources + t-sources).
     *
     * @return a list containing network sources.
     */
    public List<V> getSources()
    {
        return Collections.unmodifiableList(vertices.subList(0, config.getSourceNum()));
    }

    /**
     * Returns a list containing network t-nodes.
     *
     * @return a list containing network t-nodes.
     */
    public List<V> getTransshipmentNodes()
    {
        return Collections
            .unmodifiableList(
                vertices
                    .subList(
                        config.getSourceNum(),
                        config.getSourceNum() + config.getTransshipNodeNum()));
    }

    /**
     * Returns a list containing network pure sinks.
     *
     * @return a list containing network pure sinks.
     */
    public List<V> getPureSinks()
    {
        return Collections
            .unmodifiableList(
                vertices
                    .subList(config.getNodeNum() - config.getPureSinkNum(), config.getNodeNum()));
    }

    /**
     * Return a list containing network t-sinks.
     *
     * @return a list containing network t-sinks.
     */
    public List<V> getTransshipmentSinks()
    {
        return Collections
            .unmodifiableList(
                vertices
                    .subList(
                        config.getNodeNum() - config.getSinkNum(),
                        config.getNodeNum() - config.getPureSinkNum()));
    }

    /**
     * Returns a list containing network sinks (pure sinks + t-sinks).
     *
     * @return a list containing network sinks.
     */
    public List<V> getSinks()
    {
        return Collections
            .unmodifiableList(
                vertices.subList(config.getNodeNum() - config.getSinkNum(), config.getNodeNum()));
    }

    /**
     * Return a list of network skeleton arcs.
     *
     * @return a list of network skeleton arcs.
     */
    public List<E> getSkeletonArcs()
    {
        return Collections.unmodifiableList(skeletonArcs);
    }
}
