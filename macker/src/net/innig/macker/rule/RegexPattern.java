/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */
 
package net.innig.macker.rule;

import net.innig.macker.structure.ClassInfo;

import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public class RegexPattern
    extends Pattern
    {
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public RegexPattern(String regexStr)
        throws RegexPatternSyntaxException
        { setMatchString(regexStr); }
        
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    public String getMatchString()
        { return regexStr; }

    public void setMatchString(String regexStr)
        throws RegexPatternSyntaxException
        {
        buildStaticPatterns();
        if(!allowable.match(regexStr))
            throw new RegexPatternSyntaxException(regexStr);
        this.regexStr = regexStr;
        parts = null;
        regex = null;
        }
    
    private String regexStr;

    //--------------------------------------------------------------------------
    // Evaluation
    //--------------------------------------------------------------------------

    public boolean matches(EvaluationContext context, ClassInfo classInfo)
        throws RulesException
        {
        parseExpr(context);
        return regex.match('.' + classInfo.getClassName());
        }
    
    public String getParen(EvaluationContext context, ClassInfo classInfo)
        throws RulesException
        {
        if(matches(context,classInfo))
            return regex.getParen(regex.getParenCount() - 1);
        else
            return null;
        }
    
    private void parseExpr(EvaluationContext context)
        throws UndeclaredVariableException, RegexPatternSyntaxException
        {
        buildStaticPatterns();
        
        if(parts == null)
            {
            parts = new ArrayList();
            for(int pos = 0; pos >= 0; )
                {
                boolean hasAnotherVar = var.match(regexStr, pos);
                int expEnd = hasAnotherVar ? var.getParenStart(0) : regexStr.length();
                
                if(pos < expEnd)
                    parts.add(new ExpPart(parseSubexpr(regexStr.substring(pos, expEnd))));
                if(hasAnotherVar)
                    parts.add(new VarPart(var.getParen(1)));
                
                pos = hasAnotherVar ? var.getParenEnd(0) : -1;
                }
            }

        if(regex == null || prevContext != context)
            {
            StringBuffer builtRegexStr = new StringBuffer("^\\.?");
            for(Iterator i = parts.iterator(); i.hasNext(); )
                {
                Part part = (Part) i.next();
                if(part instanceof VarPart)
                    builtRegexStr.append(
                        parseSubexpr(
                            context.getVariableValue(
                                ((VarPart) part).varName)));
                else if(part instanceof ExpPart)
                    builtRegexStr.append(
                        ((ExpPart) part).exp);
                }
            builtRegexStr.append('$');
            
            try { regex = new RE(builtRegexStr.toString()); }
            catch(RESyntaxException rese)
                {
                System.out.println("builtRegexStr = " + builtRegexStr);
                throw new RegexPatternSyntaxException(regexStr, rese);
                }
                
            if(regex.getParenCount() > 1)
                throw new RegexPatternSyntaxException(regexStr, "Too many parenthesized expressions");
            }
        }
    
    private String parseSubexpr(String exp)
        {
        exp = partBoundary.subst(exp, "\\.");
//        exp = partBoundaryInner.subst(exp, "[\\.\\$]");
        exp = innerClassBoundary.subst(exp, "\\$");
        exp = star.subst(exp, "@");
        exp = matchAcross.subst(exp, ".*");
        exp = matchWithin.subst(exp, "[^\\.]*");
//        exp = matchWithinInner.subst(exp, "[^\\.\\$]*");
        return exp;
        }

    private static void buildStaticPatterns()
        {
        if(allowable == null)
            try {
                star = new RE("\\*");
                matchWithin = new RE("@");
                matchAcross = new RE("@@");
                partBoundary = new RE("\\.");
                innerClassBoundary = new RE("\\$");

                String varS  = "\\$\\{([A-Za-z0-9_\\.\\-]+)\\}";
                String partS = "(([:javastart:]|[\\(\\)]|\\*|" + varS + ")"
                               + "([:javapart:]|[\\(\\)]|\\*|" + varS + ")*)";
                var = new RE(varS);
                allowable = new RE("^" + partS + "([\\$\\.]" + partS + ")*$", RE.MATCH_SINGLELINE);
                }
            catch(RESyntaxException rese)
                {
                rese.printStackTrace(System.out);
                throw new RuntimeException("Can't initialize RegexPattern: " + rese);
                }
        }
    
    private RE regex;
    private List/*<Part>*/ parts;
    private EvaluationContext prevContext;
    static private RE star, matchWithin, matchAcross, partBoundary, innerClassBoundary, var, allowable;
    
    private class Part { }
    private class VarPart extends Part
        {
        public VarPart(String varName) { this.varName = varName; }
        public String varName;
        public String toString() { return "var(" + varName + ")"; }
        }
    private class ExpPart extends Part
        {
        public ExpPart(String exp) { this.exp = exp; }
        public String exp;
        public String toString() { return "exp(" + exp + ")"; }
        }

    //--------------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------------
    
    public String toString()
        { return '"' + regexStr + '"'; }
    }

