/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.io;

/**
 * Denotes the type of an attribute.
 * 
 * @author Dimitrios Michail
 */
public enum AttributeType
{
    BOOLEAN("boolean"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    STRING("string"),
    UNKNOWN("unknown");

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
        case "unknown":
            return UNKNOWN;
        }
        throw new IllegalArgumentException("Type " + value + " is unknown");
    }

}
