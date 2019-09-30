package org.jgrapht.opt.graph.sparse;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jgrapht.alg.util.Pair;

/**
 * A boolean matrix in CSR form.
 * 
 * @author Dimitrios Michail
 *
 */
public class CSRBooleanMatrix
{
    private int[] rowOffsets;
    private int[] columnIndices;
    private int columns;
    
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

            if (prev == null) {
                rowOffsets[rIndex] = prefix;
            } else if (e.getFirst() == prev.getFirst()) {
                ++prefix;
            } else {
                rowOffsets[++rIndex] = ++prefix;
            }

            int column = e.getSecond();
            if (column < 0 || column >= columns) {
                throw new IllegalArgumentException("Entry at invalid columnn");
            }
            columnIndices[cIndex++] = column;
            prev = e;
        }
        rowOffsets[++rIndex] = ++prefix;
    }

    public int columns()
    {
        return columns;
    }

    public int rows()
    {
        return rowOffsets.length - 1;
    }

    public int nonZeros(int row)
    {
        assert row >= 0 && row < rowOffsets.length;

        return rowOffsets[row + 1] - rowOffsets[row];
    }

    public Iterator<Integer> columnIterator(int row)
    {
        assert row >= 0 && row < rowOffsets.length;

        return new ColumnIterator(row);
    }
    
    public Set<Integer> rowSet(int row) { 
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
            return new ColumnIterator(row);
        }

        @Override
        public int size()
        {
            return rowOffsets[row + 1] - rowOffsets[row];
        }

    }

    private class ColumnIterator
        implements
        Iterator<Integer>
    {

        private int curPos;
        private int toPos;

        public ColumnIterator(int row)
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
