/*
 * (C) Copyright 2012-2018, by Barak Naveh and Contributors.
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

package org.jgrapht.demo;

import java.util.ArrayList;

import org.jgrapht.*;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * A simple introduction to using JGraphT.
 *
 * @author Barak Naveh
 * @since Jul 27, 2003
 */

public class LabeledEdges {
    private static final String FRIEND = "friend";
    private static final String ENEMY = "enemy";

    /**
     * The starting point for the demo.
     *
     * @param args ignored.
     */
    
    public static void main(String[] args) {
        Graph<String, RelationshipEdge> graph =
            new DirectedMultigraph<String, RelationshipEdge>(
                    new ClassBasedEdgeFactory<String, RelationshipEdge>(RelationshipEdge.class));

        ArrayList<String> people = new ArrayList<String>();
        people.add("John");
        people.add("James");
        people.add("Sarah");
        people.add("Jessica");

        // John is everyone's friend
        for (String person : people) {
            graph.addVertex(person);
            if(!person.equals(people.get(0)))
                graph.addEdge(people.get(0), person, new RelationshipEdge<String>(people.get(0), person, FRIEND));
        }

        // Apparently James doesn't really like John
        graph.addEdge("James", "John", new RelationshipEdge<String>("James", "John", ENEMY));

        // Jessica is Sarah and James's friend
        graph.addEdge("Jessica", "Sarah", new RelationshipEdge<String>("Jessica", "Sarah", FRIEND));
        graph.addEdge("Jessica", "James", new RelationshipEdge<String>("Jessica", "James", FRIEND));

        // But Sarah doesn't really like James
        graph.addEdge("Sarah", "James", new RelationshipEdge<String>("Sarah", "James", ENEMY));

         for (RelationshipEdge edge : graph.edgeSet()) {
        if (edge.toString().equals("enemy")) {
            System.out.printf(edge.getV1()+"is an enemy of "+ edge.getV2()+"\n");
        } else if (edge.toString().equals("friend")) {
            System.out.printf( edge.getV1()+" is a friend of "+ edge.getV2()+"\n");
        }
    }
    }
    
    /**
     * Relationship Edge
     * 
     * @param <V> the graph vertex type
     *
     */
    public static class RelationshipEdge<V> extends DefaultEdge {
        private V v1;
        private V v2;
        private String label;
        /**
         * Constructs a Relationship Edge
         *
         * @param v1 vertex set 
         * @param v2 vertex set
         * @param label the label of the edge.
         * 
         */
        public RelationshipEdge(V v1, V v2, String label) {
            this.v1 = v1;
            this.v2 = v2;
            this.label = label;
        }

        /**
         * method getV1
         *
         * @return v1 vertex set 
         * 
         */
        
        public V getV1() {
            return v1;
        }

        /**
         * method getV2
         *
         * @return v1 vertex set 
         * 
         */
        
        public V getV2() {
            return v2;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}

