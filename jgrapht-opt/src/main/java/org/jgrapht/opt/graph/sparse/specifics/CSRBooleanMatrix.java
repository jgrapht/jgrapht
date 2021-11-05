/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.opt.graph.sparse.specifics;

import org.jgrapht.alg.util.*;

import java.io.*;
import java.util.*;

/**
 * A sparse boolean matrix in Compressed Sparse Row (CSR) format.
 * 
 * <p>
 * This is a helper class for graph representation and thus does not provide a fully fledged matrix.
 * 
 * @author Dimitrios Michail
 */
class CSRBooleanMatrix
    implements
    Serializable
{
    private static final long serialVersionUID = -8639339411487665967L;

    private static final Comparator<Pair<Integer, Integer>> INTEGER_PAIR_LEX_COMPARATOR =
        (o1, o2) -> {
            if (o1.getFirst() < o2.getFirst()) {
                return -1;
            } else if (o1.getFirst() > o2.getFirst()) {
                return 1;
            } else if (o1.getSecond() < o2.getSecond()) {
                return -1;
            } else if (o1.getSecond() > o2.getSecond()) {
                return 1;
            }
            return 0;
        };

    private int columns;
    private int[] rowOffsets;
    private int[] columnIndices;

    /**
     * Create a new CSR boolean matrix
     * 
     * @param rows the number of rows
     * @param columns the number of columns
     * @param entries the position of the entries of the matrix
     */
    public CSRBooleanMatrix(int rows, int columns, List<Pair<Integer, Integer>> entries)
    {
        if (rows < 1) {
            throw new IllegalArgumentException("Rows must be positive");
        }
        if (columns < 1) {
            throw new IllegalArgumentException("Columns must be positive");
        }
        if (entries == null) {
            throw new IllegalArgumentException("Entries cannot be null");
        }

        this.columns = columns;
        this.rowOffsets = new int[rows + 1];
        this.columnIndices = new int[entries.size()];

        Iterator<Pair<Integer, Integer>> it =
            entries.stream().sorted(INTEGER_PAIR_LEX_COMPARATOR).iterator();

        int cIndex = 0;
        while (it.hasNext()) {
            Pair<Integer, Integer> e = it.next();

            // add column index
            int column = e.getSecond();
            if (column < 0 || column >= columns) {
                throw new IllegalArgumentException("Entry at invalid column: " + column);
            }
            columnIndices[cIndex++] = column;

            // count non-zero per row
            int row = e.getFirst();
            rowOffsets[row + 1]++;
        }

        // prefix sum
        Arrays.parallelPrefix(rowOffsets, (x, y) -> x + y);
    }

    /**
     * Get the number of columns of the matrix.
     * 
     * @return the number of columns
     */
    public int columns()
    {
        return columns;
    }

    /**
     * Get the number of rows of the matrix.
     * 
     * @return the number of rows
     */
    public int rows()
    {
        return rowOffsets.length - 1;
    }

    /**
     * Get the number of non-zero entries of a row.
     * 
     * @param row the row
     * @return the number of non-zero entries of a row
     */
    public int nonZeros(int row)
    {
        assert row >= 0 && row < rowOffsets.length;

        return rowOffsets[row + 1] - rowOffsets[row];
    }

    /**
     * Get an iterator over the non-zero entries of a row.
     * 
     * @param row the row
     * @return an iterator over the non-zero entries of a row
     */
    public Iterator<Integer> nonZerosPositionIterator(int row)
    {
        assert row >= 0 && row < rowOffsets.length;

        return new NonZerosIterator(row);
    }

    /**
     * Get the position of non-zero entries of a row as a set.
     * 
     * @param row the row
     * @return the position of non-zero entries of a row as a set.
     */
    public Set<Integer> nonZerosSet(int row)
    {
        assert row >= 0 && row < rowOffsets.length;

        Set<Integer> nonZeros = new LinkedHashSet<>();
        new NonZerosIterator(row).forEachRemaining(nonZeros::add);
        return nonZeros;
    }

    private class NonZerosIterator
        implements
        Iterator<Integer>
    {
        private int curPos;
        private int toPos;

        public NonZerosIterator(int row)
        {
            this.curPos = rowOffsets[row];
            this.toPos = rowOffsets[row + 1];
        }

        @Override
        public boolean hasNext()
        {
            return (curPos < toPos);
        }

        @Override
        public Integer next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return columnIndices[curPos++];
        }

    }

}
