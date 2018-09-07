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

/**
 * Class which represents an abstract extension/encapsulation object. An object, from here on
 * denoted as original,can be encapsulated in or extended by another object. An example would be the
 * relation between an edge (original) and an annotated edge. The annotated edge
 * encapsulates/extends an edge, thereby augmenting it with additional data. In symbolic form, if b
 * is the original class, than a(b) would be its extension. This concept is similar to java's
 * extension where one class is derived from (extends) another class (original).
 */
public interface Extension
{

}
