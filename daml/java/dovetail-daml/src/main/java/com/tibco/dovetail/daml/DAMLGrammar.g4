grammar DAMLGrammar;
 
daml					: damlMod damlAlias damlTemp+ EOF								#damlDoc													
					;
damlMod				: 'module' NAME 'where'													#damlMododule
					;

damlAlias			: 'type' NAME '=' NAME (NAME)*?;											
damlArgs				: damlArg+;
 damlArg				: NAME ':' (NAME | ARRAY) ;
 damlSig				: 'signatory' NAME (',' NAME)* ;
 damlObserver		: 'observer'  NAME (',' NAME)*;
 damlPartyRole		: (damlSig | damlObserver)+; 
 damlChoice			: NAME ':' '(' NAME? (',' NAME)* ')' ('with' damlArgs)? ;
 damlController		: 'controller' NAME 'can' damlChoice+;
 damlTemp			: 'template' NAME 
 						'with' 
 							damlArgs 
 						'where'
 							damlPartyRole
 							damlController+
 						;
 
fragment LETTER     	   	: [a-zA-Z_] ;
fragment DIGIT          : [0-9] ;
fragment LetterOrDigit 	:	[a-zA-Z0-9$_] ;
 
NAME                : LETTER LetterOrDigit*;
ARRAY				: '[' NAME ']';
 
WHITESPACE          : [ \r\n\t]+ -> skip;

COMMENT
    :   '--' ~[\r\n]* -> channel(HIDDEN)
    ;
  
 DAMLIMPORT			: 'import' (.)*? -> channel(HIDDEN);
 
 DAMLDECL			: 'daml' ~[\r\n]* -> channel(HIDDEN);

 DAMLCOND			: 'ensure' ~[\r\n]* -> channel(HIDDEN);
 
 DO					: 'do' .*? -> channel(HIDDEN);

 
 						
 
