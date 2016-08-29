/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------
 * CSV.g4
 * -------------------
 * (C) Copyright 2016-2016, by Dimitrios Michail and Contributors.
 *
 * Original Author:  Dimitrios Michail
 * Contributor(s): 
 *
 * Changes
 * -------
 * 20-Aug-2016 : Initial revision (DM);
 *
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
