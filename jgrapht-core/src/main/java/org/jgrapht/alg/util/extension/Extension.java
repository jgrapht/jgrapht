/*
 * (C) Copyright 2015-2018, by Alexey Kudinkin and Contributors.
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
