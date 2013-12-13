grammar GQL;

options {
  language = Java;
  output=AST;
}

@header {
  package org.jboss.capedwarf.gql4j.antlr;

  // to prevent BitSet to be ambigious
  import java.util.List;
  import java.util.LinkedList;

  import com.google.appengine.api.datastore.Query.FilterOperator;

  import org.jboss.capedwarf.gql4j.GqlQuery.*;
}

@lexer::header {
  package org.jboss.capedwarf.gql4j.antlr;

  // to prevent BitSet to be ambigious
  import java.util.List;
  import java.util.LinkedList;

  import com.google.appengine.api.datastore.Query.FilterOperator;

  import org.jboss.capedwarf.gql4j.GqlQuery.*;
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
query returns [ParseResult r]:
  {$r = new ParseResult();}
  select_clause {$r.setSelect($select_clause.r);}
  (from_clause {$r.setFrom($from_clause.r);} )?
  (where_clause {$r.setWhere($where_clause.r);} )?
  (orderby_clause {$r.setOrderBy($orderby_clause.r);})?
  (limit_clause {$r.setLimit($limit_clause.r);})?
  (offset_clause {$r.setOffset($offset_clause.r);})?
  ;

// SELECT [* | __key__]
select_clause returns [Select r]:
  SELECT
  ('*' {$r = new Select(false);}
  | '__key__' {$r = new Select(true);} )
;

// [FROM <kind>]]
from_clause returns [From r]:
  FROM IDENTITY {$r = new From($IDENTITY.text);}
;

// [WHERE <condition> [AND <condition> ...]]
where_clause returns [Where r]:
  {$r = new Where();}
  WHERE (c1=condition {$r.withCondition(c1.r);} | a1=ancestorCondition {$r.withAncestor(a1.r);})
    (AND (c2=condition {$r.withCondition(c2.r);} | a2=ancestorCondition {$r.withAncestor(a2.r);} )) *
;

// [ORDER BY <property> [ASC | DESC] [, <property> [ASC | DESC] ...]]
orderby_clause returns [OrderBy r]:
  {$r = new OrderBy();}
  ORDER BY i1=IDENTITY {OrderByItem it = new OrderByItem($i1.text);}  (ASC | d1=DESC {it.setDirection(false);})?  {$r.withItem(it);}
  (',' i2=IDENTITY {OrderByItem it2 = new OrderByItem($i2.text);} (ASC | d2=DESC {it2.setDirection(false);})? {$r.withItem(it2);}  )*
;

// [LIMIT [<offset>,]<count>], we discard offset here
limit_clause returns [Limit r]:
  LIMIT DECIMAL {$r = new Limit(Integer.valueOf($DECIMAL.text));}
;

// [OFFSET <offset>]
offset_clause returns [Offset r]:
  OFFSET DECIMAL {$r = new Offset(Integer.valueOf($DECIMAL.text));}
;

condition returns [Condition r]
  : // <condition> := <property> {< | <= | > | >= | = | != } <value>
  (i=IDENTITY '<' v=value    {$r = new Condition($i.text, FilterOperator.LESS_THAN, $v.r);}
  |i=IDENTITY '<=' v=value  {$r = new Condition($i.text, FilterOperator.LESS_THAN_OR_EQUAL, $v.r);}
  |i=IDENTITY '>' v=value   {$r = new Condition($i.text, FilterOperator.GREATER_THAN, $v.r);}
  |i=IDENTITY '>=' v=value  {$r = new Condition($i.text, FilterOperator.GREATER_THAN_OR_EQUAL, $v.r);}
  |i=IDENTITY '=' v=value   {$r = new Condition($i.text, FilterOperator.EQUAL, $v.r);}
  |i=IDENTITY '!=' v=value  {$r = new Condition($i.text, FilterOperator.NOT_EQUAL, $v.r);}
  )
  | // <condition> := <property> IN <list>
  ( IDENTITY IN list  {$r = new Condition($IDENTITY.text, FilterOperator.IN, new ListEvaluator($list.r));} )
;

// <condition> := ANCESTOR IS <entity or key>
ancestorCondition returns [Evaluator r]:
  ANCESTOR IS value {$r = $value.r;}
;

value returns [Evaluator r]
  : NULL  {$r = NullEvaluator.get();}
  | d=DECIMAL {$r = new DecimalEvaluator($d.text);}
  | STRING_LITERAL {$r = new StringEvaluator($STRING_LITERAL.text);}
  | BOOLEAN {$r = new BooleanEvaluator($BOOLEAN.text);}
  | ':' IDENTITY {$r = new ParamEvaluator($IDENTITY.text);}
  | ':' d=DECIMAL {$r = new ParamEvaluator($d.text);}
  | FUNCTION list {$r = new FunctionEvaluator($FUNCTION.text, $list.r);}
;

// (<value> [, <value> ...]])
list returns [List r] :
  {$r = new LinkedList<Evaluator>();}
  '(' v1=value {$r.add($v1.r);} (',' v2=value {$r.add($v2.r);} )* ')'
;

/*------------------------------------------------------------------
 * CASE INSENSITIVE LEXER RULES
 *------------------------------------------------------------------*/

// =====================keywords========================
SELECT : S E L E C T;

FROM : F R O M;

WHERE : W H E R E;

ORDER : O R D E R;

BY : B Y;

ASC : A S C;

DESC : D E S C;

LIMIT : L I M I T;

OFFSET : O F F S E T;

ANCESTOR: A N C E S T O R;

IS: I S;

// =====================functions========================
FUNCTION
  // DATETIME(year, month, day, hour, minute, second)
  // DATETIME('YYYY-MM-DD HH:MM:SS')
  : D A T E T I M E
  // DATE(year, month, day)
  // DATE('YYYY-MM-DD')
  | D A T E
  // TIME(hour, minute, second)
  // TIME('HH:MM:SS')
  | T I M E
  // KEY('encoded key')
  // KEY('kind', 'name'/ID [, 'kind', 'name'/ID...])
  | K E Y
  // USER('email-address')
  | U S E R
  // GEOPT(lat, long)
  | G E O P T
;

// =====================relations========================
AND: A N D;

IN: I N;

/*------------------------------------------------------------------
 * BASIC LEXER RULES
 *------------------------------------------------------------------*/
// =====================premitive type========================
STRING_LITERAL
  : '\''
    ( '\\' '\''
    | ~('\''|'\n'|'\r')
    )*
    '\''  // ends with single quote
;

fragment A : 'a' | 'A';
fragment B : 'b' | 'B';
fragment C : 'c' | 'C';
fragment D : 'd' | 'D';
fragment E : 'e' | 'E';
fragment F : 'f' | 'F';
fragment G : 'g' | 'G';
fragment H : 'h' | 'H';
fragment I : 'i' | 'I';
fragment J : 'j' | 'J';
fragment K : 'k' | 'K';
fragment L : 'l' | 'L';
fragment M : 'm' | 'M';
fragment N : 'n' | 'N';
fragment O : 'o' | 'O';
fragment P : 'p' | 'P';
fragment Q : 'q' | 'Q';
fragment R : 'r' | 'R';
fragment S : 's' | 'S';
fragment T : 't' | 'T';
fragment U : 'u' | 'U';
fragment V : 'v' | 'V';
fragment W : 'w' | 'W';
fragment X : 'x' | 'X';
fragment Y : 'y' | 'Y';
fragment Z : 'z' | 'Z';
fragment LETTER : 'a'..'z' | 'A'..'Z';

fragment DIGIT : '0'..'9';

fragment DOT : '.';

DECIMAL : DIGIT * (DOT DIGIT+)?;

BOOLEAN : T R U E | F A L S E;

NULL : N U L L;

// IDENTITY matches both kind name and property name
IDENTITY
  : (LETTER | '_') (LETTER | DIGIT | '_')*
;

WHITESPACE : ( '\t' | ' ' | '\r' | '\n' | '\f' )+  { $channel = HIDDEN; };

COMMENT : '//' .* ('\n'|'\r') { $channel = HIDDEN; };

MULTILINE_COMMENT : '/*' .* '*/' { $channel = HIDDEN; };
