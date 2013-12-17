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
 * AbstractExhaustiveIsomorphismInspector.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id: AbstractExhaustiveIsomorphismInspector.java 485 2006-06-26 09:12:14Z
 * perfecthash $
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.experimental.equivalence.*;
import org.jgrapht.experimental.permutation.*;
import org.jgrapht.util.*;


/**
 * Abstract base for isomorphism inspectors which exhaustively test the possible
 * mappings between graphs. The current algorithms do not support graphs with
 * multiple edges (Multigraph / Pseudograph). For the maintainer: The reason is
 * the use of GraphOrdering which currently does not support all graph types.
 *
 * @author Assaf Lehr
 * @since May 20, 2005 ver5.3
 */
abstract class AbstractExhaustiveIsomorphismInspector<V, E>
    implements GraphIsomorphismInspector<IsomorphismRelation>
{
    

    public static EquivalenceComparator<Object, Object>
        edgeDefaultIsomorphismComparator =
            new UniformEquivalenceComparator<Object, Object>();
    public static EquivalenceComparator<Object, Object>
        vertexDefaultIsomorphismComparator =
            new UniformEquivalenceComparator<Object, Object>();

    

    protected EquivalenceComparator<? super E, ? super Graph<V, ? super E>>
        edgeComparator;
    protected EquivalenceComparator<? super V, ? super Graph<? super V, E>>
        vertexComparator;

    protected Graph<V, E> graph1;
    protected Graph<V, E> graph2;

    private PrefetchIterator<IsomorphismRelation> nextSupplier;

    // kept as member, to ease computations
    private GraphOrdering lableGraph1;
    private LinkedHashSet<V> graph1VertexSet;
    private LinkedHashSet<E> graph2EdgeSet;
    private CollectionPermutationIter<V> vertexPermuteIter;
    private Set<V> currVertexPermutation; // filled every iteration, used in the

    

    // result relation.

    /**
     * @param graph1
     * @param graph2
     * @param vertexChecker eq. group checker for vertexes. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     * @param edgeChecker eq. group checker for edges. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     */
    public AbstractExhaustiveIsomorphismInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2,

        // XXX hb 060128: FOllowing parameter may need Graph<? super V,? super
        // E>
        EquivalenceComparator<? super V, ? super Graph<? super V, ? super E>> vertexChecker,
        EquivalenceComparator<? super E, ? super Graph<? super V, ? super E>> edgeChecker)
    {
        this.graph1 = graph1;
        this.graph2 = graph2;

        if (vertexChecker != null) {
            this.vertexComparator = vertexChecker;
        } else {
            this.vertexComparator = vertexDefaultIsomorphismComparator;
        }

        // Unlike vertexes, edges have better performance, when not tested for
        // Equivalence, so if the user did not supply one, use null
        // instead of edgeDefaultIsomorphismComparator.

        if (edgeChecker != null) {
            this.edgeComparator = edgeChecker;
        }

        init();
    }

    /**
     * Constructor which uses the default comparators.
     *
     * @param graph1
     * @param graph2
     *
     * @see #AbstractExhaustiveIsomorphismInspector(Graph,Graph,EquivalenceComparator,EquivalenceComparator)
     */
    public AbstractExhaustiveIsomorphismInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2)
    {
        this(
            graph1,
            graph2,
            edgeDefaultIsomorphismComparator,
            vertexDefaultIsomorphismComparator);
    }

    

    /**
     * Inits needed data-structures , among them:
     * <li>LabelsGraph which is a created in the image of graph1
     * <li>vertexPermuteIter which is created after the vertexes were divided to
     * equivalence groups. This saves order-of-magnitude in performance, because
     * the number of possible permutations dramatically decreases.
     *
     * <p>for example: if the eq.group are even/odd - only two groups. A graph
     * with consist of 10 nodes of which 5 are even , 5 are odd , will need to
     * test 5!*5! (14,400) instead of 10! (3,628,800).
     *
     * <p>besides the EquivalenceComparator`s supplied by the user, we also use
     * predefined topological comparators.
     */
    private void init()
    {
        this.nextSupplier =
            new PrefetchIterator<IsomorphismRelation>(

                // XXX hb 280106: I don't understand this warning, yet :-)
                new NextFunctor());

        this.graph1VertexSet = new LinkedHashSet<V>(this.graph1.vertexSet());

        // vertexPermuteIter will be null, if there is no match
        this.vertexPermuteIter =
            createPermutationIterator(
                this.graph1VertexSet,
                this.graph2.vertexSet());

        this.lableGraph1 =
            new GraphOrdering<V, E>(
                this.graph1,
                this.graph1VertexSet,
                this.graph1.edgeSet());

        this.graph2EdgeSet = new LinkedHashSet<E>(this.graph2.edgeSet());
    }

    /**
     * Creates the permutation iterator for vertexSet2 . The subclasses may make
     * either cause it to depend on equality groups or use vertexSet1 for it.
     *
     * @param vertexSet1 [i] may be reordered
     * @param vertexSet2 [i] may not.
     *
     * @return permutation iterator
     */
    protected abstract CollectionPermutationIter<V> createPermutationIterator(
        Set<V> vertexSet1,
        Set<V> vertexSet2);

    /**
     * <p>1. Creates a LabelsGraph of graph1 which will serve as a source to all
     * the comparisons which will follow.
     *
     * <p>2. extract the edge array of graph2; it will be permanent too.
     *
     * <p>3. for each permutation of the vertexes of graph2, test :
     *
     * <p>3.1. vertices
     *
     * <p>3.2. edges (in labelsgraph)
     *
     * <p>Implementation Notes and considerations: Let's consider a trivial
     * example: graph of strings "A","B","C" with two edges A->B,B->C. Let's
     * assume for this example that the vertex comparator always returns true,
     * meaning String value does not matter, only the graph structure does. So
     * "D" "E" "A" with D->E->A will be isomorphic , but "A","B,"C"with
     * A->B,A->C will not.
     *
     * <p>First let's extract the important info for isomorphism from the graph.
     * We don't care what the vertexes are, we care that there are 3 of them
     * with edges from first to second and from second to third. So the source
     * LabelsGraph will be: vertexes:[1,2,3] edges:[[1->2],[2->3]] Now we will
     * do several permutations of D,E,A. A few examples: D->E , E->A
     * [1,2,3]=[A,D,E] so edges are: 2->3 , 3->1 . does it match the source? NO.
     * [1,2,3]=[D,A,E] so edges are: 1->3 , 3->2 . no match either.
     * [1,2,3]=[D,E,A] so edges are: 1->2 , 2->3 . MATCH FOUND ! Trivial
     * algorithm: We will iterate on all permutations
     * [abc][acb][bac][bca][cab][cba]. (n! of them,3!=6) For each, first compare
     * vertexes using the VertexComparator(always true). Then see that the edges
     * are in the exact order 1st->2nd , 2nd->3rd. If we found a match stop and
     * return true, otherwise return false; we will compare vetices and edges by
     * their order (1st,2nd,3rd,etc) only. Two graphs are the same, by this
     * order, if: 1. for each i, sourceVertexArray[i] is equivalent to
     * targetVertexArray[i] 2. for each vertex, the edges which start in it (it
     * is the source) goes to the same ordered vertex. For multiple ones, count
     * them too.
     *
     * @return IsomorphismRelation for a permutation found, or null if no
     * permutation was isomorphic
     */
    private IsomorphismRelation<V, E> findNextIsomorphicGraph()
    {
        boolean result = false;
        IsomorphismRelation<V, E> resultRelation = null;
        if (this.vertexPermuteIter != null) {
            // System.out.println("Souce  LabelsGraph="+this.lableGraph1);
            while (this.vertexPermuteIter.hasNext()) {
                currVertexPermutation = this.vertexPermuteIter.getNextSet();

                // compare vertexes
                if (!areVertexSetsOfTheSameEqualityGroup(
                        this.graph1VertexSet,
                        currVertexPermutation))
                {
                    continue; // this one is not iso, so try the next one
                }

                // compare edges
                GraphOrdering<V, E> currPermuteGraph =
                    new GraphOrdering<V, E>(
                        this.graph2,
                        currVertexPermutation,
                        this.graph2EdgeSet);

                // System.out.println("target LablesGraph="+currPermuteGraph);
                if (this.lableGraph1.equalsByEdgeOrder(currPermuteGraph)) {
                    // create result object.
                    resultRelation =
                        new IsomorphismRelation<V, E>(
                            new ArrayList<V>(graph1VertexSet),
                            new ArrayList<V>(currVertexPermutation),
                            graph1,
                            graph2);

                    // if the edge comparator exists, check equivalence by it
                    boolean edgeEq =
                        areAllEdgesEquivalent(
                            resultRelation,
                            this.edgeComparator);
                    if (edgeEq) // only if equivalent

                    {
                        result = true;
                        break;
                    }
                }
            }
        }

        if (result == true) {
            return resultRelation;
        } else {
            return null;
        }
    }

    /**
     * Will be called on every two sets of vertexes returned by the permutation
     * iterator. From findNextIsomorphicGraph(). Should make sure that the two
     * sets are euqivalent. Subclasses may decide to implements it as an always
     * true methods only if they make sure that the permutationIterator will
     * always be already equivalent.
     *
     * @param vertexSet1 FIXME Document me
     * @param vertexSet2 FIXME Document me
     */
    protected abstract boolean areVertexSetsOfTheSameEqualityGroup(
        Set<V> vertexSet1,
        Set<V> vertexSet2);

    /**
     * For each edge in g1, get the Correspondence edge and test the pair.
     *
     * @param resultRelation
     * @param edgeComparator if null, always return true.
     */
    protected boolean areAllEdgesEquivalent(
        IsomorphismRelation<V, E> resultRelation,
        EquivalenceComparator<? super E, ? super Graph<V, E>> edgeComparator)
    {
        boolean checkResult = true;

        if (edgeComparator == null) {
            // nothing to check
            return true;
        }

        try {
            Set<E> edgeSet = this.graph1.edgeSet();

            for (E currEdge : edgeSet) {
                E correspondingEdge =
                    resultRelation.getEdgeCorrespondence(currEdge, true);

                // if one edge test fail , fail the whole method
                if (!edgeComparator.equivalenceCompare(
                        currEdge,
                        correspondingEdge,
                        this.graph1,
                        this.graph2))
                {
                    checkResult = false;
                    break;
                }
            }
        } catch (IllegalArgumentException illegal) {
            checkResult = false;
        }

        return checkResult;
    }

    /**
     * return nextElement() casted as IsomorphismRelation
     */
    public IsomorphismRelation nextIsoRelation()
    {
        return next();
    }

    /**
     * Efficiency: The value is known after the first check for isomorphism
     * activated on this class and returned there after in O(1). If called on a
     * new ("virgin") class, it activates 1 iso-check.
     *
     * @return <code>true</code> iff the two graphs are isomorphic
     */
    public boolean isIsomorphic()
    {
        return !(this.nextSupplier.isEnumerationStartedEmpty());
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#hasMoreElements()
     */
    public boolean hasNext()
    {
        boolean result = this.nextSupplier.hasMoreElements();

        return result;
    }

    /**
     * @see java.util.Iterator#next()
     */
    public IsomorphismRelation next()
    {
        return this.nextSupplier.nextElement();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
        throw new UnsupportedOperationException(
            "remove() method is not supported in AdaptiveIsomorphismInspectorFactory."
            + " There is no meaning to removing an isomorphism result.");
    }

    

    private class NextFunctor
        implements PrefetchIterator.NextElementFunctor<IsomorphismRelation>
    {
        public IsomorphismRelation nextElement()
            throws NoSuchElementException
        {
            IsomorphismRelation resultRelation = findNextIsomorphicGraph();
            if (resultRelation != null) {
                return resultRelation;
            } else {
                throw new NoSuchElementException(
                    "IsomorphismInspector does not have any more elements");
            }
        }
    }
}

// End AbstractExhaustiveIsomorphismInspector.java
