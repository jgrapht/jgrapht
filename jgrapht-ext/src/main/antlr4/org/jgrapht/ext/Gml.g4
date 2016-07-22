
// define a grammar called GML

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
      
/** "a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? )" */ NUMBER
   : '-'? ( '.' DIGIT+ | DIGIT+ ( '.' DIGIT* )? )
   ;

fragment DIGIT
   : [0-9]
   ;
   
fragment LETTER
   : [a-zA-Z\u0080-\u00FF_]
;   

/** "any double-quoted string ("...") possibly containing escaped quotes" */ STRING
   : '"' ( '\\"' | . )*? '"'
   ;

/** "Any string of alphabetic ([a-zA-Z\200-\377]) characters, underscores
 *  ('_') or digits ([0-9]), not beginning with a digit"
 */ ID
   : LETTER ( LETTER | DIGIT )*
   ;

/** "a '#' character is considered a line output from a C preprocessor (e.g.,
 *  # 34 to indicate line 34 ) and discarded"
 */ PREPROC
   : '#' .*? '\n' -> skip
   ;

WS
   : [ \t\n\r]+ -> skip
   ;
   