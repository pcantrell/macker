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
            for(RuleSet rs = ruleSet.getParent(); rs != null; rs = rs.getParent())
                System.out.print("--");
            System.out.println(' ' + getVariableName() + ": " + varValue);
            context.setVariableValue(getVariableName(), varValue);
            ruleSet.check(context, classes);
            }
        }
    
    private RuleSet ruleSet;
    private String variableName, regexS;
    private RegexPattern regexPat;
    }