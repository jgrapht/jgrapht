package org.jgrapht.imp;

/**
 * @author Ivan Gavrilović
 */
public class StringVertexParser  implements VertexParser<String>{
    @Override
    public String parseVertex(String s) {
        return s;
    }
}
