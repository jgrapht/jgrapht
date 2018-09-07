/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.interfaces;

import java.io.*;
import java.util.*;

/**
 * An algorithm which computes a graph vertex coloring.
 *
 * @param <V> the graph vertex type
 */
public interface VertexColoringAlgorithm<V>
{

    /**
     * Computes a vertex coloring.
     *
     * @return a vertex coloring
     */
    Coloring<V> getColoring();

    /**
     * A coloring. The colors are between 0 and $n-1$ where $n$ is the number of vertices of the
     * graph.
     *
     * @param <V> the graph vertex type
     */
    interface Coloring<V>
    {
        /**
         * Get the number of colors.
         * 
         * @return the number of colors
         */
        int getNumberColors();

        /**
         * Get the color map.
         * 
         * @return the color map
         */
        Map<V, Integer> getColors();

        /**
         * Get the color classes. A subset of vertices assigned to the same color is called a color
         * class; every such class forms an independent set. This method returns a partitioning of
         * the vertices in the graph in disjoint color classes.
         *
         * @return a list of color classes
         */
        List<Set<V>> getColorClasses();
    }

    /**
     * Default implementation of the coloring interface.
     *
     * @param <V> the graph vertex type
     */
    class ColoringImpl<V>
        implements
        Coloring<V>,
        Serializable
    {
        private static final long serialVersionUID = -8456580091672353150L;

        private final int numberColors;
        private final Map<V, Integer> colors;

        /**
         * Construct a new vertex coloring.
         *
         * @param colors the color map
         * @param numberColors the total number of colors used
         */
        public ColoringImpl(Map<V, Integer> colors, int numberColors)
        {
            this.numberColors = numberColors;
            this.colors = colors;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getNumberColors()
        {
            return numberColors;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<V, Integer> getColors()
        {
            return colors;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Set<V>> getColorClasses()
        {
            Map<Integer, Set<V>> groups = new HashMap<>();
            colors.forEach((v, color) -> {
                Set<V> g = groups.computeIfAbsent(color, k -> new HashSet<>());
                g.add(v);
            });
            List<Set<V>> classes = new ArrayList<>(numberColors);
            classes.addAll(groups.values());
            return classes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Coloring [number-of-colors=" + numberColors + ", colors=" + colors + "]";
        }
    }

}
