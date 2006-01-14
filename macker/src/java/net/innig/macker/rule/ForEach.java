/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
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

import net.innig.macker.event.ForEachFinished;
import net.innig.macker.event.ForEachIterationFinished;
import net.innig.macker.event.ForEachIterationStarted;
import net.innig.macker.event.ForEachStarted;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.structure.ClassManager;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class ForEach
    extends Rule
    {
    public ForEach(RuleSet parent)
        { super(parent); }
        
    public String getVariableName()
        { return variableName; }
    
    public void setVariableName(String variableName)
        { this.variableName = variableName; }
    
    public String getRegex()
        { return regexS; }
    
    public void setRegex(String regexS)
        throws MackerRegexSyntaxException
        {
        this.regexS = regexS;
        regexPat = new RegexPattern(regexS);
        }
    
    public RuleSet getRuleSet()
        { return ruleSet; }
    
    public void setRuleSet(RuleSet ruleSet)
         { this.ruleSet = ruleSet; }

    public void check(
            EvaluationContext parentContext,
            ClassManager classes)
        throws RulesException, MackerIsMadException, ListenerException
        {
        EvaluationContext context = new EvaluationContext(ruleSet, parentContext);
        
        Set<String> varValues = new TreeSet<String>();
        Set<ClassInfo> pool = new HashSet<ClassInfo>();
        for(ClassInfo curClass : classes.getPrimaryClasses())
            if(getParent().isInSubset(context, curClass))
                {
                pool.add(curClass);
                for(ClassInfo referencedClass : curClass.getReferences().keySet())
                    pool.add(referencedClass);
                }
        
        for(ClassInfo classInfo : pool)
            {
            String varValue = regexPat.getMatch(parentContext, classInfo);
            if(varValue != null)
                varValues.add(varValue);
            }
        
        context.broadcastEvent(new ForEachStarted(this));
        for(String varValue : varValues)
            {
            context.broadcastEvent(new ForEachIterationStarted(this, varValue));
            
            context.setVariableValue(getVariableName(), varValue);
            ruleSet.check(context, classes);

            context.broadcastEvent(new ForEachIterationFinished(this, varValue));
            }
        context.broadcastEvent(new ForEachFinished(this));
        }
    
    private RuleSet ruleSet;
    private String variableName, regexS;
    private RegexPattern regexPat;
    }
