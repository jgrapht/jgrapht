/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * WeightedVertexSet.java
 * -----------------
 * (C) Copyright 2016, by Joris Kinable and Contributors.
 *
 * Original Author: Joris Kinable
 * Contributor(s): Nils Olberg
 *
 */

package org.jgrapht.alg.util;

import java.util.Set;

/**
 * This class can be used to store a set of vertices together with a weight. 
 * It is used for example by algorithms for maximum weight independent sets, 
 * maximum weight cliques and minimum weight vertex covers.
 */

public class WeightedVertexSet<V> {
    protected Set<V> vertices;
    protected double weight;

    public WeightedVertexSet() {}

    public WeightedVertexSet(Set<V> vertices, double weight) {
        this.vertices = vertices;
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public Set<V> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("WeightedVertexSet(");
        builder.append(this.getWeight());
        builder.append("): ");
        builder.append(this.getVertices().toString());
        return builder.toString();
    }
}
