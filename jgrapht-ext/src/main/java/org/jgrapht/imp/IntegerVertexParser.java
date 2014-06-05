package org.jgrapht.imp;

/**
 * @author Ivan Gavrilović
 */
public class IntegerVertexParser implements VertexParser<Integer> {
    @Override
    public Integer parseVertex(String s) {
        return Integer.parseInt(s);
    }
}
