lexer grammar DecaLexer;


options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}

// Deca lexer rules.

//DUMMY_TOKEN: .; // A FAIRE : Règle bidon qui reconnait tous les caractères.
                // A FAIRE : Il faut la supprimer et la remplacer par les vraies règles.




// Opérateurs
EQEQ : '==' ;
NEQ  : '!=' ;
LEQ  : '<=' ;
GEQ  : '>=' ;
LT   : '<'  ;
GT   : '>'  ;

EQUALS  : '=' ;
TIMES   : '*' ;
PLUS    : '+' ;
MINUS   : '-' ;
SLASH   : '/' ;
PERCENT : '%' ;
EXCLAM  : '!' ;

//Ponctuation

OPARENT : '(' ;
CPARENT : ')' ;
OBRACE  : '{' ;
CBRACE  : '}' ;
SEMI    : ';' ;
COMMA   : ',' ;
DOT     : '.' ;

// INT et FLOAT
FLOAT
  : FLOATDEC
  | FLOATHEX
  ;

INT : '0' | [1-9] [0-9]* ;



// Mots-clés

IF          : 'if' ;
ELSE        : 'else' ;
WHILE       : 'while' ;
RETURN      : 'return' ;

CLASS       : 'class' ;
EXTENDS     : 'extends' ;
PROTECTED   : 'protected' ;
PUBLIC      : 'public';
NEW         : 'new' ;


PRINTLN     : 'println' ;
PRINTX      : 'printx' ;
PRINTLNX    : 'printlnx' ;
PRINT       : 'print' ;

READINT     : 'readInt' ;
READFLOAT   : 'readFloat' ;

INSTANCEOF  : 'instanceof' ;
ASM         : 'asm' ;

TRUE        : 'true' ;
FALSE       : 'false' ;
THIS        : 'this' ;
NULL        : 'null' ;

OR          : '||' ;
AND         : '&&' ;



//Inclusion de fichier

//Inclusion de fichier

INCLUDE
  : '#include' ' '* '"' FILENAME '"'
    { doInclude(getText()); }
    -> skip
  ;



//String


// Pour STRING : pas de EOL (saut de ligne physique)
STRING 
    : '"' ( ~('"' | '\\' | '\r' | '\n') | '\\' ["\\] | '\\n' )* '"' 
    ;

// Pour MULTI_LINE_STRING : accepte EOL (\r ou \n physique)
MULTI_LINE_STRING
    : '"' ( ~('"' | '\\') | '\\' ["\\] | '\\n' )* '"'
    ;


//Identificateurs

IDENT : (LETTER | '$' | '_') (LETTER | DIGIT | '$' | '_')*;

//Séparateurs et commentaires

WS : [ \t\r\n]+ -> skip ;

LINE_COMMENT
  : '//' ~[\r\n]* -> skip
  ;

BLOCK_COMMENT
  : '/*' .*? '*/' -> skip
  ;

// Fragments

fragment DIGIT  : [0-9] ;
fragment LETTER : [a-zA-Z] ;
fragment FILENAME : (LETTER | DIGIT | '.' | '-' | '_')+;


// fragments floats
fragment NUM     : DIGIT+ ;
fragment SIGN    : [+-]? ;
fragment EXP     : [eE] SIGN NUM ;
fragment DEC     : NUM '.' NUM ;
fragment SUFFIXF : [fF]? ;

fragment FLOATDEC
  : DEC EXP? SUFFIXF
  ;

fragment DIGITHEX : [0-9a-fA-F] ;
fragment NUMHEX   : DIGITHEX+ ;

fragment FLOATHEX
  : '0' [xX] NUMHEX '.' NUMHEX [pP] SIGN NUM SUFFIXF
  ;


// fragments strings
fragment EOL
  : '\n'
  ;

fragment STRING_CAR
  : ~["\\\n]
  ;


