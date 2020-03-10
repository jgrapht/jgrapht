package org.jgrapht.alg.interfaces;

import java.util.Map;

/**
 * An interface for all algorithms which assign scores to edges of a graph.
 *
 * @param <E> the edge type
 * @param <D> the score type
 *
 * @author Edwin Ouwehand
 */
public interface EdgeScoringAlgorithm<E, D> {

    /**
     * Get a map with the scores of all edges
     *
     * @return a map with all scores
     */
    Map<E, D> getScores();

    /**
     * Get an edge score
     *
     * @param e the edge
     * @return the score
     */
    D getEdgeScore(E e);
}
