/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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
grammar CSV;

@lexer::members
{
    char sep = ',';

    public void setSep(char sep)
    {
        this.sep = sep;
    }

    private char getSep()
    {
        return sep;
    }
}

file: header record+ ;

header : record ;

record : field (SEPARATOR field)* '\r'? '\n' ;

field
    : TEXT     #TextField
    | STRING   #StringField
    |          #EmptyField
    ;
    
SEPARATOR: { _input.LA(1) == sep }? . ;
    
TEXT   : TEXTCHAR+ ;

fragment TEXTCHAR: { (_input.LA(1) != sep && _input.LA(1) != '\n' && _input.LA(1) != '\r' && _input.LA(1) != '"') }? .; 

STRING : '"' ('""'|~'"')* '"' ;
