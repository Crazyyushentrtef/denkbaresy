// $ANTLR 3.1 D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g 2009-01-18 18:29:13

package de.d3web.KnOfficeParser.scmcbr;
import de.d3web.KnOfficeParser.ParserErrorHandler;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/**
 * Grammatik für SCMCBR Dateien
 * @author Markus Friedrich
 *
 */
public class SCMCBR extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "BLUB", "Tokens", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72"
    };
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int LP=15;
    public static final int FUZZY=53;
    public static final int ABSTRACT=50;
    public static final int NOT=37;
    public static final int EXCEPT=39;
    public static final int AND=35;
    public static final int ID=56;
    public static final int DD=7;
    public static final int EOF=-1;
    public static final int IF=33;
    public static final int AT=11;
    public static final int THEN=34;
    public static final int IN=44;
    public static final int UNKNOWN=40;
    public static final int EX=10;
    public static final int COMMA=8;
    public static final int INCLUDE=48;
    public static final int ALL=46;
    public static final int PROD=28;
    public static final int TILDE=14;
    public static final int PLUS=26;
    public static final int String=4;
    public static final int NL=32;
    public static final int EQ=25;
    public static final int DOT=6;
    public static final int COMMENT=31;
    public static final int GE=23;
    public static final int G=24;
    public static final int SBC=20;
    public static final int ALLOWEDNAMES=47;
    public static final int L=22;
    public static final int INSTANT=42;
    public static final int NS=13;
    public static final int MINMAX=43;
    public static final int DEFAULT=49;
    public static final int INTER=45;
    public static final int KNOWN=41;
    public static final int SET=51;
    public static final int INT=5;
    public static final int MINUS=27;
    public static final int DIVNORM=55;
    public static final int Tokens=58;
    public static final int SEMI=9;
    public static final int REF=52;
    public static final int WS=30;
    public static final int BLUB=57;
    public static final int OR=36;
    public static final int CBC=18;
    public static final int SBO=19;
    public static final int DIVTEXT=54;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;

    // delegates
    public SCMCBR_BasicParser gBasicParser;
    // delegators


        public SCMCBR(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public SCMCBR(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            gBasicParser = new SCMCBR_BasicParser(input, state, this);         
        }
        

    public String[] getTokenNames() { return SCMCBR.tokenNames; }
    public String getGrammarFileName() { return "D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g"; }


      private SCMCBRBuilder builder;
      private ParserErrorHandler eh;
      
      public SCMCBR(CommonTokenStream tokens, SCMCBRBuilder builder, ParserErrorHandler eh) {
        this(tokens);
        this.builder=builder;
        this.eh=eh;
        gBasicParser.setEH(eh);
        eh.setTokenNames(tokenNames);
      }
      
      public void setBuilder(SCMCBRBuilder builder) {
        this.builder = builder;
      }
      
      public SCMCBRBuilder getBuilder() {
        return builder;
      }
      
      private String delQuotes(String s) {
        s=s.substring(1, s.length()-1);
        s=s.replace("\\\"", "\"");
        return s;
      }
      
      private Double parseGerDouble(String s) {
        s=s.replace(',', '.');
        return Double.parseDouble(s);
      }
      
      @Override
      public void reportError(RecognitionException re) {
        if (eh!=null) {
          eh.parsererror(re);
        } else {
          super.reportError(re);
        }
      }



    // $ANTLR start "knowledge"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:57:1: knowledge : ( solution | NL )* ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:57:10: ( ( solution | NL )* )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:58:1: ( solution | NL )*
            {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:58:1: ( solution | NL )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=String && LA1_0<=INT)||LA1_0==ID) ) {
                    alt1=1;
                }
                else if ( (LA1_0==NL) ) {
                    alt1=2;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:58:2: solution
            	    {
            	    pushFollow(FOLLOW_solution_in_knowledge52);
            	    solution();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:58:11: NL
            	    {
            	    match(input,NL,FOLLOW_NL_in_knowledge54); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "knowledge"

    public static class solution_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "solution"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:60:1: solution : name DD NL ( line NL )* ;
    public final SCMCBR.solution_return solution() throws RecognitionException {
        SCMCBR.solution_return retval = new SCMCBR.solution_return();
        retval.start = input.LT(1);

        SCMCBR_BasicParser.name_return name1 = null;


        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:60:9: ( name DD NL ( line NL )* )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:61:1: name DD NL ( line NL )*
            {
            pushFollow(FOLLOW_name_in_solution63);
            name1=name();

            state._fsp--;

            builder.solution(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name1!=null?name1.value:null));
            match(input,DD,FOLLOW_DD_in_solution66); 
            match(input,NL,FOLLOW_NL_in_solution68); 
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:61:69: ( line NL )*
            loop2:
            do {
                int alt2=2;
                alt2 = dfa2.predict(input);
                switch (alt2) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:61:70: line NL
            	    {
            	    pushFollow(FOLLOW_line_in_solution71);
            	    line();

            	    state._fsp--;

            	    match(input,NL,FOLLOW_NL_in_solution73); 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "solution"

    public static class line_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "line"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:63:1: line : ( name ( assign SEMI )* assign | name DD d3double | name CBO a= d3double b= d3double CBC | name SBO INT SBC );
    public final SCMCBR.line_return line() throws RecognitionException {
        SCMCBR.line_return retval = new SCMCBR.line_return();
        retval.start = input.LT(1);

        SCMCBR_BasicParser.d3double_return a = null;

        SCMCBR_BasicParser.d3double_return b = null;

        SCMCBR_BasicParser.name_return name2 = null;

        SCMCBR_BasicParser.name_return name3 = null;

        SCMCBR_BasicParser.d3double_return d3double4 = null;

        SCMCBR_BasicParser.name_return name5 = null;

        SCMCBR_BasicParser.name_return name6 = null;


        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:63:5: ( name ( assign SEMI )* assign | name DD d3double | name CBO a= d3double b= d3double CBC | name SBO INT SBC )
            int alt4=4;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:64:1: name ( assign SEMI )* assign
                    {
                    pushFollow(FOLLOW_name_in_line82);
                    name2=name();

                    state._fsp--;

                    builder.question(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name2!=null?name2.value:null));
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:64:64: ( assign SEMI )*
                    loop3:
                    do {
                        int alt3=2;
                        alt3 = dfa3.predict(input);
                        switch (alt3) {
                    	case 1 :
                    	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:64:65: assign SEMI
                    	    {
                    	    pushFollow(FOLLOW_assign_in_line87);
                    	    assign();

                    	    state._fsp--;

                    	    match(input,SEMI,FOLLOW_SEMI_in_line89); 

                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);

                    pushFollow(FOLLOW_assign_in_line93);
                    assign();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:65:3: name DD d3double
                    {
                    pushFollow(FOLLOW_name_in_line97);
                    name3=name();

                    state._fsp--;

                    match(input,DD,FOLLOW_DD_in_line99); 
                    pushFollow(FOLLOW_d3double_in_line101);
                    d3double4=d3double();

                    state._fsp--;

                    builder.setAmount(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name3!=null?name3.value:null), (d3double4!=null?d3double4.value:null));

                    }
                    break;
                case 3 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:66:3: name CBO a= d3double b= d3double CBC
                    {
                    pushFollow(FOLLOW_name_in_line107);
                    name5=name();

                    state._fsp--;

                    match(input,CBO,FOLLOW_CBO_in_line109); 
                    pushFollow(FOLLOW_d3double_in_line113);
                    a=d3double();

                    state._fsp--;

                    pushFollow(FOLLOW_d3double_in_line117);
                    b=d3double();

                    state._fsp--;

                    match(input,CBC,FOLLOW_CBC_in_line119); 
                    builder.threshold(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name5!=null?name5.value:null), (a!=null?a.value:null), (b!=null?b.value:null));

                    }
                    break;
                case 4 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:67:3: name SBO INT SBC
                    {
                    pushFollow(FOLLOW_name_in_line125);
                    name6=name();

                    state._fsp--;

                    match(input,SBO,FOLLOW_SBO_in_line127); 
                    match(input,INT,FOLLOW_INT_in_line129); 
                    match(input,SBC,FOLLOW_SBC_in_line131); 
                    builder.questionclass(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name6!=null?name6.value:null));

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "line"

    public static class assign_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "assign"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:69:1: assign : ( eq name ( weight )? | IN ( LP | c= SBO ) a= values ( RP | d= SBC ) SBO b= values SBC | INTER ( LP | c= SBO ) a= values ( RP | d= SBC ) CBO b= values CBC | ( AND | c= OR ) LP names RP ( weight )? | NOT name ( weight )? );
    public final SCMCBR.assign_return assign() throws RecognitionException {
        SCMCBR.assign_return retval = new SCMCBR.assign_return();
        retval.start = input.LT(1);

        Token c=null;
        Token d=null;
        List<Double> a = null;

        List<Double> b = null;

        SCMCBR_BasicParser.name_return name7 = null;

        SCMCBR.weight_return weight8 = null;

        SCMCBR_BasicParser.eq_return eq9 = null;

        List<String> names10 = null;

        SCMCBR.weight_return weight11 = null;

        SCMCBR_BasicParser.name_return name12 = null;

        SCMCBR.weight_return weight13 = null;


        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:69:7: ( eq name ( weight )? | IN ( LP | c= SBO ) a= values ( RP | d= SBC ) SBO b= values SBC | INTER ( LP | c= SBO ) a= values ( RP | d= SBC ) CBO b= values CBC | ( AND | c= OR ) LP names RP ( weight )? | NOT name ( weight )? )
            int alt13=5;
            switch ( input.LA(1) ) {
            case LE:
            case L:
            case GE:
            case G:
            case EQ:
                {
                alt13=1;
                }
                break;
            case IN:
                {
                alt13=2;
                }
                break;
            case INTER:
                {
                alt13=3;
                }
                break;
            case AND:
            case OR:
                {
                alt13=4;
                }
                break;
            case NOT:
                {
                alt13=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }

            switch (alt13) {
                case 1 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:70:1: eq name ( weight )?
                    {
                    pushFollow(FOLLOW_eq_in_assign140);
                    eq9=eq();

                    state._fsp--;

                    pushFollow(FOLLOW_name_in_assign142);
                    name7=name();

                    state._fsp--;

                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:70:9: ( weight )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==SBO) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:70:9: weight
                            {
                            pushFollow(FOLLOW_weight_in_assign144);
                            weight8=weight();

                            state._fsp--;


                            }
                            break;

                    }

                    builder.answer(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name7!=null?name7.value:null), (weight8!=null?weight8.value:null), (eq9!=null?input.toString(eq9.start,eq9.stop):null));

                    }
                    break;
                case 2 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:71:3: IN ( LP | c= SBO ) a= values ( RP | d= SBC ) SBO b= values SBC
                    {
                    match(input,IN,FOLLOW_IN_in_assign151); 
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:71:6: ( LP | c= SBO )
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==LP) ) {
                        alt6=1;
                    }
                    else if ( (LA6_0==SBO) ) {
                        alt6=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 0, input);

                        throw nvae;
                    }
                    switch (alt6) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:71:7: LP
                            {
                            match(input,LP,FOLLOW_LP_in_assign154); 

                            }
                            break;
                        case 2 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:71:10: c= SBO
                            {
                            c=(Token)match(input,SBO,FOLLOW_SBO_in_assign158); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_values_in_assign163);
                    a=values();

                    state._fsp--;

                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:71:26: ( RP | d= SBC )
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==RP) ) {
                        alt7=1;
                    }
                    else if ( (LA7_0==SBC) ) {
                        alt7=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 0, input);

                        throw nvae;
                    }
                    switch (alt7) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:71:27: RP
                            {
                            match(input,RP,FOLLOW_RP_in_assign166); 

                            }
                            break;
                        case 2 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:71:30: d= SBC
                            {
                            d=(Token)match(input,SBC,FOLLOW_SBC_in_assign170); 

                            }
                            break;

                    }

                    match(input,SBO,FOLLOW_SBO_in_assign173); 
                    pushFollow(FOLLOW_values_in_assign177);
                    b=values();

                    state._fsp--;

                    match(input,SBC,FOLLOW_SBC_in_assign179); 
                    builder.in(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), a, b, (c!=null), (d!=null));

                    }
                    break;
                case 3 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:72:3: INTER ( LP | c= SBO ) a= values ( RP | d= SBC ) CBO b= values CBC
                    {
                    match(input,INTER,FOLLOW_INTER_in_assign185); 
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:72:9: ( LP | c= SBO )
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==LP) ) {
                        alt8=1;
                    }
                    else if ( (LA8_0==SBO) ) {
                        alt8=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 0, input);

                        throw nvae;
                    }
                    switch (alt8) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:72:10: LP
                            {
                            match(input,LP,FOLLOW_LP_in_assign188); 

                            }
                            break;
                        case 2 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:72:13: c= SBO
                            {
                            c=(Token)match(input,SBO,FOLLOW_SBO_in_assign192); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_values_in_assign197);
                    a=values();

                    state._fsp--;

                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:72:29: ( RP | d= SBC )
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==RP) ) {
                        alt9=1;
                    }
                    else if ( (LA9_0==SBC) ) {
                        alt9=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 9, 0, input);

                        throw nvae;
                    }
                    switch (alt9) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:72:30: RP
                            {
                            match(input,RP,FOLLOW_RP_in_assign200); 

                            }
                            break;
                        case 2 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:72:33: d= SBC
                            {
                            d=(Token)match(input,SBC,FOLLOW_SBC_in_assign204); 

                            }
                            break;

                    }

                    match(input,CBO,FOLLOW_CBO_in_assign207); 
                    pushFollow(FOLLOW_values_in_assign211);
                    b=values();

                    state._fsp--;

                    match(input,CBC,FOLLOW_CBC_in_assign213); 
                    builder.into(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), a, b, (c!=null), (d!=null));

                    }
                    break;
                case 4 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:73:3: ( AND | c= OR ) LP names RP ( weight )?
                    {
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:73:3: ( AND | c= OR )
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==AND) ) {
                        alt10=1;
                    }
                    else if ( (LA10_0==OR) ) {
                        alt10=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 0, input);

                        throw nvae;
                    }
                    switch (alt10) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:73:4: AND
                            {
                            match(input,AND,FOLLOW_AND_in_assign220); 

                            }
                            break;
                        case 2 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:73:8: c= OR
                            {
                            c=(Token)match(input,OR,FOLLOW_OR_in_assign224); 

                            }
                            break;

                    }

                    match(input,LP,FOLLOW_LP_in_assign227); 
                    pushFollow(FOLLOW_names_in_assign229);
                    names10=names();

                    state._fsp--;

                    match(input,RP,FOLLOW_RP_in_assign231); 
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:73:26: ( weight )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==SBO) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:73:26: weight
                            {
                            pushFollow(FOLLOW_weight_in_assign233);
                            weight11=weight();

                            state._fsp--;


                            }
                            break;

                    }

                    builder.and(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), names10, (weight11!=null?weight11.value:null), (c!=null));

                    }
                    break;
                case 5 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:74:3: NOT name ( weight )?
                    {
                    match(input,NOT,FOLLOW_NOT_in_assign240); 
                    pushFollow(FOLLOW_name_in_assign242);
                    name12=name();

                    state._fsp--;

                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:74:12: ( weight )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==SBO) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:74:12: weight
                            {
                            pushFollow(FOLLOW_weight_in_assign244);
                            weight13=weight();

                            state._fsp--;


                            }
                            break;

                    }

                    builder.not(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name12!=null?name12.value:null), (weight13!=null?weight13.value:null));

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assign"


    // $ANTLR start "names"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:76:1: names returns [List<String> values] : (a= name COMMA )* b= name ;
    public final List<String> names() throws RecognitionException {
        List<String> values = null;

        SCMCBR_BasicParser.name_return a = null;

        SCMCBR_BasicParser.name_return b = null;


        values = new ArrayList<String>();
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:78:1: ( (a= name COMMA )* b= name )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:78:4: (a= name COMMA )* b= name
            {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:78:4: (a= name COMMA )*
            loop14:
            do {
                int alt14=2;
                alt14 = dfa14.predict(input);
                switch (alt14) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:78:5: a= name COMMA
            	    {
            	    pushFollow(FOLLOW_name_in_names268);
            	    a=name();

            	    state._fsp--;

            	    match(input,COMMA,FOLLOW_COMMA_in_names270); 
            	    values.add((a!=null?input.toString(a.start,a.stop):null));

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            pushFollow(FOLLOW_name_in_names278);
            b=name();

            state._fsp--;

            values.add((b!=null?input.toString(b.start,b.stop):null));

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return values;
    }
    // $ANTLR end "names"


    // $ANTLR start "values"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:80:1: values returns [List<Double> values] : (a= d3double )+ ;
    public final List<Double> values() throws RecognitionException {
        List<Double> values = null;

        SCMCBR_BasicParser.d3double_return a = null;


        values = new ArrayList<Double>();
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:82:1: ( (a= d3double )+ )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:82:3: (a= d3double )+
            {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:82:3: (a= d3double )+
            int cnt15=0;
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==INT||LA15_0==MINUS) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:82:4: a= d3double
            	    {
            	    pushFollow(FOLLOW_d3double_in_values300);
            	    a=d3double();

            	    state._fsp--;

            	    values.add((a!=null?a.value:null));

            	    }
            	    break;

            	default :
            	    if ( cnt15 >= 1 ) break loop15;
                        EarlyExitException eee =
                            new EarlyExitException(15, input);
                        throw eee;
                }
                cnt15++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return values;
    }
    // $ANTLR end "values"

    public static class weight_return extends ParserRuleReturnScope {
        public String value;
    };

    // $ANTLR start "weight"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:84:1: weight returns [String value] : SBO ( PLUS | MINUS | EX )? ( INT )? SBC ;
    public final SCMCBR.weight_return weight() throws RecognitionException {
        SCMCBR.weight_return retval = new SCMCBR.weight_return();
        retval.start = input.LT(1);

        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:84:30: ( SBO ( PLUS | MINUS | EX )? ( INT )? SBC )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:85:1: SBO ( PLUS | MINUS | EX )? ( INT )? SBC
            {
            match(input,SBO,FOLLOW_SBO_in_weight315); 
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:85:5: ( PLUS | MINUS | EX )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==EX||(LA16_0>=PLUS && LA16_0<=MINUS)) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:
                    {
                    if ( input.LA(1)==EX||(input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;

            }

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:85:22: ( INT )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==INT) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\SCMCBR.g:85:22: INT
                    {
                    match(input,INT,FOLLOW_INT_in_weight326); 

                    }
                    break;

            }

            match(input,SBC,FOLLOW_SBC_in_weight329); 
            retval.value =input.toString(retval.start,input.LT(-1)).substring(1,input.toString(retval.start,input.LT(-1)).length()-1);

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "weight"

    // Delegated rules
    public String type() throws RecognitionException { return gBasicParser.type(); }
    public SCMCBR_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }
    public SCMCBR_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }
    public void eqncalc() throws RecognitionException { gBasicParser.eqncalc(); }
    public SCMCBR_BasicParser.eq_return eq() throws RecognitionException { return gBasicParser.eq(); }


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA4 dfa4 = new DFA4(this);
    protected DFA3 dfa3 = new DFA3(this);
    protected DFA14 dfa14 = new DFA14(this);
    static final String DFA2_eotS =
        "\10\uffff";
    static final String DFA2_eofS =
        "\1\1\7\uffff";
    static final String DFA2_minS =
        "\1\4\1\uffff\2\4\1\5\1\4\1\uffff\1\4";
    static final String DFA2_maxS =
        "\1\70\1\uffff\2\70\1\40\1\70\1\uffff\1\70";
    static final String DFA2_acceptS =
        "\1\uffff\1\2\4\uffff\1\1\1\uffff";
    static final String DFA2_specialS =
        "\10\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\2\1\3\32\uffff\1\1\27\uffff\1\3",
            "",
            "\1\5\1\3\1\uffff\1\4\11\uffff\1\6\1\uffff\1\6\1\uffff\5\6"+
            "\11\uffff\3\6\6\uffff\2\6\12\uffff\1\3",
            "\2\7\1\uffff\1\4\11\uffff\1\6\1\uffff\1\6\1\uffff\5\6\11\uffff"+
            "\3\6\6\uffff\2\6\12\uffff\1\7",
            "\1\6\25\uffff\1\6\4\uffff\1\1",
            "\1\5\1\3\62\uffff\1\3",
            "",
            "\2\7\1\uffff\1\4\11\uffff\1\6\1\uffff\1\6\1\uffff\5\6\11\uffff"+
            "\3\6\6\uffff\2\6\12\uffff\1\7"
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "()* loopback of 61:69: ( line NL )*";
        }
    }
    static final String DFA4_eotS =
        "\11\uffff";
    static final String DFA4_eofS =
        "\11\uffff";
    static final String DFA4_minS =
        "\4\4\4\uffff\1\4";
    static final String DFA4_maxS =
        "\4\70\4\uffff\1\70";
    static final String DFA4_acceptS =
        "\4\uffff\1\4\1\3\1\2\1\1\1\uffff";
    static final String DFA4_specialS =
        "\11\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1\1\2\62\uffff\1\2",
            "\1\3\1\2\1\uffff\1\6\11\uffff\1\5\1\uffff\1\4\1\uffff\5\7"+
            "\11\uffff\3\7\6\uffff\2\7\12\uffff\1\2",
            "\2\10\1\uffff\1\6\11\uffff\1\5\1\uffff\1\4\1\uffff\5\7\11"+
            "\uffff\3\7\6\uffff\2\7\12\uffff\1\10",
            "\1\3\1\2\62\uffff\1\2",
            "",
            "",
            "",
            "",
            "\2\10\1\uffff\1\6\11\uffff\1\5\1\uffff\1\4\1\uffff\5\7\11"+
            "\uffff\3\7\6\uffff\2\7\12\uffff\1\10"
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "63:1: line : ( name ( assign SEMI )* assign | name DD d3double | name CBO a= d3double b= d3double CBC | name SBO INT SBC );";
        }
    }
    static final String DFA3_eotS =
        "\100\uffff";
    static final String DFA3_eofS =
        "\100\uffff";
    static final String DFA3_minS =
        "\1\25\1\4\4\17\3\4\4\5\3\4\1\5\1\uffff\1\4\1\uffff\1\4\4\5\2\4"+
        "\1\5\2\4\1\5\1\24\1\11\1\5\2\23\1\5\2\21\1\4\1\11\2\4\1\5\1\24\1"+
        "\11\12\5\1\24\1\11\1\5\1\11\1\5\1\11\2\5";
    static final String DFA3_maxS =
        "\1\55\1\70\2\23\2\17\3\70\4\33\3\70\1\33\1\uffff\1\70\1\uffff\1"+
        "\70\1\5\1\33\1\5\1\33\2\70\1\33\2\70\2\24\1\40\1\5\2\23\1\5\2\21"+
        "\1\70\1\40\2\70\2\24\1\40\5\33\1\5\1\33\1\5\1\33\2\24\1\40\1\5\1"+
        "\40\1\5\1\40\2\33";
    static final String DFA3_acceptS =
        "\21\uffff\1\2\1\uffff\1\1\54\uffff";
    static final String DFA3_specialS =
        "\100\uffff}>";
    static final String[] DFA3_transitionS = {
            "\5\1\11\uffff\1\4\1\5\1\6\6\uffff\1\2\1\3",
            "\1\7\1\10\62\uffff\1\10",
            "\1\11\3\uffff\1\12",
            "\1\13\3\uffff\1\14",
            "\1\15",
            "\1\15",
            "\1\16\1\17\62\uffff\1\17",
            "\1\22\1\10\3\uffff\1\23\11\uffff\1\20\14\uffff\1\21\27\uffff"+
            "\1\10",
            "\2\24\3\uffff\1\23\11\uffff\1\20\14\uffff\1\21\27\uffff\1"+
            "\24",
            "\1\26\25\uffff\1\25",
            "\1\26\25\uffff\1\25",
            "\1\30\25\uffff\1\27",
            "\1\30\25\uffff\1\27",
            "\1\31\1\32\62\uffff\1\32",
            "\1\34\1\17\3\uffff\1\23\11\uffff\1\33\14\uffff\1\21\27\uffff"+
            "\1\17",
            "\2\35\3\uffff\1\23\11\uffff\1\33\14\uffff\1\21\27\uffff\1"+
            "\35",
            "\1\37\4\uffff\1\36\11\uffff\1\40\5\uffff\2\36",
            "",
            "\1\22\1\10\62\uffff\1\10",
            "",
            "\2\24\3\uffff\1\23\11\uffff\1\20\14\uffff\1\21\27\uffff\1"+
            "\24",
            "\1\26",
            "\1\26\1\41\1\uffff\1\41\7\uffff\1\42\3\uffff\1\43\6\uffff"+
            "\1\25",
            "\1\30",
            "\1\30\1\44\1\uffff\1\44\7\uffff\1\45\3\uffff\1\46\6\uffff"+
            "\1\27",
            "\1\47\1\32\2\uffff\1\51\7\uffff\1\50\47\uffff\1\32",
            "\2\52\2\uffff\1\51\7\uffff\1\50\47\uffff\1\52",
            "\1\54\4\uffff\1\53\11\uffff\1\55\5\uffff\2\53",
            "\1\34\1\17\62\uffff\1\17",
            "\2\35\3\uffff\1\23\11\uffff\1\33\14\uffff\1\21\27\uffff\1"+
            "\35",
            "\1\37\16\uffff\1\40",
            "\1\40",
            "\1\23\26\uffff\1\21",
            "\1\56",
            "\1\57",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\61",
            "\1\47\1\32\62\uffff\1\32",
            "\1\23\11\uffff\1\62\14\uffff\1\21",
            "\1\31\1\32\62\uffff\1\32",
            "\2\52\2\uffff\1\51\7\uffff\1\50\47\uffff\1\52",
            "\1\54\16\uffff\1\55",
            "\1\55",
            "\1\23\26\uffff\1\21",
            "\1\26\12\uffff\1\42\3\uffff\1\43\6\uffff\1\25",
            "\1\64\25\uffff\1\63",
            "\1\30\12\uffff\1\45\3\uffff\1\46\6\uffff\1\27",
            "\1\66\25\uffff\1\65",
            "\1\70\4\uffff\1\67\11\uffff\1\71\5\uffff\2\67",
            "\1\64",
            "\1\64\1\72\1\uffff\1\72\13\uffff\1\73\6\uffff\1\63",
            "\1\66",
            "\1\66\1\74\1\uffff\1\74\11\uffff\1\75\10\uffff\1\65",
            "\1\70\16\uffff\1\71",
            "\1\71",
            "\1\23\26\uffff\1\21",
            "\1\76",
            "\1\23\26\uffff\1\21",
            "\1\77",
            "\1\23\26\uffff\1\21",
            "\1\64\16\uffff\1\73\6\uffff\1\63",
            "\1\66\14\uffff\1\75\10\uffff\1\65"
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "()* loopback of 64:64: ( assign SEMI )*";
        }
    }
    static final String DFA14_eotS =
        "\7\uffff";
    static final String DFA14_eofS =
        "\7\uffff";
    static final String DFA14_minS =
        "\4\4\2\uffff\1\4";
    static final String DFA14_maxS =
        "\4\70\2\uffff\1\70";
    static final String DFA14_acceptS =
        "\4\uffff\1\2\1\1\1\uffff";
    static final String DFA14_specialS =
        "\7\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\1\1\2\62\uffff\1\2",
            "\1\3\1\2\2\uffff\1\5\7\uffff\1\4\47\uffff\1\2",
            "\2\6\2\uffff\1\5\7\uffff\1\4\47\uffff\1\6",
            "\1\3\1\2\62\uffff\1\2",
            "",
            "",
            "\2\6\2\uffff\1\5\7\uffff\1\4\47\uffff\1\6"
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "()* loopback of 78:4: (a= name COMMA )*";
        }
    }
 

    public static final BitSet FOLLOW_solution_in_knowledge52 = new BitSet(new long[]{0x0100000100000032L});
    public static final BitSet FOLLOW_NL_in_knowledge54 = new BitSet(new long[]{0x0100000100000032L});
    public static final BitSet FOLLOW_name_in_solution63 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_solution66 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_solution68 = new BitSet(new long[]{0x0100000000000032L});
    public static final BitSet FOLLOW_line_in_solution71 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_solution73 = new BitSet(new long[]{0x0100000000000032L});
    public static final BitSet FOLLOW_name_in_line82 = new BitSet(new long[]{0x0000303803E00000L});
    public static final BitSet FOLLOW_assign_in_line87 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_line89 = new BitSet(new long[]{0x0000303803E00000L});
    public static final BitSet FOLLOW_assign_in_line93 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_line97 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_line99 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_line101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_line107 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_line109 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_line113 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_line117 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CBC_in_line119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_line125 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_line127 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_line129 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_line131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_eq_in_assign140 = new BitSet(new long[]{0x0100000000080030L});
    public static final BitSet FOLLOW_name_in_assign142 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_weight_in_assign144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_assign151 = new BitSet(new long[]{0x0000000000088000L});
    public static final BitSet FOLLOW_LP_in_assign154 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_SBO_in_assign158 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_values_in_assign163 = new BitSet(new long[]{0x0000000000110000L});
    public static final BitSet FOLLOW_RP_in_assign166 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBC_in_assign170 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_assign173 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_values_in_assign177 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_assign179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTER_in_assign185 = new BitSet(new long[]{0x0000000000088000L});
    public static final BitSet FOLLOW_LP_in_assign188 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_SBO_in_assign192 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_values_in_assign197 = new BitSet(new long[]{0x0000000000110000L});
    public static final BitSet FOLLOW_RP_in_assign200 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_SBC_in_assign204 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_assign207 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_values_in_assign211 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CBC_in_assign213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_assign220 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_OR_in_assign224 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_assign227 = new BitSet(new long[]{0x0100000000000130L});
    public static final BitSet FOLLOW_names_in_assign229 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_assign231 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_weight_in_assign233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_assign240 = new BitSet(new long[]{0x0100000000080030L});
    public static final BitSet FOLLOW_name_in_assign242 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_weight_in_assign244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_names268 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_names270 = new BitSet(new long[]{0x0100000000000130L});
    public static final BitSet FOLLOW_name_in_names278 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_d3double_in_values300 = new BitSet(new long[]{0x0000000008000022L});
    public static final BitSet FOLLOW_SBO_in_weight315 = new BitSet(new long[]{0x000000000C100420L});
    public static final BitSet FOLLOW_set_in_weight317 = new BitSet(new long[]{0x0000000000100020L});
    public static final BitSet FOLLOW_INT_in_weight326 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_weight329 = new BitSet(new long[]{0x0000000000000002L});

}