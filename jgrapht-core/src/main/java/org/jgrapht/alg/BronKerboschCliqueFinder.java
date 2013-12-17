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
/* -------------------
 * BronKerboschCliqueFinder.java
 * -------------------
 * (C) Copyright 2005-2008, by Ewgenij Proschak and Contributors.
 *
 * Original Author:  Ewgenij Proschak
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 21-Jul-2005 : Initial revision (EP);
 * 26-Jul-2005 : Cleaned up and checked in (JVS);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;


/**
 * This class implements Bron-Kerbosch clique detection algorithm as it is
 * described in [Samudrala R.,Moult J.:A Graph-theoretic Algorithm for
 * comparative Modeling of Protein Structure; J.Mol. Biol. (1998); vol 279; pp.
 * 287-302]
 *
 * @author Ewgenij Proschak
 */
public class BronKerboschCliqueFinder<V, E>
{
    

    private final Graph<V, E> graph;

    private Collection<Set<V>> cliques;

    

    /**
     * Creates a new clique finder.
     *
     * @param graph the graph in which cliques are to be found; graph must be
     * simple
     */
    public BronKerboschCliqueFinder(Graph<V, E> graph)
    {
        this.graph = graph;
    }

    

    /**
     * Finds all maximal cliques of the graph. A clique is maximal if it is
     * impossible to enlarge it by adding another vertex from the graph. Note
     * that a maximal clique is not necessarily the biggest clique in the graph.
     *
     * @return Collection of cliques (each of which is represented as a Set of
     * vertices)
     */
    public Collection<Set<V>> getAllMaximalCliques()
    {
        // TODO jvs 26-July-2005:  assert that graph is simple

        cliques = new ArrayList<Set<V>>();
        List<V> potential_clique = new ArrayList<V>();
        List<V> candidates = new ArrayList<V>();
        List<V> already_found = new ArrayList<V>();
        candidates.addAll(graph.vertexSet());
        findCliques(potential_clique, candidates, already_found);
        return cliques;
    }

    /**
     * Finds the biggest maximal cliques of the graph.
     *
     * @return Collection of cliques (each of which is represented as a Set of
     * vertices)
     */
    public Collection<Set<V>> getBiggestMaximalCliques()
    {
        // first, find all cliques
        getAllMaximalCliques();

        int maximum = 0;
        Collection<Set<V>> biggest_cliques = new ArrayList<Set<V>>();
        for (Set<V> clique : cliques) {
            if (maximum < clique.size()) {
                maximum = clique.size();
            }
        }
        for (Set<V> clique : cliques) {
            if (maximum == clique.size()) {
                biggest_cliques.add(clique);
            }
        }
        return biggest_cliques;
    }

    private void findCliques(
        List<V> potential_clique,
        List<V> candidates,
        List<V> already_found)
    {
        List<V> candidates_array = new ArrayList<V>(candidates);
        if (!end(candidates, already_found)) {
            // for each candidate_node in candidates do
            for (V candidate : candidates_array) {
                List<V> new_candidates = new ArrayList<V>();
                List<V> new_already_found = new ArrayList<V>();

                // move candidate node to potential_clique
                potential_clique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (V new_candidate : candidates) {
                    if (graph.containsEdge(candidate, new_candidate)) {
                        new_candidates.add(new_candidate);
                    } // of if
                } // of for

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (V new_found : already_found) {
                    if (graph.containsEdge(candidate, new_found)) {
                        new_already_found.add(new_found);
                    } // of if
                } // of for

                // if new_candidates and new_already_found are empty
                if (new_candidates.isEmpty() && new_already_found.isEmpty()) {
                    // potential_clique is maximal_clique
                    cliques.add(new HashSet<V>(potential_clique));
                } // of if
                else {
                    // recursive call
                    findCliques(
                        potential_clique,
                        new_candidates,
                        new_already_found);
                } // of else

                // move candidate_node from potential_clique to already_found;
                already_found.add(candidate);
                potential_clique.remove(candidate);
            } // of for
        } // of if
    }

    private boolean end(List<V> candidates, List<V> already_found)
    {
        // if a node in already_found is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for (V found : already_found) {
            edgecounter = 0;
            for (V candidate : candidates) {
                if (graph.containsEdge(found, candidate)) {
                    edgecounter++;
                } // of if
            } // of for
            if (edgecounter == candidates.size()) {
                end = true;
            }
        } // of for
        return end;
    }
}

// End BronKerboschCliqueFinder.java
