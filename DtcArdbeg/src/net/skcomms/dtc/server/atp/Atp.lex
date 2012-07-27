package net.skcomms.dtc.server.atp;

import java_cup.runtime.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static net.skcomms.dtc.server.atp.AtpSym.*;
%%

%debug
%class AtpLex

%unicode
// %line
// %column

// %public
%final
// %abstract

%cupsym AtpSym
%cup
// %cupdebug

%init{
	// TODO: code that goes to constructor
%init}

%{

    private StringBuilder string = new StringBuilder();

    private InputStream is;
    
    public AtpLex(InputStream is, String charset) throws UnsupportedEncodingException {
        this(new InputStreamReader(is, charset));
        this.is = is;
    }
    
	private Symbol sym(int type) {
		return sym(type, yytext());
	}

	private Symbol sym(int type, Object value) {
	   //return new Symbol(type, yyline, yycolumn, value);
	   return new Symbol(type, value);
	}

	private void error()
	throws IOException {
		throw new IOException("illegal text at line = "+yyline+", column = "+yycolumn+", text = '"+yytext()+"'");
	}
	
	public byte[] getBinaryData(int size) throws IOException {
	   byte[] bytes = new byte[size];
	   if (size > 0) {
		   is.read(bytes);
	   }
	   return bytes;
	}
	
	public void print(String msg) {
	   System.out.println(msg);
	}
	
%}

LT              =   \x0A | \x1E
FT              =   \x09 | \x1F
DECIMAL         =   0 | [1-9][0-9]*
REASON          =   "SUCCESS" | "Continue"
STRING          =   ([^\x09\x0A\x1E\x1F] | \n)*

%state          BODY

%%

<YYINITIAL> {
	"ATP"          { return sym(ATP); }
	"/"            { return sym(SLASH); }
	" "            { return sym(SP); }
	"."            { return sym(DOT); }
	{REASON}       { yybegin(BODY); return sym(REASON); }
	{DECIMAL}      { return sym(DECIMAL, new Integer(yytext())); }
}

<BODY> {
	{LT}           { return sym(LT); }
	{FT}           { return sym(FT); }
	{STRING}       { return sym(STRING); }
}

