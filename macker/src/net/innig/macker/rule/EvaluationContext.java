package net.innig.macker.rule;

import java.util.*;

public class EvaluationContext
    {
    public EvaluationContext(RuleSet ruleSet)
        {
        this.ruleSet = ruleSet;
        varValues = new HashMap();
        }
    
    public EvaluationContext(RuleSet ruleSet, EvaluationContext parent)
        {
        this(ruleSet);
        this.parent = parent;
        if(getParent().getRuleSet() != getRuleSet().getParent())
            throw new IllegalArgumentException(
                "Parent EvaluationContext must be associated with parent RuleSet");
        }
    
    public EvaluationContext getParent()
        { return parent; }
    
    public RuleSet getRuleSet()
        { return ruleSet; }
    
    public void setVariableValue(String name, String value)
        { varValues.put(name, value); }
    
    public String getVariableValue(String name)
        throws UndeclaredVariableException
        {
        String value = (String) varValues.get(name);
        if(value != null)
            return value;
        if(parent != null)
            return parent.getVariableValue(name);
        throw new UndeclaredVariableException(name);
        }
        
    private RuleSet ruleSet;
    private EvaluationContext parent;
    private Map varValues;
    }
