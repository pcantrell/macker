package net.innig.macker.rule;

import net.innig.macker.structure.ClassManager;

import java.util.*;

public class ForEach
    extends Rule
    {
    public String getVariableName()
        { return variableName; }
    
    public void setVariableName(String variableName)
        { this.variableName = variableName; }
    
    public String getRegex()
        { return regex; }
    
    public void setRegex(String regex)
        { this.regex = regex; }
    
    public RuleSet getRuleSet()
        { return ruleSet; }
    
    public void setRuleSet(RuleSet ruleSet)
         { this.ruleSet = ruleSet; }

    public void check(
            EvaluationContext context,
            ClassManager classes)
        throws RulesException
        {
        throw new UnsupportedOperationException();
        }
    
    private RuleSet ruleSet;
    private String variableName, regex;
    }