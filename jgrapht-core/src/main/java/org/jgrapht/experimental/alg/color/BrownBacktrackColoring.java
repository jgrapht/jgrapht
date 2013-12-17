/* This program and the accompanying materials are dual-licensed under
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
/**
 *
 */
package org.jgrapht.experimental.alg.color;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.experimental.alg.*;


/**
 * @author micha
 */
public class BrownBacktrackColoring<V, E>
    extends IntArrayGraphAlgorithm<V, E>
    implements ExactAlgorithm<Integer, V>
{
    

    private int [] _color;
    private int [] _colorCount;
    private BitSet [] _allowedColors;
    private int _chi;

    

    /**
     * @param g
     */
    public BrownBacktrackColoring(final Graph<V, E> g)
    {
        super(g);
    }

    

    void recursiveColor(int pos)
    {
        _colorCount[pos] = _colorCount[pos - 1];
        _allowedColors[pos].set(0, _colorCount[pos] + 1);
        for (int i = 0; i < _neighbors[pos].length; i++) {
            final int nb = _neighbors[pos][i];
            if (_color[nb] > 0) {
                _allowedColors[pos].clear(_color[nb]);
            }
        }
        for (
            int i = 1;
            (i <= _colorCount[pos])
            && (_colorCount[pos] < _chi);
            i++)
        {
            if (_allowedColors[pos].get(i)) {
                _color[pos] = i;
                if (pos < (_neighbors.length - 1)) {
                    recursiveColor(pos + 1);
                } else {
                    _chi = _colorCount[pos];
                }
            }
        }
        if ((_colorCount[pos] + 1) < _chi) {
            _colorCount[pos]++;
            _color[pos] = _colorCount[pos];
            if (pos < (_neighbors.length - 1)) {
                recursiveColor(pos + 1);
            } else {
                _chi = _colorCount[pos];
            }
        }
        _color[pos] = 0;
    }

    /* (non-Javadoc)
     * @see org.jgrapht.experimental.alg.ExactAlgorithm#getResult()
     */
    public Integer getResult(Map<V, Object> additionalData)
    {
        _chi = _neighbors.length;
        _color = new int[_neighbors.length];
        _color[0] = 1;
        _colorCount = new int[_neighbors.length];
        _colorCount[0] = 1;
        _allowedColors = new BitSet[_neighbors.length];
        for (int i = 0; i < _neighbors.length; i++) {
            _allowedColors[i] = new BitSet(1);
        }
        recursiveColor(1);
        if (additionalData != null) {
            for (int i = 0; i < _vertices.size(); i++) {
                additionalData.put(_vertices.get(i), _color[i]);
            }
        }
        return _chi;
    }
}

// End BrownBacktrackColoring.java
