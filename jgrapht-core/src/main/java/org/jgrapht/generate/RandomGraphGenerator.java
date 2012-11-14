/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * RandomGraphGenerator.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.generate;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * This Generator creates a random-topology graph of a specified number of
 * vertexes and edges. An instance of this generator will always return the same
 * graph-topology in calls to generateGraph(). The vertexes can be different
 * (depends on the VertexFactory implementation)
 *
 * <p>However, two instances which use the same constructor parameters will
 * produce two different random graphs (note: as with any random generator,
 * there is always a small possibility that two instances will create the same
 * results).
 *
 * @author Assaf Lehr
 * @since Aug 6, 2005
 */
public class RandomGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    //~ Static fields/initializers ---------------------------------------------

    private static long seedUniquifier = 8682522807148012L;

    //~ Instance fields --------------------------------------------------------

    protected int numOfVertexes;
    protected int numOfEdges;
    protected Random randomizer;
    private final long randomizerSeed;

    //~ Constructors -----------------------------------------------------------

    public RandomGraphGenerator(final int aNumOfVertexes, final int aNumOfEdges)
    {
        if (aNumOfVertexes < 0 || aNumOfEdges < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }
        numOfVertexes = aNumOfVertexes;
        numOfEdges = aNumOfEdges;

        randomizerSeed = chooseRandomSeedOnce();
        randomizer = new Random(randomizerSeed);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Should be called only once on creation. Chooses a seed which can be used
     * later to reset the randomizer before each method call. This
     * implementation copies the java.util.Random constructor because there is
     * no getSeed() there, and seed is protected.
     *
     * @author Assaf
     * @since Aug 6, 2005
     */
    private synchronized static long chooseRandomSeedOnce()
    {
        return ++seedUniquifier + System.nanoTime();
    }

    /**
     * Resets seed to generate the same random stream.
     */
    private void resetRandomSeed()
    {
        randomizer.setSeed(randomizerSeed);
    }

    /**
     * (non-Javadoc)
     *
     * @throws IllegalArgumentException if the aNumOfEdges passed in the
     * constructor, cannot be created on a graph of the concrete type with
     * aNumOfVertexes.
     * org.jgrapht.generate.RandomGraphGenerator.DefaultEdgeTopologyFactory#isNumberOfEdgesValid(org.jgrapht.Graph,
     * int)
     *
     * @see GraphGenerator#generateGraph(Graph, VertexFactory, Map)
     */
    @Override
    public void generateGraph(
        final Graph<V, E> target,
        final VertexFactory<V> vertexFactory,
        final Map<String, V> resultMap)
    {
        resetRandomSeed();

        // key = generation order (1st,2nd,3rd,...) value=vertex Object
        // will be used later
        final Map<Integer, V> orderToVertexMap =
            new HashMap<Integer, V>(numOfVertexes);

        for (int i = 0; i < numOfVertexes; i++) {
            final V currVertex = vertexFactory.createVertex();
            target.addVertex(currVertex);
            orderToVertexMap.put(Integer.valueOf(i), currVertex);
        }

        if (target.vertexSet().size() != numOfVertexes) {
            throw new IllegalArgumentException(
                "Vertex factory did not produce " + numOfVertexes
                + " distinct vertices.");
        }

        // use specific type of edge factory, depending of the graph type
        // and edge density
        final EdgeTopologyFactory<V, E> edgesFactory =
            edgeTopologyFactoryChooser(target, numOfEdges);
        if (!edgesFactory.isNumberOfEdgesValid(target, numOfEdges)) {
            throw new IllegalArgumentException(
                "numOfEdges is not valid for the graph type "
                + "\n-> Invalid number Of Edges=" + numOfEdges + " for:"
                + " graph type=" + target.getClass()
                + " ,number Of Vertexes=" + numOfVertexes
                + "\n-> Advice: For the Max value , check the javadoc for"
                + " org.jgrapht.generate.RandomGraphGenerator.DefaultEdgeTopologyFactory");
        }

        edgesFactory.createEdges(
            target,
            orderToVertexMap, numOfEdges, randomizer);
    }

    /**
     * Returns a concrete EdgeTopologyFactory, depending on graph type and
     * numOfEdges
     *
     * @param target
     *
     * @return
     */
    private EdgeTopologyFactory<V, E> edgeTopologyFactoryChooser(
        final Graph<V, E> target,
        final int numOfEdges)
    {
        return new DefaultEdgeTopologyFactory<V, E>();
    }

    //~ Inner Interfaces -------------------------------------------------------

    /**
     * This class is used to generate the edge topology for a graph.
     *
     * @author Assaf
     * @since Aug 6, 2005
     */
    public interface EdgeTopologyFactory<VV, EE>
    {
        /**
         * Two different calls to the createEdges() with the same parameters
         * must result in the generation of the same. But if the randomizer is
         * different, it should, usually, create different edge topology.
         *
         * @param targetGraph - guranteed to start with zero edges.
         * @param orderToVertexMap - key=Integer of vertex order . between zero
         * to numOfVertexes (exclusive). value = vertex from the graph. unique.
         * @param numberOfEdges - to create in the graph
         * @param randomizer
         */
        void createEdges(
            Graph<VV, EE> targetGraph,
            Map<Integer, VV> orderToVertexMap,
            int numberOfEdges,
            Random randomizer);

        /**
         * Checks if the graph can contain the givven numberOfEdges according to
         * the graph type restrictions. For example: <i>#V means number of
         * vertexes in graph
         * <li>a Simple Graph, can have max of #V*(#V-1)/2 edges. etc
         *
         * @param targetGraph guranteed to start with zero edges.
         * @param numberOfEdges
         */
        boolean isNumberOfEdgesValid(
            Graph<VV, EE> targetGraph,
            int numberOfEdges);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Default implementation of the EdgeTopologyFactory interface. randomly
     * chooses an edge and tries to add it. If the add fails from any reason
     * (like: self edge / multiple edges in unpermitted graph type) it will just
     * choose another and try again. Performance:
     * <li>when the number of possible edges becomes slim , this class will have
     * a very poor performance , cause it will not use gready methods to choose
     * them. for example : In simple graph , if #V = N (#x = number Of x) and we
     * want full mesh #edges= N*(N-1)/2 , the first added edges will do so
     * quickly (O(1) , the last will take O(N^2). So , do not use it in this
     * kind of graphs.
     * <li>If the numberOfEdges is bigger than what the graph can add, there
     * will be an infinite loop here. It is not tested.
     *
     * @author Assaf
     * @since Aug 6, 2005
     */
    public class DefaultEdgeTopologyFactory<VV, EE>
        implements EdgeTopologyFactory<VV, EE>
    {
        @Override
        public void createEdges(
            final Graph<VV, EE> targetGraph,
            final Map<Integer, VV> orderToVertexMap,
            final int numberOfEdges,
            final Random randomizer)
        {
            int iterationsCounter = 0;
            int edgesCounter = 0;
            while (edgesCounter < numberOfEdges) {
                // randomizer.nextInt(int n) return a number between zero
                // (inclusive) and n(exclusive)
                final VV startVertex =
                    orderToVertexMap.get(
                        Integer.valueOf(randomizer.nextInt(numOfVertexes)));
                final VV endVertex =
                    orderToVertexMap.get(
                        Integer.valueOf(randomizer.nextInt(numOfVertexes)));
                try {
                    final EE resultEdge = targetGraph.addEdge(startVertex, endVertex);
                    if (resultEdge != null) {
                        edgesCounter++;
                    }
                } catch (Exception e) {
                    // do nothing.just ignore the edge
                }

                iterationsCounter++;
            }
        }

        /**
         * checks if the numOfEdges is smaller than the Max edges according to
         * the following table:
         *
         * <p>
         * <table border=1 cellpadding=5>
         * <tr align="center">
         * <th>Graph Type</th>
         * <th><i>Directed / UnDirected</i></th>
         * <th><i>multiple edges</i></th>
         * <th><i>loops</i></th>
         * <th><i>Max Edges</i></th>
         * </tr>
         * <tr align="center">
         * <td>SimpleGraph</td>
         * <td>UnDirected</td>
         * <td>-</td>
         * <td>-</td>
         * <td>N(N-1)/2</td>
         * </tr>
         * <tr align="center">
         * <td>Multigraph</td>
         * <td>UnDirected</td>
         * <td>+</td>
         * <td>-</td>
         * <td>Infinite</td>
         * </tr>
         * <tr align="center">
         * <td>Pseudograph</td>
         * <td>UnDirected</td>
         * <td>+</td>
         * <td>+</td>
         * <td>Infinite</td>
         * </tr>
         * <tr align="center">
         * <td>SimpleDirectedGraph</td>
         * <td>Directed</td>
         * <td>-</td>
         * <td>-</td>
         * <td>N (N-1)</td>
         * </tr>
         * <tr align="center">
         * <td>DefaultDirectedGraph</td>
         * <td>Directed</td>
         * <td>-</td>
         * <td>+</td>
         * <td>N*(N-1)+ N = N^2</td>
         * </tr>
         * <tr align="center">
         * <td>DirectedMultigraph</td>
         * <td>Directed</td>
         * <td>+</td>
         * <td>+</td>
         * <td>Infinite</td>
         * </tr>
         * </table>
         *
         * @see RandomGraphGenerator.EdgeTopologyFactory#isNumberOfEdgesValid(Graph,
         * int)
         */
        @Override
        public boolean isNumberOfEdgesValid(
            final Graph<VV, EE> targetGraph,
            final int numberOfEdges)
        {
            final boolean result;

            boolean infinite = false;
            final int maxAllowedEdges = getMaxEdgesForVertexNum(targetGraph);
            if (maxAllowedEdges == -1) {
                infinite = true;
            }

            if (infinite) {
                result = true;
            } else
                result = numberOfEdges <= maxAllowedEdges;
            return result;
        }

        /**
         * Return max edges for that graph. If it is infinite return -1 instead.
         */
        public int getMaxEdgesForVertexNum(final Graph<VV, EE> targetGraph)
        {
            int maxAllowedEdges = 0;
            if (targetGraph instanceof SimpleGraph<?, ?>) {
                maxAllowedEdges = numOfVertexes * (numOfVertexes - 1) / 2;
            } else if (targetGraph instanceof SimpleDirectedGraph<?, ?>) {
                maxAllowedEdges = numOfVertexes * (numOfVertexes - 1);
            } else if (targetGraph instanceof DefaultDirectedGraph<?, ?>) {
                maxAllowedEdges = numOfVertexes * numOfVertexes;
            } else {
                // This may be overly liberal in the case of something
                // like a simple graph which has been wrapped with
                // a graph adapter or view.
                maxAllowedEdges = -1; // infinite
            }
            return maxAllowedEdges;
        }
    }
}

// End RandomGraphGenerator.java
