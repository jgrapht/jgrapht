/*
 * (C) Copyright 2017-2017, by Karolina Rezkova and Contributors.
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
package org.jgrapht.alg.planarity;

import java.util.LinkedList;

/**
 * Internal class for storing properties of graph vertices
 *
 * @author Karolina
 */

    public class VertexProperties {

        private final int vertex;
        private final int parent;
        private int low;
        private int low2;
        private LinkedList<Integer> adjacent;

    /**
     * constructor for DFS root
     * 
     * @param vertex number of vertex
     */
    protected VertexProperties(int vertex) {
            this(vertex, -1);
        }

    /**
     * Constructor for DFS vertex 
     * 
     * @param vertex number of vertex
     * @param parent number of parent
     */
    protected VertexProperties(int vertex, int parent) {
            this.parent = parent;
            this.vertex = vertex;
            this.low = vertex;
            this.low2 = vertex;
            this.adjacent = new LinkedList();
        }

    /**
     * getter for atribute vertex
     *
     * @return number of vertex
     */
    protected int getVertex() {
            return vertex;
        }

    /**
     * getter for atribute low
     *
     * @return low of vertex
     */
    protected int getLow() {
            return low;
        }

   /**
     * getter for atribute low2
     *
     * @return low2 of vertex
     */
    protected int getLow2() {
            return low2;
        }

    /**
     * setter for atribute low
     *
     * @param low new low number
     */
    protected void setLow(int low) {
            this.low = low;
        }

     /**
     * setter for atribute low2
     *
     * @param low new low2 number
     */
        protected void setLow2(int low) {
            this.low2 = low;
        }

    /**
     * getter for atribute parent
     * 
     * @return number of parent
     */
    protected int getParent() {
            return parent;
        }

    /**
     * getter for atribute adjacent
     * 
     * @return LinkedList of adjacent vertices
     */
    protected LinkedList<Integer> getAdjacent() {
            return adjacent;
        }

    /**
     * setter for adjacent vertices
     *
     * @param list LinkedList of adjacent vertices
     */
    protected void setAdjacent(LinkedList<Integer> list) {
            this.adjacent = list;
        }

    /**
     * adds new adjacent vertex number
     * 
     * @param next vertex number to be added
     * @return true if addition was succesfull
     */
    protected boolean addAdjacent(int next) {
            return this.adjacent.add(next);            
        }

    /**
     * moves low to low2
     *
     */
    protected void lowToLow2() {
            this.low2 = this.low;
        }

        @Override
        public String toString() {
            return "VertexProperties{" + "vertex=" + vertex + ", parent=" + parent + ", low=" + low + ", low2=" + low2 + ", adjacent=" + adjacent + '}';
        }

    
    
}
