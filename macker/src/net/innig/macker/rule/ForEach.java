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

import net.innig.macker.structure.ClassManager;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.event.MackerIsMadException;

import java.util.*;

public class ForEach
    extends Rule
    {
    public String getVariableName()
        { return variableName; }
    
    public void setVariableName(String variableName)
        { this.variableName = variableName; }
    
    public String getRegex()
        { return regexS; }
    
    public void setRegex(String regexS)
        throws RegexPatternSyntaxException
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
        throws RulesException, MackerIsMadException
        {
        EvaluationContext context = new EvaluationContext(ruleSet, parentContext);
        
        Set varValues = new TreeSet();
        for(Iterator i = classes.getPrimaryClasses().iterator(); i.hasNext(); )
            {
            ClassInfo classInfo = (ClassInfo) i.next();
            String varValue = regexPat.getParen(parentContext, classInfo);
            if(varValue != null)
                varValues.add(varValue);
            }
            
        for(Iterator i = varValues.iterator(); i.hasNext(); )
            {
            String varValue = (String) i.next();
//            for(RuleSet rs = ruleSet.getParent(); rs != null; rs = rs.getParent())
//                System.out.print("--");
            System.out.println('(' + getVariableName() + ": " + varValue + ')');
            context.setVariableValue(getVariableName(), varValue);
            ruleSet.check(context, classes);
            }
        }
    
    private RuleSet ruleSet;
    private String variableName, regexS;
    private RegexPattern regexPat;
    }