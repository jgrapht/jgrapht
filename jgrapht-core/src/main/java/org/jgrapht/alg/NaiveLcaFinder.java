/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://org.org.jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2013, by Barak Naveh and Contributors.
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
/* -------------------------
 * NaiveLcaFinder.java
 * -------------------------
 *
 * Original Author:  Leo Crawford
 * Contributor(s):
 *
 */

package org.jgrapht.alg;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

public class NaiveLcaFinder<V, E> {

    private DirectedGraph<V, E> graph;

    /**
     * Find the Lowest Common Ancestor of a directed graph.
     * <p>
     * Find the LCA, defined as <i>Let G = (V, E) be a DAG, and let x, y ∈ V . Let G x,y be the subgraph of G induced by
     * the set of all common ancestors of x and y. Define SLCA (x, y) to be the set of out-degree 0 nodes (leafs) in G
     * x,y . The lowest common ancestors of x and y are the elements of SLCA (x, y). This naive algorithm simply starts
     * at a and b, recursing upwards to the root(s) of the DAG. Wherever the recursion paths cross we have found our
     * LCA.</i> from http://www.cs.sunysb.edu/~bender/pub/JALG05-daglca.pdf. The algorithm:
     * 
     * <pre>
     * 1. Start at each of nodes you wish to find the lca for (a and b) 
     * 2. Create sets aSet containing a, and bSet containing b
     * 3. If either set intersects with the union of the other sets previous values (i.e. the set of notes visited) then
     *    that intersection is LCA. if there are multiple intersections then the earliest one added is the LCA. 
     * 4. Repeat from step 3, with aSet now the parents of everything in aSet, and bSet the parents of everything in bSet
     * 5. If there are no more parents to descend to then there is no LCA
     * </pre>
     * 
     * The rationale for this working is that in each iteration of the loop we are considering all the ancestors of a
     * that have a path of length n back to a, where n is the depth of the recursion. The same is true of b.
     * <p>
     * We start by checking if a == b. <br>
     * if not we look to see if there is any intersection between parents(a) and (parents(b) union b) (and the same with
     * a and b swapped) <br>
     * if not we look to see if there is any intersection between parents(parents(a)) and (parents(parents(b)) union
     * parents(b) union b) (and the same with a and b swapped) <br>
     * continues
     * <p>
     * This means at the end of recursion n, we know if there is an LCA that has a path of <=n to a and b. Of course we
     * may have to wait longer if the path to a is of length n, but the path to b>n. at the first loop we have a path of
     * 0 length from the nodes we are considering as LCA to their respective children which we wish to find the LCA for.
     */
    public NaiveLcaFinder(DirectedGraph<V, E> graph) {
	this.graph = graph;
    }

    /**
     * Return the first found LCA of a and b
     * 
     * @param a
     *            the first element to find LCA for
     * @param b
     *            the other element to find the LCA for
     * @return the first found LCA of a and b, or null if there is no LCA.
     */
    public V findLca(V a, V b) {
	return findLca(Collections.singleton(a), Collections.singleton(b), new LinkedHashSet<V>(),
		new LinkedHashSet<V>());
    }
    
    /**
     * Return all the LCA of a and b. Currently not implemented
     * 
     * @param a
     *            the first element to find LCA for
     * @param b
     *            the other element to find the LCA for
     * @return the set of all LCA of a and b, or empty set if there is no LCA.
     */
    public Set<V> findLcas(V a, V b) {
	throw new UnsupportedOperationException("findLcas has not yet been implemented");
    }

    /**
     * Recurse through the descendants of aSet and bSet looking for the LCA of a and b, which are members of sets
     * aSeenSet and bSeenSet respectively, along with all elements on the paths from every member of aSet and bSet
     */
    private V findLca(Set<V> aSet, Set<V> bSet, LinkedHashSet<V> aSeenSet, LinkedHashSet<V> bSeenSet) {

	// if there is no LCA...
	if (aSet.size() == 0 && bSet.size() == 0)
	    return null;
	// does aSet intersect with bSeenSet
	if (!Collections.disjoint(aSet, bSeenSet))
	    return overlappingMember(aSet, bSeenSet);
	// does bSet intersect with aSeenSet
	if (!Collections.disjoint(bSet, aSeenSet))
	    return overlappingMember(bSet, aSeenSet);
	if (!Collections.disjoint(aSet, bSet))
	    return overlappingMember(aSet, bSet);

	aSeenSet.addAll(aSet);
	bSeenSet.addAll(bSet);

	aSet = allParents(aSet);
	// no point doing the same again (and it can stop us getting stuck in an infinite loop)
	aSet.removeAll(aSeenSet);

	bSet = allParents(bSet);
	bSet.removeAll(bSeenSet);

	return findLca(aSet, bSet, aSeenSet, bSeenSet);

    }

    /**
     * Find the immediate parents of every item in the given set, and return a set containing all those parents
     * 
     * @param vertexSet
     *            the set of vertex to find parents of
     * @return a set of every parent of every vertex passed in
     */
    private Set<V> allParents(Set<V> vertexSet) {
	HashSet<V> result = new HashSet<V>();
	for (V e : vertexSet) {
	    for (E edge : graph.incomingEdgesOf(e)) {
		if (graph.getEdgeTarget(edge).equals(e))
		    result.add(graph.getEdgeSource(edge));
	    }
	}
	return result;
    }

    /**
     * Return a single vertex that is both in x and y. If there is more than one then select the first element from the
     * iterator returned from y, after all the elements of x have been removed. this allows an orderedSet to be passed
     * in to give predictable results.
     * 
     * @param x
     *            set containing vertex
     * @param y
     *            set containing vertex, which may be ordered to give predictable results
     * @return the first element of y that is also in x, or null if no such element
     */
    private V overlappingMember(Set<V> x, Set<V> y) {
	y.retainAll(x);
	return y.iterator().next();
    }

}
