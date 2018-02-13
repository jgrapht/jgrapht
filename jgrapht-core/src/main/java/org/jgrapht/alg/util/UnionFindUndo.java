/*
 * (C) Copyright 2017-2017, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of <a href="http://en.wikipedia.org/wiki/Disjoint-set_data_structure">Union
 * Find</a> data structure that can also undo or redo the last performed operation.
 * Union Find is a disjoint-set data structure. It supports two operations:
 * finding the set a specific element is in, and merging two sets. The implementation uses union by
 * rank to achieve an amortized cost of O(log n) per operation.
 * UnionFindUndo uses the hashCode and equals method of the elements it operates on.
 *
 * <p>
 * Note: Only {@link #addElement(Object)} and {@link #union(Object, Object)} can be undone/redone.
 *
 * @param <T> element type
 *
 * @author Alexandru Valeanu
 */
public class UnionFindUndo<T> extends AbstractUnionFind<T>
{
    /** An element of the undo history. */
    private static abstract class Change {
        /**
         * Reset the subject to its previous state.
         */
        abstract void undo(); // abstract

        /**
         * Reset the subject to the state after the change.
         */
        abstract void redo(); // abstract
    }

    private class AddElementChange extends Change{
        final T element;

        private AddElementChange(T element) {
            this.element = element;
        }

        @Override
        void undo() {
            parentMap.remove(element);
            rankMap.remove(element);
            count--;
        }

        @Override
        void redo() {
            parentMap.put(element, element);
            rankMap.put(element, 1);
            count++;
        }
    }

    private class UnionChange extends Change{
        final T child;
        final T parent;

        private UnionChange(T child, T parent) {
            this.child = child;
            this.parent = parent;
        }

        @Override
        void undo() {
            cut(child, parent, rankMap.get(parent) - rankMap.get(child));
        }

        @Override
        void redo() {
            link(child, parent, rankMap.get(parent) + rankMap.get(child));
        }
    }

    /** A stack of undo changes from executed actions. */
    private final List<Change> history = new ArrayList<>();

    /** Index into undo stack.  Elements history[0..u) have been executed
     but not undone, and elements history[u..) have been undone. */
    private int undoPointer = 0;

    /**
     * Creates a UnionFindUndo instance with all the elements in separate sets.
     *
     * @param elements the initial elements to include (each element in a singleton set).
     */
    public UnionFindUndo(Set<T> elements)
    {
        super();

        if (Objects.nonNull(elements)){
            for (T element : elements) {
                parentMap.put(element, element);
                rankMap.put(element, 0);
            }
            count = elements.size();
        }
    }

    /**
     * Creates a UnionFindUndo instance with no elements.
     */
    public UnionFindUndo()
    {
        this(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addElement(T element)
    {
        if (parentMap.containsKey(element))
            throw new IllegalArgumentException("element is already contained in UnionFindUndo: " + element);

        // Update the UFU data-structure
        parentMap.put(element, element);
        rankMap.put(element, 1);
        count++;

        // Record undo info (AddElementChange)
        addChange(new AddElementChange(element));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T find(final T element)
    {
        if (!parentMap.containsKey(element)) {
            throw new IllegalArgumentException(
                "element is not contained in this UnionFindUndo data structure: " + element);
        }

        T current = element;
        while (true) {
            T parent = parentMap.get(current);
            if (parent.equals(current)) {
                break;
            }
            current = parent;
        }

        return current;
    }

    private void link(T child, T parent, int totalSize){
        parentMap.put(child, parent);
        rankMap.put(parent, totalSize);
        count--;
    }

    private void cut(T child, T parent, int totalSize){
        parentMap.put(child, child);
        rankMap.put(parent, totalSize);
        count++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void union(T element1, T element2)
    {
        if (!parentMap.containsKey(element1) || !parentMap.containsKey(element2)) {
            throw new IllegalArgumentException("elements must be contained in given set");
        }

        T parent1 = find(element1);
        T parent2 = find(element2);

        Change change = null;

        // check if the elements are not already in the same set
        if (!parent1.equals(parent2)) {
            int size1 = rankMap.get(parent1);
            int size2 = rankMap.get(parent2);

            if (size1 >= size2) {
                link(parent2, parent1, size1 + size2);
                change = new UnionChange(parent2, parent1);
            } else {
                link(parent1, parent2, size1 + size2);
                change = new UnionChange(parent1, parent2);
            }
        }

        // Record undo info (UnionChange)
        addChange(change);
    }

    /**
     * Resets the UnionFindUndo data structure: each element is placed in its own singleton set.
     */
    @Override
    public void reset()
    {
        for (T element : parentMap.keySet()) {
            parentMap.put(element, element);
            rankMap.put(element, 1);
        }
        count = parentMap.size();

        // clear the stack of performed changes
        history.clear();
        undoPointer = 0;
    }

    /*
        Add change to history
     */
    private void addChange(Change change){
        while (history.size() > undoPointer){
            history.remove(history.size() - 1);
        }

        history.add(change);
        undoPointer++;
    }

    /**
     * Undo the latest operation. If there is no operation to be undone, nothing happens.
     */
    public void undo() {
        if (undoPointer != 0){
            undoPointer -= 1;
            Change change = history.get(undoPointer);

            if (change != null)
                change.undo();
        }
    }

    /**
     * Undo the latest k operations.
     *
     * @param k number of operations to undo
     */
    public void undo(int k){
        while (k > 0){
            undo();
            k--;
        }
    }

    /**
     * Redo the latest undone operation. If there is no operation to be redone, nothing happens.
     */
    public void redo() {
        if (undoPointer < history.size()){
            Change change = history.get(undoPointer);
            undoPointer += 1;

            if (change != null)
                change.redo();
        }
    }

    /**
     * Redo the latest k undone operations.
     *
     * @param k number of operations to redo
     */
    public void redo(int k){
        while (k > 0){
            redo();
            k--;
        }
    }
}
