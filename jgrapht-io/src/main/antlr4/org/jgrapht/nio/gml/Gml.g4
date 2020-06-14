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
grammar Gml;

gml
    : 
	keyValuePair*
	;

keyValuePair
    : ID STRING   #StringKeyValue  
    | ID NUMBER   #NumberKeyValue  
    | ID '[' keyValuePair* ']' #ListKeyValue
    ;
      
NUMBER
   : '-'? ( '.' DIGIT+ | DIGIT+ ( '.' DIGIT* )? )
   ;

fragment DIGIT
   : [0-9]
   ;
   
fragment LETTER
   : [a-zA-Z\u0080-\u00FF_]
;   

STRING
   : '"' ( '\\"' | . )*? '"'
   ;

ID
   : LETTER ( LETTER | DIGIT )*
   ;

COMMENT
   : '#' .*? '\n' -> skip
   ;

WS
   : [ \t\n\r]+ -> skip
   ;
   