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
grammar DOT;

graph
   : graphHeader compoundStatement
   ;
   
compoundStatement
   : '{' ( statement ';'? )* '}'
   ; 
   
graphHeader
   : STRICT? ( GRAPH | DIGRAPH ) graphIdentifier?
   ;
   
graphIdentifier
   : identifier
   ;

statement
   : nodeStatement | edgeStatement | attributeStatement | identifierPairStatement | subgraphStatement
   ;

identifierPairStatement
   : identifierPair
   ;

attributeStatement
   : ( GRAPH | NODE | EDGE ) attributesList
   ;

attributesList
   : ( '[' aList? ']' )+
   ;
   
aList
   : ( identifierPair (';'|',')? )+   
   ;

edgeStatement
   : ( nodeStatementNoAttributes | subgraphStatement ) ( ('->' | '--') ( nodeStatementNoAttributes | subgraphStatement ) )+ attributesList?
   ;

nodeStatement
   : nodeIdentifier attributesList?
   ;
   
nodeStatementNoAttributes
   : nodeIdentifier
   ;

nodeIdentifier
   : identifier (port)?
   ;
   
port
   : ':' identifier ( ':' identifier )?
   ;

subgraphStatement
   : ( SUBGRAPH identifier? )? compoundStatement
   ;

identifierPair
   : identifier '=' identifier
   ;

identifier
   : Id | String | HtmlString | Numeral      
   ;

// LEXER

STRICT
   : ('S'|'s')('T'|'t')('R'|'r')('I'|'i')('C'|'c')('T'|'t')
   ;

GRAPH
   : ('G'|'g')('R'|'r')('A'|'a')('P'|'p')('H'|'h')
   ;

DIGRAPH
   : ('D'|'d')('I'|'i')('G'|'g')('R'|'r')('A'|'a')('P'|'p')('H'|'h')
   ;

NODE
   : ('N'|'n')('O'|'o')('D'|'d')('E'|'e')
   ;

EDGE
   : ('E'|'e')('D'|'d')('G'|'g')('E'|'e')
   ;

SUBGRAPH
   : ('S'|'s')('U'|'u')('B'|'b')('G'|'g')('R'|'r')('A'|'a')('P'|'p')('H'|'h')
   ;

Numeral
   : '-'? ( '.' Digit+ | Digit+ ( '.' Digit* )? )
   ;

String
   : '"' SCharSequence? '"'
   ;

Id
   : Letter ( Letter | Digit )*
   ;

HtmlString
   : '<' ( HtmlTag | ~[<>] )* '>'
   ;

fragment 
HtmlTag
   : '<' .*? '>'
   ;

fragment
SCharSequence
   : SChar+
   ;

fragment
SChar
   : ~["\\]
   | '\\' ["\\]
   | '\\\n'
   | '\\\r\n'  
   ;

fragment
Digit
   : [0-9]
   ;

fragment 
Letter
   : [a-zA-Z\u0080-\u00FF_]
   ;

WS
   : [ \t\n\r]+ -> skip
   ;

COMMENT
   : '/*' .*? '*/' -> skip
   ;

LINE_COMMENT
   : '//' .*? '\r'? '\n' -> skip
   ;

PREPROC
   : '#' .*? '\n' -> skip
   ;

