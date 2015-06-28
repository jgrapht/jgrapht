package org.jgrapht.imp;

/**
 * @author Ivan Gavrilović
 */
interface VertexParser<V> {
    V parseVertex(String s);
}
