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
    //~ Instance fields --------------------------------------------------------

    private final Graph<V, E> graph;

    private Collection<Set<V>> cliques;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new clique finder.
     *
     * @param graph the graph in which cliques are to be found; graph must be
     * simple
     */
    public BronKerboschCliqueFinder(final Graph<V, E> graph)
    {
        this.graph = graph;
    }

    //~ Methods ----------------------------------------------------------------

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
        final List<V> potential_clique = new ArrayList<V>();
        final List<V> candidates = new ArrayList<V>();
        final List<V> already_found = new ArrayList<V>();
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
        final Collection<Set<V>> biggest_cliques = new ArrayList<Set<V>>();
        for (final Set<V> clique : cliques) {
            if (maximum < clique.size()) {
                maximum = clique.size();
            }
        }
        for (final Set<V> clique : cliques) {
            if (maximum == clique.size()) {
                biggest_cliques.add(clique);
            }
        }
        return biggest_cliques;
    }

    private void findCliques(
        final List<V> potential_clique,
        final List<V> candidates,
        final List<V> already_found)
    {
        final List<V> candidates_array = new ArrayList<V>(candidates);
        if (!end(candidates, already_found)) {
            // for each candidate_node in candidates do
            for (final V candidate : candidates_array) {
                final List<V> new_candidates = new ArrayList<V>();
                final List<V> new_already_found = new ArrayList<V>();

                // move candidate node to potential_clique
                potential_clique.add(candidate);
                candidates.remove(candidate);

                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                for (final V new_candidate : candidates) {
                    if (graph.containsEdge(candidate, new_candidate)) {
                        new_candidates.add(new_candidate);
                    } // of if
                } // of for

                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                for (final V new_found : already_found) {
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

    private boolean end(final List<V> candidates, final List<V> already_found)
    {
        // if a node in already_found is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for (final V found : already_found) {
            edgecounter = 0;
            for (final V candidate : candidates) {
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
