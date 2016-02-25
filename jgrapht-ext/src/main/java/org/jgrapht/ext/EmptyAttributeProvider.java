package org.jgrapht.ext;

import java.util.Collections;
import java.util.Map;

public class EmptyAttributeProvider<T> implements ComponentAttributeProvider<T> {

    @Override
    public Map<String, String> getComponentAttributes(T component) {
        return Collections.emptyMap();
    }

}
