/*
 * (C) Copyright 2015-2018, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.util.extension;

import java.util.*;

/**
 * Convenience class to manage extensions/encapsulations. This class creates and manages object
 * extensions and encapsulations. An object, from here on denoted as 'original', can be encapsulated
 * in or extended by another object. An example would be the relation between an edge (original) and
 * an annotated edge. The annotated edge encapsulates/extends an edge, thereby augmenting it with
 * additional data. In symbolic form, if b is the original class, than a(b) would be its extension.
 * This concept is similar to java's extension where one class is derived from (extends) another
 * class (original).
 *
 * @param <T> class-type to be extended (class-type of original)
 * @param <B> class-type of extension
 *
 */
public class ExtensionManager<T, B extends Extension>
{

    /* Factory class to create new extensions */
    private ExtensionFactory<B> extensionFactory;
    /* Mapping of originals to their extensions */
    private Map<T, B> originalToExtensionMap = new HashMap<>();

    /**
     * Create a new extension manager.
     * 
     * @param factory the extension factory to use
     */
    public ExtensionManager(ExtensionFactory<B> factory)
    {
        this.extensionFactory = factory;
    }

    /**
     * Creates and returns an extension object.
     * 
     * @return Extension object
     */
    public B createExtension()
    {
        return extensionFactory.create();
    }

    /**
     * Creates a new singleton extension object for original t if no such object exists, returns the
     * old one otherwise.
     * 
     * @param t the original object
     * @return the extension object
     */
    public B getExtension(T t)
    {
        if (originalToExtensionMap.containsKey(t)) {
            return originalToExtensionMap.get(t);
        }

        B extension = createExtension();
        originalToExtensionMap.put(t, extension);
        return extension;
    }
}
