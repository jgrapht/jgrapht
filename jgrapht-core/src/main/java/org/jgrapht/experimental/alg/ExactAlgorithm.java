package org.jgrapht.experimental.alg;

import java.util.Map;


public interface ExactAlgorithm<ResultType, V>
{
    //~ Methods ----------------------------------------------------------------

    ResultType getResult(Map<V, Object> optionalData);
}

// End ExactAlgorithm.java
