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

import java.util.*;
import org.jgrapht.Graph;

/**
 * Hopcroft Tarjan segment strong planarity testing algorithm
 * 
 * Internal class for the Hopcroft Tarjan planarity test
 *
 * The
 * <a href="https://www.cs.princeton.edu/courses/archive/fall05/cos528/handouts/Efficient%20Planarity.pdf">
 * Hopcroft Tarjan algorithm</a> is based on finding bi-components, building 
 * DFS tree for each bi-component and testing its planarity. It finds a cycle $C$, 
 * goes down its spine and checks segments $S_i$ defined by outgoing edges 
 * from $C$. Segment $S_i$ is said to be strongly planar if $S_i \cup C$ is  planar.
 *
 * @author Karolina
 * @param <V>  the graph vertex type
 * @param <E> the graph edge type
 */
public class HopcroftTarjanStrongPlanarityInspector<V, E> {

    private final ArrayList<V> vertexList;
    private final ArrayList<VertexProperties> vertexProperties;
    

    /**
     * Constructor of HopcroftTarjanStrongPlanarityInspector
     * 
     * computes initial information about given bi-conected component,
     * builds DFS tree of given graph
     * 
     * @param g graph to be inspected
     */
    protected HopcroftTarjanStrongPlanarityInspector(Graph<V, E> g) {

        Map<V, LinkedList<V>> adjacent = new HashMap();
        LinkedList<V> adjVer = new LinkedList();
        Iterator<E> adjEdg;
        Iterator<V> vertices = g.vertexSet().iterator();
        V next = null, to, from;
        E tempEdge;

        while (vertices.hasNext()) {
            next = vertices.next();
            adjEdg = g.edgesOf(next).iterator();
            while (adjEdg.hasNext()) {

                tempEdge = adjEdg.next();
                from = g.getEdgeSource(tempEdge);
                to = g.getEdgeTarget(tempEdge);

                if (to.equals(next)) {
                    adjVer.add(from);
                } else {
                    adjVer.add(to);
                }
            }
            adjacent.put(next, (LinkedList) adjVer.clone());
            adjVer.clear();
        }

        Set<V> seen = new HashSet();
        int counter = 0, parent, tempInt;
        boolean haveNext = true;
        vertexList = new ArrayList();
        vertexProperties = new ArrayList();

        seen.add(next);

        LinkedList<Integer> toCheck = new LinkedList();
        vertexList.add(next);
        vertexProperties.add(new VertexProperties(counter));
        if (adjacent.get(next).isEmpty()) {
            haveNext = false;
        }
        next = adjacent.get(next).pop();
        parent = counter;
        while (haveNext) {
            if (seen.contains(next)) {
                tempInt = vertexList.indexOf(next);
                if (tempInt > parent) {
                    vertexProperties.get(tempInt).addAdjacent(parent);
                    if (vertexProperties.get(tempInt).getLow() > parent) {
                        vertexProperties.get(tempInt).lowToLow2();
                        vertexProperties.get(tempInt).setLow(parent);
                    } else if ((vertexProperties.get(tempInt).getLow2() > parent)
                            && (vertexProperties.get(tempInt).getLow() < parent)) {
                        vertexProperties.get(tempInt).setLow2(parent);
                    }
                }
                while (parent >= 0 && adjacent.get(vertexList.get(parent)).isEmpty()) {
                    parent = vertexProperties.get(parent).getParent();
                }
                if (parent == -1) {
                    haveNext = false;
                } else {
                    next = adjacent.get(vertexList.get(parent)).pop();
                }
            } else {
                counter++;
                vertexList.add(next);

                vertexProperties.add(new VertexProperties(counter, parent));
                vertexProperties.get(parent).addAdjacent(counter);
                seen.add(next);
                next = adjacent.get(next).pop();
                toCheck.add(parent);
                parent = counter;
                toCheck.add(counter);
            }
        }
        while (!toCheck.isEmpty()) {
            tempInt = toCheck.pollLast();
            parent = toCheck.pollLast();
            if (vertexProperties.get(parent).getLow() > vertexProperties.get(tempInt).getLow()) {
                vertexProperties.get(parent).lowToLow2();
                vertexProperties.get(parent).setLow(vertexProperties.get(tempInt).getLow());
                if (vertexProperties.get(parent).getLow2() > vertexProperties.get(tempInt).getLow2()) {
                    vertexProperties.get(parent).setLow2(vertexProperties.get(tempInt).getLow2());
                }
            } else if ((vertexProperties.get(parent).getLow2() > vertexProperties.get(tempInt).getLow())
                    && (vertexProperties.get(parent).getLow() < vertexProperties.get(tempInt).getLow())) {
                vertexProperties.get(parent).setLow2(vertexProperties.get(tempInt).getLow());
            } else if (vertexProperties.get(parent).getLow2() > vertexProperties.get(tempInt).getLow2()) {
                vertexProperties.get(parent).setLow2(vertexProperties.get(tempInt).getLow2());
            }
        }
        vertices = g.vertexSet().iterator();
        VertexProperties temp;
        while (vertices.hasNext()) {
            next = vertices.next();

            temp = vertexProperties.get(vertexList.indexOf(next));
            temp.setAdjacent(radsort(vertexList.indexOf(next), temp.getAdjacent()));
        }
    }

    /**
     * tests if given bi-component is planar
     *
     * @return true if given bi-component is planar, false otherwise
     */
    protected boolean isComponentPlanar() {
        return (isSegmentStronglyPlanar(0, 1) != null);
    }

    
    /**
     * tests if given segment is strongly planar
     * 
     * @param vertexS the source of the edge determinig the tested segment
     * @param vertexT the target of the edge determinig the tested segment
     *
     * @return list of segments attachments if segment is strongly planar, null otherwise
     */
    private LinkedList<Integer> isSegmentStronglyPlanar(int vertexS, int vertexT) {
        LinkedList<LinkedList<Integer>> aLefts, aRights;
        aLefts = new LinkedList();
        aRights = new LinkedList();
        LinkedList<Integer> result = new LinkedList();
        LinkedList<Integer> A;
        if (this.vertexList.size() < 5) {
            return new LinkedList();
        }
        LinkedList<Integer> actAdjacent, aMaxL, aMaxR;
        LinkedList<Integer> spine = new LinkedList();
        int aMin, previous, w1;
        boolean stop;
        int source = vertexT;

        int target = this.vertexProperties.get(vertexT).getAdjacent().getFirst();
        if (vertexT > vertexS) {
            spine.addLast(vertexT);
            while (target > source) {
                spine.addLast(target);
                source = target;
                target = this.vertexProperties.get(target).getAdjacent().getFirst();
            }
            result.add(target);
        } else {
            result.add(vertexT);
        }
        while (!spine.isEmpty()) {
            source = spine.removeLast();

            actAdjacent = this.vertexProperties.get(source).getAdjacent();
            for (int i = 1; i < actAdjacent.size(); i++) {
                target = actAdjacent.get(i);

                A = isSegmentStronglyPlanar(source, target);
                if (A == null) {
                    return null;
                }
                if (target < source) {
                    aMin = target;
                } else {
                    aMin = this.vertexProperties.get(target).getLow();
                }
                if (!bipartityTestAndComponentsUpdate(aMin, A, aLefts, aRights)) {
                    return null;
                }
            }
            previous = vertexProperties.get(source).getParent();
            stop = false;
            while (!stop) {
                if (!aLefts.isEmpty() && previous >= 0) {
                    aMaxL = aLefts.removeLast();
                    while (!aMaxL.isEmpty() && (aMaxL.peekLast() == previous)) {
                        aMaxL.pollLast();
                    }
                    aMaxR = aRights.removeLast();
                    while (!aMaxR.isEmpty() && (aMaxR.peekLast() == previous)) {
                        aMaxR.pollLast();
                    }
                    if (!aMaxL.isEmpty() || !aMaxR.isEmpty()) {
                        aLefts.addLast(aMaxL);
                        aRights.addLast(aMaxR);
                        stop = true;
                    }
                } else {
                    stop = true;
                }
            }
        }
        LinkedList<Integer> arb, alb;
        w1 = vertexT;
        previous = vertexS;
        while (vertexProperties.get(vertexT).getLow() < previous) {
            w1 = previous;
            previous = vertexProperties.get(previous).getParent();
        }

        while (!aLefts.isEmpty()) {
            arb = aRights.removeFirst();
            alb = aLefts.removeFirst();
            if ((!alb.isEmpty()) && (!arb.isEmpty()) && (alb.peekLast() >= w1) && (arb.peekLast() >= w1)) {
                return null;
            }
            if ((!alb.isEmpty()) && (alb.peekLast() >= w1)) {
                result.addAll(arb);
                result.addAll(alb);
            } else {
                result.addAll(alb);
                result.addAll(arb);
            }
        }
        return result;
    }

    
    /**
     * sorts the neighbouring vertices of given vertex
     * 
     * @param from number of given vertex
     * @param list unsorted list of neighbouring vertices
     *
     * @return sorted list of neighbouring vertices
     */
    private LinkedList<Integer> radsort(int from, LinkedList<Integer> list) {

        int size = vertexList.size() * 2;
        int to;

        ArrayList<LinkedList<Integer>> tempLists = new ArrayList(size);

        for (int i = 0; i < size; i++) {
            tempLists.add(new LinkedList());
        }

        while (!list.isEmpty()) {
            to = list.removeFirst();
            tempLists.get(phi(from, to)).add(to);
        }

        for (int i = 0; i <size; i++) {

            list.addAll(tempLists.get(i));
        }

        return list;
    }

    /**
     * sorts given list of attachments using radixsort
     * 
     * @param list unsorted list of attachments
     *
     * @return sorted list of attachments
     */
    private LinkedList<Integer> attachmentSort(LinkedList<Integer> list) {
        if (list.isEmpty()) {
            return list;
        }
        
        int size = -1;
        if (!list.isEmpty()){
            size = Collections.max(list);
        }
        int to;

        ArrayList<Integer> tempList = new ArrayList(size);

        for (int i = 0; i <= size; i++) {
            tempList.add(0);
        }

        while (!list.isEmpty()) {
            to = list.removeFirst();
            tempList.set(to, tempList.get(to) + 1);
        }

        for (int i = 0; i <= size; i++) {
            for (int j = 0; j < tempList.get(i); j++) {
                list.addLast(i);

            }
        }
        return list;
    }

    
    /**
     * computes phi of given edge
     * 
     * @param from the source of the given edge 
     * @param to the target of the given edge 
     *
     * @return the value of function phi for the given edge
     */
    private int phi(int from, int to) {
        if (from > to) {
            return 2 * to;
        } else if (vertexProperties.get(to).getLow2() < from) {
            return 2 * vertexProperties.get(to).getLow() + 1;
        } else {
            return 2 * vertexProperties.get(to).getLow();
        }
    }

    
    /**
     * tests if given segment can be added to already tested segments 
     * 
     * @param minimum minimal value of tested segment's attachments
     * @param A the list of attachments of given segment
     * @param aLefts the list of left attachments
     * @param aRights the list of right attachments
     *
     * @return true if given segment can be added to processed segments, 
     * false otherwise
     */
    private boolean bipartityTestAndComponentsUpdate(int minimum, LinkedList<Integer> A, 
            LinkedList<LinkedList<Integer>> aLefts, LinkedList<LinkedList<Integer>> aRights) {
        int i = aLefts.size();
        if (i == 0) {
            aLefts.add(A);
            aRights.add(new LinkedList());
            return true;
        }
        LinkedList<Integer> aL = new LinkedList();
        LinkedList<Integer> aR = new LinkedList();
        LinkedList<Integer> helper;
        aL.addAll(A);
        i--;
        while (i >= 0 && maxComponentAttachment(aLefts.get(i), aRights.get(i)) > minimum) {
            if (!aLefts.get(i).isEmpty() && aLefts.get(i).peekLast() > minimum) {
                helper = aRights.get(i);
                aRights.set(i, aLefts.get(i));
                aLefts.set(i, helper);
            }
            if (!aLefts.get(i).isEmpty() && aLefts.get(i).peekLast() > minimum) {
                return false;
            }
            aL.addAll(aLefts.removeLast());
            aR.addAll(aRights.removeLast());

            i--;
        }
        aLefts.addLast(attachmentSort(aL));
        aRights.addLast(attachmentSort(aR));
        return true;
    }

    
    /**
     * finds the maximal component's attachment
     * 
     * @param aLefts the list of left attachments
     * @param aRights the list of right attachments
     *
     * @return the value of maximal component's attachment if the component has 
     * any, -1 if both given lists are empty
     */
    private int maxComponentAttachment(LinkedList<Integer> aLefts, LinkedList<Integer> aRights) {
        int resultL = -1;
        int resultR = -1;
        if (!aLefts.isEmpty()) {
            resultL = aLefts.peekLast();
        }
        if (!aRights.isEmpty()) {

            resultR = aRights.peekLast();
        }
        if (resultR > resultL) {
            return resultR;
        }
        return resultL;
    }

}
