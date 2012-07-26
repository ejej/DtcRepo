package net.skcomms.dtc.server.atp;

import java_cup.runtime.*;
import java.io.IOException;

import static net.skcomms.dtc.server.atp.AtpSym.*;
%%

%debug
%class AtpLex

%unicode
%line
%column

// %public
%final
// %abstract

%cupsym AtpSym
%cup
%cupdebug

%init{
	// TODO: code that goes to constructor
%init}

%{
	private Symbol sym(int type) {
		return sym(type, yytext());
	}

	private Symbol sym(int type, Object value) {
	   System.out.println("Type:" + type + ", val:" + value);
	   return new Symbol(type, yyline, yycolumn, value);
	}

	private void error()
	throws IOException {
		throw new IOException("illegal text at line = "+yyline+", column = "+yycolumn+", text = '"+yytext()+"'");
	}
	
%}

LT              =   \x0A | \x1E
FT              =   \x09 | \x1F
DECIMAL         =   0 | [1-9][0-9]*
REASON          =   "SUCCESS" | "Continue"
STRING          =   [^\x09\x0A\x1E\x1F]*

%state          BODY

%%
{DECIMAL}          { return sym(DECIMAL); }

<YYINITIAL> {
	"ATP"          { return sym(ATP); }
	"/"            { return sym(SLASH); }
	" "            { return sym(SP); }
	"."            { return sym(DOT); }
	{REASON}       { yybegin(BODY); return sym(REASON); }
}

<BODY> {
	{LT}           { return sym(LT); }
	{FT}           { return sym(FT); }
	{STRING}       { return sym(STRING); }
}

