/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio;

/**
 * Denotes the type of an attribute.
 * 
 * @author Dimitrios Michail
 */
public enum AttributeType
{
    NULL("null"),
    BOOLEAN("boolean"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    STRING("string"),
    HTML("html"),
    UNKNOWN("unknown"),
    IDENTIFIER("identifier");

    private String name;

    private AttributeType(String name)
    {
        this.name = name;
    }

    /**
     * Get a string representation of the attribute type
     * 
     * @return the string representation of the attribute type
     */
    public String toString()
    {
        return name;
    }

    /**
     * Create a type from a string representation
     * 
     * @param value the name of the type
     * @return the attribute type
     */
    public static AttributeType create(String value)
    {
        switch (value) {
        case "null":
            return NULL;
        case "boolean":
            return BOOLEAN;
        case "int":
            return INT;
        case "long":
            return LONG;
        case "float":
            return FLOAT;
        case "double":
            return DOUBLE;
        case "string":
            return STRING;
        case "html":
            return HTML;
        case "unknown":
            return UNKNOWN;
        case "identifier":
            return IDENTIFIER;
        }
        throw new IllegalArgumentException("Type " + value + " is unknown");
    }

}
