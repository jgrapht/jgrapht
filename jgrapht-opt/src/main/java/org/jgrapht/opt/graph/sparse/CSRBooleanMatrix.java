/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jgrapht.alg.util.Pair;

/**
 * Compressed Sparse Row Boolean Matrix
 * 
 * @author Dimitrios Michail
 *
 */
public class CSRBooleanMatrix
    implements
    Serializable
{
    private static final long serialVersionUID = -8639339411487665967L;

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

        int cIndex = 0;
        int rIndex = 0;
        int prefix = 0;

        Iterator<Pair<Integer, Integer>> it =
            entries.stream().sorted(new PairComparator()).iterator();
        Pair<Integer, Integer> prev = null;
        while (it.hasNext()) {
            Pair<Integer, Integer> e = it.next();

            int row = e.getFirst();
            if (row < 0 || row >= rows) {
                throw new IllegalArgumentException("Entry at invalid row: " + row);
            }

            if (prev == null) {
                rowOffsets[rIndex] = prefix;
            } else if (row == prev.getFirst()) {
                ++prefix;
            } else {
                rowOffsets[++rIndex] = ++prefix;
            }

            int column = e.getSecond();
            if (column < 0 || column >= columns) {
                throw new IllegalArgumentException("Entry at invalid column: " + column);
            }
            columnIndices[cIndex++] = column;
            prev = e;
        }
        rowOffsets[++rIndex] = ++prefix;
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
    public Iterator<Integer> nonZerosIterator(int row)
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
    public Set<Integer> rowSet(int row)
    {
        assert row >= 0 && row < rowOffsets.length;

        return new RowSet(row);
    }

    private class RowSet
        extends
        AbstractSet<Integer>
    {
        private int row;

        public RowSet(int row)
        {
            this.row = row;
        }

        @Override
        public Iterator<Integer> iterator()
        {
            return new NonZerosIterator(row);
        }

        @Override
        public int size()
        {
            return rowOffsets[row + 1] - rowOffsets[row];
        }
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

    private class PairComparator
        implements
        Comparator<Pair<Integer, Integer>>
    {

        @Override
        public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2)
        {
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
        }

    }

}
