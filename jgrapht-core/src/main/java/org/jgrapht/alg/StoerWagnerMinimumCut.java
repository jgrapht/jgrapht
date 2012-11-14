/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2011, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ----------------
 * StoerWagnerMinimumCut.java
 * ----------------
 * (C) Copyright 2011-2011, by Robby McKilliam and Contributors.
 *
 * Original Author:  Robby McKilliam
 * Contributor(s):   -
 *
 * $Id: StoerWagnerMinimumCut.java $
 *
 * Changes
 * -------
 *
 */
package org.jgrapht.alg;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


/**
 * Implements the <a href="http://dl.acm.org/citation.cfm?id=263872">Stoer and
 * Wagner minimum cut algorithm</a>. Deterministically computes the minimum cut
 * in O(|V||E| + |V|log|V|) time. This implementation uses Java's PriorityQueue
 * and requires O(|V||E|log|E|) time. M. Stoer and F. Wagner, "A Simple Min-Cut
 * Algorithm", Journal of the ACM, volume 44, number 4. pp 585-591, 1997.
 *
 * @author Robby McKilliam
 */
public class StoerWagnerMinimumCut<V, E>
{
    //~ Instance fields --------------------------------------------------------

    final WeightedGraph<Set<V>, DefaultWeightedEdge> workingGraph;

    double bestcutweight = Double.POSITIVE_INFINITY;
    Set<V> bestCut;

    boolean firstRun = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Will compute the minimum cut in graph.
     *
     * @param graph graph over which to run algorithm
     */
    public StoerWagnerMinimumCut(WeightedGraph<V, E> graph)
    {
        //get a version of this graph where each vertex is wrapped with a list
        workingGraph =
            new SimpleWeightedGraph<Set<V>, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        Map<V, Set<V>> vertexMap = Maps.newHashMap();
        for (V v : graph.vertexSet()) {
            Set<V> list = Sets.newHashSet();
            list.add(v);
            vertexMap.put(v, list);
            workingGraph.addVertex(list);
        }
        for (E e : graph.edgeSet()) {
            V s = graph.getEdgeSource(e);
            Set<V> sNew = vertexMap.get(s);
            V t = graph.getEdgeTarget(e);
            Set<V> tNew = vertexMap.get(t);
            DefaultWeightedEdge eNew = workingGraph.addEdge(sNew, tNew);
            workingGraph.setEdgeWeight(eNew, graph.getEdgeWeight(e));
        }

        //arbitrary vertex used to seed the algorithm.
        Set<V> a = workingGraph.vertexSet().iterator().next();
        while (workingGraph.vertexSet().size() > 2) {
            minimumCutPhase(a);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Implements the MinimumCutPhase function of Stoer and Wagner
     */
    protected void minimumCutPhase(Set<V> a)
    {
        //construct sorted queue with vertices connected to vertex a
        PriorityQueue<VertexAndWeight> queue = Queues.newPriorityQueue();
        Map<Set<V>, VertexAndWeight> dmap = Maps.newHashMap();
        for (Set<V> v : workingGraph.vertexSet()) {
            if (v != a) {
                Double w =
                    -workingGraph.getEdgeWeight(workingGraph.getEdge(v, a));
                VertexAndWeight vandw = new VertexAndWeight(v, w);
                queue.add(vandw);
                dmap.put(v, vandw);
            }
        }

        //now iteratatively update the queue to get the required vertex ordering
        List<Set<V>> list =
            new ArrayList<Set<V>>(workingGraph.vertexSet().size());
        list.add(a);
        while (!queue.isEmpty()) {
            Set<V> v = queue.poll().vertex;
            dmap.remove(v);
            list.add(v);
            for (DefaultWeightedEdge e : workingGraph.edgesOf(v)) {
                Set<V> vc;
                if (v != workingGraph.getEdgeSource(e)) {
                    vc = workingGraph.getEdgeSource(e);
                } else {
                    vc = workingGraph.getEdgeTarget(e);
                }
                if (dmap.get(vc) != null) {
                    Double neww =
                        -workingGraph.getEdgeWeight(workingGraph.getEdge(v, vc))
                        + dmap.get(vc).weight;
                    queue.remove(dmap.get(vc)); //this is O(logn) but could be
                                                //O(1)?
                    dmap.get(vc).weight = neww;
                    queue.add(dmap.get(vc)); //this is O(logn) but could be
                                             //O(1)?
                }
            }
        }

        //if this is the first run we compute the weight of last vertex in the
        //list
        if (firstRun) {
            Set<V> v = list.get(list.size() - 1);
            double w = vertexWeight(v);
            if (w < bestcutweight) {
                bestcutweight = w;
                bestCut = v;
            }
            firstRun = false;
        }

        //the last two elements in list are the vertices we want to merge.
        Set<V> s = list.get(list.size() - 2);
        Set<V> t = list.get(list.size() - 1);

        //merge these vertices and get the weight.
        VertexAndWeight vw = mergeVertices(s, t);

        //If this is the best cut so far store it.
        if (vw.weight < bestcutweight) {
            bestcutweight = vw.weight;
            bestCut = vw.vertex;
        }
    }

    /**
     * Return the weight of the minimum cut
     */
    public double minCutWeight()
    {
        return bestcutweight;
    }

    /**
     * Return a set of vertices on one side of the cut
     */
    public Set<V> minCut()
    {
        return bestCut;
    }

    /**
     * Merges vertex t into vertex s, summing the weights as required. Returns
     * the merged vertex and the sum of its weights
     */
    protected VertexAndWeight mergeVertices(Set<V> s, Set<V> t)
    {
        //construct the new combinedvertex
        Set<V> set = new HashSet<V>();
        for (V v : s) {
            set.add(v);
        }
        for (V v : t) {
            set.add(v);
        }
        workingGraph.addVertex(set);

        //add edges and weights to the combined vertex
        double wsum = 0.0;
        for (Set<V> v : workingGraph.vertexSet()) {
            if ((s != v) && (t != v)) {
                DefaultWeightedEdge etv = workingGraph.getEdge(t, v);
                DefaultWeightedEdge esv = workingGraph.getEdge(s, v);
                double wtv = 0.0, wsv = 0.0;
                if (etv != null) {
                    wtv = workingGraph.getEdgeWeight(etv);
                }
                if (esv != null) {
                    wsv = workingGraph.getEdgeWeight(esv);
                }
                double neww = wtv + wsv;
                wsum += neww;
                if (neww != 0.0) {
                    workingGraph.setEdgeWeight(
                        workingGraph.addEdge(set, v),
                        neww);
                }
            }
        }

        //remove original vertices
        workingGraph.removeVertex(t);
        workingGraph.removeVertex(s);

        return new VertexAndWeight(set, wsum);
    }

    /**
     * Compute the sum of the weights entering a vertex
     */
    public double vertexWeight(Set<V> v)
    {
        double wsum = 0.0;
        for (DefaultWeightedEdge e : workingGraph.edgesOf(v)) {
            wsum += workingGraph.getEdgeWeight(e);
        }
        return wsum;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Class for weighted vertices
     */
    protected class VertexAndWeight
        implements Comparable<VertexAndWeight>
    {
        public Set<V> vertex;
        public Double weight;

        public VertexAndWeight(Set<V> v, double w)
        {
            this.vertex = v;
            this.weight = w;
        }

        @Override public int compareTo(VertexAndWeight that)
        {
            return Double.compare(weight, that.weight);
        }

        @Override public String toString()
        {
            return "(" + vertex + ", " + weight + ")";
        }
    }
}

// End StoerWagnerMinimumCut.java
